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
import java.math.BigInteger;
import io.netty.buffer.ByteBuf;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusByteUtils;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * A addressed Modbus message for bit-related blocks, like coils and discrete
 * input registers.
 *
 * @author matt
 * @version 1.0
 */
public class BitsModbusMessage extends AddressedModbusMessage
		implements net.solarnetwork.io.modbus.BitsModbusMessage {

	private final BigInteger bits;

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
	 * @param bits
	 *        the bit values
	 * @throws IllegalArgumentException
	 *         if {@code function} is not valid
	 */
	public BitsModbusMessage(int unitId, byte function, int address, int count, BigInteger bits) {
		this(unitId, ModbusFunctionCode.forCode(function), null, address, count, bits);
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
	 * @param bits
	 *        the bit values
	 * @throws IllegalArgumentException
	 *         if {@code function} or {@code error} are not valid
	 */
	public BitsModbusMessage(int unitId, byte function, byte error, int address, int count,
			BigInteger bits) {
		this(unitId, ModbusFunctionCode.forCode(function), ModbusErrorCode.forCode(error), address,
				count, bits);
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
	 * @param bits
	 *        the bit values
	 * @throws IllegalArgumentException
	 *         if {@code function} is {@literal null}
	 */
	public BitsModbusMessage(int unitId, ModbusFunctionCode function, ModbusErrorCode error, int address,
			int count, BigInteger bits) {
		super(unitId, function, error, address, count);
		this.bits = bits;
	}

	/**
	 * Create a read coils request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the coil register address to start reading from
	 * @param count
	 *        the number of bits to read
	 * @return the new message
	 */
	public static BitsModbusMessage readCoilsRequest(int unitId, int address, int count) {
		return readCoilsResponse(unitId, address, count, null);
	}

	/**
	 * Create a read coils response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the coil register address to start reading from
	 * @param count
	 *        the number of bits read
	 * @param bits
	 *        the bits
	 * @return the new message
	 */
	public static BitsModbusMessage readCoilsResponse(int unitId, int address, int count,
			BigInteger bits) {
		return new BitsModbusMessage(unitId, ModbusFunctionCode.ReadCoils, null, address, count, bits);
	}

	/**
	 * Create a write coil request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the coil register address to write to
	 * @param enabled
	 *        {@literal true} to set the coil, or {@literal false} to clear it
	 * @return the new message
	 */
	public static BitsModbusMessage writeCoilRequest(int unitId, int address, boolean enabled) {
		return new BitsModbusMessage(unitId, ModbusFunctionCode.WriteCoil, null, address, 1,
				enabled ? BigInteger.ONE : BigInteger.ZERO);
	}

	/**
	 * Create a write coil response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the coil register address to write to
	 * @param enabled
	 *        {@literal true} to set the coil, or {@literal false} to clear it
	 * @return the new message
	 */
	public static BitsModbusMessage writeCoilResponse(int unitId, int address, boolean enabled) {
		return writeCoilRequest(unitId, address, enabled);
	}

	/**
	 * Create a write coils request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the coil register address to start writing to
	 * @param count
	 *        the number of bits to set
	 * @param bits
	 *        the bit values
	 * @return the new message
	 */
	public static BitsModbusMessage writeCoilsRequest(int unitId, int address, int count,
			BigInteger bits) {
		return new BitsModbusMessage(unitId, ModbusFunctionCode.WriteCoils, null, address, count, bits);
	}

	/**
	 * Create a write coils response message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the coil register address to start writing to
	 * @param count
	 *        the number of bits to set
	 * @param bits
	 *        the bit values
	 * @return the new message
	 */
	public static BitsModbusMessage writeCoilsResponse(int unitId, int address, int count) {
		return writeCoilsRequest(unitId, address, count, null);
	}

	/**
	 * Create a read discrete request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the discrete register address to start reading from
	 * @param count
	 *        the number of bits to read
	 * @return the new message
	 */
	public static BitsModbusMessage readDiscretesRequest(int unitId, int address, int count) {
		return readDiscretesResponse(unitId, address, count, null);
	}

	/**
	 * Create a read discrete request message.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the discrete register address to start reading from
	 * @param count
	 *        the number of bits to read
	 * @param bits
	 *        the bits
	 * @return the new message
	 */
	public static BitsModbusMessage readDiscretesResponse(int unitId, int address, int count,
			BigInteger bits) {
		return new BitsModbusMessage(unitId, ModbusFunctionCode.ReadDiscreteInputs, null, address, count,
				bits);
	}

	/**
	 * Create a bits register request message.
	 * 
	 * @param type
	 *        the block type; only Coil and Discrete types are supported
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the bit register address to start reading from
	 * @param count
	 *        the number of bits to read
	 * @return the new message
	 */
	public static BitsModbusMessage readBitsRequest(ModbusBlockType type, int unitId, int address,
			int count) {
		switch (type) {
			case Coil:
				return readCoilsRequest(unitId, address, count);

			case Discrete:
				return readDiscretesRequest(unitId, address, count);

			default:
				throw new IllegalArgumentException(
						"Only Coil/Discrete types are supported; got " + type);
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
	public static ModbusMessage decodeRequestPayload(final int unitId, byte functionCode,
			final int address, final int count, final ByteBuf in) {
		ModbusFunctionCode function = ModbusFunctionCode.forCode(functionCode);
		ModbusErrorCode error = ModbusMessageUtils.decodeErrorCode(functionCode, in);
		if ( error != null ) {
			return new BaseModbusMessage(unitId, function, error);
		}
		int addr = address;
		int cnt = count;
		BigInteger data = null;
		if ( error == null ) {
			switch (function) {
				case ReadCoils:
				case ReadDiscreteInputs:
					addr = in.readUnsignedShort();
					cnt = in.readUnsignedShort();
					break;

				case WriteCoil: {
					addr = in.readUnsignedShort();
					cnt = 1;
					int val = in.readUnsignedShort();
					data = (val == 0xFF00 ? BigInteger.ONE : BigInteger.ZERO);
				}
					break;

				case WriteCoils: {
					addr = in.readUnsignedShort();
					cnt = in.readUnsignedShort();
					byte[] byteData = new byte[in.readUnsignedByte()];
					in.readBytes(byteData);
					ModbusByteUtils.reverse(byteData);
					data = new BigInteger(byteData);
				}
					break;

				default:
					return null;
			}
		}
		return new BitsModbusMessage(unitId, function, error, addr, cnt, data);
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
		ModbusFunctionCode function = ModbusFunctionCode.forCode(functionCode);
		ModbusErrorCode error = ModbusMessageUtils.decodeErrorCode(functionCode, in);
		if ( error != null ) {
			return new BaseModbusMessage(unitId, function, error);
		}
		int addr = address;
		int cnt = count;
		BigInteger data = null;
		if ( error == null ) {
			switch (function) {
				case ReadCoils:
				case ReadDiscreteInputs: {
					byte[] byteData = new byte[in.readUnsignedByte()];
					in.readBytes(byteData);
					ModbusByteUtils.reverse(byteData);
					data = new BigInteger(byteData);
				}
					break;

				case WriteCoil: {
					addr = in.readUnsignedShort();
					cnt = 1;
					int val = in.readUnsignedShort();
					data = (val == 0xFF00 ? BigInteger.ONE : BigInteger.ZERO);
				}
					break;

				case WriteCoils:
					addr = in.readUnsignedShort();
					cnt = in.readUnsignedShort();
					break;

				default:
					return null;
			}
		}
		return new BitsModbusMessage(unitId, function, error, addr, cnt, data);
	}

	@Override
	public BigInteger getBits() {
		return bits;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BitsModbusMessage{");
		builder.append("unitId=");
		builder.append(getUnitId());
		if ( getFunction() != null ) {
			builder.append(", function=");
			builder.append(getFunction());
		}
		if ( getError() != null ) {
			builder.append(", error=");
			builder.append(getError());
		}
		builder.append(", address=");
		builder.append(getAddress());
		builder.append(", count=");
		builder.append(getCount());
		if ( bits != null ) {
			builder.append(", bits=");
			builder.append(bits.toString(2));
			builder.append(", ");
		}
		builder.append("}");
		return builder.toString();
	}

	private static int byteCount(int count) {
		return ((count / 8) + (count % 8 != 0 ? 1 : 0));
	}

	@Override
	public int payloadLength() {
		switch (getFunction()) {
			case ReadCoils:
			case ReadDiscreteInputs:
				return (bits == null ? 5 : 2 + byteCount(getCount()));

			case WriteCoil:
				return 5;

			case WriteCoils:
				return (bits != null ? 6 + byteCount(getCount()) : 5);

			default:
				return super.payloadLength();

		}
	}

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		final ModbusFunctionCode fn = getFunction();
		final int count = getCount();
		final int byteCount = byteCount(count);
		byte[] header = null;
		switch (fn) {
			case ReadCoils:
			case ReadDiscreteInputs:
				if ( bits == null ) {
					header = new byte[5];
					header[0] = fn.getCode();
					encode16(header, 1, getAddress());
					encode16(header, 3, count);
				} else {
					header = new byte[2];
					header[0] = fn.getCode();
					header[1] = (byte) byteCount;
				}
				break;

			case WriteCoil:
				header = new byte[3];
				header[0] = fn.getCode();
				encode16(header, 1, getAddress());
				break;

			case WriteCoils:
				if ( bits != null ) {
					header = new byte[6];
					header[0] = fn.getCode();
					encode16(header, 1, getAddress());
					encode16(header, 3, count);
					header[5] = (byte) byteCount;
				} else {
					header = new byte[5];
					header[0] = fn.getCode();
					encode16(header, 1, getAddress());
					encode16(header, 3, count);
				}
				break;

			default:
				super.encodeModbusPayload(out);
		}
		if ( header != null ) {
			out.writeBytes(header);
		}
		if ( bits != null ) {
			byte[] data;
			if ( fn == ModbusFunctionCode.WriteCoil ) {
				if ( bits.testBit(0) ) {
					data = new byte[] { (byte) 0xFF, (byte) 0x00 };
				} else {
					data = new byte[] { 0x00, 0x00 };
				}
			} else {
				data = bits.toByteArray();
				if ( data.length < byteCount ) {
					byte[] tmp = new byte[byteCount];
					System.arraycopy(data, 0, tmp, tmp.length - data.length, data.length);
					data = tmp;
				} else if ( data.length > byteCount ) {
					// truncate... should this be raised as an exception?
					byte[] tmp = new byte[byteCount];
					System.arraycopy(data, data.length - byteCount, tmp, 0, byteCount);
					data = tmp;
				}
				ModbusByteUtils.reverse(data);
			}
			out.writeBytes(data);
		}
	}

}
