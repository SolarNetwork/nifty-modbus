/* ==================================================================
 * TcpModbusServer.java - 30/11/2022 1:44:33 pm
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

package net.solarnetwork.io.modbus.tcp.test.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessageDecoder;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessageEncoder;

/**
 * A Modbus TCP server for testing purposes.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusServer extends SimpleChannelInboundHandler<ModbusMessage> {

	private static final Logger log = LoggerFactory.getLogger(TcpModbusServer.class);

	/** A mapping of transaction messages to pair requests/responses. */
	private final ConcurrentMap<Integer, TcpModbusMessage> pendingMessages;

	/** A transaction ID supplier. */
	private final IntSupplier transactionIdSupplier;

	private final int port;

	private Channel channel;

	/**
	 * Constructor.
	 * 
	 * @param port
	 *        the port to listen on
	 */
	public TcpModbusServer(int port) {
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
	public TcpModbusServer(int port, ConcurrentMap<Integer, TcpModbusMessage> pendingMessages,
			IntSupplier transactionIdSupplier) {
		super();
		this.port = port;
		if ( pendingMessages == null ) {
			throw new IllegalArgumentException("The messages argument must not be null.");
		}
		this.pendingMessages = pendingMessages;
		if ( transactionIdSupplier == null ) {
			throw new IllegalArgumentException("The transactionIdSupplier argument must not be null.");
		}
		this.transactionIdSupplier = transactionIdSupplier;
	}

	/**
	 * Start the server.
	 */
	public synchronized void start() {
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
		} catch ( Exception e ) {
			String msg = String.format("Error starting Modbus server on port %d", port);
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	;

	private final class ChildHandlerInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(new TcpModbusMessageEncoder(pendingMessages, transactionIdSupplier),
					new TcpModbusMessageDecoder(false, pendingMessages), this);
		}

	}

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
		log.info("Hello: {}", msg);
	}

	/**
	 * Stop the server.
	 */
	public synchronized void stop() {
		channel.close().awaitUninterruptibly();
	}

	/**
	 * Get the port the server is listening to.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

}
