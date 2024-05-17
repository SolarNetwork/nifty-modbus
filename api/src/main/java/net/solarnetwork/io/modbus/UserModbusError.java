/* ==================================================================
 * UserModbusError.java - 3/12/2022 5:35:43 pm
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
 * A user-defined or otherwise unknown error code.
 *
 * @author matt
 * @version 1.1
 */
public final class UserModbusError implements ModbusError {

	private final byte code;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *        the error code
	 */
	public UserModbusError(byte code) {
		super();
		this.code = code;
	}

	@Override
	public int hashCode() {
		return Byte.hashCode(code);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof UserModbusError) ) {
			return false;
		}
		UserModbusError other = (UserModbusError) obj;
		return code == other.code;
	}

	@Override
	public byte getCode() {
		return code;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserModbusError{0x");
		builder.append(Integer.toString(Byte.toUnsignedInt(code), 16));
		builder.append("}");
		return builder.toString();
	}

}
