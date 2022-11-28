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
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * Utilities for encoding/decoding Modbus messages.
 *
 * @author matt
 * @version 1.0
 */
public final class ModbusMessageUtils {

	/**
	 * Encode a Modbus message.
	 * 
	 * @param message
	 *        the message to encode
	 * @param out
	 *        the buffer to encode the message into
	 */
	public static void encodePayload(final ModbusMessage message, ByteBuf out) {
		if ( message == null ) {
			return;
		} else if ( !(message instanceof ModbusPayloadEncoder) ) {
			throw new UnsupportedOperationException(
					"Only messages that implement ModbusPayloadEncoder are supported; got " + message);
		}
		((ModbusPayloadEncoder) message).encodeModbusPayload(out);
	}

	/**
	 * Decode a full Modbus request message.
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
		ModbusFunctionCode function = ModbusFunctionCode.forCode(fn);
		switch (function) {
			case ReadCoils:
			case ReadDiscreteInputs:
			case WriteCoil:
			case WriteCoils:
				return BitsModbusMessage.decodeRequestPayload(unitId, fn, address, count, in);

			case ReadInputRegisters:
			case ReadHoldingRegisters:
			case ReadWriteHoldingRegisters:
			case WriteHoldingRegister:
			case WriteHoldingRegisters:
			case ReadFifoQueue:
				return RegistersModbusMessage.decodeRequestPayload(unitId, fn, address, count, in);

			case MaskWriteHoldingRegister:
				return MaskWriteRegisterMessage.decodeRequestPayload(unitId, fn, address, count, in);

			case GetCommEventCounter:
			case GetCommEventLog:
			case ReadFileRecord:
			case WriteFileRecord:
			case ReadExceptionStatus:
			case Diagnostic:
			case ReportServerId:
			case EncapsulatedInterfaceTransport:
				throw new UnsupportedOperationException(
						"Modbus function [" + function + "] not supported.");

		}
		return null;
	}

	/**
	 * Decode a full Modbus response message.
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
		ModbusFunctionCode function = ModbusFunctionCode.forCode(fn);
		switch (function) {
			case ReadCoils:
			case ReadDiscreteInputs:
			case WriteCoil:
			case WriteCoils:
				return BitsModbusMessage.decodeResponsePayload(unitId, fn, address, count, in);

			case ReadInputRegisters:
			case ReadHoldingRegisters:
			case ReadWriteHoldingRegisters:
			case WriteHoldingRegister:
			case WriteHoldingRegisters:
			case ReadFifoQueue:
				return RegistersModbusMessage.decodeResponsePayload(unitId, fn, address, count, in);

			case MaskWriteHoldingRegister:
				return MaskWriteRegisterMessage.decodeResponsePayload(unitId, fn, address, count, in);

			case GetCommEventCounter:
			case GetCommEventLog:
			case ReadFileRecord:
			case WriteFileRecord:
			case ReadExceptionStatus:
			case Diagnostic:
			case ReportServerId:
			case EncapsulatedInterfaceTransport:
				throw new UnsupportedOperationException(
						"Modbus function [" + function + "] not supported.");

		}
		return null;

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
	public static ModbusErrorCode decodeErrorCode(final byte functionCode, final ByteBuf in) {
		ModbusErrorCode error = null;
		if ( functionCode < 0 ) {
			// error
			byte err = in.readByte();
			error = ModbusErrorCode.forCode(err);
		}
		return error;
	}

}
