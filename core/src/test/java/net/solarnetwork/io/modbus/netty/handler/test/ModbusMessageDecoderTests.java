/* ==================================================================
 * ModbusMessageDecoderTests.java - 21/12/2022 4:37:44 pm
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

package net.solarnetwork.io.modbus.netty.handler.test;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusByteUtils;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.ModbusMessageDecoder;
import net.solarnetwork.io.modbus.test.support.ModbusTestUtils;

/**
 * Test cases for the {@link ModbusMessageDecoder} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageDecoderTests {

	private static final Logger log = LoggerFactory.getLogger(ModbusMessageDecoderTests.class);

	private EmbeddedChannel controllerChannel;
	private EmbeddedChannel responderChannel;

	@BeforeEach
	public void setup() {
		controllerChannel = new EmbeddedChannel(new ModbusMessageDecoder(true));
		responderChannel = new EmbeddedChannel(new ModbusMessageDecoder(false));
	}

	@Test
	public void controller_noData() throws Exception {
		// WHEN
		ByteBuf buf = Unpooled.buffer();
		boolean result = controllerChannel.writeInbound(buf);

		// THEN
		assertThat("No object is decoded", result, is(equalTo(false)));
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
		assertThat("Unit ID not decoded", msg.getUnitId(), is(equalTo(0x0)));
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

	@Test
	public void responder_unknownFunctionCode() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				(byte)0x55,
				(byte)0x00,
				(byte)0x08,
				(byte)0x00,
				(byte)0x01,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		boolean result = responderChannel.writeInbound(buf);

		// THEN
		assertThat("Decoder did not produce", result, is(equalTo(false)));
	}

	@Test
	public void responder_request_readInputs() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.READ_INPUT_REGISTERS,
				(byte)0x00,
				(byte)0x08,
				(byte)0x00,
				(byte)0x01,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		boolean result = responderChannel.writeInbound(buf);

		// THEN
		assertThat("Decoder produced", result, is(equalTo(true)));
		ModbusMessage msg = responderChannel.readInbound();

		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID not decoded", msg.getUnitId(), is(equalTo(0)));
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
	}

}
