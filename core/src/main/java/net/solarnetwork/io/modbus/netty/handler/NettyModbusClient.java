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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusClientConfig;
import net.solarnetwork.io.modbus.ModbusClientConnectionObserver;
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
public abstract class NettyModbusClient<C extends ModbusClientConfig> implements ModbusClient {

	/** The {@code pendingMessageTtl} property default value. */
	public static final long DEFAULT_PENDING_MESSAGE_TTL = TimeUnit.MINUTES.toMillis(2);

	/**
	 * A channel attribute key for the last encoded message.
	 * 
	 * <p>
	 * This can be used by encoders if requests/responses can not be
	 * multiplexed, such as a single-threaded serial connection.
	 * </p>
	 */
	public static final AttributeKey<ModbusMessage> LAST_ENCODED_MESSAGE = AttributeKey
			.valueOf("ModbusMessageEncoder.LAST");

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/** The client configuration. */
	protected final C clientConfig;

	/** The scheduler. */
	protected final ScheduledExecutorService scheduler;

	/** Flag if the scheduler is internally created. */
	private final boolean privateScheduler;

	private ModbusClientConnectionObserver connectionObserver;
	private boolean wireLogging;
	private long pendingMessageTtl = DEFAULT_PENDING_MESSAGE_TTL;

	private ScheduledFuture<?> cleanupTask;
	private Future<?> connFuture;
	private volatile Channel channel;
	private volatile boolean stopped;

	private final ConcurrentMap<ModbusMessage, PendingMessage> pending;

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param scheduler
	 *        the scheduler, or {@literal null} to create an internal one
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public NettyModbusClient(C clientConfig, ScheduledExecutorService scheduler) {
		this(clientConfig, scheduler, new ConcurrentHashMap<>(8, 0.9f, 2));
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param scheduler
	 *        the scheduler, or {@literal null} to create an internal one
	 * @param pending
	 *        a map for request messages pending responses
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public NettyModbusClient(C clientConfig, ScheduledExecutorService scheduler,
			ConcurrentMap<ModbusMessage, PendingMessage> pending) {
		super();
		if ( clientConfig == null ) {
			throw new IllegalArgumentException("The clientConfig argument must not be null.");
		}
		this.clientConfig = clientConfig;

		if ( scheduler == null ) {
			scheduler = Executors.newSingleThreadScheduledExecutor();
			this.privateScheduler = true;
		} else {
			this.privateScheduler = false;
		}
		this.scheduler = scheduler;

		if ( pending == null ) {
			throw new IllegalArgumentException("The pending argument must not be null.");
		}
		this.pending = pending;
	}

	/**
	 * Start the client.
	 */
	public synchronized Future<?> start() {
		if ( connFuture != null ) {
			return connFuture;
		}
		CompletableFuture<?> result = handleConnect(false);
		connFuture = result;
		if ( cleanupTask == null ) {
			long period = getPendingMessageTtl() * 2;
			if ( period > 0 ) {
				result.thenRun(() -> {
					cleanupTask = scheduler.scheduleWithFixedDelay(new PendingMessageExpiredCleaner(),
							period, period, TimeUnit.MILLISECONDS);
				});
			}
		}
		return result;
	}

	/**
	 * Stop the client.
	 */
	public synchronized void stop() {
		this.stopped = true;
		if ( privateScheduler && !scheduler.isShutdown() ) {
			scheduler.shutdown();
		}
		if ( connFuture != null ) {
			if ( !connFuture.isDone() ) {
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

	private synchronized CompletableFuture<?> handleConnect(boolean reconnecting) {
		CompletableFuture<Void> completable = new CompletableFuture<>();
		try {
			ChannelFuture channelFuture = connect();
			channelFuture.addListener((ChannelFutureListener) f -> {
				if ( f.isSuccess() ) {
					Channel c = f.channel();
					c.closeFuture().addListener((ChannelFutureListener) chFuture -> {
						if ( isConnected() ) {
							return;
						}
						// Needed? NettyModbusClient.this.channel = null;
						// TODO: could offer a "connection lost" callback API here
						scheduleConnectIfRequired(true);
					});
					channel = c;
					completable.complete(null);
				} else {
					scheduleConnectIfRequired(reconnecting);
					if ( !reconnecting ) {
						completable.completeExceptionally(f.cause());
					}
				}
			});
		} catch ( Exception e ) {
			completable.completeExceptionally(e);
		}
		return completable;
	}

	private synchronized void scheduleConnectIfRequired(boolean reconnecting) {
		if ( channel != null ) {
			try {
				channel.close().sync();
			} catch ( InterruptedException e ) {
				// ignore
			}
			channel = null;
		}
		if ( clientConfig.isAutoReconnect() && !stopped ) {
			scheduler.schedule((Runnable) () -> handleConnect(reconnecting),
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
		ChannelPipeline pipeline = channel.pipeline();
		if ( wireLogging ) {
			pipeline.addFirst(
					new LoggingHandler("net.solarnetwork.io.modbus." + clientConfig.getDescription()));
		}
		pipeline.addLast("modbusClient", new ModbusChannelHandler());
	}

	private ChannelFuture sendAndFlushPacket(Channel channel, ModbusMessage message) {
		if ( channel == null ) {
			return null;
		}
		if ( channel.isActive() ) {
			return channel.writeAndFlush(message);
		}
		return channel.newFailedFuture(new IOException(
				String.format("Connection to %s is closed.", clientConfig.getDescription())));
	}

	/**
	 * Establish the connection.
	 * 
	 * @return a connection future
	 * @throws IOException
	 *         if an error prevents the connection future from being created
	 */
	protected abstract ChannelFuture connect() throws IOException;

	/**
	 * Test if the connection is active.
	 * 
	 * @return {@literal true} if the connection is active
	 */
	public boolean isConnected() {
		return !stopped && channel != null && channel.isActive();
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
		final Channel channel = this.channel;
		if ( channel == null ) {
			CompletableFuture<ModbusMessage> fail = new CompletableFuture<>();
			fail.completeExceptionally(new IOException("Client not connected."));
			return fail;
		}
		CompletableFuture<ModbusMessage> resp = new CompletableFuture<>();
		pending.put(request, new PendingMessage(request, resp));
		ChannelFuture f = sendAndFlushPacket(channel, request);
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

	private final class ModbusChannelHandler extends SimpleChannelInboundHandler<ModbusMessage> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ModbusMessage msg) throws Exception {
			ModbusMessage req = null;
			ModbusMessageReply reply = msg.unwrap(ModbusMessageReply.class);
			if ( reply != null ) {
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

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
			final ModbusClientConnectionObserver obs = getConnectionObserver();
			if ( obs != null ) {
				try {
					obs.connectionOpened(NettyModbusClient.this, clientConfig);
				} catch ( Exception t ) {
					log.warn("Connection observer [{}] threw exception: ");
				}
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			final ModbusClientConnectionObserver obs = getConnectionObserver();
			if ( obs != null ) {
				try {
					obs.connectionClosed(NettyModbusClient.this, clientConfig, null,
							clientConfig.isAutoReconnect() && !stopped);
				} catch ( Exception t ) {
					log.warn("Connection observer [{}] threw exception: ");
				}
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PendingMessage{created=");
			builder.append(created);
			builder.append(", request=");
			builder.append(request);
			builder.append("}");
			return builder.toString();
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
								"Dropping pending Modbus request that has not received a response within {}ms: {}",
								pendingMessageTtl, pending);
						itr.remove();
					}
				}
			} catch ( Exception e ) {
				log.warn("Exception cleaning expired pending Modbus requests: {}", e.toString(), e);
			} finally {
				if ( expiredCount < 1 ) {
					log.debug("Finished cleaning expired pending Modbus requests; none expired.");
				} else {
					log.info("Finished cleaning expired pending Modbus requests; {} expired.",
							expiredCount);
				}
			}
		}

	}

	/**
	 * Get the client configuration.
	 * 
	 * @return the client configuration
	 */
	public C getClientConfig() {
		return clientConfig;
	}

	/**
	 * Get the connection observer.
	 * 
	 * @return the connection observer, or {@literal null}
	 */
	public ModbusClientConnectionObserver getConnectionObserver() {
		return connectionObserver;
	}

	/**
	 * Set the connection observer.
	 * 
	 * @param connectionObserver
	 *        the connection observer to set, or {@literal null}
	 */
	public void setConnectionObserver(ModbusClientConnectionObserver connectionObserver) {
		this.connectionObserver = connectionObserver;
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
