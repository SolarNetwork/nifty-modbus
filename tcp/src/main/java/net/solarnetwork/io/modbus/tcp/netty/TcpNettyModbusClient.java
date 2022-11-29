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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;
import net.solarnetwork.io.modbus.tcp.TcpModbusClientConfig;

/**
 * TCP implementation of {@link ModbusClient}.
 *
 * @author matt
 * @version 1.0
 */
public class TcpNettyModbusClient extends NettyModbusClient<TcpModbusClientConfig> {

	private final Class<? extends Channel> channelClass;

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
		this(clientConfig, new NioEventLoopGroup(), null);
	}

	/**
	 * Constructor.
	 * 
	 * @param clientConfig
	 *        the client configuration
	 * @param eventLoopGroup
	 *        the event loop group
	 * @param channelClass
	 *        the channel class, or {@literal null} to use
	 *        {@link NioEventLoopGroup}
	 */
	public TcpNettyModbusClient(TcpModbusClientConfig clientConfig, EventLoopGroup eventLoopGroup,
			Class<? extends Channel> channelClass) {
		super(clientConfig, eventLoopGroup);
		this.channelClass = (channelClass != null ? channelClass : NioSocketChannel.class);
	}

	@Override
	protected ChannelFuture connect() {
		final String host = clientConfig.getHost();
		if ( host == null || host.isEmpty() ) {
			throw new IllegalArgumentException("No host configured, cannot connect.");
		}
		// @formatter:off
		Bootstrap bootstrap = new Bootstrap()
				.group(eventLoopGroup)
				.channel(channelClass)
				.remoteAddress(host, clientConfig.getPort())
				.handler(this);
		// @formatter:on
		return bootstrap.connect();
	}

}
