/* ==================================================================
 * TcpModbusClientConfig.java - 29/11/2022 4:52:50 pm
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

package net.solarnetwork.io.modbus.tcp;

import net.solarnetwork.io.modbus.ModbusClientConfig;

/**
 * TCP Modbus client configuration.
 *
 * @author matt
 * @version 1.0
 */
public interface TcpModbusClientConfig extends ModbusClientConfig {

	/** The default IP port. */
	int DEFAULT_PORT = 502;

	/**
	 * Get the IP address or host name to connect to.
	 * 
	 * @return the host
	 */
	String getHost();

	/**
	 * Get the IP port to connect to.
	 * 
	 * @return the IP port
	 */
	default int getPort() {
		return DEFAULT_PORT;
	}

	/**
	 * Get the TCP client description.
	 * 
	 * <p>
	 * This implementation returns a string in the form {@literal host:port}.
	 * </p>
	 */
	@Override
	default String getDescription() {
		String host = getHost();
		if ( host == null ) {
			host = "";
		}
		return host + ':' + getPort();
	}

}
