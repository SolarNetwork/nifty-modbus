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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigInteger;
import java.util.BitSet;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.UserModbusFunction;
import net.solarnetwork.io.modbus.netty.msg.BitsModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;

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
	public void construct_error_primitive() {
		// GIVEN
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCodes.READ_COILS,
				ModbusErrorCodes.ILLEGAL_FUNCTION, 0, 0, null);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Error code decoded", msg.getError(), is(equalTo(ModbusErrorCode.IllegalFunction)));
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
	public void encode_readCoils_response_8bit() {
		// GIVEN
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCode.ReadCoils, null, 2, 8,
				new BigInteger("FF", 16));
		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_COILS,
						(byte)0x01,
						(byte)0xFF,
				})));
		// @formatter:on
		assertThat("Payload length", msg.payloadLength(), is(equalTo(3)));
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

	@Test
	public void encode_readBitsRequest_coil() {
		BitsModbusMessage msg = BitsModbusMessage.readBitsRequest(ModbusBlockType.Coil, 1, 107, 3);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_COILS,
						(byte)0x00,
						(byte)0x6B,
						(byte)0x00,
						(byte)0x03,
				})));
		// @formatter:on
	}

	@Test
	public void encode_readBitsRequest_discrete() {
		BitsModbusMessage msg = BitsModbusMessage.readBitsRequest(ModbusBlockType.Discrete, 1, 8, 1);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_DISCRETE_INPUTS,
						(byte)0x00,
						(byte)0x08,
						(byte)0x00,
						(byte)0x01,
				})));
		// @formatter:on
	}

	@Test
	public void encode_readBitsRequest_unsupported() {
		assertThrows(IllegalArgumentException.class, () -> {
			BitsModbusMessage.readBitsRequest(ModbusBlockType.Holding, 1, 8, 1);
		}, "Unsupported block type throws IllegalArgumentException");
	}

	@Test
	public void encode_userFunction() {
		BitsModbusMessage msg = new BitsModbusMessage(1, new UserModbusFunction((byte) 0x56), null, 2, 0,
				null);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)0x56,
				})));
		// @formatter:on
		assertThat("Payload length", msg.payloadLength(), is(equalTo(1)));
	}

	@Test
	public void encode_userFunction_data() {
		BitsModbusMessage msg = new BitsModbusMessage(1, new UserModbusFunction((byte) 0x56), null, 2, 3,
				new BigInteger("FF", 16));

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)0x56,
						(byte)0xFF,
				})));
		// @formatter:on
		assertThat("Payload length", msg.payloadLength(), is(equalTo(2)));
	}

	@Test
	public void encode_unsupportedFunction() {
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCode.ReadHoldingRegisters, null,
				2, 3, BigInteger.ONE);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)0x01,
				})));
		// @formatter:on
	}

	@Test
	public void decode_request_error() {
		// GIVEN
		byte fn = ModbusFunctionCodes.READ_COILS + ModbusFunctionCodes.ERROR_OFFSET;
		// @formatter:off
		ByteBuf buf =Unpooled.wrappedBuffer(new byte[] {
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
		});
		// @formatter:on

		// WHEN
		ModbusMessage msg = BitsModbusMessage.decodeRequestPayload(1, fn, 2, 3, buf);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Error code decoded", msg.getError(),
				is(equalTo(ModbusErrorCode.IllegalDataAddress)));
		assertThat("Message is exception", msg.isException(), is(equalTo(true)));
	}

	@Test
	public void decode_request_unsupported() {
		// GIVEN
		byte fn = ModbusFunctionCodes.READ_HOLDING_REGISTERS;
		// @formatter:off
		ByteBuf buf =Unpooled.wrappedBuffer(new byte[] {
				0x00,
		});
		// @formatter:on

		// WHEN
		ModbusMessage msg = BitsModbusMessage.decodeRequestPayload(1, fn, 2, 3, buf);

		// THEN
		assertThat("Non-bits function not decoded", msg, is(nullValue()));
	}

	@Test
	public void decode_request_unknown() {
		// GIVEN
		// @formatter:off
		ByteBuf buf =Unpooled.wrappedBuffer(new byte[] {
				0x00,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final byte fn = (byte) 0x56;
		final int addr = 2;
		final int count = 3;
		ModbusMessage msg = BitsModbusMessage.decodeRequestPayload(unitId, fn, addr, count, buf);

		// THEN
		assertThat("User-defined function message decoded", msg,
				is(instanceOf(BitsModbusMessage.class)));
		assertThat("Function code decoded", msg.getFunction(), is(equalTo(new UserModbusFunction(fn))));
		assertThat("Unit preserved", msg.getUnitId(), is(equalTo(unitId)));
		BitsModbusMessage regs = (BitsModbusMessage) msg;
		assertThat("Address preserved", regs.getAddress(), is(equalTo(addr)));
		assertThat("Count preserved", regs.getCount(), is(equalTo(count)));
	}

	@Test
	public void decode_response_error() {
		// GIVEN
		byte fn = ModbusFunctionCodes.READ_COILS + ModbusFunctionCodes.ERROR_OFFSET;
		// @formatter:off
		ByteBuf buf =Unpooled.wrappedBuffer(new byte[] {
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
		});
		// @formatter:on

		// WHEN
		ModbusMessage msg = BitsModbusMessage.decodeResponsePayload(1, fn, 2, 3, buf);

		// THEN
		assertThat("Function code decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Error code decoded", msg.getError(),
				is(equalTo(ModbusErrorCode.IllegalDataAddress)));
		assertThat("Message is exception", msg.isException(), is(equalTo(true)));
	}

	@Test
	public void decode_response_unsupported() {
		// GIVEN
		byte fn = ModbusFunctionCodes.READ_HOLDING_REGISTERS;
		// @formatter:off
		ByteBuf buf =Unpooled.wrappedBuffer(new byte[] {
				0x00,
		});
		// @formatter:on

		// WHEN
		ModbusMessage msg = BitsModbusMessage.decodeResponsePayload(1, fn, 2, 3, buf);

		// THEN
		assertThat("Non-bits function not decoded", msg, is(nullValue()));
	}

	@Test
	public void decode_response_unknown() {
		// GIVEN
		// @formatter:off
		ByteBuf buf =Unpooled.wrappedBuffer(new byte[] {
				0x00,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final byte fn = (byte) 0x56;
		final int addr = 2;
		final int count = 3;
		ModbusMessage msg = BitsModbusMessage.decodeResponsePayload(unitId, fn, addr, count, buf);

		// THEN
		assertThat("User-defined function message decoded", msg,
				is(instanceOf(BitsModbusMessage.class)));
		assertThat("Function code decoded", msg.getFunction(), is(equalTo(new UserModbusFunction(fn))));
		assertThat("Unit preserved", msg.getUnitId(), is(equalTo(unitId)));
		BitsModbusMessage regs = (BitsModbusMessage) msg;
		assertThat("Address preserved", regs.getAddress(), is(equalTo(addr)));
		assertThat("Count preserved", regs.getCount(), is(equalTo(count)));
	}

	@Test
	public void isSameAs() {
		// GIVEN
		BitsModbusMessage msg1 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 2,
				3, BigInteger.ONE);
		BitsModbusMessage msg2 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 2,
				3, BigInteger.ONE);

		// THEN
		assertThat("Sameness is based on properties", msg1.isSameAs(msg2), is(equalTo(true)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void isNotSameAs() {
		// GIVEN
		BitsModbusMessage msg1 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 2,
				3, BigInteger.ONE);
		BitsModbusMessage msg2 = new BitsModbusMessage(2, ModbusFunctionCode.ReadInputRegisters, null, 2,
				3, BigInteger.ONE);
		BitsModbusMessage msg3 = new BitsModbusMessage(1, ModbusFunctionCode.ReadHoldingRegisters, null,
				2, 3, BigInteger.ONE);
		BitsModbusMessage msg4 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters,
				ModbusErrorCode.IllegalFunction, 2, 3, BigInteger.ONE);
		BitsModbusMessage msg5 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 3,
				3, BigInteger.ONE);
		BitsModbusMessage msg6 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 2,
				4, BigInteger.ONE);
		BitsModbusMessage msg7 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 2,
				3, BigInteger.ZERO);

		// THEN
		assertThat("Difference is based on properties", msg1.isSameAs(msg2), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));

		assertThat("Difference is based on properties", msg1.isSameAs(msg3), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg3))));

		assertThat("Difference is based on properties", msg1.isSameAs(msg4), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg4))));

		assertThat("Difference is based on properties", msg1.isSameAs(msg5), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg5))));

		assertThat("Difference is based on properties", msg1.isSameAs(msg6), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg6))));

		assertThat("Difference is based on properties", msg1.isSameAs(msg7), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg7))));
	}

	@Test
	public void isNotSameAs_otherClass() {
		// GIVEN
		BitsModbusMessage msg1 = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 2,
				3, BigInteger.ZERO);
		RegistersModbusMessage msg2 = new RegistersModbusMessage(1,
				ModbusFunctionCode.ReadInputRegisters, null, 2, 3, new byte[] { 1, 2 });

		// THEN
		assertThat("Difference is based on properties", msg1.isSameAs(msg2), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void stringValue() {
		// GIVEN
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null, 2,
				3, BigInteger.ONE);

		// THEN
		assertThat("String value", msg.toString(), matchesRegex("BitsModbusMessage\\{.*\\}"));
	}

	@Test
	public void stringValue_error() {
		// GIVEN
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCode.ReadInputRegisters,
				ModbusErrorCode.IllegalDataAddress, 2, 3, null);

		// THEN
		assertThat("String value", msg.toString(), matchesRegex("BitsModbusMessage\\{.*\\}"));
	}

	@Test
	public void payloadLength_userFunction() {
		// GIVEN
		BitsModbusMessage msg = new BitsModbusMessage(1, new UserModbusFunction((byte) 0x56), null, 2, 3,
				BigInteger.ONE);

		// THEN
		assertThat("Payload length is fn + data", msg.payloadLength(), is(equalTo(2)));
	}

	@Test
	public void payloadLength_unsupportedFunction() {
		BitsModbusMessage msg = new BitsModbusMessage(1, ModbusFunctionCode.ReadHoldingRegisters, null,
				2, 3, BigInteger.ONE);

		// THEN
		assertThat("Payload length is fn + data", msg.payloadLength(), is(equalTo(2)));
	}
}
