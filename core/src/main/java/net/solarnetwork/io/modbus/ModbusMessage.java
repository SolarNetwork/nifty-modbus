/* ==================================================================
 * ModbusMessage.java - 25/11/2022 3:10:59 pm
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
 * API for a Modbus message.
 * 
 * @author matt
 * @version 1.0
 */
public interface ModbusMessage {

	/**
	 * Get the device unit ID.
	 * 
	 * <p>
	 * Modbus allows only values from 0-255 for the unit ID.
	 * </p>
	 * 
	 * @return the unit ID, as an integer
	 */
	int getUnitId();

	/**
	 * Get the Modbus function code.
	 * 
	 * @return the function code, never {@literal null}
	 */
	ModbusFunctionCode getFunction();

	/**
	 * Get the Modbus error code.
	 * 
	 * @return the error code, or {@literal null} if not an error
	 */
	ModbusErrorCode getError();

	/**
	 * Test if this message is an exception (error).
	 * 
	 * @return {@literal true} if {@link #getError()} it not {@literal null}
	 */
	default boolean isException() {
		return (getError() != null);
	}

}
