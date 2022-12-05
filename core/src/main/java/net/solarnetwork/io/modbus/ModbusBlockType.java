/* ==================================================================
 * ModbusBlockType.java - 25/11/2022 4:07:04 pm
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
 * Modbus register block types.
 * 
 * @author matt
 * @version 1.0
 */
public enum ModbusBlockType {

	/** Coil (toggle) type. */
	Coil(0, 1, false),

	/** Discrete (input) type. */
	Discrete(1, 1, true),

	/** Holding (output) type. */
	Holding(3, 16, false),

	/** Input type. */
	Input(4, 16, true),

	/** Diagnostic information. */
	Diagnostic(-1, 0, true),

	;

	private final int code;
	private final int bitCount;
	private final boolean readOnly;

	private ModbusBlockType(int code, int bitCount, boolean readOnly) {
		this.code = code;
		this.bitCount = bitCount;
		this.readOnly = readOnly;
	}

	/**
	 * Get the function code.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get the number of bits registers of this type use.
	 * 
	 * @return the bit count
	 */
	public int getBitCount() {
		return bitCount;
	}

	/**
	 * Get the read-only flag.
	 * 
	 * @return {@literal true} if registers of this type are read-only
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Get the "bit type-ness" of this register block type.
	 * 
	 * @return {@literal true} if this is a coil or discrete register block
	 */
	public boolean isBitType() {
		return bitCount == 1;
	}

	/**
	 * Get an enumeration instance for a code value.
	 * 
	 * @param code
	 *        the code value to get the enumeration for
	 * @return the enumeration, or {@literal null} if not supported
	 */
	public static ModbusBlockType valueOf(int code) {
		switch (code) {
			case 0:
				return Coil;

			case 1:
				return Discrete;

			case 3:
				return Holding;

			case 4:
				return Input;

			case -1:
				return Diagnostic;

			default:
				return null;
		}
	}

	/**
	 * Get an enumeration instance for a code value.
	 * 
	 * @param code
	 *        the code value to get the enumeration for
	 * @return the enumeration
	 * @throws IllegalArgumentException
	 *         if {@literal code} is not a valid value
	 */
	public static ModbusBlockType forCode(int code) {
		ModbusBlockType b = valueOf(code);
		if ( b != null ) {
			return b;
		}
		throw new IllegalArgumentException("ModbusBlockType code [" + code + "] not supported.");
	}

}
