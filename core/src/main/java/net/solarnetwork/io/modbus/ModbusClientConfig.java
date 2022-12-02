/* ==================================================================
 * ModbusClientConfig.java - 29/11/2022 4:04:19 pm
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
 * API for Modbus client configuration.
 *
 * @author matt
 * @version 1.0
 */
public interface ModbusClientConfig {

	/** The default automatic reconnection setting. */
	boolean DEFAULT_AUTO_RECONNECT = true;

	/** The default reconnection delay seconds. */
	long DEFAULT_RECONNECT_DELAY_SECS = 10L;

	/**
	 * Get the "auto reconnect" setting.
	 * 
	 * @return {@literal true} if the Modbus connection should try to maintain a
	 *         persistent connection, and reestablish it if it fails/closes/ends
	 *         for any reason; {@literal false} to not automatically reestablish
	 *         the connection if it fails
	 */
	default boolean isAutoReconnect() {
		return DEFAULT_AUTO_RECONNECT;
	}

	/**
	 * Get the number of seconds to delay attempting to automatically
	 * reestablish a connection when the {@link #isAutoReconnect()} setting is
	 * enabled.
	 * 
	 * @return the number of seconds to delay before attempting to reestablish a
	 *         failed/closed/ended connection and {@link #isAutoReconnect()}
	 *         returns {@literal true}
	 */
	default long getAutoReconnectDelaySeconds() {
		return DEFAULT_RECONNECT_DELAY_SECS;
	}

	/**
	 * Get a description of this Modbus client configuration.
	 * 
	 * <p>
	 * This intention of this value is for use in logging and debugging
	 * contexts. It should return something meaningful and specific to the
	 * configuration, such as a description of the connection like the serial
	 * port name or TCP host and port.
	 * </p>
	 * 
	 * @return the configuration description, never {@literal null}
	 */
	String getDescription();

}
