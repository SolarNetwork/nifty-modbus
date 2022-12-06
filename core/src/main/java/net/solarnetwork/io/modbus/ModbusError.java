/* ==================================================================
 * ModbusExceptionCode.java - 3/12/2022 5:33:37 pm
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
 * API for a Modbus exception code.
 *
 * @author matt
 * @version 1.0
 */
public interface ModbusError {

	/**
	 * Get the error code value.
	 * 
	 * @return the code value
	 */
	byte getCode();

	/**
	 * Get this function as a {@link ModbusFunctionCode}.
	 * 
	 * @return the function code enumeration value, or {@literal null} if cannot
	 *         be represented as one
	 */
	default ModbusErrorCode errorCode() {
		try {
			return ModbusErrorCode.forCode(getCode());
		} catch ( IllegalArgumentException e ) {
			return null;
		}
	}

	@Override
	int hashCode();

	@Override
	boolean equals(Object obj);

}
