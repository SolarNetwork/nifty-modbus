/* ==================================================================
 * RtuModbusExchangeHandlerTests.java - 2/10/2026 6:40:27 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessageReply;
import net.solarnetwork.io.modbus.ModbusTimeoutException;
import net.solarnetwork.io.modbus.ModbusUnsupportedFunctionException;
import net.solarnetwork.io.modbus.ModbusValidationException;
import net.solarnetwork.io.modbus.UserModbusError;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.BitsModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusExchangeHandler;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessageDecoder;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessageEncoder;

/**
 * Test cases for the {@link RtuModbusExchangeHandler} class.
 *
 * @author matt
 * @version 1.0
 */
public class RtuModbusExchangeHandlerTests {

	private static final long REPLY_TIMEOUT = 1000L;
	private static final long BROADCAST_DELAY = 200L;

	private ConcurrentMap<ModbusMessage, PendingMessage> pending;
	private AtomicLong replyTimeout;
	private AtomicLong broadcastDelay;
	private AtomicLong sendDelay;
	private EmbeddedChannel channel;

	@BeforeEach
	public void setup() {
		pending = new ConcurrentHashMap<>(8, 0.9f, 2);
		replyTimeout = new AtomicLong(REPLY_TIMEOUT);
		broadcastDelay = new AtomicLong(BROADCAST_DELAY);
		sendDelay = new AtomicLong(0);
		channel = new EmbeddedChannel(new RtuModbusMessageEncoder(), new RtuModbusMessageDecoder(true),
				new RtuModbusExchangeHandler(pending, replyTimeout::get, broadcastDelay::get,
						sendDelay::get));
	}

	@AfterEach
	public void teardown() {
		channel.finishAndReleaseAll();
	}

	/**
	 * Send a request in the same way as {@code NettyModbusClient}.
	 */
	private CompletableFuture<ModbusMessage> send(ModbusMessage request) {
		final CompletableFuture<ModbusMessage> f = new CompletableFuture<>();
		pending.put(request, new PendingMessage(request, f));
		channel.writeAndFlush(request).addListener(w -> {
			if ( !w.isSuccess() ) {
				pending.remove(request);
				f.completeExceptionally(w.cause());
			}
		});
		return f;
	}

	private static byte[] frame(int unitId, ModbusMessage msg) {
		final RtuModbusMessage rtu = new RtuModbusMessage(unitId, msg);
		final ByteBuf buf = Unpooled.buffer(rtu.payloadLength());
		rtu.encodeModbusPayload(buf);
		return ByteBufUtil.getBytes(buf);
	}

	private static byte[] readHoldingsResponseFrame(int unitId, int address, int... values) {
		final short[] data = new short[values.length];
		for ( int i = 0; i < values.length; i++ ) {
			data[i] = (short) values[i];
		}
		return frame(unitId, RegistersModbusMessage.readHoldingsResponse(unitId, address, data));
	}

	private void receive(byte[]... data) {
		// use a buffer with spare capacity, like the one a real channel reads into
		final ByteBuf buf = Unpooled.buffer(2048);
		for ( byte[] d : data ) {
			buf.writeBytes(d);
		}
		channel.writeInbound(buf);
	}

	private void assertRequestWritten(String msg, int unitId, ModbusMessage request) {
		final ByteBuf buf = channel.readOutbound();
		assertThat(msg + " written", buf, is(notNullValue()));
		assertThat(msg + " encoded", byteObjectArray(ByteBufUtil.getBytes(buf)),
				is(arrayContaining(byteObjectArray(frame(unitId, request)))));
		buf.release();
	}

	private void assertNothingWritten(String msg) {
		final Object out = channel.readOutbound();
		assertThat(msg, out, is(nullValue()));
	}

	private ModbusMessageReply assertReplyPassedOn(String msg, ModbusMessage request, int... values) {
		final ModbusMessage m = channel.readInbound();
		assertThat(msg + " passed on", m, is(notNullValue()));
		final ModbusMessageReply reply = m.unwrap(ModbusMessageReply.class);
		assertThat(msg + " is a reply", reply, is(notNullValue()));
		assertThat(msg + " is reply to request", reply.getRequest(), is(sameInstance(request)));
		if ( values.length > 0 ) {
			net.solarnetwork.io.modbus.RegistersModbusMessage reg = m
					.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
			assertThat(msg + " is registers", reg, is(notNullValue()));
			final int[] actual = reg.dataDecodeUnsigned();
			assertThat(msg + " register count", actual.length, is(equalTo(values.length)));
			for ( int i = 0; i < values.length; i++ ) {
				assertThat(msg + " register " + i, actual[i], is(equalTo(values[i])));
			}
		}
		return reply;
	}

	private void assertNothingPassedOn(String msg) {
		final Object in = channel.readInbound();
		assertThat(msg, in, is(nullValue()));
	}

	private void expireReplyTimeout() {
		channel.advanceTimeBy(REPLY_TIMEOUT + 1, TimeUnit.MILLISECONDS);
		channel.runScheduledPendingTasks();
	}

	private static Throwable failure(CompletableFuture<?> f) {
		assertThat("Future is done", f.isDone(), is(equalTo(true)));
		ExecutionException e = assertThrows(ExecutionException.class, () -> f.get());
		return e.getCause();
	}

	@Test
	public void construct_nullPending() {
		assertThrows(IllegalArgumentException.class, () -> {
			new RtuModbusExchangeHandler(null, replyTimeout::get);
		}, "Null pending not allowed");
	}

	@Test
	public void construct_nullReplyTimeout() {
		assertThrows(IllegalArgumentException.class, () -> {
			new RtuModbusExchangeHandler(pending, null);
		}, "Null replyTimeout not allowed");
	}

	@Test
	public void request_reply() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 7, 2);

		// WHEN
		send(req);
		assertRequestWritten("Request", 1, req);
		receive(readHoldingsResponseFrame(1, 7, 11, 22));

		// THEN
		assertReplyPassedOn("Reply", req, 11, 22);
		assertNothingPassedOn("Only one message produced");
	}

	@Test
	public void requests_sentOneAtATime() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);
		final ModbusMessage req3 = RegistersModbusMessage.readHoldingsRequest(2, 300, 1);

		// WHEN
		send(req1);
		send(req2);
		send(req3);

		// THEN
		assertRequestWritten("Request 1", 1, req1);
		assertNothingWritten("Request 2 held back until reply 1 received");

		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply 1", req1, 1);
		assertRequestWritten("Request 2", 1, req2);
		assertNothingWritten("Request 3 held back until reply 2 received");

		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2);
		assertRequestWritten("Request 3", 2, req3);

		receive(readHoldingsResponseFrame(2, 300, 3));
		assertReplyPassedOn("Reply 3", req3, 3);
		assertNothingWritten("Nothing more to send");
	}

	@Test
	public void reply_timeout() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f1 = send(req1);
		final CompletableFuture<ModbusMessage> f2 = send(req2);
		assertRequestWritten("Request 1", 1, req1);
		expireReplyTimeout();

		// THEN
		assertThat("Request 1 failed with timeout", failure(f1),
				is(instanceOf(ModbusTimeoutException.class)));
		assertThat("Request 1 no longer pending", pending.containsKey(req1), is(equalTo(false)));
		assertThat("Request 2 not completed", f2.isDone(), is(equalTo(false)));
		assertRequestWritten("Request 2 after timeout", 1, req2);

		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2);
	}

	@Test
	public void reply_timeoutCancelledByReply() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(req);
		assertRequestWritten("Request", 1, req);
		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply", req, 1);
		expireReplyTimeout();

		// THEN
		assertThat("Request not failed by timeout after reply", f.isCompletedExceptionally(),
				is(equalTo(false)));
	}

	@Test
	public void reply_noTimeout() {
		// GIVEN
		replyTimeout.set(0);
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(req);
		assertRequestWritten("Request", 1, req);
		expireReplyTimeout();

		// THEN
		assertThat("Request still outstanding", f.isDone(), is(equalTo(false)));
		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply", req, 1);
	}

	@Test
	public void reply_lateAfterTimeout_discarded() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(req1);
		assertRequestWritten("Request 1", 1, req1);
		expireReplyTimeout();

		// reply 1 shows up late, before request 2 is sent
		receive(readHoldingsResponseFrame(1, 100, 1));
		assertNothingPassedOn("Late reply 1 discarded");

		send(req2);
		assertRequestWritten("Request 2", 1, req2);
		receive(readHoldingsResponseFrame(1, 200, 2));

		// THEN
		assertReplyPassedOn("Reply 2", req2, 2);
		assertNothingPassedOn("Only reply 2 produced");
	}

	@Test
	public void reply_truncated_doesNotCorruptNextExchange() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);
		final byte[] reply1 = readHoldingsResponseFrame(1, 100, 1);
		final byte[] truncated = new byte[4];
		System.arraycopy(reply1, 0, truncated, 0, truncated.length);

		// WHEN
		final CompletableFuture<ModbusMessage> f1 = send(req1);
		assertRequestWritten("Request 1", 1, req1);
		receive(truncated);
		assertNothingPassedOn("Truncated reply not produced");
		expireReplyTimeout();
		assertThat("Request 1 failed with timeout", failure(f1),
				is(instanceOf(ModbusTimeoutException.class)));

		// THEN
		for ( int i = 2; i < 5; i++ ) {
			final ModbusMessage req = (i == 2 ? req2
					: RegistersModbusMessage.readHoldingsRequest(1, 100 * i, 1));
			send(req);
			assertRequestWritten("Request " + i, 1, req);
			receive(readHoldingsResponseFrame(1, 100 * i, i));
			ModbusMessageReply reply = assertReplyPassedOn("Reply " + i, req, i);
			reply.validate();
		}
	}

	@Test
	public void reply_staleInSameBuffer_discarded() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(req1);
		final CompletableFuture<ModbusMessage> f2 = send(req2);
		assertRequestWritten("Request 1", 1, req1);

		// two frames arrive together, before request 2 has been sent
		receive(readHoldingsResponseFrame(1, 100, 1), readHoldingsResponseFrame(1, 100, 99));

		// THEN
		assertReplyPassedOn("Reply 1", req1, 1);
		assertNothingPassedOn("Extra frame not treated as reply to request 2");
		assertThat("Request 2 still outstanding", f2.isDone(), is(equalTo(false)));
		assertRequestWritten("Request 2", 1, req2);

		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2);
	}

	@Test
	public void reply_noRequest_discarded() {
		// WHEN
		receive(readHoldingsResponseFrame(1, 100, 1));

		// THEN
		assertNothingPassedOn("Message without request discarded");
	}

	@Test
	public void reply_wrongUnit_discarded() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(req);
		assertRequestWritten("Request", 1, req);
		receive(readHoldingsResponseFrame(9, 100, 99));

		// THEN
		assertNothingPassedOn("Message from other unit discarded");
		assertThat("Request still outstanding", f.isDone(), is(equalTo(false)));
		assertThat("Request still pending", pending.containsKey(req), is(equalTo(true)));

		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply", req, 1);
	}

	@Test
	public void reply_wrongFunction_discarded() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(req);
		assertRequestWritten("Request", 1, req);
		receive(frame(1, RegistersModbusMessage.readInputsResponse(1, 100, new short[] { 99 })));

		// THEN
		assertNothingPassedOn("Message for other function discarded");
		assertThat("Request still outstanding", f.isDone(), is(equalTo(false)));

		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply", req, 1);
	}

	@Test
	public void reply_error() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f1 = send(req1);
		send(req2);
		assertRequestWritten("Request 1", 1, req1);
		receive(frame(1, new BaseModbusMessage(1, ModbusFunctionCode.ReadHoldingRegisters,
				ModbusErrorCode.IllegalDataAddress)));

		// THEN
		final ModbusMessageReply reply = assertReplyPassedOn("Error reply", req1);
		assertThat("Reply is an exception", reply.isException(), is(equalTo(true)));
		assertThat("Reply provides the error", reply.getError(),
				is(equalTo(ModbusErrorCode.IllegalDataAddress)));
		assertThat("Reply function is that of request", reply.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
		reply.validate();
		assertThat("Request not failed, as error is a valid reply", f1.isCompletedExceptionally(),
				is(equalTo(false)));
		assertThat("Request left pending for the client to complete with the reply",
				pending.containsKey(req1), is(equalTo(true)));

		// error reply completes the exchange
		assertRequestWritten("Request 2", 1, req2);
		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2);
	}

	@Test
	public void reply_error_nonStandardCode() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.writeHoldingRequest(1, 100, 9);

		// WHEN
		send(req);
		assertRequestWritten("Request", 1, req);
		receive(frame(1, new BaseModbusMessage(1, ModbusFunctionCode.WriteHoldingRegister,
				new UserModbusError((byte) 0x7F))));

		// THEN
		final ModbusMessageReply reply = assertReplyPassedOn("Error reply", req);
		assertThat("Reply is an exception", reply.isException(), is(equalTo(true)));
		assertThat("Reply provides the error", reply.getError(),
				is(equalTo(new UserModbusError((byte) 0x7F))));
	}

	@Test
	public void reply_invalidCrc_passedOn() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);
		final byte[] corrupt = readHoldingsResponseFrame(1, 100, 1);
		corrupt[4] = 99;

		// WHEN
		send(req1);
		assertRequestWritten("Request 1", 1, req1);
		// junk follows the corrupt frame
		receive(corrupt, new byte[] { 1, 3 });

		// THEN
		final ModbusMessageReply reply = assertReplyPassedOn("Corrupt reply", req1, 99);
		assertThrows(ModbusValidationException.class, () -> {
			reply.validate();
		}, "Corrupt reply fails validation");

		// junk following corrupt frame was discarded
		send(req2);
		assertRequestWritten("Request 2", 1, req2);
		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2).validate();
	}

	@Test
	public void reply_undecodable_failsRequest() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f1 = send(req1);
		send(req2);
		assertRequestWritten("Request 1", 1, req1);
		// @formatter:off
		receive(new byte[] {
				(byte)0x01,
				ModbusFunctionCodes.GET_COMM_EVENT_LOG,
				(byte)0x02,
				(byte)0xCC,
				(byte)0xDD,
				(byte)0x01,
				(byte)0x02,
		});
		// @formatter:on

		// THEN
		assertNothingPassedOn("Nothing produced for undecodable message");
		assertThat("Request 1 failed with decoding exception", failure(f1),
				is(instanceOf(ModbusUnsupportedFunctionException.class)));
		assertThat("Request 1 no longer pending", pending.containsKey(req1), is(equalTo(false)));

		assertRequestWritten("Request 2", 1, req2);
		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2);
	}

	@Test
	public void request_abandoned_notSent() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);
		final ModbusMessage req3 = BitsModbusMessage.readCoilsRequest(1, 300, 1);

		// WHEN
		send(req1);
		final CompletableFuture<ModbusMessage> f2 = send(req2);
		send(req3);
		assertRequestWritten("Request 1", 1, req1);

		// caller gives up waiting for request 2 before it is sent
		f2.cancel(true);
		receive(readHoldingsResponseFrame(1, 100, 1));

		// THEN
		assertReplyPassedOn("Reply 1", req1, 1);
		assertRequestWritten("Request 3 sent, skipping abandoned request 2", 1, req3);
		assertNothingWritten("Request 2 never sent");
		assertThat("Request 2 no longer pending", pending.containsKey(req2), is(equalTo(false)));
	}

	@Test
	public void channelClosed_failsRequests() {
		// GIVEN
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f1 = send(req1);
		final CompletableFuture<ModbusMessage> f2 = send(req2);
		assertRequestWritten("Request 1", 1, req1);
		channel.close();

		// THEN
		assertThat("Outstanding request failed", failure(f1), is(instanceOf(IOException.class)));
		assertThat("Queued request failed", failure(f2), is(instanceOf(IOException.class)));
		assertThat("Nothing pending", pending.keySet(), hasSize(0));
		assertNothingWritten("Queued request not sent");
	}

	@Test
	public void exception_failsRequest() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final IOException t = new IOException("Port gone.");

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(req);
		assertRequestWritten("Request", 1, req);
		channel.pipeline().fireExceptionCaught(t);

		// THEN
		assertThat("Outstanding request failed with exception", failure(f), is(sameInstance(t)));
		// exception was handled, not propagated to end of pipeline
		channel.checkException();
	}

	@Test
	public void exception_noRequest_handled() {
		// WHEN
		channel.pipeline().fireExceptionCaught(new IOException("Port gone."));

		// THEN
		// exception was handled, not propagated to end of pipeline
		channel.checkException();
	}

	private void expireBroadcastDelay() {
		channel.advanceTimeBy(BROADCAST_DELAY + 1, TimeUnit.MILLISECONDS);
		channel.runScheduledPendingTasks();
	}

	@Test
	public void construct_nullBroadcastTurnaroundDelay() {
		assertThrows(IllegalArgumentException.class, () -> {
			new RtuModbusExchangeHandler(pending, replyTimeout::get, null);
		}, "Null broadcastTurnaroundDelay not allowed");
	}

	@Test
	public void broadcast_write() {
		// GIVEN
		final ModbusMessage broadcast = RegistersModbusMessage.writeHoldingRequest(0, 100, 9);
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(broadcast);
		send(req);

		// THEN
		assertRequestWritten("Broadcast request", 0, broadcast);

		// a reply is provided without any response arriving
		final ModbusMessageReply reply = assertReplyPassedOn("Broadcast reply", broadcast);
		assertThat("Broadcast reply is not an exception", reply.isException(), is(equalTo(false)));
		assertThat("Broadcast reply has no error", reply.getError(), is(nullValue()));
		assertThat("Broadcast reply unit ID", reply.getUnitId(), is(equalTo(0)));
		assertThat("Broadcast reply can be identified",
				net.solarnetwork.io.modbus.rtu.RtuModbusMessage.isBroadcast(reply), is(equalTo(true)));
		net.solarnetwork.io.modbus.rtu.RtuModbusMessage rtuReply = reply
				.unwrap(net.solarnetwork.io.modbus.rtu.RtuModbusMessage.class);
		assertThat("Broadcast reply is an RTU message", rtuReply, is(notNullValue()));
		assertThat("Broadcast reply can be identified as RTU message", rtuReply.isBroadcast(),
				is(equalTo(true)));
		assertThat("Broadcast reply CRC is that of the request", rtuReply.isCrcValid(),
				is(equalTo(true)));
		assertThat("Broadcast reply function", reply.getFunction(),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegister)));
		net.solarnetwork.io.modbus.RegistersModbusMessage reg = reply
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("Broadcast reply echoes address", reg.getAddress(), is(equalTo(100)));
		reply.validate();
		assertThat("Broadcast request not failed", f.isCompletedExceptionally(), is(equalTo(false)));

		// the next request waits for the turnaround delay
		assertNothingWritten("Next request held back for turnaround delay");
		expireBroadcastDelay();
		assertRequestWritten("Next request", 1, req);
		receive(readHoldingsResponseFrame(1, 200, 2));
		final ModbusMessageReply nextReply = assertReplyPassedOn("Next reply", req, 2);
		assertThat("Reply from a device is not a broadcast",
				net.solarnetwork.io.modbus.rtu.RtuModbusMessage.isBroadcast(nextReply),
				is(equalTo(false)));
		assertThat("Reply from a device is an RTU message that is not a broadcast",
				nextReply.unwrap(net.solarnetwork.io.modbus.rtu.RtuModbusMessage.class).isBroadcast(),
				is(equalTo(false)));

		// and the broadcast is never timed out
		expireReplyTimeout();
		assertThat("Broadcast request not failed", f.isCompletedExceptionally(), is(equalTo(false)));
	}

	@Test
	public void broadcast_write_multiple() {
		// GIVEN
		final ModbusMessage broadcast = RegistersModbusMessage.writeHoldingsRequest(0, 100,
				new short[] { 1, 2, 3 });

		// WHEN
		send(broadcast);

		// THEN
		assertRequestWritten("Broadcast request", 0, broadcast);
		final ModbusMessageReply reply = assertReplyPassedOn("Broadcast reply", broadcast);
		assertThat("Broadcast reply function", reply.getFunction(),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegisters)));
	}

	@Test
	public void broadcast_noTurnaroundDelay() {
		// GIVEN
		broadcastDelay.set(0);
		final ModbusMessage broadcast = RegistersModbusMessage.writeHoldingRequest(0, 100, 9);
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(broadcast);
		send(req);

		// THEN
		assertRequestWritten("Broadcast request", 0, broadcast);
		assertReplyPassedOn("Broadcast reply", broadcast);
		assertRequestWritten("Next request written without delay", 1, req);
	}

	@Test
	public void broadcast_responseDuringTurnaround_discarded() {
		// GIVEN
		final ModbusMessage broadcast = RegistersModbusMessage.writeHoldingRequest(0, 100, 9);
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(broadcast);
		send(req);
		assertRequestWritten("Broadcast request", 0, broadcast);
		assertReplyPassedOn("Broadcast reply", broadcast);

		// a device responds to the broadcast, which it should not
		receive(frame(0, RegistersModbusMessage.writeHoldingResponse(0, 100, 9)));

		// THEN
		assertNothingPassedOn("Response to broadcast discarded");
		assertNothingWritten("Next request still held back for turnaround delay");
		expireBroadcastDelay();
		assertRequestWritten("Next request", 1, req);
		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Next reply", req, 2);
	}

	@Test
	public void broadcast_channelClosedDuringTurnaround() {
		// GIVEN
		final ModbusMessage broadcast = RegistersModbusMessage.writeHoldingRequest(0, 100, 9);
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(broadcast);
		final CompletableFuture<ModbusMessage> f = send(req);
		assertRequestWritten("Broadcast request", 0, broadcast);
		assertReplyPassedOn("Broadcast reply", broadcast);
		channel.close();

		// THEN
		assertThat("Queued request failed", failure(f), is(instanceOf(IOException.class)));
		assertNothingWritten("Queued request not sent");
	}

	@Test
	public void unitZero_read_notBroadcast() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(0, 100, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(req);

		// THEN
		assertRequestWritten("Request", 0, req);
		assertNothingPassedOn("No reply provided for a read");
		assertThat("Request waiting for response", f.isDone(), is(equalTo(false)));

		// a device that does answer on unit 0 is still supported
		receive(readHoldingsResponseFrame(0, 100, 7));
		assertReplyPassedOn("Reply", req, 7);
	}

	@Test
	public void unitZero_read_timeout() {
		// GIVEN
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(0, 100, 1);

		// WHEN
		final CompletableFuture<ModbusMessage> f = send(req);
		assertRequestWritten("Request", 0, req);
		expireReplyTimeout();

		// THEN
		assertThat("Request failed with timeout", failure(f),
				is(instanceOf(ModbusTimeoutException.class)));
	}

	private void advanceTime(long ms) {
		channel.advanceTimeBy(ms, TimeUnit.MILLISECONDS);
		channel.runScheduledPendingTasks();
	}

	@Test
	public void construct_nullSendMinimumDelay() {
		assertThrows(IllegalArgumentException.class, () -> {
			new RtuModbusExchangeHandler(pending, replyTimeout::get, broadcastDelay::get, null);
		}, "Null sendMinimumDelay not allowed");
	}

	@Test
	public void sendDelay_betweenWrites() {
		// GIVEN
		channel.freezeTime();
		sendDelay.set(300);
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);
		final ModbusMessage req3 = RegistersModbusMessage.readHoldingsRequest(1, 300, 1);

		// WHEN
		send(req1);
		send(req2);
		send(req3);

		// THEN
		assertRequestWritten("First request written without delay", 1, req1);

		// response 1 arrives 100ms after request 1 was written
		advanceTime(100);
		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply 1", req1, 1);
		assertNothingWritten("Request 2 held back for the rest of the delay");

		advanceTime(199);
		assertNothingWritten("Request 2 still held back 299ms after request 1");

		advanceTime(1);
		assertRequestWritten("Request 2 written 300ms after request 1", 1, req2);

		// response 2 arrives straight away, and request 3 waits the whole delay
		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2);
		advanceTime(299);
		assertNothingWritten("Request 3 held back 299ms after request 2");
		advanceTime(1);
		assertRequestWritten("Request 3 written 300ms after request 2", 1, req3);
	}

	@Test
	public void sendDelay_alreadyPassed() {
		// GIVEN
		channel.freezeTime();
		sendDelay.set(300);
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(req1);
		send(req2);
		assertRequestWritten("Request 1", 1, req1);

		// response 1 takes longer than the delay to arrive
		advanceTime(400);
		receive(readHoldingsResponseFrame(1, 100, 1));

		// THEN
		assertReplyPassedOn("Reply 1", req1, 1);
		assertRequestWritten("Request 2 written as soon as reply 1 received", 1, req2);
	}

	@Test
	public void sendDelay_requestSubmittedLater() {
		// GIVEN
		channel.freezeTime();
		sendDelay.set(300);
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);
		final ModbusMessage req3 = RegistersModbusMessage.readHoldingsRequest(1, 300, 1);

		// WHEN
		send(req1);
		assertRequestWritten("Request 1", 1, req1);
		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply 1", req1, 1);

		// request 2 submitted 100ms after request 1 was written
		advanceTime(100);
		send(req2);

		// THEN
		assertNothingWritten("Request 2 held back for the rest of the delay");
		advanceTime(200);
		assertRequestWritten("Request 2 written 300ms after request 1", 1, req2);
		receive(readHoldingsResponseFrame(1, 200, 2));
		assertReplyPassedOn("Reply 2", req2, 2);

		// request 3 submitted after the delay has passed
		advanceTime(500);
		send(req3);
		assertRequestWritten("Request 3 written without delay", 1, req3);
	}

	@Test
	public void sendDelay_abandonedWhileDelayed() {
		// GIVEN
		channel.freezeTime();
		sendDelay.set(300);
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);
		final ModbusMessage req3 = RegistersModbusMessage.readHoldingsRequest(1, 300, 1);

		// WHEN
		send(req1);
		final CompletableFuture<ModbusMessage> f2 = send(req2);
		send(req3);
		assertRequestWritten("Request 1", 1, req1);
		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply 1", req1, 1);

		// caller gives up on request 2 while it is being held back
		f2.cancel(true);
		advanceTime(300);

		// THEN
		assertRequestWritten("Request 3 written, skipping abandoned request 2", 1, req3);
		assertNothingWritten("Request 2 never sent");
	}

	@Test
	public void sendDelay_channelClosedWhileDelayed() {
		// GIVEN
		channel.freezeTime();
		sendDelay.set(300);
		final ModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 100, 1);
		final ModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(req1);
		final CompletableFuture<ModbusMessage> f2 = send(req2);
		assertRequestWritten("Request 1", 1, req1);
		receive(readHoldingsResponseFrame(1, 100, 1));
		assertReplyPassedOn("Reply 1", req1, 1);
		channel.close();

		// THEN
		assertThat("Held back request failed", failure(f2), is(instanceOf(IOException.class)));
		assertNothingWritten("Held back request not sent");
	}

	@Test
	public void sendDelay_afterBroadcast() {
		// GIVEN
		channel.freezeTime();
		sendDelay.set(BROADCAST_DELAY + 100);
		final ModbusMessage broadcast = RegistersModbusMessage.writeHoldingRequest(0, 100, 9);
		final ModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 200, 1);

		// WHEN
		send(broadcast);
		send(req);
		assertRequestWritten("Broadcast request", 0, broadcast);
		assertReplyPassedOn("Broadcast reply", broadcast);

		// THEN
		advanceTime(BROADCAST_DELAY);
		assertNothingWritten("Next request held back for minimum delay after turnaround delay");
		advanceTime(100);
		assertRequestWritten("Next request written once minimum delay has passed", 1, req);
	}

}
