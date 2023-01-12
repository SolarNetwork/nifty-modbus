/* ==================================================================
 * TcpNettyModbusClient.java - 29/11/2022 4:52:29 pm
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.IntSupplier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;
import net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier;
import net.solarnetwork.io.modbus.tcp.TcpModbusClientConfig;

/**
 * TCP implementation of {@link ModbusClient}.
 *
 * @author matt
 * @version 1.0
 */
public class TcpNettyModbusClient extends NettyModbusClient<TcpModbusClientConfig> {

	/** The event loop group. */
	private final EventLoopGroup eventLoopGroup;

	/** Flag if event loop group is internal. */
	private final boolean privateEventLoopGroup;

	/** The channel class to use. */
	private final Class<? extends Channel> channelClass;

	/** A mapping of transaction pendingMessages to pair requests/responses. */
	private final ConcurrentMap<Integer, TcpModbusMessage> pendingMessages;

	/** A provider of transaction IDs. */
	private final IntSupplier transactionIdSupplier;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * A default {@link NioEventLoopGroup} will be used.
	 * </p>
	 * 
	 * @param clientConfig
	 *        the client configuration
	 */
	public TcpNettyModbusClient(TcpModbusClientConfig clientConfig) {
		this(clientConfig, null, new ConcurrentHashMap<>(8, 0.9f, 2), null, NioSocketChannel.class,
				new ConcurrentHashMap<>(8, 0.9f, 2), SimpleTransactionIdSupplier.INSTANCE);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param eventLoopGroup
	 *        the event loop group, or {@literal null} to create an internal one
	 * @param channelClass
	 *        the channel class, or {@literal null} to use
	 *        {@link NioEventLoopGroup}
	 */
	public TcpNettyModbusClient(TcpModbusClientConfig clientConfig, EventLoopGroup eventLoopGroup,
			Class<? extends Channel> channelClass) {
		this(clientConfig, null, new ConcurrentHashMap<>(8, 0.9f, 2), eventLoopGroup, channelClass,
				new ConcurrentHashMap<>(8, 0.9f, 2), SimpleTransactionIdSupplier.INSTANCE);
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * A default {@link NioEventLoopGroup} will be used.
	 * </p>
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param pending
	 *        a map for request messages pending responses
	 * @param pendingMessages
	 *        a mapping of transaction IDs to associated pendingMessages, to
	 *        handle request and response pairing
	 * @param transactionIdSupplier
	 *        a TCP Modbus transaction ID supplier; only values from 1-65535
	 *        should be supplied
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public TcpNettyModbusClient(TcpModbusClientConfig clientConfig,
			ConcurrentMap<ModbusMessage, PendingMessage> pending,
			ConcurrentMap<Integer, TcpModbusMessage> pendingMessages,
			IntSupplier transactionIdSupplier) {
		this(clientConfig, null, pending, null, null, pendingMessages, transactionIdSupplier);
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
	 * @param eventLoopGroup
	 *        the event loop group, or {@literal null} to create an internal one
	 * @param channelClass
	 *        the channel class, or {@literal null} to use
	 *        {@link NioEventLoopGroup}
	 * @param pendingMessages
	 *        a mapping of transaction IDs to associated pendingMessages, to
	 *        handle request and response pairing
	 * @param transactionIdSupplier
	 *        a TCP Modbus transaction ID supplier; only values from 1-65535
	 *        should be supplied
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public TcpNettyModbusClient(TcpModbusClientConfig clientConfig, ScheduledExecutorService scheduler,
			ConcurrentMap<ModbusMessage, PendingMessage> pending, EventLoopGroup eventLoopGroup,
			Class<? extends Channel> channelClass,
			ConcurrentMap<Integer, TcpModbusMessage> pendingMessages,
			IntSupplier transactionIdSupplier) {
		super(clientConfig, scheduler, pending);
		if ( eventLoopGroup == null ) {
			eventLoopGroup = new NioEventLoopGroup();
			this.privateEventLoopGroup = true;
		} else {
			this.privateEventLoopGroup = false;
		}
		this.eventLoopGroup = eventLoopGroup;
		this.channelClass = (channelClass != null ? channelClass : NioSocketChannel.class);
		if ( pendingMessages == null ) {
			throw new IllegalArgumentException("The pendingMessages argument must not be null.");
		}
		this.pendingMessages = pendingMessages;
		if ( transactionIdSupplier == null ) {
			throw new IllegalArgumentException("The transactionIdSupplier argument must not be null.");
		}
		this.transactionIdSupplier = transactionIdSupplier;
	}

	@Override
	protected synchronized ChannelFuture connect() throws IOException {
		final String host = clientConfig.getHost();
		if ( host == null || host.isEmpty() ) {
			throw new IllegalArgumentException("No host configured, cannot connect.");
		}
		if ( eventLoopGroup.isShutdown() ) {
			throw new IOException("Client is stopped.");
		}
		// @formatter:off
		Bootstrap bootstrap = new Bootstrap()
				.group(eventLoopGroup)
				.channel(channelClass)
				.remoteAddress(host, clientConfig.getPort())
				.handler(new HandlerInitializer());
		// @formatter:on
		return bootstrap.connect();
	}

	@Override
	public synchronized void stop() {
		super.stop();
		if ( privateEventLoopGroup ) {
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Override
	protected void initChannel(Channel channel) {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast(MESSAGE_ENCODER_HANDLER_NAME,
				new TcpModbusMessageEncoder(pendingMessages, transactionIdSupplier));
		pipeline.addLast(MESSAGE_DECODER_HANDLER_NAME,
				new TcpModbusMessageDecoder(true, pendingMessages));
		super.initChannel(channel);
	}

	private final class HandlerInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			TcpNettyModbusClient.this.initChannel(ch);
		}

	}

}
