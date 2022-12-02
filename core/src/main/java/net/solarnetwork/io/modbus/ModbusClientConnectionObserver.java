/* ==================================================================
 * ModbusClientConnectionObserver.java - 3/12/2022 10:07:02 am
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

package net.solarnetwork.io.modbus;

/**
 * API for an observer of ModbusClient connection state.
 *
 * @author matt
 * @version 1.0
 */
public interface ModbusClientConnectionObserver {

	/**
	 * A connection has been established.
	 * 
	 * @param client
	 *        the client that has established the connection
	 * @param config
	 *        the client's configuration
	 */
	void connectionOpened(ModbusClient client, ModbusClientConfig config);

	/**
	 * A connection has been closed.
	 * 
	 * @param client
	 *        the client whose connection has been closed
	 * @param config
	 *        the client's configuration
	 * @param exception
	 *        an exception, if any
	 * @param willReconnect
	 *        {@literal true} if the client will automatically attempt to
	 *        re-establish the connection
	 */
	void connectionClosed(ModbusClient client, ModbusClientConfig config, Throwable exception,
			boolean willReconnect);

}
