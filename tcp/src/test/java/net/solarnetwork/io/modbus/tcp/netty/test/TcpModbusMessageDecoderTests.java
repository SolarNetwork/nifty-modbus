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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import net.solarnetwork.io.modbus.ModbusException;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessageReply;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.tcp.TcpModbusUnsupportedFunctionException;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessageDecoder;

/**
 * Test cases for the {@link TcpModbusMessageDecoder} class.
 *
 * @author matt
 * @version 1.1
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
				(byte) 0x05,
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

	@Test
	public void responder_request_unsupportedFunction() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(false, messages));

		final short unitId = 1;
		final int transactionId = 123;

		BaseModbusMessage req = new BaseModbusMessage(unitId, ModbusFunctionCodes.GET_COMM_EVENT_LOG);
		TcpModbusMessage tcp = new TcpModbusMessage(transactionId, req);
		ByteBuf buf = Unpooled.buffer(tcp.payloadLength());
		tcp.encodeModbusPayload(buf);

		// WHEN
		DecoderException result = assertThrows(DecoderException.class, () -> {
			channel.writeInbound(buf);
		}, "DecoderException thrown by unsupported function");

		// THEN
		assertThat("TcpModbusUnsupportedFunctionException is cause", result.getCause(),
				is(instanceOf(TcpModbusUnsupportedFunctionException.class)));

		TcpModbusUnsupportedFunctionException ufe = (TcpModbusUnsupportedFunctionException) result
				.getCause();
		assertThat("Unit ID returned on exception", ufe.getUnitId(), is(equalTo(unitId)));
		assertThat("Function code returned on exception", ufe.getCode(),
				is(equalTo(ModbusFunctionCodes.GET_COMM_EVENT_LOG)));
		assertThat("Transaction ID returned on exception", ufe.getTransactionId(),
				is(equalTo(transactionId)));
	}

	private static byte[] frame(int transactionId, ModbusMessage msg) {
		TcpModbusMessage tcp = new TcpModbusMessage(transactionId, msg);
		ByteBuf buf = Unpooled.buffer(tcp.payloadLength());
		tcp.encodeModbusPayload(buf);
		return ByteBufUtil.getBytes(buf);
	}

	private static byte[] bytes(int... values) {
		byte[] result = new byte[values.length];
		for ( int i = 0; i < values.length; i++ ) {
			result[i] = (byte) values[i];
		}
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

	private static void assertReadHoldingsRequest(String msg, TcpModbusMessage message,
			int transactionId, int address) {
		assertThat(msg + " decoded", message, is(notNullValue()));
		assertThat(msg + " transaction ID", message.getTransactionId(), is(equalTo(transactionId)));
		assertThat(msg + " unit ID", message.getUnitId(), is(equalTo(1)));
		assertThat(msg + " function", message.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
		net.solarnetwork.io.modbus.RegistersModbusMessage reg = message
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat(msg + " is registers", reg, is(notNullValue()));
		assertThat(msg + " address", reg.getAddress(), is(equalTo(address)));
	}

	@Test
	public void request_in_multiple() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(false, messages));

		// WHEN
		channel.writeInbound(readBuffer(frame(1, RegistersModbusMessage.readHoldingsRequest(1, 10, 1)),
				frame(2, RegistersModbusMessage.readHoldingsRequest(1, 20, 1))));

		// THEN
		assertReadHoldingsRequest("Request 1", channel.readInbound(), 1, 10);
		assertReadHoldingsRequest("Request 2", channel.readInbound(), 2, 20);
	}

	@Test
	public void request_unsupportedFunction_followingDecoded() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(false, messages));

		// read device identification request, with data after the function code
		final byte[] unsupported = bytes(0x00, 0x7B, 0x00, 0x00, 0x00, 0x05, 0x01,
				ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT, 0x0E, 0x01, 0x00);

		// WHEN
		DecoderException result = assertThrows(DecoderException.class, () -> {
			channel.writeInbound(readBuffer(unsupported,
					frame(124, RegistersModbusMessage.readHoldingsRequest(1, 10, 1))));
		}, "DecoderException raised by unsupported function");

		// THEN
		assertThat("TcpModbusUnsupportedFunctionException is cause", result.getCause(),
				is(instanceOf(TcpModbusUnsupportedFunctionException.class)));
		TcpModbusUnsupportedFunctionException ufe = (TcpModbusUnsupportedFunctionException) result
				.getCause();
		assertThat("Function code returned on exception", ufe.getCode(),
				is(equalTo(ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT)));
		assertThat("Transaction ID returned on exception", ufe.getTransactionId(), is(equalTo(123)));

		// request in same read as unsupported request is decoded
		assertReadHoldingsRequest("Request after unsupported", channel.readInbound(), 124, 10);

		// as are later requests
		channel.writeInbound(
				readBuffer(frame(125, RegistersModbusMessage.readHoldingsRequest(1, 20, 1))));
		assertReadHoldingsRequest("Later request", channel.readInbound(), 125, 20);
	}

	@Test
	public void request_userFunction_followingDecoded() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(false, messages));

		// user-defined function request, with data after the function code
		final byte[] user = bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x01, 0x41, 0xAA, 0xBB, 0xCC);

		// WHEN
		channel.writeInbound(
				readBuffer(user, frame(2, RegistersModbusMessage.readHoldingsRequest(1, 10, 1))));

		// THEN
		TcpModbusMessage msg = channel.readInbound();
		assertThat("User function request decoded", msg, is(notNullValue()));
		assertThat("User function request transaction ID", msg.getTransactionId(), is(equalTo(1)));
		assertThat("User function request function", msg.getFunction().getCode(),
				is(equalTo((byte) 0x41)));
		assertReadHoldingsRequest("Request after user function", channel.readInbound(), 2, 10);
	}

	@Test
	public void response_lengthTooShortForFunction_followingDecoded() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(true, messages));

		// read holding registers response claiming 6 bytes of data, in a frame with only 2
		final byte[] truncated = bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x01,
				ModbusFunctionCodes.READ_HOLDING_REGISTERS, 0x06, 0x00, 0x01);

		// WHEN
		assertThrows(DecoderException.class, () -> {
			channel.writeInbound(readBuffer(truncated,
					frame(2, RegistersModbusMessage.readHoldingsResponse(1, 0, new short[] { 7 }))));
		}, "DecoderException raised by frame that cannot be decoded");

		// THEN
		TcpModbusMessage msg = channel.readInbound();
		assertThat("Response after undecodable frame decoded", msg, is(notNullValue()));
		assertThat("Response transaction ID", msg.getTransactionId(), is(equalTo(2)));
		net.solarnetwork.io.modbus.RegistersModbusMessage reg = msg
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("Response data", Arrays.equals(reg.dataDecodeUnsigned(), new int[] { 7 }),
				is(equalTo(true)));
	}

	@Test
	public void invalidLength_tooLarge() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(false, messages));

		// header with length of 255, followed by more junk
		final byte[] junk = bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0xFF, 0x01, 0x03, 0x00, 0x01, 0x02);

		// WHEN
		assertThrows(CorruptedFrameException.class, () -> {
			channel.writeInbound(readBuffer(junk));
		}, "CorruptedFrameException thrown by invalid length");

		// THEN
		Object none = channel.readInbound();
		assertThat("Nothing decoded", none, is(nullValue()));

		// junk discarded, so next frame decoded
		channel.writeInbound(readBuffer(frame(2, RegistersModbusMessage.readHoldingsRequest(1, 10, 1))));
		assertReadHoldingsRequest("Request after junk", channel.readInbound(), 2, 10);
	}

	@Test
	public void invalidLength_tooSmall() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(false, messages));

		// header with length of 1, which leaves no room for a function code
		final byte[] junk = bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x03, 0x00);

		// WHEN
		assertThrows(CorruptedFrameException.class, () -> {
			channel.writeInbound(readBuffer(junk));
		}, "CorruptedFrameException thrown by invalid length");

		// THEN
		Object none = channel.readInbound();
		assertThat("Nothing decoded", none, is(nullValue()));

		// junk discarded, so next frame decoded
		channel.writeInbound(readBuffer(frame(2, RegistersModbusMessage.readHoldingsRequest(1, 10, 1))));
		assertReadHoldingsRequest("Request after junk", channel.readInbound(), 2, 10);
	}

	@Test
	public void length_maximum() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(true, messages));

		// largest response possible: 125 registers, for a frame length of 253
		final short[] data = new short[125];
		for ( int i = 0; i < data.length; i++ ) {
			data[i] = (short) i;
		}

		// WHEN
		channel.writeInbound(
				readBuffer(frame(1, RegistersModbusMessage.readHoldingsResponse(1, 0, data))));

		// THEN
		TcpModbusMessage msg = channel.readInbound();
		assertThat("Response decoded", msg, is(notNullValue()));
		net.solarnetwork.io.modbus.RegistersModbusMessage reg = msg
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("All registers decoded", reg.getCount(), is(equalTo(125)));
		assertThat("Register data decoded", Arrays.equals(reg.dataDecode(), data), is(equalTo(true)));
	}

	/**
	 * A response to a report server ID request, which is not a supported
	 * function.
	 */
	private static byte[] reportServerIdResponse(int transactionId) {
		return bytes(transactionId >>> 8, transactionId, 0x00, 0x00, 0x00, 0x05, 0x01,
				ModbusFunctionCodes.REPORT_SERVER_ID, 0x02, 0xAA, 0xFF);
	}

	@Test
	public void response_unsupportedFunction_failureHandler() {
		// GIVEN
		final List<ModbusMessage> failedRequests = new ArrayList<>(1);
		final List<Throwable> failures = new ArrayList<>(1);
		EmbeddedChannel channel = new EmbeddedChannel(
				new TcpModbusMessageDecoder(true, messages, (request, cause) -> {
					failedRequests.add(request);
					failures.add(cause);
				}));

		final BaseModbusMessage request = new BaseModbusMessage(1, ModbusFunctionCodes.REPORT_SERVER_ID);
		final TcpModbusMessage req = new TcpModbusMessage(123, request);
		messages.put(req.getTransactionId(), req);

		// WHEN
		// response to transaction 123 that cannot be decoded, followed by one that can
		channel.writeInbound(readBuffer(reportServerIdResponse(123),
				frame(124, RegistersModbusMessage.readHoldingsResponse(1, 0, new short[] { 7 }))));

		// THEN
		assertThat("Handler given request whose response could not be decoded", failedRequests,
				hasSize(1));
		assertThat("Handler given the request", failedRequests.get(0), is(sameInstance(request)));
		assertThat("Handler given the reason", failures.get(0),
				is(instanceOf(TcpModbusUnsupportedFunctionException.class)));
		TcpModbusUnsupportedFunctionException ufe = (TcpModbusUnsupportedFunctionException) failures
				.get(0);
		assertThat("Function code returned on exception", ufe.getCode(),
				is(equalTo(ModbusFunctionCodes.REPORT_SERVER_ID)));
		assertThat("Transaction ID returned on exception", ufe.getTransactionId(), is(equalTo(123)));
		assertThat("Transaction no longer pending", messages.containsKey(123), is(equalTo(false)));

		// no exception raised on the pipeline, and the following response is decoded
		channel.checkException();
		TcpModbusMessage msg = channel.readInbound();
		assertThat("Following response decoded", msg, is(notNullValue()));
		assertThat("Following response transaction ID", msg.getTransactionId(), is(equalTo(124)));
	}

	@Test
	public void response_undecodable_failureHandler() {
		// GIVEN
		final List<ModbusMessage> failedRequests = new ArrayList<>(1);
		final List<Throwable> failures = new ArrayList<>(1);
		EmbeddedChannel channel = new EmbeddedChannel(
				new TcpModbusMessageDecoder(true, messages, (request, cause) -> {
					failedRequests.add(request);
					failures.add(cause);
				}));

		final RegistersModbusMessage request = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		final TcpModbusMessage req = new TcpModbusMessage(1, request);
		messages.put(req.getTransactionId(), req);

		// read holding registers response claiming 6 bytes of data, in a frame with only 2
		final byte[] truncated = bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x01,
				ModbusFunctionCodes.READ_HOLDING_REGISTERS, 0x06, 0x00, 0x01);

		// WHEN
		channel.writeInbound(readBuffer(truncated));

		// THEN
		assertThat("Handler given request whose response could not be decoded", failedRequests,
				hasSize(1));
		assertThat("Handler given the request", failedRequests.get(0), is(sameInstance(request)));
		assertThat("Handler given a Modbus exception", failures.get(0),
				is(instanceOf(ModbusException.class)));
		assertThat("Modbus exception has the reason as its cause", failures.get(0).getCause(),
				is(notNullValue()));
		assertThat("Transaction no longer pending", messages.containsKey(1), is(equalTo(false)));
		channel.checkException();
	}

	@Test
	public void response_unsupportedFunction_failureHandler_noRequest() {
		// GIVEN
		final List<ModbusMessage> failedRequests = new ArrayList<>(1);
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(true, messages,
				(request, cause) -> failedRequests.add(request)));

		// WHEN
		// no request is pending for the transaction
		DecoderException result = assertThrows(DecoderException.class, () -> {
			channel.writeInbound(readBuffer(reportServerIdResponse(123)));
		}, "DecoderException raised when there is no request to fail");

		// THEN
		assertThat("TcpModbusUnsupportedFunctionException is cause", result.getCause(),
				is(instanceOf(TcpModbusUnsupportedFunctionException.class)));
		assertThat("Handler not invoked", failedRequests, hasSize(0));
	}

	@Test
	public void response_unsupportedFunction_noFailureHandler() {
		// GIVEN
		EmbeddedChannel channel = new EmbeddedChannel(new TcpModbusMessageDecoder(true, messages));

		final TcpModbusMessage req = new TcpModbusMessage(123,
				new BaseModbusMessage(1, ModbusFunctionCodes.REPORT_SERVER_ID));
		messages.put(req.getTransactionId(), req);

		// WHEN
		DecoderException result = assertThrows(DecoderException.class, () -> {
			channel.writeInbound(readBuffer(reportServerIdResponse(123)));
		}, "DecoderException raised when there is no failure handler");

		// THEN
		assertThat("TcpModbusUnsupportedFunctionException is cause", result.getCause(),
				is(instanceOf(TcpModbusUnsupportedFunctionException.class)));
		assertThat("Transaction left pending, for whatever handles the exception",
				messages.containsKey(123), is(equalTo(true)));
	}

}
