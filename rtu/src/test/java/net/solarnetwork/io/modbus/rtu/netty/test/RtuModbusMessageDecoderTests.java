/* ==================================================================
 * RtuModbusMessageDecoderTests.java - 1/12/2022 5:03:05 pm
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

package net.solarnetwork.io.modbus.rtu.netty.test;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import net.solarnetwork.io.modbus.ModbusByteUtils;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusUnsupportedFunctionException;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessageDecoder;
import net.solarnetwork.io.modbus.test.support.ModbusTestUtils;

/**
 * Test cases for the {@link RtuModbusMessageDecoder} class.
 *
 * @author matt
 * @version 1.1
 */
public class RtuModbusMessageDecoderTests {

	private static final Logger log = LoggerFactory.getLogger(RtuModbusMessageDecoderTests.class);

	private EmbeddedChannel responderChannel;
	private EmbeddedChannel controllerChannel;

	@BeforeEach
	public void setup() {
		responderChannel = new EmbeddedChannel(new RtuModbusMessageDecoder(false));
		controllerChannel = new EmbeddedChannel(new RtuModbusMessageDecoder(true));
	}

	@Test
	public void responder_request_readInputs() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				(byte)0x01,
				ModbusFunctionCodes.READ_INPUT_REGISTERS,
				(byte)0x00,
				(byte)0x08,
				(byte)0x00,
				(byte)0x01,
				(byte)0xCC,
				(byte)0xDD,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		boolean result = responderChannel.writeInbound(buf);

		// THEN
		assertThat("Decoder produced", result, is(equalTo(true)));
		RtuModbusMessage msg = responderChannel.readInbound();

		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID decoded", msg.getUnitId(), is(equalTo(1)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		RegistersModbusMessage rmm = msg.unwrap(RegistersModbusMessage.class);
		assertThat("Type is Registers", msg, is(notNullValue()));
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x0008)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(1)));
		assertThat("No data", rmm.dataCopy(), is(nullValue()));
		assertThat("No data (shorts)", rmm.dataDecode(), is(nullValue()));
		assertThat("No data (ints)", rmm.dataDecodeUnsigned(), is(nullValue()));
		assertThat("Provided CRC preserved", msg.getCrc(), is(equalTo((short) 0xDDCC)));
		assertThat("Calculated CRC", msg.computeCrc(), is(equalTo((short) 0x08B0)));
		assertThat("CRC invalid", msg.isCrcValid(), is(equalTo(false)));
	}

	@Test
	public void responder_request_unsupportedFunction() {
		// GIVEN

		// write unsupported function... with trailing bytes that includes start of next message...
		// @formatter:off
		final byte[] data = new byte[] {
				(byte)0x01,
				ModbusFunctionCodes.GET_COMM_EVENT_LOG,
				(byte)0x00,
				(byte)0xCC,
				(byte)0xDD,
				(byte)0x01
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN

		DecoderException result = assertThrows(DecoderException.class, () -> {
			responderChannel.writeInbound(buf);
		}, "DecoderException thrown by unsupported function");

		// THEN
		assertThat("ModbusUnsupportedFunctionException is cause", result.getCause(),
				is(instanceOf(ModbusUnsupportedFunctionException.class)));

		ModbusUnsupportedFunctionException ufe = (ModbusUnsupportedFunctionException) result.getCause();
		assertThat("Unit ID returned on exception", ufe.getUnitId(), is(equalTo((short) 0x01)));
		assertThat("Function code returned on exception", ufe.getCode(),
				is(equalTo(ModbusFunctionCodes.GET_COMM_EVENT_LOG)));
	}

	@Test
	public void controller_request_writeHoldings() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				(byte)0x0B,
				ModbusFunctionCodes.WRITE_HOLDING_REGISTERS,
				(byte)0x02,
				(byte)0x1A,
				(byte)0x00,
				(byte)0x01,
				(byte)0x02,
				(byte)0x00,
				(byte)0x03,
				(byte)0xB9,
				(byte)0x0B,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		boolean result = responderChannel.writeInbound(buf);

		// THEN
		assertThat("Decoder produced", result, is(equalTo(true)));
		RtuModbusMessage msg = responderChannel.readInbound();

		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID decoded", msg.getUnitId(), is(equalTo(0x0B)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		RegistersModbusMessage rmm = msg.unwrap(RegistersModbusMessage.class);
		assertThat("Type is Registers", msg, is(notNullValue()));
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x021A)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(1)));
		assertThat("Data", Arrays.equals(rmm.dataCopy(), new byte[] { 0x00, 0x03 }), is(true));
		assertThat("Data (shorts)", Arrays.equals(rmm.dataDecode(), new short[] { (short) 0x03 }),
				is(true));
		assertThat("Data (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] { 3 }), is(true));
		assertThat("Provided CRC preserved", msg.getCrc(), is(equalTo((short) 0x0BB9)));
		assertThat("Calculated CRC", msg.computeCrc(), is(equalTo((short) 0x0BB9)));
		assertThat("CRC invalid", msg.isCrcValid(), is(equalTo(true)));
	}

	@Test
	public void controller_response_readInputs_parts() {
		// GIVEN
		// @formatter:off
		final byte[] data1 = new byte[] {
				(byte)0x0A,
				ModbusFunctionCodes.READ_INPUT_REGISTERS,
				(byte)0x0C,
				(byte)0x01,
				(byte)0x02,
				(byte)0x03,
				(byte)0x04,
		};
		final byte[] data2 = new byte[] {
				(byte)0x05,
				(byte)0x06,
				(byte)0x07,
				(byte)0x08,
				(byte)0x09,
				(byte)0x0A,
				(byte)0x0B,
				(byte)0x0C,
				(byte)0xCC,
				(byte)0xDD,
		};
		// @formatter:on

		// WHEN
		boolean result1 = controllerChannel.writeInbound(Unpooled.wrappedBuffer(data1));
		boolean result2 = controllerChannel.writeInbound(Unpooled.wrappedBuffer(data2));

		// THEN
		assertThat("Decoder did not produce after partial data", result1, is(equalTo(false)));
		assertThat("Decoder produced after full data", result2, is(equalTo(true)));
		RtuModbusMessage msg = controllerChannel.readInbound();

		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID decoded", msg.getUnitId(), is(equalTo(0x0A)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		RegistersModbusMessage rmm = msg.unwrap(RegistersModbusMessage.class);
		assertThat("Type is Registers", msg, is(notNullValue()));
		assertThat("Address not available", rmm.getAddress(), is(equalTo(0)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(6)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x01,
				(byte)0x02,
				(byte)0x03,
				(byte)0x04,
				(byte)0x05,
				(byte)0x06,
				(byte)0x07,
				(byte)0x08,
				(byte)0x09,
				(byte)0x0A,
				(byte)0x0B,
				(byte)0x0C,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x0102,
				(short)0x0304,
				(short)0x0506,
				(short)0x0708,
				(short)0x090A,
				(short)0x0B0C,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x0102,
				0x0304,
				0x0506,
				0x0708,
				0x090A,
				0x0B0C,
		}), is(equalTo(true)));
		// @formatter:on
		assertThat("Provided CRC preserved", msg.getCrc(), is(equalTo((short) 0xDDCC)));
		assertThat("Calculated CRC", msg.computeCrc(), is(equalTo((short) 0xE688)));
		assertThat("CRC invalid", msg.isCrcValid(), is(equalTo(false)));
	}

	@Test
	public void controller_partialMessages() throws Exception {
		// GIVEN
		BufferedReader in = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("test-response-01.txt"), StandardCharsets.UTF_8));
		List<byte[]> packets = ModbusTestUtils.parseWireLogLines(in, ModbusByteUtils::decodeHexString);
		log.info("Test Modbus packet data: [{}]", packets.stream().map(a -> {
			return ModbusByteUtils.encodeHexString(a, 0, a.length, true, true);
		}).collect(joining("\n\t", "\n\t", "\n")));

		// WHEN
		boolean lastResult = false;
		for ( byte[] packet : packets ) {
			ByteBuf buf = Unpooled.wrappedBuffer(packet);
			lastResult = controllerChannel.writeInbound(buf);
		}

		// THEN
		assertThat("Object is decoded", lastResult, is(equalTo(true)));
		ModbusMessage msg = controllerChannel.readInbound();

		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID decoded", msg.getUnitId(), is(equalTo(0x03)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		RegistersModbusMessage rmm = msg.unwrap(RegistersModbusMessage.class);
		assertThat("Type is Registers", msg, is(notNullValue()));
		assertThat("Address not available", rmm.getAddress(), is(equalTo(0)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(64)));

		short[] data = rmm.dataDecode();
		assertThat("First reg value", data[0], is(equalTo((short) 0x436a)));
		assertThat("Last reg value", data[63], is(equalTo((short) 0xfffe)));
	}

	private static byte[] readHoldingsResponseFrame(int unitId, int value) {
		final RtuModbusMessage rtu = new RtuModbusMessage(unitId,
				net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readHoldingsResponse(unitId,
						0, new short[] { (short) value }));
		final ByteBuf buf = Unpooled.buffer(rtu.payloadLength());
		rtu.encodeModbusPayload(buf);
		final byte[] result = new byte[buf.readableBytes()];
		buf.readBytes(result);
		return result;
	}

	private static ByteBuf readBuffer(byte[]... data) {
		// use a buffer with spare capacity, like the one a real channel reads into
		final ByteBuf buf = Unpooled.buffer(2048);
		for ( byte[] d : data ) {
			buf.writeBytes(d);
		}
		return buf;
	}

	private static void assertReadHoldingsResponse(String msg, RtuModbusMessage message, int unitId,
			int value) {
		assertThat(msg + " decoded", message, is(notNullValue()));
		assertThat(msg + " unit ID", message.getUnitId(), is(equalTo(unitId)));
		assertThat(msg + " function", message.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
		assertThat(msg + " CRC valid", message.isCrcValid(), is(equalTo(true)));
		RegistersModbusMessage rmm = message.unwrap(RegistersModbusMessage.class);
		assertThat(msg + " is registers", rmm, is(notNullValue()));
		assertThat(msg + " data", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] { value }),
				is(equalTo(true)));
	}

	@Test
	public void controller_badData_discardsBufferedInput() {
		// GIVEN
		// a valid message followed by noise with an unknown function code, in the same read
		final byte[] noise = new byte[] { (byte) 0x01, (byte) 0x65, (byte) 0xAA };

		// WHEN
		controllerChannel.writeInbound(readBuffer(readHoldingsResponseFrame(1, 7), noise));

		// THEN
		assertReadHoldingsResponse("Message before noise", controllerChannel.readInbound(), 1, 7);
		Object none = controllerChannel.readInbound();
		assertThat("Noise not decoded", none, is(nullValue()));

		// every following message decoded
		for ( int i = 1; i <= 3; i++ ) {
			controllerChannel.writeInbound(readBuffer(readHoldingsResponseFrame(1, i)));
			assertReadHoldingsResponse("Message " + i + " after noise", controllerChannel.readInbound(),
					1, i);
		}
	}

	@Test
	public void controller_unsupportedFunction_recovers() {
		// GIVEN
		// @formatter:off
		final byte[] unsupported = new byte[] {
				(byte)0x01,
				ModbusFunctionCodes.GET_COMM_EVENT_LOG,
				(byte)0x02,
				(byte)0xCC,
				(byte)0xDD,
				(byte)0x01,
				(byte)0x02,
		};
		// @formatter:on

		// WHEN
		assertThrows(DecoderException.class, () -> {
			controllerChannel.writeInbound(readBuffer(unsupported));
		}, "DecoderException thrown by unsupported function");

		// THEN
		for ( int i = 1; i <= 3; i++ ) {
			controllerChannel.writeInbound(readBuffer(readHoldingsResponseFrame(1, i)));
			assertReadHoldingsResponse("Message " + i + " after unsupported function",
					controllerChannel.readInbound(), 1, i);
		}
	}

	@Test
	public void responder_unsupportedFunction_recovers() {
		// GIVEN
		// @formatter:off
		final byte[] unsupported = new byte[] {
				(byte)0x01,
				ModbusFunctionCodes.GET_COMM_EVENT_LOG,
				(byte)0x00,
				(byte)0xCC,
				(byte)0xDD,
				(byte)0x01
		};
		final byte[] request = new byte[] {
				(byte)0x01,
				ModbusFunctionCodes.READ_INPUT_REGISTERS,
				(byte)0x00,
				(byte)0x08,
				(byte)0x00,
				(byte)0x01,
				(byte)0xB0,
				(byte)0x08,
		};
		// @formatter:on

		// WHEN
		assertThrows(DecoderException.class, () -> {
			responderChannel.writeInbound(readBuffer(unsupported));
		}, "DecoderException thrown by unsupported function");

		// THEN
		for ( int i = 1; i <= 3; i++ ) {
			responderChannel.writeInbound(readBuffer(request));
			RtuModbusMessage msg = responderChannel.readInbound();
			assertThat("Request " + i + " decoded", msg, is(notNullValue()));
			assertThat("Request " + i + " unit ID", msg.getUnitId(), is(equalTo(1)));
			assertThat("Request " + i + " function", msg.getFunction(),
					is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
			assertThat("Request " + i + " CRC valid", msg.isCrcValid(), is(equalTo(true)));
			RegistersModbusMessage rmm = msg.unwrap(RegistersModbusMessage.class);
			assertThat("Request " + i + " address", rmm.getAddress(), is(equalTo(0x0008)));
		}
	}

	@Test
	public void controller_reset() {
		// GIVEN
		final RtuModbusMessageDecoder decoder = controllerChannel.pipeline()
				.get(RtuModbusMessageDecoder.class);
		final byte[] message = readHoldingsResponseFrame(1, 7);
		final byte[] partial = Arrays.copyOf(message, 4);

		// WHEN
		controllerChannel.writeInbound(readBuffer(partial));
		Object none = controllerChannel.readInbound();
		assertThat("Partial message not decoded", none, is(nullValue()));
		decoder.reset();
		controllerChannel.writeInbound(readBuffer(message));

		// THEN
		assertReadHoldingsResponse("Message after reset", controllerChannel.readInbound(), 1, 7);
	}

	@Test
	public void controller_reset_noInput() {
		// GIVEN
		final RtuModbusMessageDecoder decoder = controllerChannel.pipeline()
				.get(RtuModbusMessageDecoder.class);

		// WHEN
		decoder.reset();
		controllerChannel.writeInbound(readBuffer(readHoldingsResponseFrame(1, 7)));

		// THEN
		assertReadHoldingsResponse("Message after reset", controllerChannel.readInbound(), 1, 7);
	}

}
