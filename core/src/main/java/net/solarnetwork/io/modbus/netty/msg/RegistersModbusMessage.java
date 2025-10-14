/* ==================================================================
 * BitsModbusMessage.java - 26/11/2022 11:15:18 am
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
import java.util.Arrays;
import io.netty.buffer.ByteBuf;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusByteUtils;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * A Modbus message related to a 16-bit register-based (input/holding) register
 * address range.
 *
 * @author matt
 * @version 1.0
 */
public class RegistersModbusMessage extends AddressedModbusMessage
		implements net.solarnetwork.io.modbus.RegistersModbusMessage {

	/** The maximum number of registers that can be read at once. */
	public static final int MAX_READ_REGISTERS_COUNT = 0x7D;

	/** The maximum number of registers that can be written at once. */
	public static final int MAX_WRITE_REGISTERS_COUNT = 0x79;

	/**
	 * The maximum number of FIFO queue registers that can be returned at once.
	 */
	public static final int MAX_READ_FIFO_QUEUE_COUNT = 0x31;

	/**
	 * A special value used to mark the read/write holding registers response
	 * data.
	 */
	public static final int READ_WRITE_RESPONSE_FLAG = 0xFFFF;

	/**
	 * A special value used to mark the read/write holding register response
	 * data.
	 */
	public static final byte READ_WRITE_RESPONSE_FLAG_BYTE = (byte) 0xFF;

	private final byte[] data;

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
	 * @param count
	 *        the value count
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public RegistersModbusMessage(int unitId, byte function, int address, int count) {
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
	 *        the address
	 * @param count
	 *        the value count
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public RegistersModbusMessage(int unitId, byte function, int address, int count, byte[] data) {
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
	 *        the address
	 * @param count
	 *        the value count
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} or {@code error} are not valid
	 */
	public RegistersModbusMessage(int unitId, byte function, byte error, int address, int count,
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
	 *        the address
	 * @param count
	 *        the value count
	 * @param data
	 *        the register data, in most-to-least byte order (e.g. big endian);
	 *        note the array is <b>not</b> copied
	 * @throws IllegalArgumentException
	 *         if {@code function} is {@literal null}, or if {@code data} does
	 *         not have an even length (divisible by 2)
	 */
	public RegistersModbusMessage(int unitId, ModbusFunction function, ModbusError error, int address,
			int count, byte[] data) {
		super(unitId, function, error, address, count);
		if ( data != null && data.length % 2 != 0 ) {
			throw new IllegalArgumentException("The byte data has an odd length, but it must be even.");
		}
		this.data = (data != null && data.length > 0 ? data : null);
	}

	/**
	 * Create a read input registers request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the input register address to start reading from
	 * @param count
	 *        the number of registers to read
	 * @return the new message
	 */
	public static RegistersModbusMessage readInputsRequest(int unitId, int address, int count) {
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.ReadInputRegisters, null, address,
				count, null);
	}

	/**
	 * Create a read input registers response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the input register address to start reading from
	 * @param values
	 *        the values
	 * @return the new message
	 */
	public static RegistersModbusMessage readInputsResponse(int unitId, int address, short[] values) {
		final int count = (values != null ? values.length : 0);
		if ( count < 1 ) {
			throw new IllegalArgumentException("Values to write must be provided.");
		} else if ( count > MAX_READ_REGISTERS_COUNT ) {
			throw new IllegalArgumentException(
					String.format("Can only write up to %d registers, but %d values provided.",
							MAX_READ_REGISTERS_COUNT, count));
		}
		byte[] data = ModbusByteUtils.encode(values);
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.ReadInputRegisters, null, address,
				count, data);
	}

	/**
	 * Create a read holding registers request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to start reading from
	 * @param count
	 *        the number of registers to read
	 * @return the new message
	 */
	public static RegistersModbusMessage readHoldingsRequest(int unitId, int address, int count) {
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.ReadHoldingRegisters, null, address,
				count, null);
	}

	/**
	 * Create a read holding registers response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to start reading from
	 * @param values
	 *        the values
	 * @return the new message
	 */
	public static RegistersModbusMessage readHoldingsResponse(int unitId, int address, short[] values) {
		final int count = (values != null ? values.length : 0);
		if ( count < 1 ) {
			throw new IllegalArgumentException("Values to write must be provided.");
		} else if ( count > MAX_READ_REGISTERS_COUNT ) {
			throw new IllegalArgumentException(
					String.format("Can only write up to %d registers, but %d values provided.",
							MAX_READ_REGISTERS_COUNT, count));
		}
		byte[] data = ModbusByteUtils.encode(values);
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.ReadHoldingRegisters, null, address,
				count, data);
	}

	/**
	 * Create a write holding register request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to write to
	 * @param value
	 *        the 16-bit register value
	 * @return the new message
	 */
	public static RegistersModbusMessage writeHoldingRequest(int unitId, int address, int value) {
		byte[] data = new byte[2];
		ModbusByteUtils.encode16(data, 0, value);
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.WriteHoldingRegister, null, address,
				1, data);
	}

	/**
	 * Create a write holding register response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to write to
	 * @param value
	 *        the 16-bit register value
	 * @return the new message
	 */
	public static RegistersModbusMessage writeHoldingResponse(int unitId, int address, int value) {
		return writeHoldingRequest(unitId, address, value);
	}

	/**
	 * Create a write holding registers request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to write to
	 * @param values
	 *        the 16-bit register values
	 * @return the new message
	 */
	public static RegistersModbusMessage writeHoldingsRequest(int unitId, int address, short[] values) {
		final int count = (values != null ? values.length : 0);
		if ( count < 1 ) {
			throw new IllegalArgumentException("Values to write must be provided.");
		} else if ( count > MAX_WRITE_REGISTERS_COUNT ) {
			throw new IllegalArgumentException(
					String.format("Can only write up to %d registers, but %d values provided.",
							MAX_WRITE_REGISTERS_COUNT, count));
		}
		byte[] data = ModbusByteUtils.encode(values);
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.WriteHoldingRegisters, null,
				address, count, data);
	}

	/**
	 * Create a write holding registers response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the holding register address to write to
	 * @param count
	 *        the register count values
	 * @return the new message
	 */
	public static RegistersModbusMessage writeHoldingsResponse(int unitId, int address, int count) {
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.WriteHoldingRegisters, null,
				address, count, null);
	}

	/**
	 * Create a read FIFO queue request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the FIFO register address to read from
	 * @return the new message
	 */
	public static RegistersModbusMessage readFifoQueueRequest(int unitId, int address) {
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.ReadFifoQueue, null, address, 0,
				null);
	}

	/**
	 * Create a read FIFO queue response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the FIFO register address read from
	 * @param values
	 *        the values
	 * @return the new message
	 */
	public static RegistersModbusMessage readFifoQueueResponse(int unitId, int address, short[] values) {
		final int count = (values != null ? values.length : 0);
		if ( count < 1 ) {
			throw new IllegalArgumentException("Count read must be provided.");
		} else if ( count > MAX_READ_FIFO_QUEUE_COUNT ) {
			throw new IllegalArgumentException(
					String.format("Can only read up to %d FIFO queue values, but %d provded.",
							MAX_READ_FIFO_QUEUE_COUNT, count));
		}
		byte[] data = ModbusByteUtils.encode(values);
		return new RegistersModbusMessage(unitId, ModbusFunctionCode.ReadFifoQueue, null, address, count,
				data);
	}

	/**
	 * Create a register request message.
	 * 
	 * @param type
	 *        the block type; only Input and Holding types are supported
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the register address to start reading from
	 * @param count
	 *        the number of bits to read
	 * @return the new message
	 */
	public static RegistersModbusMessage readRegistersRequest(ModbusBlockType type, int unitId,
			int address, int count) {
		switch (type) {
			case Input:
				return readInputsRequest(unitId, address, count);

			case Holding:
				return readHoldingsRequest(unitId, address, count);

			default:
				throw new IllegalArgumentException(
						"Only Input/Holding types are supported; got " + type);
		}
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
				case ReadInputRegisters:
				case ReadHoldingRegisters:
					addr = in.readUnsignedShort();
					cnt = in.readUnsignedShort();
					break;

				case WriteHoldingRegister:
					addr = in.readUnsignedShort();
					cnt = 1;
					data = new byte[2];
					in.readBytes(data);
					break;

				case WriteHoldingRegisters:
					addr = in.readUnsignedShort();
					cnt = in.readUnsignedShort();
					data = new byte[in.readUnsignedByte()];
					in.readBytes(data);
					break;

				case ReadFifoQueue:
					addr = in.readUnsignedShort();
					cnt = 0;
					break;

				default:
					return null;
			}
		}
		return new RegistersModbusMessage(unitId, fn, error, addr, cnt, data);
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
				case ReadInputRegisters:
				case ReadHoldingRegisters:
					data = new byte[in.readUnsignedByte()];
					cnt = data.length / 2;
					in.readBytes(data);
					break;

				case WriteHoldingRegister:
					addr = in.readUnsignedShort();
					cnt = 1;
					data = new byte[2];
					in.readBytes(data);
					break;

				case WriteHoldingRegisters:
					addr = in.readUnsignedShort();
					cnt = in.readUnsignedShort();
					break;

				case ReadFifoQueue:
					data = new byte[in.readUnsignedShort() - 2];
					cnt = in.readUnsignedShort();
					in.readBytes(data);
					break;

				default:
					return null;
			}
		}
		return new RegistersModbusMessage(unitId, fn, error, addr, cnt, data);
	}

	@Override
	public boolean isSameAs(ModbusMessage obj) {
		if ( !super.isSameAs(obj) ) {
			return false;
		}
		if ( !(obj instanceof RegistersModbusMessage) ) {
			return false;
		}
		RegistersModbusMessage other = (RegistersModbusMessage) obj;
		return Arrays.equals(data, other.data);
	}

	/**
	 * Get the raw data.
	 * 
	 * @return the raw data
	 */
	protected byte[] data() {
		return data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegistersModbusMessage{");
		builder.append("unitId=");
		builder.append(getUnitId());
		builder.append(", function=");
		builder.append(getFunction());
		if ( getError() != null ) {
			builder.append(", error=");
			builder.append(getError());
		}
		builder.append(", address=");
		builder.append(getAddress());
		builder.append(", count=");
		builder.append(getCount());
		if ( data != null ) {
			builder.append(", data=");
			builder.append(ModbusByteUtils.encodeHexString(data, 0, data.length, true));
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public byte[] dataCopy() {
		if ( data == null ) {
			return null;
		}
		byte[] copy = new byte[data.length];
		System.arraycopy(data, 0, copy, 0, data.length);
		return copy;
	}

	@Override
	public short[] dataDecode() {
		return ModbusByteUtils.decode(data);
	}

	@Override
	public int[] dataDecodeUnsigned() {
		return ModbusByteUtils.decodeUnsigned(data);
	}

	@Override
	public int payloadLength() {
		final ModbusFunctionCode fn = getFunction().functionCode();
		if ( fn != null ) {
			switch (fn) {
				case ReadInputRegisters:
				case ReadHoldingRegisters:
					return (data == null ? 5 : 2 + byteCount(getCount()));

				case WriteHoldingRegister:
					return 5;

				case ReadFifoQueue:
					return (data == null ? 3 : 5 + byteCount(getCount()));

				case WriteHoldingRegisters:
					return (data != null ? 6 + getCount() * 2 : 5);

				default:
					// fall through
			}
		}
		return super.payloadLength() + (data != null ? data.length : 0);
	}

	private static int byteCount(int count) {
		return count * 2;
	}

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		final ModbusFunctionCode fn = getFunction().functionCode();
		if ( fn != null ) {
			final int count = getCount();
			final int byteCount = byteCount(count);
			byte[] header = null;
			byte[] data = this.data;
			switch (fn) {
				case ReadInputRegisters:
				case ReadHoldingRegisters:
					if ( data == null ) {
						// request
						header = new byte[5];
						header[0] = fn.getCode();
						encode16(header, 1, getAddress());
						encode16(header, 3, count);
					} else {
						// response
						header = new byte[2];
						header[0] = fn.getCode();
						header[1] = (byte) byteCount;
					}
					break;

				case WriteHoldingRegister:
					header = new byte[3];
					header[0] = fn.getCode();
					encode16(header, 1, getAddress());
					break;

				case WriteHoldingRegisters:
					if ( data != null ) {
						// request
						header = new byte[6];
						header[0] = fn.getCode();
						encode16(header, 1, getAddress());
						encode16(header, 3, count);
						header[5] = (byte) byteCount;
					} else {
						// response
						header = new byte[5];
						header[0] = fn.getCode();
						encode16(header, 1, getAddress());
						encode16(header, 3, count);
					}
					break;

				case ReadFifoQueue:
					if ( data == null ) {
						// request
						header = new byte[3];
						header[0] = fn.getCode();
						encode16(header, 1, getAddress());
					} else {
						// response
						header = new byte[5];
						header[0] = fn.getCode();
						encode16(header, 1, 2 + byteCount);
						encode16(header, 3, count);
					}
					break;

				default:
					super.encodeModbusPayload(out);
			}
			if ( header != null ) {
				out.writeBytes(header);
			}
			if ( data != null ) {
				out.writeBytes(data);
			}
		} else {
			super.encodeModbusPayload(out);
		}
	}

}
