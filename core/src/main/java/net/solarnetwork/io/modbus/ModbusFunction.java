/* ==================================================================
 * ModbusFunction.java - 3/12/2022 11:29:35 am
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
 * API for a Modbus function.
 * 
 * <p>
 * This API exists to support user-defined Modbus functions that are not defined
 * in the specification.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public interface ModbusFunction {

	/**
	 * Get the function code.
	 * 
	 * @return the code
	 */
	public byte getCode();

	/**
	 * Get this function as a {@link ModbusFunctionCode}.
	 * 
	 * @return the function code enumeration value, or {@literal null} if cannot
	 *         be represented as one
	 */
	default ModbusFunctionCode functionCode() {
		try {
			return ModbusFunctionCode.forCode(getCode());
		} catch ( IllegalArgumentException e ) {
			return null;
		}
	}

	/**
	 * Get a friendly display string for this function.
	 * 
	 * @return a display string
	 */
	public String toDisplayString();

	/**
	 * Return {@literal true} if this function represents a read operation.
	 * 
	 * @return {@literal true} if this function represents a read operation,
	 *         {@literal false} if a write operation
	 */
	public boolean isReadFunction();

	/**
	 * Get an "opposite" function from this function.
	 * 
	 * <p>
	 * This method is used to get a read function for a given write function,
	 * and a write function for a given read function. Note that not all
	 * functions have exact opposites, such that:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * ModbusFunction a = myFunction();
	 * ModbusFunction b = a.oppositeFunction();
	 * ModbusFunction c = b.oppositeFunction();
	 * // at this stage c is not necessarily equal to a
	 * </code>
	 * </pre>
	 * 
	 * @return the function, or {@literal null} if not applicable
	 */
	public ModbusFunction oppositeFunction();

	/**
	 * Get the register block type related to this function.
	 * 
	 * @return the block type
	 */
	public ModbusBlockType blockType();

}
