/* ==================================================================
 * SerialDatabits.java - 2/12/2022 9:05:33 am
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

package net.solarnetwork.io.modbus.serial;

/**
 * Enumeration of serial "stopbit" settings.
 *
 * @author matt
 * @version 1.0
 */
public enum SerialStopBits {

	/** One stop bit. */
	One(SerialStopBits.ONE_STOP_BIT),

	/** One and a half stop bits. */
	OnePointFive(SerialStopBits.ONE_POINT_FIVE_STOP_BITS),

	/** Two stop bits. */
	Two(SerialStopBits.TWO_STOP_BITS),

	;

	/** Code value for one stop bit. */
	public static final int ONE_STOP_BIT = 1;

	/** Code value for 1.5 stop bits. */
	public static final int ONE_POINT_FIVE_STOP_BITS = 3;

	/** Code value for two stop bits. */
	public static final int TWO_STOP_BITS = 2;

	private final int code;

	SerialStopBits(int code) {
		this.code = code;
	}

	/**
	 * Get the code value.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get an enum instance for a code value.
	 * 
	 * @param code
	 *        the code
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not a valid value
	 */
	public static SerialStopBits forCode(int code) {
		switch (code) {
			case ONE_STOP_BIT:
				return SerialStopBits.One;

			case ONE_POINT_FIVE_STOP_BITS:
				return SerialStopBits.OnePointFive;

			case TWO_STOP_BITS:
				return SerialStopBits.Two;

			default:
				throw new IllegalArgumentException("Unknown serial stopbits code [" + code + "]");
		}
	}

}
