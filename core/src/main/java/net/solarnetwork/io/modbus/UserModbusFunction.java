/* ==================================================================
 * UserModbusFunction.java - 3/12/2022 11:31:48 am
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
 * A user-defined Modbus function.
 *
 * @author matt
 * @version 1.0
 */
public class UserModbusFunction implements ModbusFunction {

	private final String displayName;
	private final byte code;
	private final ModbusBlockType blockType;
	private final boolean readFunction;
	private final ModbusFunction oppositeFunction;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *        the function code
	 */
	public UserModbusFunction(byte code) {
		this(null, code, null, false, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param displayName
	 *        the display name
	 * @param code
	 *        the function code
	 * @param blockType
	 *        the block type
	 * @param readFunction
	 *        the read function flag
	 * @param oppositeFunction
	 *        the opposite function
	 */
	public UserModbusFunction(String displayName, byte code, ModbusBlockType blockType,
			boolean readFunction, ModbusFunction oppositeFunction) {
		super();
		this.displayName = displayName;
		this.code = code;
		this.blockType = blockType;
		this.readFunction = readFunction;
		this.oppositeFunction = oppositeFunction;
	}

	@Override
	public byte getCode() {
		return code;
	}

	@Override
	public ModbusBlockType blockType() {
		return blockType;
	}

	@Override
	public boolean isReadFunction() {
		return readFunction;
	}

	@Override
	public ModbusFunction oppositeFunction() {
		return oppositeFunction;
	}

	@Override
	public String toDisplayString() {
		return displayName;
	}

}