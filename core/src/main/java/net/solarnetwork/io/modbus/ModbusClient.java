/* ==================================================================
 * ModbusClient.java - 29/11/2022 3:25:52 pm
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

import java.util.concurrent.Future;

/**
 * API for a Modbus client application.
 *
 * @author matt
 * @version 1.0
 */
public interface ModbusClient {

	/**
	 * Get the client configuration.
	 * 
	 * @return the client configuration
	 */
	ModbusClientConfig getClientConfig();

	/**
	 * Start the client.
	 * 
	 * <p>
	 * This method must be called before using the {@link #send(ModbusMessage)}
	 * or {@link #sendAsync(ModbusMessage)} methods. Calling this method on a
	 * client that has already been started is allowed, and will return the same
	 * result as first returned.
	 * </p>
	 * 
	 * @return a future that completes when the client is ready to be used
	 */
	Future<?> start();

	/**
	 * Test if the client has been started by a call to {@link #start()}
	 * already.
	 * 
	 * <p>
	 * This will return {@literal true} if {@link #start()} has been called,
	 * until {@link #stop()} is called.
	 * </p>
	 * 
	 * @return {@literal true} if the client has been started (and not stopped)
	 */
	boolean isStarted();

	/**
	 * Stop the client.
	 * 
	 * <p>
	 * This method shuts the client down, disconnecting it from whatever Modbus
	 * network it had been connected to. It can be started again by calling
	 * {@link #start()}. Calling this method on a client that has already been
	 * stopped is allowed and will not result in any error.
	 * </p>
	 * 
	 * <p>
	 * After calling this method {@link #isStarted()} will return
	 * {@literal false}.
	 * </p>
	 */
	void stop();

	/**
	 * Test if the client is started and connected to the Modbus network.
	 * 
	 * <p>
	 * Some clients may automatically reconnect to the network if the connection
	 * fails for any reason. After the connection has failed, and until it
	 * reconnects, this method will return {@literal false}.
	 * </p>
	 * 
	 * @return {@literal true} if the client is connected
	 */
	boolean isConnected();

	/**
	 * Send a request and receive a response, synchronously.
	 * 
	 * @param request
	 *        the request to send
	 * @return the response
	 */
	ModbusMessage send(ModbusMessage request);

	/**
	 * Send a request and receive a response, asynchronously.
	 * 
	 * @param request
	 *        the request to send
	 * @return the response future
	 */
	Future<ModbusMessage> sendAsync(ModbusMessage request);

	/**
	 * Configure a connection observer.
	 * 
	 * @param connectionObserver
	 *        the observer to set, or {@literal null} to clear
	 */
	void setConnectionObserver(ModbusClientConnectionObserver connectionObserver);

}
