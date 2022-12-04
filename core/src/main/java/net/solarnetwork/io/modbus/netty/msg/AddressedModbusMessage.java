/* ==================================================================
 * AddressedModbusMessage.java - 26/11/2022 6:38:49 am
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

package net.solarnetwork.io.modbus.netty.msg;

import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * An addressed Modbus message.
 * 
 * <p>
 * This type of message supports reading/writing block data values, using a
 * starting address and a count of values.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class AddressedModbusMessage extends BaseModbusMessage
		implements net.solarnetwork.io.modbus.AddressedModbusMessage {

	private final int address;
	private final int count;

	/**
	 * Constructor.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param function
	 *        the function
	 * @param address
	 *        the address
	 * @param count
	 *        the value count
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public AddressedModbusMessage(int unitId, byte function, int address, int count) {
		this(unitId, ModbusFunctionCode.valueOf(function), null, address, count);
	}

	/**
	 * Constructor.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param function
	 *        the function
	 * @param error
	 *        the error
	 * @param address
	 *        the address
	 * @param count
	 *        the value count
	 * @throws IllegalArgumentException
	 *         if {@code function} or {@code error} are not valid
	 */
	public AddressedModbusMessage(int unitId, byte function, byte error, int address, int count) {
		this(unitId, ModbusFunctionCode.valueOf(function), ModbusErrorCode.valueOf(error), address,
				count);
	}

	/**
	 * Constructor.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param function
	 *        the function
	 * @param error
	 *        the error, or {@literal null} if no error
	 * @param address
	 *        the address; if less than {@literal 0} then {@literal 0} will be
	 *        set
	 * @param count
	 *        the value count; if less than {@literal 0} then {@literal 0} will
	 *        be set
	 * @throws IllegalArgumentException
	 *         if {@code function} is {@literal null}
	 */
	public AddressedModbusMessage(int unitId, ModbusFunction function, ModbusError error, int address,
			int count) {
		super(unitId, function, error);
		this.address = (address < 0 ? 0 : address);
		this.count = (count < 0 ? 0 : count);
	}

	@Override
	public boolean isSameAs(ModbusMessage obj) {
		if ( !super.isSameAs(obj) ) {
			return false;
		}
		if ( !(obj instanceof AddressedModbusMessage) ) {
			return false;
		}
		AddressedModbusMessage other = (AddressedModbusMessage) obj;
		return address == other.address && count == other.count;
	}

	@Override
	public int getAddress() {
		return address;
	}

	@Override
	public int getCount() {
		return count;
	}

}
