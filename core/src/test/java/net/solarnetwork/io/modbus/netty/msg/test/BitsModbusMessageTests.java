/* ==================================================================
 * BitsModbusMessageTests.java - 26/11/2022 11:42:06 am
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.math.BigInteger;
import java.util.BitSet;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.netty.msg.BitsModbusMessage;

/**
 * Test cases for the {@link BitsModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class BitsModbusMessageTests {

	@Test
	public void construct_nullBits() {
		// WHEN
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCodes.READ_COILS, 0, 8, null);

		// THEN
		assertThat("Message created from null bits", msg.getBits(), is(nullValue()));
	}

	@Test
	public void toBitSet() {
		// GIVEN
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCodes.READ_COILS, 0, 8,
				new BigInteger("00101001", 2));

		// WHEN
		BitSet s = msg.toBitSet();

		// THEN
		BitSet expected = new BitSet(8);
		expected.set(0);
		expected.set(3);
		expected.set(5);
		assertThat("Message created from bits", s, is(equalTo(expected)));
	}

	@Test
	public void encode_readCoils_request() {
		BitsModbusMessage msg = BitsModbusMessage.readCoilsRequest(1, 19, 10);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x0A,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_readCoils_response() {
		BitsModbusMessage msg = BitsModbusMessage.readCoilsResponse(1, 0x13, 18,
				new BigInteger("56BCD", 16));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_COILS,
						(byte)0x03,
						(byte)0xCD,
						(byte)0x6B,
						(byte)0x05,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoil_on_request() {
		// GIVEN
		BitsModbusMessage msg = BitsModbusMessage.writeCoilRequest(1, 19, true);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COIL,
						(byte)0x00,
						(byte)0x13,
						(byte)0xFF,
						(byte)0x00,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoil_on_response() {
		// GIVEN
		BitsModbusMessage msg = BitsModbusMessage.writeCoilResponse(1, 19, true);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COIL,
						(byte)0x00,
						(byte)0x13,
						(byte)0xFF,
						(byte)0x00,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoil_off_request() {
		BitsModbusMessage msg = BitsModbusMessage.writeCoilRequest(1, 19, false);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COIL,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x00,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoil_off_response() {
		BitsModbusMessage msg = BitsModbusMessage.writeCoilResponse(1, 19, false);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COIL,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x00,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoils_request() {
		// GIVEN
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsRequest(1, 19, 10,
				new BigInteger("0111001101", 2));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x0A,
						(byte)0x02,
						(byte)0xCD,
						(byte)0x01,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(8)));
	}

	@Test
	public void encode_writeCoils_response() {
		// GIVEN
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsResponse(1, 19, 10);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x0A,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoils_oneCoil_on_request() {
		// GIVEN
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsRequest(1, 19, 1, new BigInteger("1", 2));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x01,
						(byte)0x01,
						(byte)0x01,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(7)));
	}

	@Test
	public void encode_writeCoils_oneCoil_on_response() {
		// GIVEN
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsResponse(1, 19, 1);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x01,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoils_oneCoil_off_request() {
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsRequest(1, 19, 1, new BigInteger("0", 2));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x01,
						(byte)0x01,
						(byte)0x00,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(7)));
	}

	@Test
	public void encode_writeCoils_oneCoil_off_response() {
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsResponse(1, 19, 1);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x01,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_writeCoils_oneCoil_leadingZeros_request() {
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsRequest(1, 19, 10,
				new BigInteger("1101", 2));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x0A,
						(byte)0x02,
						(byte)0x0D,
						(byte)0x00,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(8)));
	}

	@Test
	public void encode_writeCoils_oneCoil_leadingZeros_response() {
		BitsModbusMessage msg = BitsModbusMessage.writeCoilsResponse(1, 19, 10);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.WRITE_COILS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x0A,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_readDiscretes_request() {
		BitsModbusMessage msg = BitsModbusMessage.readDiscretesRequest(1, 19, 10);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_DISCRETE_INPUTS,
						(byte)0x00,
						(byte)0x13,
						(byte)0x00,
						(byte)0x0A,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_readDiscretes_response() {
		BitsModbusMessage msg = BitsModbusMessage.readDiscretesResponse(1, 19, 18,
				new BigInteger("5CB02", 16));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_DISCRETE_INPUTS,
						(byte)0x03,
						(byte)0x02,
						(byte)0xCB,
						(byte)0x05,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

	@Test
	public void encode_readDiscretes_response_excessBits() {
		BitsModbusMessage msg = BitsModbusMessage.readDiscretesResponse(1, 19, 18,
				new BigInteger("FFF15CB02", 16));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_DISCRETE_INPUTS,
						(byte)0x03,
						(byte)0x02,
						(byte)0xCB,
						(byte)0x15,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(5)));
	}

}
