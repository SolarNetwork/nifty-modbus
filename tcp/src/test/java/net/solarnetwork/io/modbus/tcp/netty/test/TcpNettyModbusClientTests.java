/* ==================================================================
 * TcpNettyModbusClientTests.java - 30/11/2022 2:07:44 pm
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.tcp.TcpModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpNettyModbusClient;

/**
 * Test cases for the {@link TcpNettyModbusClient} class.
 *
 * @author matt
 * @version 1.0
 */
public class TcpNettyModbusClientTests {

	private static final class TestTcpNettyModbusClient extends TcpNettyModbusClient {

		private final EmbeddedChannel channel;

		private TestTcpNettyModbusClient(TcpModbusClientConfig clientConfig, EmbeddedChannel channel,
				ConcurrentMap<ModbusMessage, PendingMessage> pending,
				ConcurrentMap<Integer, TcpModbusMessage> pendingMessages,
				IntSupplier transactionIdSupplier) {
			super(clientConfig, null, pending, channel.eventLoop(), null, pendingMessages,
					transactionIdSupplier);
			this.channel = channel;
			setWireLogging(true);
		}

		@Override
		protected ChannelFuture connect() {
			super.initChannel(channel);
			return channel.newSucceededFuture();
		}

	}

	private ConcurrentMap<ModbusMessage, PendingMessage> pending;
	private ConcurrentMap<Integer, TcpModbusMessage> pendingMessages;
	private AtomicInteger idSupplier = new AtomicInteger();
	private EmbeddedChannel channel;
	private TcpNettyModbusClient client;

	@BeforeEach
	public void setup() {
		pendingMessages = new ConcurrentHashMap<>(8, 0.9f, 2);
		pending = new ConcurrentHashMap<>(8, 0.9f, 2);
		channel = new EmbeddedChannel();
		client = new TestTcpNettyModbusClient(new NettyTcpModbusClientConfig() {

			@Override
			public String getHost() {
				return "test.localhost";
			}

			@Override
			public String getDescription() {
				return "Test";
			}
		}, channel, pending, pendingMessages, idSupplier::incrementAndGet);
	}

	@AfterEach
	public void teardown() {
		if ( client != null ) {
			client.stop();
		}
	}

	@Test
	public void send() throws Exception {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start().get();
		Future<ModbusMessage> f = client.sendAsync(req);

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should be pending", pending.keySet(), hasSize(1));
		Entry<ModbusMessage, PendingMessage> pendingMessage = pending.entrySet().iterator().next();
		assertThat("Pending entry key is message", pendingMessage.getKey(), is(sameInstance(req)));

		ByteBuf buf = channel.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		final int txId = idSupplier.get();
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(txId >>> 8 & 0xFF),
						(byte)(txId & 0xFF),
						(byte)0x00,
						(byte)0x00,
						(byte)0x00,
						(byte)0x06,
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on
	}

	@Test
	public void responseTimeout() throws Exception {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start().get();
		Future<ModbusMessage> f = client.sendAsync(req);

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should be pending", pending.keySet(), hasSize(1));
		Entry<ModbusMessage, PendingMessage> pendingMessage = pending.entrySet().iterator().next();
		assertThat("Pending entry key is message", pendingMessage.getKey(), is(sameInstance(req)));

		ByteBuf buf = channel.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		final int txId = idSupplier.get();
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(txId >>> 8 & 0xFF),
						(byte)(txId & 0xFF),
						(byte)0x00,
						(byte)0x00,
						(byte)0x00,
						(byte)0x06,
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on
	}

}
