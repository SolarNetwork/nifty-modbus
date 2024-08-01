/* ==================================================================
 * TcpModbusMessageDecoderTests.java - 4/12/2022 12:46:28 pm
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

package net.solarnetwork.io.modbus.tcp.netty.test;

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessageReply;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessageDecoder;

/**
 * Test cases for the {@link TcpModbusMessageDecoder} class.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusMessageDecoderTests {

	private ConcurrentMap<Integer, TcpModbusMessage> messages;

	@BeforeEach
	public void setup() {
		messages = new ConcurrentHashMap<>(8, 0.9f, 2);
	}

	@Test
	public void construct_nullValue() {
		assertThrows(IllegalArgumentException.class, () -> {
			new TcpModbusMessageDecoder(false, null);
		}, "Pending messages map is required");
	}

	@Test
	public void request_in() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(false, messages));

		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		TcpModbusMessage tcp = new TcpModbusMessage(1, req);
		ByteBuf buf = Unpooled.buffer(tcp.payloadLength());
		tcp.encodeModbusPayload(buf);

		// WHEN
		boolean decoded = channel.writeInbound(buf);

		// THEN
		assertThat("Message handled", decoded, is(equalTo(true)));
		TcpModbusMessage msg = channel.readInbound();
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Decoded message is same as input", msg.isSameAs(tcp), is(equalTo(true)));
	}

	@Test
	public void response_in() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(true, messages));

		// make request available
		TcpModbusMessage req = new TcpModbusMessage(1,
				RegistersModbusMessage.readHoldingsRequest(1, 2, 3));
		messages.put(req.getTransactionId(), req);

		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(req.getUnitId(), 2,
				new short[] { 1, 2, 3 });
		TcpModbusMessage tcp = new TcpModbusMessage(1, res);
		ByteBuf buf = Unpooled.buffer(tcp.payloadLength());
		tcp.encodeModbusPayload(buf);

		// WHEN
		boolean decoded = channel.writeInbound(buf);

		// THEN
		assertThat("Message handled", decoded, is(equalTo(true)));
		ModbusMessageReply reply = channel.readInbound();
		assertThat("Message decoded", reply, is(notNullValue()));

		net.solarnetwork.io.modbus.RegistersModbusMessage msg = reply
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("RegistersModbusMessage available", msg, is(notNullValue()));
		assertThat("Decoded message is same as input", msg.isSameAs(res), is(equalTo(true)));
	}

	@Test
	public void response_in_parts() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(true, messages));

		// make request available
		TcpModbusMessage req = new TcpModbusMessage(1,
				RegistersModbusMessage.readHoldingsRequest(1, 2, 1));
		messages.put(req.getTransactionId(), req);

		// @formatter:off
		ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				(byte) 0x00,
				(byte) 0x01,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x06,
				(byte) 0x01,
				ModbusFunctionCodes.READ_HOLDING_REGISTERS,
				(byte) 0x02,
				(byte) 0x00,
				(byte) 0x03, 
		});
		// @formatter:on

		// WHEN
		boolean decoded1 = channel.writeInbound(buf.copy(0, 4));
		boolean decoded2 = channel.writeInbound(buf.copy(4, 4));
		boolean decoded3 = channel.writeInbound(buf.copy(8, 3));

		// THEN
		assertThat("Message not yet handled", decoded1, is(equalTo(false)));
		assertThat("Message not yet handled", decoded2, is(equalTo(false)));
		assertThat("Message handled once all bytes available", decoded3, is(equalTo(true)));
		ModbusMessageReply reply = channel.readInbound();
		assertThat("Message decoded", reply, is(notNullValue()));
		assertThat("Reply request same instance", reply.getRequest(), is(sameInstance(req.getBody())));

		net.solarnetwork.io.modbus.RegistersModbusMessage msg = reply
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("RegistersModbusMessage available", msg, is(notNullValue()));
		assertThat("Decoded message is same as input",
				msg.isSameAs(RegistersModbusMessage.readHoldingsResponse(1, 2, new short[] { 0x0003 })),
				is(equalTo(true)));
	}

	@Test
	public void response_in_noRequest() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(true, messages));

		// no request available

		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });
		TcpModbusMessage tcp = new TcpModbusMessage(1, res);
		ByteBuf buf = Unpooled.buffer(tcp.payloadLength());
		tcp.encodeModbusPayload(buf);

		// WHEN
		boolean decoded = channel.writeInbound(buf);

		// THEN
		assertThat("Message handled", decoded, is(equalTo(true)));
		TcpModbusMessage result = channel.readInbound();
		assertThat("Message decoded as plain message (not reply)", result, is(notNullValue()));
		assertThat("Decoded message is not same as input because of missing request information",
				result.isSameAs(tcp), is(equalTo(false)));
		assertThat("Transaction ID decoded", result.getTransactionId(),
				is(equalTo(tcp.getTransactionId())));
		assertThat("Unit ID decoded", result.getUnitId(), is(equalTo(tcp.getUnitId())));

		RegistersModbusMessage r = result.unwrap(RegistersModbusMessage.class);
		assertThat("Address NOT decoded", r.getAddress(), is(equalTo(0)));
		assertThat("Count decoded", r.getCount(), is(equalTo(3)));
		// @formatter:off
		assertThat("Data decoded", byteObjectArray(res.dataCopy()),
				arrayContaining(byteObjectArray(new byte[] { 
						(byte)0x00, 
						(byte)0x01, 
						(byte)0x00, 
						(byte)0x02, 
						(byte)0x00, 
						(byte)0x03 
		})));
		// @formatter:on
	}

}
