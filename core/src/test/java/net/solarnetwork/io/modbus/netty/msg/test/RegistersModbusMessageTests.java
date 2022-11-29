/* ==================================================================
 * RegistersModbusMessageTests.java - 27/11/2022 6:47:46 am
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

import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_HOLDING_REGISTERS;
import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.netty.msg.ReadWriteRegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;

/**
 * Test cases for the {@link RegistersModbusMessage}.
 *
 * @author matt
 * @version 1.0
 */
public class RegistersModbusMessageTests {

	@Test
	public void construct_odd() {
		assertThrows(IllegalArgumentException.class, () -> {
			new RegistersModbusMessage(1, READ_HOLDING_REGISTERS, 0, 1, new byte[] { 1 });
		}, "Not allowed to constrct with an odd-length data array");

	}

	@Test
	public void dataCopy_empty() {
		// GIVEN
		RegistersModbusMessage msg = new RegistersModbusMessage(1, READ_HOLDING_REGISTERS, 0, 1);

		// WHEN
		byte[] d = msg.dataCopy();

		// THEN
		assertThat("Null data when no data", d, is(nullValue()));
	}

	@Test
	public void dataCopy() {
		// GIVEN
		byte[] orig = new byte[] { 1, 2 };
		RegistersModbusMessage msg = new RegistersModbusMessage(1, READ_HOLDING_REGISTERS, 0, 1, orig);

		// WHEN
		byte[] d = msg.dataCopy();

		// THEN
		assertThat("Copy of data returned", d, not(sameInstance(orig)));
		assertThat("Copy same values as original", Arrays.equals(orig, d), is(equalTo(true)));
	}

	@Test
	public void dataDecode_empty() {
		// GIVEN
		RegistersModbusMessage msg = new RegistersModbusMessage(1, READ_HOLDING_REGISTERS, 0, 1);

		// WHEN
		short[] r = msg.dataDecode();

		// THEN
		assertThat("Null signed data when no data", r, is(nullValue()));
	}

	@Test
	public void dataDecode() {
		// GIVEN
		// @formatter:off
		byte[] data = new byte[] {
				(byte)0xAB,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x12,
				(byte)0x34,
				(byte)0x00,
		};
		// @formatter:on
		RegistersModbusMessage msg = new RegistersModbusMessage(1, READ_HOLDING_REGISTERS, 0, 3, data);

		// WHEN
		short[] r = msg.dataDecode();

		// THEN
		// @formatter:off
		assertThat("Signed data extracted", Arrays.equals(r, new short[] {
				(short)0xABCD,
				(short)0x0012,
				(short)0x3400,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void dataDecodeUnsigned_empty() {
		// GIVEN
		RegistersModbusMessage msg = new RegistersModbusMessage(1, READ_HOLDING_REGISTERS, 0, 1);

		// WHEN
		int[] r = msg.dataDecodeUnsigned();

		// THEN
		assertThat("Null unsigned data when no data", r, is(nullValue()));
	}

	@Test
	public void dataDecodeUnsigned() {
		// GIVEN
		// @formatter:off
		byte[] data = new byte[] {
				(byte)0xAB,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x12,
				(byte)0x34,
				(byte)0x00,
		};
		// @formatter:on
		RegistersModbusMessage msg = new RegistersModbusMessage(1, READ_HOLDING_REGISTERS, 0, 3, data);

		// WHEN
		int[] r = msg.dataDecodeUnsigned();

		// THEN
		// @formatter:off
		assertThat("Unsigned data extracted", Arrays.equals(r, new int[] {
				0xABCD,
				0x0012,
				0x3400,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void encode_readInputs_request() {
		RegistersModbusMessage msg = RegistersModbusMessage.readInputsRequest(1, 8, 1);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_INPUT_REGISTERS,
						(byte)0x00,
						(byte)0x08,
						(byte)0x00,
						(byte)0x01,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_readInputs_response() {
		final short[] values = new short[] { 0x1234, 0x4321 };
		RegistersModbusMessage msg = RegistersModbusMessage.readInputsResponse(1, 8, values);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_INPUT_REGISTERS,
						(byte)0x04,
						(byte)0x12,
						(byte)0x34,
						(byte)0x43,
						(byte)0x21,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(6)));
	}

	@Test
	public void encode_readHoldings_request() {
		RegistersModbusMessage msg = RegistersModbusMessage.readHoldingsRequest(1, 107, 3);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)0x00,
						(byte)0x6B,
						(byte)0x00,
						(byte)0x03,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_readHoldings_response() {
		short[] values = new short[] { 0x1234, 0x4321 };
		RegistersModbusMessage msg = RegistersModbusMessage.readHoldingsResponse(1, 107, values);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)0x04,
						(byte)0x12,
						(byte)0x34,
						(byte)0x43,
						(byte)0x21,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(6)));
	}

	@Test
	public void encode_writeHolding_request() {
		RegistersModbusMessage msg = RegistersModbusMessage.writeHoldingRequest(1, 1, 0x0003);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_HOLDING_REGISTER,
						(byte)0x00,
						(byte)0x01,
						(byte)0x00,
						(byte)0x03,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeHolding_response() {
		RegistersModbusMessage msg = RegistersModbusMessage.writeHoldingResponse(1, 1, 0x0003);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_HOLDING_REGISTER,
						(byte)0x00,
						(byte)0x01,
						(byte)0x00,
						(byte)0x03,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeHoldings_request() {
		final short[] data = new short[] { 0x000A, 0x0102 };
		RegistersModbusMessage msg = RegistersModbusMessage.writeHoldingsRequest(1, 1, data);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_HOLDING_REGISTERS,
						(byte)0x00,
						(byte)0x01,
						(byte)0x00,
						(byte)0x02,
						(byte)0x04,
						(byte)0x00,
						(byte)0x0A,
						(byte)0x01,
						(byte)0x02,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(10)));
	}

	@Test
	public void encode_writeHoldings_response() {
		RegistersModbusMessage msg = RegistersModbusMessage.writeHoldingsResponse(1, 1, 2);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_HOLDING_REGISTERS,
						(byte)0x00,
						(byte)0x01,
						(byte)0x00,
						(byte)0x02,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_readWriteRegisters_request() {
		final short[] values = new short[] { (short) 0x00FF, (short) 0x00FD, (short) 0x00FC };
		ReadWriteRegistersModbusMessage msg = ReadWriteRegistersModbusMessage.readWriteHoldingsRequest(1,
				3, 6, 14, values);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS,
						(byte)0x00,
						(byte)0x03,
						(byte)0x00,
						(byte)0x06,
						(byte)0x00,
						(byte)0x0E,
						(byte)0x00,
						(byte)0x03,
						(byte)0x06,
						(byte)0x00,
						(byte)0xFF,
						(byte)0x00,
						(byte)0xFD,
						(byte)0x00,
						(byte)0xFC,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(16)));
	}

	@Test
	public void encode_readWriteRegisters_response() {
		final short[] values = new short[] { (short) 0x1234, (short) 0x2345, (short) 0x3456 };
		ReadWriteRegistersModbusMessage msg = ReadWriteRegistersModbusMessage
				.readWriteHoldingsResponse(1, 3, values);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS,
						(byte)0x06,
						(byte)0x12,
						(byte)0x34,
						(byte)0x23,
						(byte)0x45,
						(byte)0x34,
						(byte)0x56,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(8)));
	}

	@Test
	public void encode_readFifoQueue_request() {
		RegistersModbusMessage msg = RegistersModbusMessage.readFifoQueueRequest(1, 0x04DE);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_FIFO_QUEUE,
						(byte)0x04,
						(byte)0xDE,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(3)));
	}

	@Test
	public void encode_readFifoQueue_response() {
		final short[] values = new short[] { (short) 0x1234, (short) 0x2345, (short) 0x3456 };
		RegistersModbusMessage msg = RegistersModbusMessage.readFifoQueueResponse(1, 0x04DE, values);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_FIFO_QUEUE,
						(byte)0x00,
						(byte)0x08,
						(byte)0x00,
						(byte)0x03,
						(byte)0x12,
						(byte)0x34,
						(byte)0x23,
						(byte)0x45,
						(byte)0x34,
						(byte)0x56,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(11)));
	}

}
