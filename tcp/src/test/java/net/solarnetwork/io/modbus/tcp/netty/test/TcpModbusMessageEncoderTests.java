/* ==================================================================
 * TcpModbusMessageEncoderTests.java - 29/11/2022 9:08:54 am
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
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;
import net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessageEncoder;

/**
 * Test cases for the {@link TcpModbusMessageEncoder} class.
 *
 * @author matt
 * @version 1.1
 */
public class TcpModbusMessageEncoderTests {

	private AtomicInteger ID_SUPPLIER = new AtomicInteger();
	private ConcurrentMap<Integer, TcpModbusMessage> messages;
	private EmbeddedChannel channel;

	@BeforeEach
	public void setup() {
		messages = new ConcurrentHashMap<>(8, 0.9f, 2);
		channel = new EmbeddedChannel(
				new TcpModbusMessageEncoder(messages, ID_SUPPLIER::incrementAndGet));
	}

	@Test
	public void construct_nullValues() {
		assertThrows(IllegalArgumentException.class, () -> {
			new TcpModbusMessageEncoder(null);
		}, "Pending messages map is required");

		assertThrows(IllegalArgumentException.class, () -> {
			new TcpModbusMessageEncoder(new ConcurrentHashMap<>(), null);
		}, "Tx ID supplier is required");

	}

	@Test
	public void construct_defaultIdSupplier() {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage msg = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);
		EmbeddedChannel ch = new EmbeddedChannel(new TcpModbusMessageEncoder(messages));

		// WHEN
		boolean result = ch.writeOutbound(msg);

		// THEN
		assertThat("Message handled", result, is(equalTo(true)));
		ByteBuf buf = ch.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		// @formatter:off
		int txId = SimpleTransactionIdSupplier.INSTANCE.nextId() - 1;
		assertThat("Message encoded using default ID supplier", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(txId >>> 8 & 0xFF),
						(byte)(txId & 0xFF),
						(byte)0x00,
						(byte)0x00,
						(byte)0x00,
						(byte)0x06,
						(byte)unitId,
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on
	}

	@Test
	public void request_out() {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage msg = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		boolean result = channel.writeOutbound(msg);

		// THEN
		assertThat("Message handled", result, is(equalTo(true)));
		ByteBuf buf = channel.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		// @formatter:off
		int txId = ID_SUPPLIER.get();
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(txId >>> 8 & 0xFF),
						(byte)(txId & 0xFF),
						(byte)0x00,
						(byte)0x00,
						(byte)0x00,
						(byte)0x06,
						(byte)unitId,
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on
	}

	@Test
	public void response_out() {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// the request should be in the pending messages buffer
		final TcpModbusMessage tcpReq = new TcpModbusMessage(System.currentTimeMillis(),
				ID_SUPPLIER.incrementAndGet(), req);
		messages.put(tcpReq.getTransactionId(), tcpReq);

		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(unitId, addr,
				new short[] { 0x1234, 0x2345, 0x3456 });
		SimpleModbusMessageReply reply = new SimpleModbusMessageReply(tcpReq, res);

		// WHEN
		boolean result = channel.writeOutbound(reply);

		// THEN
		assertThat("Message handled", result, is(equalTo(true)));
		ByteBuf buf = channel.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		// @formatter:off
		int txId = ID_SUPPLIER.get();
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(txId >>> 8 & 0xFF),
						(byte)(txId & 0xFF),
						(byte)0x00,
						(byte)0x00,
						(byte)0x00,
						(byte)0x09,
						(byte)unitId,
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)0x06,
						(byte)0x12,
						(byte)0x34,
						(byte)0x23,
						(byte)0x45,
						(byte)0x34,
						(byte)0x56,
				})));
		// @formatter:on

		assertThat("Pending request message has been removed from buffer", messages, is(anEmptyMap()));
	}

	@Test
	public void tcp_passedThrough() {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		final int txId = 4;
		RegistersModbusMessage msg = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);
		TcpModbusMessage tcp = new TcpModbusMessage(txId, msg);

		// WHEN
		boolean result = channel.writeOutbound(tcp);

		// THEN
		assertThat("Message handled", result, is(equalTo(true)));
		ByteBuf buf = channel.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(txId >>> 8 & 0xFF),
						(byte)(txId & 0xFF),
						(byte)0x00,
						(byte)0x00,
						(byte)0x00,
						(byte)0x06,
						(byte)unitId,
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on
	}

}
