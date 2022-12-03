/* ==================================================================
 * NettyTcpModbusServer.java - 30/11/2022 1:44:33 pm
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

package net.solarnetwork.io.modbus.tcp.netty;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;
import net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier;

/**
 * A basic asynchronous Modbus TCP server.
 * 
 * <p>
 * This server listens for Modbus requests, decodes them into
 * {@link ModbusMessage} instances, and then passes those to the handler
 * configured via {@link #setMessageHandler(BiConsumer)}. The handler must
 * provide a response {@link ModbusMessage}, which this server will then encode
 * and send back to the connected client.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class NettyTcpModbusServer {

	/** The {@code pendingMessageTtl} property default value. */
	public static final long DEFAULT_PENDING_MESSAGE_TTL = TimeUnit.MINUTES.toMillis(2);

	private static final Logger log = LoggerFactory.getLogger(NettyTcpModbusServer.class);

	/** A mapping of transaction messages to pair requests/responses. */
	private final ConcurrentMap<Integer, TcpModbusMessage> pendingMessages;

	/** A transaction ID supplier. */
	private final IntSupplier transactionIdSupplier;

	private final int port;
	private ScheduledFuture<?> cleanupTask;

	private BiConsumer<ModbusMessage, Consumer<ModbusMessage>> messageHandler;
	private long pendingMessageTtl = DEFAULT_PENDING_MESSAGE_TTL;
	private boolean wireLogging;

	private Channel channel;

	/**
	 * Constructor.
	 * 
	 * @param port
	 *        the port to listen on
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public NettyTcpModbusServer(int port) {
		this(port, new ConcurrentHashMap<>(8, 0.9f, 2), SimpleTransactionIdSupplier.INSTANCE);
	}

	/**
	 * Constructor.
	 * 
	 * @param port
	 *        the port to listen on
	 * @param pendingMessages
	 *        a map to use for saving request messages, using transaction IDs
	 *        for keys
	 * @param transactionIdSupplier
	 *        the transaction ID supplier
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public NettyTcpModbusServer(int port, ConcurrentMap<Integer, TcpModbusMessage> pendingMessages,
			IntSupplier transactionIdSupplier) {
		super();
		this.port = port;
		if ( pendingMessages == null ) {
			throw new IllegalArgumentException("The pendingMessages argument must not be null.");
		}
		this.pendingMessages = pendingMessages;
		if ( transactionIdSupplier == null ) {
			throw new IllegalArgumentException("The transactionIdSupplier argument must not be null.");
		}
		this.transactionIdSupplier = transactionIdSupplier;
	}

	/**
	 * Start the server.
	 * 
	 * <p>
	 * Upon return the server will be bound and ready to accept connections on
	 * the configured port.
	 * </p>
	 */
	public synchronized void start() throws IOException {
		if ( this.channel != null ) {
			return;
		}
		try {
			final EventLoopGroup bossGroup = new NioEventLoopGroup();
			final EventLoopGroup workerGroup = new NioEventLoopGroup();

			// @formatter:off
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChildHandlerInitializer())
					.option(ChannelOption.SO_REUSEADDR, true)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			// @formatter:on

			Channel channel = bootstrap.bind(port).sync().channel();
			channel.closeFuture().addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					workerGroup.shutdownGracefully();
					bossGroup.shutdownGracefully();
				}
			});
			this.channel = channel;
			if ( cleanupTask == null ) {
				long period = getPendingMessageTtl() * 2;
				if ( period > 0 ) {
					cleanupTask = bossGroup.scheduleWithFixedDelay(new PendingMessageExpiredCleaner(),
							period, period, TimeUnit.MILLISECONDS);
				}
			}
		} catch ( Exception e ) {
			String msg = String.format("Error starting Modbus server on port %d", port);
			if ( e instanceof IOException ) {
				log.warn("{}: {}", msg, e.getMessage());
				throw (IOException) e;
			} else {
				log.error(msg, e);
			}
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Stop the server.
	 */
	public synchronized void stop() {
		if ( cleanupTask != null ) {
			cleanupTask.cancel(true);
			cleanupTask = null;
		}
		if ( channel != null ) {
			channel.close().awaitUninterruptibly();
			channel = null;
		}
	}

	/**
	 * Initializer for client connections.
	 */
	private final class ChildHandlerInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			if ( wireLogging ) {
				pipeline.addLast(new LoggingHandler("net.solarnetwork.io.modbus.server." + port));
			}
			pipeline.addLast(new TcpModbusMessageEncoder(pendingMessages, transactionIdSupplier),
					new TcpModbusMessageDecoder(false, pendingMessages), new ChildHandler());
		}

	}

	/**
	 * Handler for client connections.
	 */
	private final class ChildHandler extends SimpleChannelInboundHandler<ModbusMessage> {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.info("Client connected: {}", ctx.channel());
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			log.info("Client disconnected: {}", ctx.channel());
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ModbusMessage msg) throws Exception {
			log.debug("Request: {}", msg);
			final BiConsumer<ModbusMessage, Consumer<ModbusMessage>> h = getMessageHandler();
			if ( h == null ) {
				return;
			}
			h.accept(msg, (r) -> {
				ctx.channel().writeAndFlush(new SimpleModbusMessageReply(msg, r));
			});
		}

	}

	private final class PendingMessageExpiredCleaner implements Runnable {

		@Override
		public void run() {
			log.debug("Looking for expired pending Modbus messages");
			int expiredCount = 0;
			final long now = System.currentTimeMillis();
			try {
				for ( Iterator<TcpModbusMessage> itr = pendingMessages.values().iterator(); itr
						.hasNext(); ) {
					TcpModbusMessage pending = itr.next();
					if ( pending.getTimestamp() + pendingMessageTtl < now ) {
						log.warn(
								"Dropping pending Modbus request message that has not had a response provided within {}ms: {}",
								pendingMessageTtl, pending);
						itr.remove();
						expiredCount++;
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
	 * Get the port the server is listening to.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Get the message handler.
	 * 
	 * @return the handler
	 */
	public BiConsumer<ModbusMessage, Consumer<ModbusMessage>> getMessageHandler() {
		return messageHandler;
	}

	/**
	 * Set the message handler.
	 * 
	 * <p>
	 * This handler will be passed an inbound message along with another
	 * {@code Consumer} for the reply message.
	 * </p>
	 * 
	 * @param messageHandler
	 *        the handler to set
	 */
	public void setMessageHandler(BiConsumer<ModbusMessage, Consumer<ModbusMessage>> messageHandler) {
		this.messageHandler = messageHandler;
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
