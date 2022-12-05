/* ==================================================================
 * ReadWriteRegistersModbusMessage.java - 30/11/2022 11:13:56 am
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
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * An addressed Modbus message for holding read/write registers.
 *
 * @author matt
 * @version 1.0
 */
public class ReadWriteRegistersModbusMessage extends RegistersModbusMessage
		implements net.solarnetwork.io.modbus.ReadWriteRegistersModbusMessage {

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
	 *        the read address
	 * @param count
	 *        the read count
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public ReadWriteRegistersModbusMessage(int unitId, byte function, int address, int count) {
		this(unitId, ModbusFunctionCode.valueOf(function), null, address, count, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param function
	 *        the function
	 * @param address
	 *        the read address
	 * @param count
	 *        the read count
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public ReadWriteRegistersModbusMessage(int unitId, byte function, int address, int count,
			byte[] data) {
		this(unitId, ModbusFunctionCode.valueOf(function), null, address, count, data);
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
	 *        the read address
	 * @param count
	 *        the read count
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} or {@code error} are not valid
	 */
	public ReadWriteRegistersModbusMessage(int unitId, byte function, byte error, int address, int count,
			byte[] data) {
		this(unitId, ModbusFunctionCode.valueOf(function), ModbusErrorCode.valueOf(error), address,
				count, data);
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
	 *        the read address
	 * @param count
	 *        the read count
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} is {@literal null}, or if {@code data} does
	 *         not have an even length (divisible by 2)
	 */
	public ReadWriteRegistersModbusMessage(int unitId, ModbusFunction function, ModbusError error,
			int address, int count, byte[] data) {
		super(unitId, function, error, address, count, data);
	}

	/**
	 * Create a read/write holding registers request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the register address to start reading from
	 * @param count
	 *        the number of registers to read
	 * @param writeAddress
	 *        the address to start writing to
	 * @param values
	 *        the values to write
	 * @return the new message
	 */
	public static ReadWriteRegistersModbusMessage readWriteHoldingsRequest(int unitId, int address,
			int count, int writeAddress, short[] values) {
		if ( count < 1 ) {
			throw new IllegalArgumentException("Count to read must be provided.");
		} else if ( count > MAX_READ_REGISTERS_COUNT ) {
			throw new IllegalArgumentException(
					String.format("Can only read up to %d registers, but %d requested.",
							MAX_READ_REGISTERS_COUNT, count));
		}
		final int writeCount = (values != null ? values.length : 0);
		if ( writeCount < 1 ) {
			throw new IllegalArgumentException("Values to write must be provided.");
		} else if ( writeCount > MAX_WRITE_REGISTERS_COUNT ) {
			throw new IllegalArgumentException(
					String.format("Can only write up to %d registers, but %d values provided.",
							MAX_WRITE_REGISTERS_COUNT, count));
		}
		byte[] data = new byte[2 + writeCount * 2];
		ModbusByteUtils.encode16(data, 0, writeAddress);
		ModbusByteUtils.encode(values, data, 2);
		return new ReadWriteRegistersModbusMessage(unitId, ModbusFunctionCode.ReadWriteHoldingRegisters,
				null, address, count, data);
	}

	/**
	 * Create a read/write holding registers response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the register address read from
	 * @param values
	 *        the values read
	 * @return the new message
	 */
	public static ReadWriteRegistersModbusMessage readWriteHoldingsResponse(int unitId, int address,
			short[] values) {
		final int count = (values != null ? values.length : 0);
		if ( count < 1 ) {
			throw new IllegalArgumentException("Count read must be provided.");
		} else if ( count > MAX_READ_REGISTERS_COUNT ) {
			throw new IllegalArgumentException(
					String.format("Can only read up to %d registers, but %d provded.",
							MAX_READ_REGISTERS_COUNT, count));
		}
		byte[] data = new byte[2 + count * 2];
		ModbusByteUtils.encode16(data, 0, READ_WRITE_RESPONSE_FLAG);
		ModbusByteUtils.encode(values, data, 2);
		return new ReadWriteRegistersModbusMessage(unitId, ModbusFunctionCode.ReadWriteHoldingRegisters,
				null, address, count, data);
	}

	/**
	 * Decode a Modbus read/write registers request message.
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
		ModbusFunction fn = ModbusFunctionCode.valueOf(functionCode);
		ModbusFunctionCode function = fn.functionCode();
		ModbusError error = ModbusMessageUtils.decodeError(functionCode, in);
		if ( error != null ) {
			return new BaseModbusMessage(unitId, function, error);
		}
		int addr = address;
		int cnt = count;
		byte[] data = null;
		if ( function != null ) {
			switch (function) {
				case ReadWriteHoldingRegisters: {
					addr = in.readUnsignedShort();
					cnt = in.readUnsignedShort();
					int writeAddr = in.readUnsignedShort();
					in.skipBytes(2);
					int len = in.readUnsignedByte();
					data = new byte[len + 2];
					ModbusByteUtils.encode16(data, 0, writeAddr);
					in.readBytes(data, 2, len);
				}
					break;

				default:
					return null;
			}
		}
		return new ReadWriteRegistersModbusMessage(unitId, fn, error, addr, cnt, data);
	}

	/**
	 * Decode a Modbus read/write registers request message.
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
		ModbusFunction fn = ModbusFunctionCode.valueOf(functionCode);
		ModbusFunctionCode function = fn.functionCode();
		ModbusError error = ModbusMessageUtils.decodeError(functionCode, in);
		if ( error != null ) {
			return new BaseModbusMessage(unitId, function, error);
		}
		int addr = address;
		int cnt = count;
		byte[] data = null;
		if ( function != null ) {
			switch (function) {
				case ReadWriteHoldingRegisters: {
					int len = in.readUnsignedByte();
					data = new byte[len + 2];
					data[0] = READ_WRITE_RESPONSE_FLAG_BYTE;
					data[1] = READ_WRITE_RESPONSE_FLAG_BYTE;
					cnt = len / 2;
					in.readBytes(data, 2, len);
				}
					break;

				default:
					return null;
			}
		}
		return new ReadWriteRegistersModbusMessage(unitId, fn, error, addr, cnt, data);
	}

	private boolean isRequest(final byte[] data) {
		return (data != null && data.length > 1 && !(data[0] == READ_WRITE_RESPONSE_FLAG_BYTE
				&& data[1] == READ_WRITE_RESPONSE_FLAG_BYTE));
	}

	private boolean isResponse(final byte[] data) {
		return (data != null && data.length > 1 && data[0] == READ_WRITE_RESPONSE_FLAG_BYTE
				&& data[1] == READ_WRITE_RESPONSE_FLAG_BYTE);
	}

	@Override
	public int payloadLength() {
		final ModbusFunctionCode fn = getFunction().functionCode();
		if ( fn != null ) {
			switch (fn) {
				case ReadWriteHoldingRegisters: {
					final byte[] data = data();
					if ( data[0] == READ_WRITE_RESPONSE_FLAG_BYTE
							&& data[1] == READ_WRITE_RESPONSE_FLAG_BYTE ) {
						return data.length;
					} else {
						return 8 + data.length;
					}
				}

				default:
					// fall through
			}
		}
		return super.payloadLength();
	}

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		final ModbusFunctionCode fn = getFunction().functionCode();
		final int count = getCount();
		byte[] header = null;
		final byte[] data_src = data();
		byte[] data;
		if ( data_src[0] == READ_WRITE_RESPONSE_FLAG_BYTE
				&& data_src[1] == READ_WRITE_RESPONSE_FLAG_BYTE ) {
			// response
			header = new byte[2];
			header[0] = fn.getCode();
			header[1] = (byte) (data_src.length - 2);
			data = new byte[header[1]];
			System.arraycopy(data_src, 2, data, 0, header[1]);
		} else {
			// request
			header = new byte[10];
			header[0] = fn.getCode();
			encode16(header, 1, getAddress());
			encode16(header, 3, count);
			System.arraycopy(data_src, 0, header, 5, 2);
			encode16(header, 7, data_src.length / 2 - 1);
			header[9] = (byte) (data_src.length - 2);
			data = new byte[header[9]];
			System.arraycopy(data_src, 2, data, 0, data.length);
		}
		out.writeBytes(header);
		out.writeBytes(data);
	}

	@Override
	public byte[] dataCopy() {
		final byte[] data = data();
		if ( !isResponse(data) ) {
			return null;
		}
		final byte[] readData = new byte[data.length - 2];
		System.arraycopy(data, 2, readData, 0, readData.length);
		return readData;
	}

	@Override
	public short[] dataDecode() {
		final byte[] data = data();
		if ( !isResponse(data) ) {
			return null;
		}
		return ModbusByteUtils.decode(data, 2, data.length);
	}

	@Override
	public int[] dataDecodeUnsigned() {
		final byte[] data = data();
		if ( !isResponse(data) ) {
			return null;
		}
		return ModbusByteUtils.decodeUnsigned(data, 2, data.length);
	}

	@Override
	public int getWriteAddress() {
		final byte[] data = data();
		if ( !isRequest(data) ) {
			return 0;
		}
		return (((data[0] & 0xFF) << 8) | (data[1] & 0xFF));
	}

	@Override
	public short[] writeDataDecode() {
		final byte[] data = data();
		if ( !isRequest(data) ) {
			return null;
		}
		return ModbusByteUtils.decode(data, 2, data.length);
	}

	@Override
	public int[] writeDataDecodeUnsigned() {
		final byte[] data = data();
		if ( !isRequest(data) ) {
			return null;
		}
		return ModbusByteUtils.decodeUnsigned(data, 2, data.length);
	}

}
