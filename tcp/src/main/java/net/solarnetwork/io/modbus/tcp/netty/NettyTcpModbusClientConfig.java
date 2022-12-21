/* ==================================================================
 * NettyTcpModbusClientConfig.java - 29/11/2022 4:57:08 pm
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

import net.solarnetwork.io.modbus.netty.handler.NettyModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.TcpModbusClientConfig;

/**
 * Netty implementation of {@link TcpModbusClientConfig}.
 *
 * @author matt
 * @version 1.0
 */
public class NettyTcpModbusClientConfig extends NettyModbusClientConfig
		implements TcpModbusClientConfig {

	private String host;
	private int port;

	/**
	 * Constructor.
	 */
	public NettyTcpModbusClientConfig() {
		super();
		this.port = DEFAULT_PORT;
	}

	/**
	 * Constructor.
	 * 
	 * @param host
	 *        the host
	 * @param port
	 *        the port
	 */
	public NettyTcpModbusClientConfig(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	/**
	 * Get the host to connect to.
	 * 
	 * @return the host
	 */
	@Override
	public String getHost() {
		return host;
	}

	/**
	 * Set the host to connect to.
	 * 
	 * @param host
	 *        the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Get the IP port to connect to.
	 * 
	 * @return the port
	 */
	@Override
	public int getPort() {
		return port;
	}

	/**
	 * Set the IP port to connect to.
	 * 
	 * @param port
	 *        the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

}
