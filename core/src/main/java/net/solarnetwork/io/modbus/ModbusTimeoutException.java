/* ==================================================================
 * ModbusTimeoutException.java - 16/04/2023 6:49:59 pm
 *
 * Copyright 2023 SolarNetwork.net Dev Team
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
 * A Modbus timeout exception.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusTimeoutException extends ModbusException {

	private static final long serialVersionUID = -6107760863617321318L;

	/**
	 * Constructor.
	 */
	public ModbusTimeoutException() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        the message
	 */
	public ModbusTimeoutException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *        the cause
	 */
	public ModbusTimeoutException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 */
	public ModbusTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        the message
	 * @param cause
	 *        the cause
	 * @param enableSuppression
	 *        {@literal true} to enable stack trace suppression
	 * @param writableStackTrace
	 *        {@literal true} for a writable stack trace
	 */
	public ModbusTimeoutException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
