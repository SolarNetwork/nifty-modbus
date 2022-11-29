/* ==================================================================
 * MaskWriteRegisterModbusMessage.java - 27/11/2022 2:45:51 pm
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

import static net.solarnetwork.io.modbus.ModbusByteUtils.encode16;
import io.netty.buffer.ByteBuf;
import net.solarnetwork.io.modbus.ModbusByteUtils;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * An addressed Modbus message for holding mask write register.
 *
 * @author matt
 * @version 1.0
 */
public class MaskWriteRegisterModbusMessage extends RegistersModbusMessage
		implements net.solarnetwork.io.modbus.MaskWriteRegisterModbusMessage {

	/**
	 * Constructor.
	 * 
	 * <p>
	 * This constructs a message without any data.
	 * </p>
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param function
	 *        the function
	 * @param address
	 *        the address
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public MaskWriteRegisterModbusMessage(int unitId, byte function, int address) {
		this(unitId, ModbusFunctionCode.forCode(function), null, address, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param function
	 *        the function
	 * @param address
	 *        the address
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public MaskWriteRegisterModbusMessage(int unitId, byte function, int address, byte[] data) {
		this(unitId, ModbusFunctionCode.forCode(function), null, address, data);
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
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} or {@code error} are not valid
	 */
	public MaskWriteRegisterModbusMessage(int unitId, byte function, byte error, int address, byte[] data) {
		this(unitId, ModbusFunctionCode.forCode(function), ModbusErrorCode.forCode(error), address,
				data);
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
	 *        the address
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} is {@literal null}, or if {@code data} does
	 *         not have an even length (divisible by 2)
	 */
	public MaskWriteRegisterModbusMessage(int unitId, ModbusFunctionCode function, ModbusErrorCode error,
			int address, byte[] data) {
		super(unitId, function, error, address, 1, data);
	}

	/**
	 * Create a mask write request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to write to
	 * @param andMask
	 *        the 16-bit and mask
	 * @param orMask
	 *        the 16-bit or mask
	 * @return the new message
	 */
	public static MaskWriteRegisterModbusMessage maskWriteRequest(int unitId, int address, int andMask,
			int orMask) {
		byte[] data = new byte[4];
		ModbusByteUtils.encode16(data, 0, andMask);
		ModbusByteUtils.encode16(data, 2, orMask);
		return new MaskWriteRegisterModbusMessage(unitId, ModbusFunctionCode.MaskWriteHoldingRegister, null,
				address, data);
	}

	/**
	 * Create a mask write response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to write to
	 * @param andMask
	 *        the 16-bit and mask
	 * @param orMask
	 *        the 16-bit or mask
	 * @return the new message
	 */
	public static MaskWriteRegisterModbusMessage maskWriteResponse(int unitId, int address, int andMask,
			int orMask) {
		return maskWriteRequest(unitId, address, andMask, orMask);
	}

	/**
	 * Decode a Modbus request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param functionCode
	 *        the function code value
	 * @param address
	 *        the address if known in advance, otherwise {@code 0}
	 * @param count
	 *        the count if known in advance, otherwise {@code 0}
	 * @param in
	 *        the input, assumed to be positioned after the function code byte
	 *        in the payload
	 * @return the message, or {@literal null} if a message cannot be decoded
	 */
	public static ModbusMessage decodeRequestPayload(final int unitId, final byte functionCode,
			final int address, final int count, final ByteBuf in) {
		ModbusFunctionCode function = ModbusFunctionCode.forCode(functionCode);
		ModbusErrorCode error = ModbusMessageUtils.decodeErrorCode(functionCode, in);
		if ( error != null ) {
			return new BaseModbusMessage(unitId, function, error);
		}
		int addr = address;
		byte[] data = null;
		if ( error == null ) {
			switch (function) {
				case MaskWriteHoldingRegister:
					addr = in.readUnsignedShort();
					data = new byte[4];
					in.readBytes(data);
					break;

				default:
					return null;
			}
		}
		return new MaskWriteRegisterModbusMessage(unitId, function, error, addr, data);
	}

	/**
	 * Decode a Modbus response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param functionCode
	 *        the function code value
	 * @param address
	 *        the address if known in advance, otherwise {@code 0}
	 * @param count
	 *        the count if known in advance, otherwise {@code 0}
	 * @param in
	 *        the input, assumed to be positioned after the function code byte
	 *        in the payload
	 * @return the message, or {@literal null} if a message cannot be decoded
	 */
	public static ModbusMessage decodeResponsePayload(final int unitId, final byte functionCode,
			final int address, final int count, final ByteBuf in) {
		ModbusFunctionCode function = ModbusFunctionCode.forCode(functionCode);
		ModbusErrorCode error = ModbusMessageUtils.decodeErrorCode(functionCode, in);
		if ( error != null ) {
			return new BaseModbusMessage(unitId, function, error);
		}
		int addr = address;
		byte[] data = null;
		if ( error == null ) {
			switch (function) {
				case MaskWriteHoldingRegister:
					addr = in.readUnsignedShort();
					data = new byte[4];
					in.readBytes(data);
					break;

				default:
					return null;
			}
		}
		return new MaskWriteRegisterModbusMessage(unitId, function, error, addr, data);
	}

	@Override
	public int getAndMask() {
		final byte[] data = data();
		if ( data != null && data.length > 3 ) {
			return (((data[0] & 0xFF) << 8) | data[1] & 0xFF);
		}
		return 0;
	}

	@Override
	public int getOrMask() {
		final byte[] data = data();
		if ( data != null && data.length > 3 ) {
			return (((data[2] & 0xFF) << 8) | data[3] & 0xFF);
		}
		return 0;
	}

	@Override
	public int payloadLength() {
		switch (getFunction()) {
			case MaskWriteHoldingRegister:
				return 7;

			default:
				return super.payloadLength();

		}
	}

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		byte[] data = new byte[7];
		data[0] = ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER;
		encode16(data, 1, getAddress());
		encode16(data, 3, getAndMask());
		encode16(data, 5, getOrMask());
		out.writeBytes(data);
	}

}
