/* ==================================================================
 * ModbusMessageUtils.java - 27/11/2022 11:05:32 am
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
 * Utilities for encoding/decoding Modbus messages.
 *
 * @author matt
 * @version 1.0
 */
public final class ModbusMessageUtils {

	private ModbusMessageUtils() {
		// not allowed
	}

	/**
	 * Encode a Modbus message.
	 * 
	 * @param message
	 *        the message to encode
	 * @param out
	 *        the buffer to encode the message into
	 * @throws IllegalArgumentException
	 *         if message is not {@literal null} but does not implement
	 *         {@link ModbusPayloadEncoder}
	 */
	public static void encodePayload(final ModbusMessage message, ByteBuf out) {
		if ( message == null ) {
			return;
		} else if ( !(message instanceof ModbusPayloadEncoder) ) {
			throw new IllegalArgumentException(
					"Only messages that implement ModbusPayloadEncoder are supported; got " + message);
		}
		((ModbusPayloadEncoder) message).encodeModbusPayload(out);
	}

	/**
	 * Determine the expected payload length for Modbus request message.
	 * 
	 * <p>
	 * The input is expected to be positioned at the Modbus function code byte.
	 * </p>
	 * 
	 * @param in
	 *        the input; the reader index is not changed by this method
	 * @return the expected message payload length, or -1 if not known
	 */
	public static int discoverRequestPayloadLength(final ByteBuf in) {
		if ( in.readableBytes() < 1 ) {
			return -1;
		}
		final int idx = in.readerIndex();
		final byte fn = in.getByte(idx);
		ModbusFunction func = ModbusFunctionCode.valueOf(fn);
		ModbusFunctionCode function = func.functionCode();
		if ( function == null ) {
			// user function, don't know
			return -1;
		}
		if ( fn < 0 ) {
			// an error message
			return 2;
		}
		switch (function) {
			case ReadCoils:
			case ReadDiscreteInputs:
			case ReadInputRegisters:
			case ReadHoldingRegisters:
			case WriteCoil:
			case WriteHoldingRegister:
				// fn, addr, count/value
				return 5;

			case ReadExceptionStatus:
				return 1;

			case Diagnostics: {
				if ( in.readableBytes() < 2 ) {
					return -1;
				}
				byte subFn = in.getByte(idx + 1);
				if ( subFn == (byte) 0 ) {
					// no pre-determined length! this is a cop-out to just return whatever is available
					return in.readableBytes();
				}

				// all published message have single 16-bit value
				return 5;
			}

			case GetCommEventCounter:
			case GetCommEventLog:
			case ReportServerId:
				return 1;

			case WriteCoils:
			case WriteHoldingRegisters:
				// 6th byte is byte count
				if ( in.readableBytes() < 6 ) {
					return -1;
				}
				return Byte.toUnsignedInt(in.getByte(idx + 5)) + 6;

			case ReadFileRecord:
			case WriteFileRecord:
				// 2nd byte is byte count
				if ( in.readableBytes() < 2 ) {
					return -1;
				}
				return Byte.toUnsignedInt(in.getByte(idx + 1)) + 2;

			case MaskWriteHoldingRegister:
				// fn, addr, and, or
				return 7;

			case ReadWriteHoldingRegisters:
				// 10th byte is byte count
				if ( in.readableBytes() < 10 ) {
					return -1;
				}
				return Byte.toUnsignedInt(in.getByte(idx + 9)) + 10;

			case ReadFifoQueue:
				// fn, addr
				return 3;

			case EncapsulatedInterfaceTransport:
				// fall though

		}
		return -1;
	}

	/**
	 * Determine the expected payload length for Modbus response message.
	 * 
	 * <p>
	 * The input is expected to be positioned at the Modbus function code byte.
	 * </p>
	 * 
	 * @param in
	 *        the input; the reader index is not changed by this method
	 * @return the expected message payload length, or -1 if not known
	 */
	public static int discoverResponsePayloadLength(final ByteBuf in) {
		final int idx = in.readerIndex();
		if ( in.readableBytes() < 1 ) {
			return -1;
		}
		final byte fn = in.getByte(idx);
		ModbusFunction func = ModbusFunctionCode.valueOf(fn);
		ModbusFunctionCode function = func.functionCode();
		if ( function == null ) {
			// user function, don't know
			return -1;
		}
		if ( fn < 0 ) {
			// an error message
			return 2;
		}
		switch (function) {
			case ReadCoils:
			case ReadDiscreteInputs:
			case ReadInputRegisters:
			case ReadHoldingRegisters:
			case GetCommEventLog:
			case ReadFileRecord:
			case WriteFileRecord:
			case ReadWriteHoldingRegisters:
			case ReadFifoQueue:
				// 2nd byte is byte count
				if ( in.readableBytes() < 2 ) {
					return -1;
				}
				return Byte.toUnsignedInt(in.getByte(idx + 1)) + 2;

			case WriteCoil:
			case WriteHoldingRegister:
				// fn, addr, value
				return 5;

			case ReadExceptionStatus:
				return 2;

			case Diagnostics: {
				if ( in.readableBytes() < 2 ) {
					return -1;
				}
				byte subFn = in.getByte(idx + 1);
				if ( subFn == (byte) 0 ) {
					// no pre-determined length! this is a cop-out to just return whatever is available
					return in.readableBytes();
				}
				// all published message have single 16-bit value
				return 5;
			}

			case GetCommEventCounter:
				// fn, status, count
				return 5;

			case WriteCoils:
			case WriteHoldingRegisters:
				return 5;

			case ReportServerId:
				// no pre-determined length! this is a cop-out to just return whatever is available
				return in.readableBytes();

			case MaskWriteHoldingRegister:
				// fn, addr, and, or
				return 7;

			case EncapsulatedInterfaceTransport:
				// fall through
				break;

		}
		return -1;
	}

	/**
	 * Decode a full Modbus request message.
	 * 
	 * @param in
	 *        the input
	 * @return the message, or {@literal null} if a message cannot be decoded
	 */
	public static ModbusMessage decodeRequestPayload(final ByteBuf in) {
		return decodeRequestPayload(0, 0, 0, in);
	}

	/**
	 * Decode a full Modbus request message with specific attributes.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the address, or {@code 0} for no address
	 * @param count
	 *        the count, or {@code 0} for no count
	 * @param in
	 *        the input
	 * @return the message, or {@literal null} if a message cannot be decoded
	 */
	public static ModbusMessage decodeRequestPayload(final int unitId, final int address,
			final int count, final ByteBuf in) {
		final byte fn = in.readByte();
		ModbusFunction func = ModbusFunctionCode.valueOf(fn);
		ModbusFunctionCode function = func.functionCode();
		ModbusError error = decodeError(fn, in);
		if ( error != null || function == null ) {
			return new BaseModbusMessage(unitId, func, error);
		}
		switch (function) {
			case ReadCoils:
			case ReadDiscreteInputs:
			case WriteCoil:
			case WriteCoils:
				return BitsModbusMessage.decodeRequestPayload(unitId, fn, address, count, in);

			case ReadInputRegisters:
			case ReadHoldingRegisters:
			case WriteHoldingRegister:
			case WriteHoldingRegisters:
			case ReadFifoQueue:
				return RegistersModbusMessage.decodeRequestPayload(unitId, fn, address, count, in);

			case MaskWriteHoldingRegister:
				return MaskWriteRegisterModbusMessage.decodeRequestPayload(unitId, fn, address, count,
						in);

			case ReadWriteHoldingRegisters:
				return ReadWriteRegistersModbusMessage.decodeRequestPayload(unitId, fn, address, count,
						in);

			case GetCommEventCounter:
			case GetCommEventLog:
			case ReadFileRecord:
			case WriteFileRecord:
			case ReadExceptionStatus:
			case Diagnostics:
			case ReportServerId:
			case EncapsulatedInterfaceTransport:
				// fall though
				break;

		}
		throw new UnsupportedOperationException(
				"Modbus function [" + Byte.toUnsignedInt(fn) + "] not supported.");
	}

	/**
	 * Decode a full Modbus response message.
	 * 
	 * @param in
	 *        the input
	 * @return the message, or {@literal null} if a message cannot be decoded
	 */
	public static ModbusMessage decodeResponsePayload(final ByteBuf in) {
		return decodeResponsePayload(0, 0, 0, in);
	}

	/**
	 * Decode a full Modbus response message with specific attributes.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param address
	 *        the address, or {@code 0} for no address
	 * @param count
	 *        the count, or {@code 0} for no count
	 * @param in
	 *        the input
	 * @return the message, or {@literal null} if a message cannot be decoded
	 */
	public static ModbusMessage decodeResponsePayload(final int unitId, final int address,
			final int count, final ByteBuf in) {
		final byte fn = in.readByte();
		ModbusFunction func = ModbusFunctionCode.valueOf(fn);
		ModbusFunctionCode function = func.functionCode();
		ModbusError error = decodeError(fn, in);
		if ( error != null || function == null ) {
			return new BaseModbusMessage(unitId, func, error);
		}
		switch (function) {
			case ReadCoils:
			case ReadDiscreteInputs:
			case WriteCoil:
			case WriteCoils:
				return BitsModbusMessage.decodeResponsePayload(unitId, fn, address, count, in);

			case ReadInputRegisters:
			case ReadHoldingRegisters:
			case WriteHoldingRegister:
			case WriteHoldingRegisters:
			case ReadFifoQueue:
				return RegistersModbusMessage.decodeResponsePayload(unitId, fn, address, count, in);

			case MaskWriteHoldingRegister:
				return MaskWriteRegisterModbusMessage.decodeResponsePayload(unitId, fn, address, count,
						in);

			case ReadWriteHoldingRegisters:
				return ReadWriteRegistersModbusMessage.decodeResponsePayload(unitId, fn, address, count,
						in);

			case GetCommEventCounter:
			case GetCommEventLog:
			case ReadFileRecord:
			case WriteFileRecord:
			case ReadExceptionStatus:
			case Diagnostics:
			case ReportServerId:
			case EncapsulatedInterfaceTransport:
				// fall through
				break;

		}
		throw new UnsupportedOperationException(
				"Modbus function [" + Byte.toUnsignedInt(fn) + "] not supported.");

	}

	/**
	 * Decode an error code from a Modbus message payload.
	 * 
	 * @param functionCode
	 *        the Modbus function code value
	 * @param in
	 *        the input data, starting after the Modbus function code
	 * @return the error code, or {@literal null} if {@code functionCode} is not
	 *         an exception value
	 */
	public static ModbusError decodeError(final byte functionCode, final ByteBuf in) {
		ModbusError error = null;
		if ( functionCode < 0 ) {
			// error
			byte err = in.readByte();
			error = ModbusErrorCode.valueOf(err);
		}
		return error;
	}

}
