/* ==================================================================
 * ReadWriteRegistersModbusMessageTests.java - 6/12/2022 8:22:09 am
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

package net.solarnetwork.io.modbus.netty.msg.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.UserModbusFunction;
import net.solarnetwork.io.modbus.netty.msg.ReadWriteRegistersModbusMessage;

/**
 * Test cases for the {@link ReadWriteRegistersModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class ReadWriteRegistersModbusMessageTests {

	@Test
	public void construct_primitive() {
		// GIVEN
		final int unitId = 1;
		final byte fn = ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS;
		final int addr = 2;
		final int count = 3;

		// WHEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(unitId, fn, addr,
				count);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("No error decoded", msg.getError(), is(nullValue()));
		assertThat("Function decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("Unit preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Address preserved", msg.getAddress(), is(equalTo(addr)));
		assertThat("Count preserved", msg.getCount(), is(equalTo(count)));
		assertThat("No data", msg.dataCopy(), is(nullValue()));
	}

	@Test
	public void construct_primitive_data() {
		// GIVEN
		final int unitId = 1;
		final byte fn = ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS;
		final int addr = 2;
		final int count = 3;
		final byte[] data = new byte[] { 1, 2 };

		// WHEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(unitId, fn, addr,
				count, data);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("No error decoded", msg.getError(), is(nullValue()));
		assertThat("Function decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("Unit preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Address preserved", msg.getAddress(), is(equalTo(addr)));
		assertThat("Count preserved", msg.getCount(), is(equalTo(count)));
	}

	@Test
	public void construct_error_primitive() {
		// GIVEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(1,
				ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS, ModbusErrorCodes.ILLEGAL_FUNCTION, 0,
				0, null);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("Error code decoded", msg.getError(), is(equalTo(ModbusErrorCode.IllegalFunction)));
	}

	@Test
	public void construct_null() {
		// GIVEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(1,
				ModbusFunctionCode.ReadWriteHoldingRegisters, null, 0, 0, null);

		// THEN
		assertThat("Error code null", msg.getError(), is(nullValue()));
	}

	@Test
	public void readWriteHoldingsRequest_count_zero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsRequest(1, 2, 0, 0, null);
		}, "Count less than 1 throws IllegalArgumentException");
	}

	@Test
	public void readWriteHoldingsRequest_count_overshoot() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsRequest(1, 2,
					ReadWriteRegistersModbusMessage.MAX_READ_REGISTERS_COUNT + 1, 0, null);
		}, "Count overshoot throws IllegalArgumentException");
	}

	@Test
	public void readWriteHoldingsRequest_data_overshoot() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsRequest(1, 2, 1, 0,
					new short[ReadWriteRegistersModbusMessage.MAX_WRITE_REGISTERS_COUNT + 1]);
		}, "Write count overshoot throws IllegalArgumentException");
	}

	@Test
	public void readWriteHoldingsRequest_data_null() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsRequest(1, 2, 1, 0, null);
		}, "Write null throws IllegalArgumentException");
	}

	@Test
	public void readWriteHoldingsRequest_data_empty() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsRequest(1, 2, 1, 0, new short[0]);
		}, "Write empty throws IllegalArgumentException");
	}

	@Test
	public void readWriteHoldingsResponse_data_null() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsResponse(1, 2, null);
		}, "Write null throws IllegalArgumentException");
	}

	@Test
	public void readWriteHoldingsResponse_data_empty() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsResponse(1, 2, new short[0]);
		}, "Write empty throws IllegalArgumentException");
	}

	@Test
	public void readWriteHoldingsResponse_count_overshoot() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ReadWriteRegistersModbusMessage.readWriteHoldingsResponse(1, 2,
					new short[ReadWriteRegistersModbusMessage.MAX_READ_REGISTERS_COUNT + 1]);
		}, "Count overshoot throws IllegalArgumentException");
	}

	@Test
	public void decodeRequestPayload_error() {
		// GIVEN
		// @formatter:off
		final byte fn = ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS + ModbusFunctionCodes.ERROR_OFFSET;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ReadWriteRegistersModbusMessage.decodeRequestPayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("Is an exception", msg.isException(), is(equalTo(true)));
		assertThat("Error decoded", msg.getError(), is(equalTo(ModbusErrorCode.IllegalDataAddress)));
	}

	@Test
	public void decodeRequestPayload_unsupported() {
		// GIVEN
		// @formatter:off
		final byte fn = ModbusFunctionCodes.READ_COILS;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				(byte)0x00,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ReadWriteRegistersModbusMessage.decodeRequestPayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message not decoded", msg, is(nullValue()));
	}

	@Test
	public void decodeRequestPayload_user() {
		// GIVEN
		// @formatter:off
		final byte fn = (byte)0x56;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				(byte)0x00,
				(byte)0x01
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ReadWriteRegistersModbusMessage.decodeRequestPayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(new UserModbusFunction(fn))));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("Error not present", msg.getError(), is(nullValue()));
	}

	@Test
	public void decodeResponsePayload_error() {
		// GIVEN
		// @formatter:off
		final byte fn = ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS + ModbusFunctionCodes.ERROR_OFFSET;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ReadWriteRegistersModbusMessage.decodeResponsePayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("Is an exception", msg.isException(), is(equalTo(true)));
		assertThat("Error decoded", msg.getError(), is(equalTo(ModbusErrorCode.IllegalDataAddress)));
	}

	@Test
	public void decodeResponesPayload_unsupported() {
		// GIVEN
		// @formatter:off
		final byte fn = ModbusFunctionCodes.READ_COILS;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				(byte)0x00,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ReadWriteRegistersModbusMessage.decodeResponsePayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message not decoded", msg, is(nullValue()));
	}

	@Test
	public void decodeResponsePayload_user() {
		// GIVEN
		// @formatter:off
		final byte fn = (byte)0x56;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				(byte)0x00,
				(byte)0x01
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ReadWriteRegistersModbusMessage.decodeResponsePayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message decoded", msg, is(instanceOf(ReadWriteRegistersModbusMessage.class)));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(new UserModbusFunction(fn))));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("Error not present", msg.getError(), is(nullValue()));

		ReadWriteRegistersModbusMessage m = (ReadWriteRegistersModbusMessage) msg;
		assertThat("Payload length", m.payloadLength(), is(equalTo(1)));
	}

	@Test
	public void payloadLength_unsupportedFunction() {
		// WHEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(1,
				ModbusFunctionCode.ReadCoils, null, 0, 1, new byte[] { 1, 2 });

		// THEN
		assertThat("Payload length is fn + data", msg.payloadLength(), is(equalTo(3)));
	}

	@Test
	public void payloadLength_nullData() {
		// GIVEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(1,
				ModbusFunctionCode.ReadWriteHoldingRegisters, null, 0, 1, null);

		// THEN
		assertThat("Payload length is 1", msg.payloadLength(), is(equalTo(1)));
	}

	@Test
	public void payloadLength_emptyData() {
		// GIVEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(1,
				ModbusFunctionCode.ReadWriteHoldingRegisters, null, 0, 1, new byte[0]);

		// THEN
		assertThat("Payload length is 1", msg.payloadLength(), is(equalTo(1)));
	}

	@Test
	public void payloadLength_almostResponse() {
		// GIVEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(1,
				ModbusFunctionCode.ReadWriteHoldingRegisters, null, 0, 1, new byte[] { (byte) 0xFF, 2 });

		// THEN
		assertThat("Payload length is 8 + data", msg.payloadLength(), is(equalTo(10)));
	}

	@Test
	public void payloadLength_almostResponse2() {
		// GIVEN
		ReadWriteRegistersModbusMessage msg = new ReadWriteRegistersModbusMessage(1,
				ModbusFunctionCode.ReadWriteHoldingRegisters, null, 0, 1, new byte[] { 1, (byte) 0xFF });

		// THEN
		assertThat("Payload length is 8 + data", msg.payloadLength(), is(equalTo(10)));
	}

}
