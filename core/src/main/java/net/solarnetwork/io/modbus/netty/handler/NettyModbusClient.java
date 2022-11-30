/* ==================================================================
 * NettyModbusClient.java - 29/11/2022 3:39:46 pm
 *
 * Copyright 2022 SolarNetwork.net Dev Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.io.modbus.netty.handler;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusClientConfig;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessageReply;

/**
 * Netty implementation of {@link ModbusClient}.
 *
 * @param <C>
 *        the configuration type
 * @author matt
 * @version 1.0
 */
public abstract class NettyModbusClient<C extends ModbusClientConfig>
		extends SimpleChannelInboundHandler<ModbusMessage> implements ModbusClient {

	/** The {@code pendingMessageTtl} property default value. */
	public static final long DEFAULT_PENDING_MESSAGE_TTL = TimeUnit.MINUTES.toMillis(2);

	/** A channel attribute key for the last encoded message. */
	public static final AttributeKey<ModbusMessage> LAST_ENCODED_MESSAGE = AttributeKey
			.valueOf("ModbusMessageEncoder.LAST");

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** The client configuration. */
	protected final C clientConfig;

	/** The event loop group. */
	protected final EventLoopGroup eventLoopGroup;

	private boolean wireLogging;
	private long pendingMessageTtl = DEFAULT_PENDING_MESSAGE_TTL;

	private io.netty.util.concurrent.ScheduledFuture<?> cleanupTask;
	private ChannelFuture connFuture;
	private volatile Channel channel;
	private volatile boolean disconnected;
	private volatile boolean reconnecting;

	private final ConcurrentMap<ModbusMessage, PendingMessage> pending;

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param eventLoopGroup
	 *        the event loop group
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public NettyModbusClient(C clientConfig, EventLoopGroup eventLoopGroup) {
		this(clientConfig, eventLoopGroup, new ConcurrentHashMap<>(8, 0.9f, 2));
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param eventLoopGroup
	 *        the event loop group
	 * @param pending
	 *        a map for request messages pending responses
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public NettyModbusClient(C clientConfig, EventLoopGroup eventLoopGroup,
			ConcurrentMap<ModbusMessage, PendingMessage> pending) {
		super();
		if ( clientConfig == null ) {
			throw new IllegalArgumentException("The clientConfig argument must not be null.");
		}
		this.clientConfig = clientConfig;
		if ( eventLoopGroup == null ) {
			throw new IllegalArgumentException("The eventLoopGroup argument must not be null.");
		}
		this.eventLoopGroup = eventLoopGroup;
		if ( pending == null ) {
			throw new IllegalArgumentException("The pending argument must not be null.");
		}
		this.pending = pending;
	}

	/**
	 * Start the client.
	 */
	public synchronized void start() {
		if ( connFuture != null ) {
			return;
		}
		handleConnect(false);
		cleanupTask = eventLoopGroup.scheduleWithFixedDelay(new PendingMessageExpiredCleaner(), 5, 5,
				TimeUnit.MINUTES);
	}

	/**
	 * Stop the client.
	 */
	public synchronized void stop() {
		if ( connFuture != null ) {
			if ( connFuture.isCancellable() ) {
				connFuture.cancel(true);
			}
			connFuture = null;
		}
		if ( channel != null ) {
			channel.disconnect();
			channel = null;
		}
		if ( cleanupTask != null ) {
			cleanupTask.cancel(true);
			cleanupTask = null;
		}
	}

	private void handleConnect(boolean reconnecting) {
		connFuture = connect();
		connFuture.addListener((ChannelFutureListener) f -> {
			if ( f.isSuccess() ) {
				Channel c = f.channel();
				c.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
					if ( isConnected() ) {
						return;
					}
					// Needed? NettyModbusClient.this.channel = null;
					// TODO: could offer a "connection lost" callback API here
					scheduleConnectIfRequired(true);
				});
				NettyModbusClient.this.channel = c;
			} else {
				scheduleConnectIfRequired(reconnecting);
			}
		});
	}

	private void scheduleConnectIfRequired(boolean reconnecting) {
		if ( clientConfig.isAutoReconnect() && !disconnected ) {
			if ( reconnecting ) {
				this.reconnecting = true;
			}
			eventLoopGroup.schedule((Runnable) () -> handleConnect(reconnecting),
					clientConfig.getAutoReconnectDelaySeconds(), TimeUnit.SECONDS);
		}
	}

	/**
	 * Initialize the channel.
	 * 
	 * <p>
	 * This should be called by extending classes via a
	 * {@code ClientInitializer} implementation configured on the
	 * {@link Bootstrap}.
	 * </p>
	 * 
	 * @param channel
	 *        the channel to initialize
	 */
	protected void initChannel(Channel channel) {
		if ( wireLogging ) {
			channel.pipeline().addLast(
					new LoggingHandler("net.solarnetwork.io.modbus." + clientConfig.getDescription()));
		}
		channel.pipeline().addLast("modbusClient", this);
	}

	private ChannelFuture sendAndFlushPacket(ModbusMessage message) {
		if ( this.channel == null ) {
			return null;
		}
		if ( this.channel.isActive() ) {
			return this.channel.writeAndFlush(message);
		}
		return this.channel.newFailedFuture(new IOException(
				String.format("Connection to %s is closed.", clientConfig.getDescription())));
	}

	/**
	 * Establish the connection.
	 * 
	 * @return a connection future
	 */
	protected abstract ChannelFuture connect();

	/**
	 * Test if the connection is active.
	 * 
	 * @return {@literal true} if the connection is active
	 */
	public boolean isConnected() {
		return !disconnected && channel != null && channel.isActive();
	}

	@Override
	public ModbusMessage send(ModbusMessage request) {
		Future<ModbusMessage> f = sendAsync(request);
		try {
			return f.get(1, TimeUnit.MINUTES);
		} catch ( InterruptedException e ) {
			log.warn("Interrupted waiting for response to {}", request);
			throw new RuntimeException(e); // TODO: use sensible exception
		} catch ( ExecutionException e ) {
			Throwable t = e.getCause();
			log.warn("Internal exception waiting for response to {}: {}", request, t.toString(), t);
			if ( t instanceof RuntimeException ) {
				throw (RuntimeException) t;
			}
			throw new RuntimeException(t); // TODO: use sensible exception
		} catch ( TimeoutException e ) {
			log.warn("Timeout waiting for response to {}", request);
			throw new RuntimeException(e); // TODO: use sensible exception
		}
	}

	@Override
	public Future<ModbusMessage> sendAsync(ModbusMessage request) {
		CompletableFuture<ModbusMessage> resp = new CompletableFuture<>();
		pending.put(request, new PendingMessage(request, resp));
		ChannelFuture f = sendAndFlushPacket(request);
		f.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if ( !future.isSuccess() ) {
					pending.remove(request);
					resp.completeExceptionally(future.cause());
				}
			}
		});
		return resp;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ModbusMessage msg) throws Exception {
		ModbusMessage req = null;
		if ( msg instanceof ModbusMessageReply ) {
			ModbusMessageReply reply = (ModbusMessageReply) msg;
			req = reply.getRequest();
		} else {
			// fall back to the last sent request
			req = ctx.channel().attr(LAST_ENCODED_MESSAGE).getAndSet(null);
		}
		if ( req != null ) {
			PendingMessage p = pending.remove(req);
			if ( p != null ) {
				p.future.complete(msg);
			}
		}
	}

	/**
	 * A request message pending a response message.
	 */
	public static final class PendingMessage {

		private final ModbusMessage request;
		private final CompletableFuture<ModbusMessage> future;
		private final long created; // so can expire

		/**
		 * Constructor.
		 * 
		 * @param request
		 *        the request
		 * @param future
		 *        the future awaiting the response
		 * @throws IllegalArgumentException
		 *         if any argument is {@literal null}
		 */
		public PendingMessage(ModbusMessage request, CompletableFuture<ModbusMessage> future) {
			super();
			if ( request == null ) {
				throw new IllegalArgumentException("The request argument must not be null.");
			}
			this.request = request;
			if ( future == null ) {
				throw new IllegalArgumentException("The future argument must not be null.");
			}
			this.future = future;
			this.created = System.currentTimeMillis();
		}

		/**
		 * Get the request message.
		 * 
		 * @return the request
		 */
		public ModbusMessage getRequest() {
			return request;
		}

		/**
		 * Get the future awaiting the response.
		 * 
		 * @return the future the future
		 */
		public CompletableFuture<ModbusMessage> getFuture() {
			return future;
		}

		/**
		 * Get the creation date.
		 * 
		 * @return the creation date, as a millisecond epoch
		 */
		public long getCreated() {
			return created;
		}

	}

	private final class PendingMessageExpiredCleaner implements Runnable {

		@Override
		public void run() {
			log.debug("Looking for expired pending Modbus messages");
			int expiredCount = 0;
			final long now = System.currentTimeMillis();
			try {
				for ( Iterator<PendingMessage> itr = pending.values().iterator(); itr.hasNext(); ) {
					PendingMessage pending = itr.next();
					if ( pending.created + pendingMessageTtl < now ) {
						log.warn(
								"Dropping pending Modbus request message that has not received a response within {}ms: {}",
								pendingMessageTtl, pending);
						itr.remove();
					}
				}
			} catch ( Exception e ) {
				log.warn("Exception cleaning expired pending Modbus messages: {}", e.toString(), e);
			} finally {
				if ( expiredCount < 1 ) {
					log.debug("Finished cleaning expired pending Modbus messages; none expired.");
				} else {
					log.info("Finished cleaning expired pending Modbus messages; {} expired.",
							expiredCount);
				}
			}
		}

	}

	/**
	 * Get the "wire logging" setting.
	 * 
	 * @return {@literal true} to enable wire-level logging of all messages
	 */
	public boolean isWireLogging() {
		return wireLogging;
	}

	/**
	 * Set the "wire logging" setting.
	 * 
	 * @param wireLogging
	 *        {@literal true} to enable wire-level logging of all messages
	 */
	public void setWireLogging(boolean wireLogging) {
		this.wireLogging = wireLogging;
	}

	/**
	 * Get the pending Modbus message time-to-live expiration time.
	 * 
	 * @return the pendingMessageTtl the pending Modbus message time-to-live, in
	 *         milliseconds; defaults to {@link #DEFAULT_PENDING_MESSAGE_TTL}
	 */
	public long getPendingMessageTtl() {
		return pendingMessageTtl;
	}

	/**
	 * Set the pending Modbus message time-to-live expiration time.
	 * 
	 * <p>
	 * This timeout represents the minimum amount of time the client will wait
	 * for a Modbus message response, before it qualifies for removal from the
	 * pending message queue.
	 * </p>
	 * 
	 * @param pendingMessageTtl
	 *        the pending Modbus message time-to-live, in milliseconds
	 */
	public void setPendingMessageTtl(long pendingMessageTtl) {
		this.pendingMessageTtl = pendingMessageTtl;
	}

}
