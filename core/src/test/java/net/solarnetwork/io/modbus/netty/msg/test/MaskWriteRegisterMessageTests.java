/* ==================================================================
 * MaskWriteRegisterMessageTests.java - 28/11/2022 4:39:50 pm
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

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.UserModbusFunction;
import net.solarnetwork.io.modbus.netty.msg.MaskWriteRegisterModbusMessage;

/**
 * Test cases for the {@link MaskWriteRegisterModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class MaskWriteRegisterMessageTests {

	@Test
	public void encode_maskWriteRegisters_request() {
		MaskWriteRegisterModbusMessage msg = MaskWriteRegisterModbusMessage.maskWriteHoldingRequest(1, 4,
				0x00F2, 0x0025);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER,
						(byte)0x00,
						(byte)0x04,
						(byte)0x00,
						(byte)0xF2,
						(byte)0x00,
						(byte)0x25,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(7)));
	}

	@Test
	public void encode_maskWriteRegisters_response() {
		MaskWriteRegisterModbusMessage msg = MaskWriteRegisterModbusMessage.maskWriteHoldingResponse(1,
				0x1A, 0x01F3, 0x0227);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER,
						(byte)0x00,
						(byte)0x1A,
						(byte)0x01,
						(byte)0xF3,
						(byte)0x02,
						(byte)0x27,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(7)));
	}

	@Test
	public void construct_primitive() {
		// GIVEN
		final int unitId = 1;
		final byte fn = ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER;
		final int addr = 2;

		// WHEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(unitId, fn, addr);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat("No error decoded", msg.getError(), is(nullValue()));
		assertThat("Function decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat("Unit preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Address preserved", msg.getAddress(), is(equalTo(addr)));
		assertThat("Count fixed", msg.getCount(), is(equalTo(1)));
		assertThat("No data", msg.dataCopy(), is(nullValue()));
	}

	@Test
	public void construct_primitive_data() {
		// GIVEN
		final int unitId = 1;
		final byte fn = ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER;
		final int addr = 2;
		final byte[] data = new byte[] { 1, 2 };

		// WHEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(unitId, fn, addr, data);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat("No error decoded", msg.getError(), is(nullValue()));
		assertThat("Function decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat("Unit preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Address preserved", msg.getAddress(), is(equalTo(addr)));
		assertThat("Count fixed", msg.getCount(), is(equalTo(1)));
	}

	@Test
	public void construct_error_primitive() {
		// GIVEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(1,
				ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER, ModbusErrorCodes.ILLEGAL_FUNCTION, 0,
				null);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat("Error code decoded", msg.getError(), is(equalTo(ModbusErrorCode.IllegalFunction)));
	}

	@Test
	public void construct_null() {
		// GIVEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(1,
				ModbusFunctionCode.MaskWriteHoldingRegister, null, 0, null);

		// THEN
		assertThat("Error code null", msg.getError(), is(nullValue()));
	}

	@Test
	public void decodeRequestPayload_error() {
		// GIVEN
		// @formatter:off
		final byte fn = ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER + ModbusFunctionCodes.ERROR_OFFSET;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = MaskWriteRegisterModbusMessage.decodeRequestPayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
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
		ModbusMessage msg = MaskWriteRegisterModbusMessage.decodeRequestPayload(unitId, fn, address,
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
		ModbusMessage msg = MaskWriteRegisterModbusMessage.decodeRequestPayload(unitId, fn, address,
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
		final byte fn = ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER + ModbusFunctionCodes.ERROR_OFFSET;
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = MaskWriteRegisterModbusMessage.decodeResponsePayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
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
		ModbusMessage msg = MaskWriteRegisterModbusMessage.decodeResponsePayload(unitId, fn, address,
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
		ModbusMessage msg = MaskWriteRegisterModbusMessage.decodeResponsePayload(unitId, fn, address,
				count, buf);

		// THEN
		assertThat("Message decoded", msg, is(instanceOf(MaskWriteRegisterModbusMessage.class)));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(new UserModbusFunction(fn))));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("Error not present", msg.getError(), is(nullValue()));

		MaskWriteRegisterModbusMessage m = (MaskWriteRegisterModbusMessage) msg;
		assertThat("Payload length", m.payloadLength(), is(equalTo(1)));
	}

	@Test
	public void getMasks() {
		// GIVEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(1,
				ModbusFunctionCode.MaskWriteHoldingRegister, null, 2, new byte[] { 1, 2, 3, 4 });

		// THEN
		assertThat("And mask decoded from data", msg.getAndMask(), is(equalTo(0x0102)));
		assertThat("Or mask decoded frmo data", msg.getOrMask(), is(equalTo(0x0304)));
	}

	@Test
	public void getMasks_data_null() {
		// GIVEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(1,
				ModbusFunctionCode.MaskWriteHoldingRegister, null, 2, null);

		// THEN
		assertThat("And mask forced when null data", msg.getAndMask(), is(equalTo(0)));
		assertThat("Or mask forced when null data", msg.getOrMask(), is(equalTo(0)));
	}

	@Test
	public void getMasks_data_empty() {
		// GIVEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(1,
				ModbusFunctionCode.MaskWriteHoldingRegister, null, 2, new byte[0]);

		// THEN
		assertThat("And mask forced when null data", msg.getAndMask(), is(equalTo(0)));
		assertThat("Or mask forced when null data", msg.getOrMask(), is(equalTo(0)));
	}

	@Test
	public void getMasks_data_undershoot() {
		// GIVEN
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(1,
				ModbusFunctionCode.MaskWriteHoldingRegister, null, 2, new byte[] { 1, 2 });

		// THEN
		assertThat("And mask forced when null data", msg.getAndMask(), is(equalTo(0)));
		assertThat("Or mask forced when null data", msg.getOrMask(), is(equalTo(0)));
	}

	@Test
	public void payloadLength_unsupportedFunction() {
		MaskWriteRegisterModbusMessage msg = new MaskWriteRegisterModbusMessage(1,
				ModbusFunctionCode.ReadCoils, null, 2, new byte[] { 1, 2, 3, 4 });

		// THEN
		assertThat("Payload length is fn + data", msg.payloadLength(), is(equalTo(5)));
	}

}
