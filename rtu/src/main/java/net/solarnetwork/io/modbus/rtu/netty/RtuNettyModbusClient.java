/* ==================================================================
 * RtuNettyModbusClient.java - 2/12/2022 10:45:07 am
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

package net.solarnetwork.io.modbus.rtu.netty;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;
import net.solarnetwork.io.modbus.netty.serial.SerialAddress;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannel;
import net.solarnetwork.io.modbus.rtu.RtuModbusClientConfig;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * RTU implementation of {@link ModbusClient}.
 *
 * @author matt
 * @version 1.1
 */
public class RtuNettyModbusClient extends NettyModbusClient<RtuModbusClientConfig>
		implements ChannelFactory<SerialPortChannel> {

	private final boolean privateEventLoopGroup;
	private final SerialPortProvider serialPortProvider;
	private @Nullable EventLoopGroup eventLoopGroup;
	private @Nullable CompletableFuture<?> eventLoopGroupStopFuture;

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public RtuNettyModbusClient(RtuModbusClientConfig clientConfig,
			SerialPortProvider serialPortProvider) {
		this(clientConfig, null, new ConcurrentHashMap<>(8, 0.9f, 2), null, serialPortProvider);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param eventLoopGroup
	 *        the event loop group, or {@code null} to create an internal one
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument except {@code eventLoopGroup} is {@code null}
	 */
	public RtuNettyModbusClient(RtuModbusClientConfig clientConfig,
			@Nullable EventLoopGroup eventLoopGroup, SerialPortProvider serialPortProvider) {
		this(clientConfig, null, new ConcurrentHashMap<>(8, 0.9f, 2), eventLoopGroup,
				serialPortProvider);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param scheduler
	 *        the scheduler, or {@code null} to create an internal one
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument except {@code scheduler} or
	 *         {@code eventLoopGroup} is {@code null}
	 */
	public RtuNettyModbusClient(RtuModbusClientConfig clientConfig,
			@Nullable ScheduledExecutorService scheduler, @Nullable EventLoopGroup eventLoopGroup,
			SerialPortProvider serialPortProvider) {
		this(clientConfig, scheduler, new ConcurrentHashMap<>(8, 0.9f, 2), eventLoopGroup,
				serialPortProvider);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param scheduler
	 *        the scheduler, or {@code null} to create an internal one
	 * @param pending
	 *        a map for request messages pending responses
	 * @param eventLoopGroup
	 *        the event loop group, or {@code null} to create an internal one
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument except {@code scheduler} or
	 *         {@code eventLoopGroup} is {@code null}
	 */
	public RtuNettyModbusClient(RtuModbusClientConfig clientConfig,
			@Nullable ScheduledExecutorService scheduler,
			ConcurrentMap<ModbusMessage, PendingMessage> pending,
			@Nullable EventLoopGroup eventLoopGroup, SerialPortProvider serialPortProvider) {
		super(clientConfig, scheduler, pending);
		if ( eventLoopGroup == null ) {
			this.privateEventLoopGroup = true;
		} else {
			this.privateEventLoopGroup = false;
		}
		this.eventLoopGroup = eventLoopGroup;
		if ( serialPortProvider == null ) {
			throw new IllegalArgumentException("The serialPortProvider argument must not be null.");
		}
		this.serialPortProvider = serialPortProvider;
	}

	private EventLoopGroup defaultEventLoopGroup() {
		final BiFunction<Object, Boolean, EventLoopGroup> provider = getEventLoopGroupProvider();
		if ( provider != null ) {
			return provider.apply(this, false);
		}
		return net.solarnetwork.io.modbus.netty.channel.OioEventLoopGroupFactory.INSTANCE.apply(provider,
				false);
	}

	@Override
	public SerialPortChannel newChannel() {
		SerialPortChannel channel = new SerialPortChannel(serialPortProvider);
		channel.config().setSerialParameters(clientConfig.getSerialParameters());
		return channel;
	}

	@Override
	protected ChannelFuture connect() throws IOException {
		eventLoopGroupStopFuture = null;
		final String name = clientConfig.getName();
		if ( name == null || name.isEmpty() ) {
			throw new IllegalArgumentException("No serial device name configured, cannot connect.");
		}
		EventLoopGroup eventLoopGroup = this.eventLoopGroup;
		if ( eventLoopGroup == null || eventLoopGroup.isShuttingDown() ) {
			if ( privateEventLoopGroup ) {
				eventLoopGroup = defaultEventLoopGroup();
				this.eventLoopGroup = eventLoopGroup;
			} else {
				throw new IOException("External EventLoopGroup is stopped.");
			}
		}
		// @formatter:off
		Bootstrap bootstrap = new Bootstrap()
				.group(eventLoopGroup)
				.channelFactory(this)
				.remoteAddress(new SerialAddress(clientConfig.getName()))
				.handler(new HandlerInitializer());
		// @formatter:on
		return bootstrap.connect();
	}

	@Override
	public synchronized CompletableFuture<?> stop() {
		CompletableFuture<?> f = super.stop();
		if ( !privateEventLoopGroup ) {
			return f;
		}
		final EventLoopGroup eventLoopGroup = this.eventLoopGroup;
		if ( eventLoopGroupStopFuture == null && eventLoopGroup != null ) {
			eventLoopGroupStopFuture = new CompletableFuture<Void>();
			try {
				eventLoopGroup.shutdownGracefully().get(10L, TimeUnit.SECONDS);
				eventLoopGroupStopFuture.complete(null);
			} catch ( TimeoutException e ) {
				log.warn("Timeout waiting for {} EventLoopGroup to shutdown",
						clientConfig.getDescription());
				eventLoopGroupStopFuture.completeExceptionally(e);
			} catch ( Exception e ) {
				log.warn("{} waiting for {} EventLoopGroup to shutdown", e.getClass().getSimpleName(),
						clientConfig.getDescription());
				eventLoopGroupStopFuture.completeExceptionally(e);
			}
		}
		return f.thenCompose(s -> eventLoopGroupStopFuture);
	}

	@Override
	protected void initChannel(Channel channel) {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast(MESSAGE_ENCODER_HANDLER_NAME, new RtuModbusMessageEncoder());
		pipeline.addLast(MESSAGE_DECODER_HANDLER_NAME, new RtuModbusMessageDecoder(true));
		super.initChannel(channel);
	}

	private final class HandlerInitializer extends ChannelInitializer<SerialPortChannel> {

		@Override
		protected void initChannel(SerialPortChannel ch) throws Exception {
			RtuNettyModbusClient.this.initChannel(ch);
		}

	}

}
