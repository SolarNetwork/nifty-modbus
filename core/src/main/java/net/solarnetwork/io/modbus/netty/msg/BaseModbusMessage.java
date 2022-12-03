/* ==================================================================
 * BaseModbusMessage.java - 25/11/2022 6:19:20 pm
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

import io.netty.buffer.ByteBuf;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * A base implementation of {@link ModbusMessage}.
 *
 * @author matt
 * @version 1.0
 */
public class BaseModbusMessage implements ModbusMessage, ModbusPayloadEncoder {

	private final int unitId;
	private final ModbusFunction function;
	private final ModbusError error;

	/**
	 * Constructor.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param function
	 *        the function
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public BaseModbusMessage(int unitId, byte function) {
		this(unitId, ModbusFunctionCode.valueOf(function), null);
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
	 * @throws IllegalArgumentException
	 *         if {@code function} or {@code error} are not valid
	 */
	public BaseModbusMessage(int unitId, byte function, byte error) {
		this(unitId, ModbusFunctionCode.valueOf(function), ModbusErrorCode.valueOf(error));
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
	 * @throws IllegalArgumentException
	 *         if {@code function} is {@literal null}
	 */
	public BaseModbusMessage(int unitId, ModbusFunction function, ModbusError error) {
		super();
		this.unitId = unitId;
		if ( function == null ) {
			throw new IllegalArgumentException("The function argument must not be null.");
		}
		this.function = function;
		this.error = error;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
		if ( msgType.isAssignableFrom(getClass()) ) {
			return (T) this;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseModbusMessage{unitId=");
		builder.append(unitId);
		builder.append(", ");
		if ( function != null ) {
			builder.append("function=");
			builder.append(function);
			builder.append(", ");
		}
		if ( error != null ) {
			builder.append("error=");
			builder.append(error);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public int getUnitId() {
		return unitId;
	}

	@Override
	public ModbusFunction getFunction() {
		return function;
	}

	@Override
	public ModbusError getError() {
		return error;
	}

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		out.writeByte(function.getCode());
	}

	@Override
	public int payloadLength() {
		return 1;
	}

}
