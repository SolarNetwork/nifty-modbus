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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
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
import net.solarnetwork.io.modbus.rtu.RtuModbusClientConfig;
import net.solarnetwork.io.modbus.serial.SerialAddress;
import net.solarnetwork.io.modbus.serial.SerialPortChannel;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * RTU implementation of {@link ModbusClient}.
 *
 * @author matt
 * @version 1.0
 */
public class RtuNettyModbusClient extends NettyModbusClient<RtuModbusClientConfig>
		implements ChannelFactory<SerialPortChannel> {

	private final EventLoopGroup eventLoopGroup;
	private final boolean privateEventLoopGroup;
	private final SerialPortProvider serialPortProvider;

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
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
	 *        the event loop group, or {@literal null} to create an internal one
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument except {@code eventLoopGroup} is {@literal null}
	 */
	public RtuNettyModbusClient(RtuModbusClientConfig clientConfig, EventLoopGroup eventLoopGroup,
			SerialPortProvider serialPortProvider) {
		this(clientConfig, null, new ConcurrentHashMap<>(8, 0.9f, 2), eventLoopGroup,
				serialPortProvider);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param scheduler
	 *        the scheduler, or {@literal null} to create an internal one
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument except {@code eventLoopGroup} is {@literal null}
	 */
	public RtuNettyModbusClient(RtuModbusClientConfig clientConfig, ScheduledExecutorService scheduler,
			EventLoopGroup eventLoopGroup, SerialPortProvider serialPortProvider) {
		this(clientConfig, scheduler, new ConcurrentHashMap<>(8, 0.9f, 2), eventLoopGroup,
				serialPortProvider);
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
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument except {@code eventLoopGroup} is {@literal null}
	 */
	@SuppressWarnings("deprecation")
	public RtuNettyModbusClient(RtuModbusClientConfig clientConfig, ScheduledExecutorService scheduler,
			ConcurrentMap<ModbusMessage, PendingMessage> pending, EventLoopGroup eventLoopGroup,
			SerialPortProvider serialPortProvider) {
		super(clientConfig, scheduler, pending);
		if ( eventLoopGroup == null ) {
			// TODO: need a non-deprecated replacement
			eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
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

	@Override
	public SerialPortChannel newChannel() {
		SerialPortChannel channel = new SerialPortChannel(serialPortProvider);
		channel.config().setSerialParameters(clientConfig.getSerialParameters());
		return channel;
	}

	@Override
	protected ChannelFuture connect() {
		final String name = clientConfig.getName();
		if ( name == null || name.isEmpty() ) {
			throw new IllegalArgumentException("No serial device name configured, cannot connect.");
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
	public synchronized void stop() {
		super.stop();
		if ( privateEventLoopGroup ) {
			eventLoopGroup.shutdownGracefully();
		}
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
