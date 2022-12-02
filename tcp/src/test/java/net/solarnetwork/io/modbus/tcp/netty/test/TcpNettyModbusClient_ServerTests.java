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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusServer;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpNettyModbusClient;

/**
 * Test cases for the {@link TcpNettyModbusClient} class.
 *
 * @author matt
 * @version 1.0
 */
public class TcpNettyModbusClient_ServerTests {

	private static final Logger log = LoggerFactory.getLogger(TcpNettyModbusClient_ServerTests.class);

	private ConcurrentMap<ModbusMessage, PendingMessage> pending;
	private ConcurrentMap<Integer, TcpModbusMessage> pendingMessages;
	private AtomicInteger idSupplier = new AtomicInteger();
	private TcpNettyModbusClient client;

	private AtomicInteger serverIdSupplier = new AtomicInteger();
	private ConcurrentMap<Integer, TcpModbusMessage> serverPendingMessages;
	private NettyTcpModbusServer server;

	@BeforeEach
	public void setup() {
		serverPendingMessages = new ConcurrentHashMap<>(8, 0.9f, 2);
		server = new NettyTcpModbusServer(freePort(), serverPendingMessages,
				serverIdSupplier::incrementAndGet);
		server.setWireLogging(true);
		server.start();

		pendingMessages = new ConcurrentHashMap<>(8, 0.9f, 2);
		pending = new ConcurrentHashMap<>(8, 0.9f, 2);
		client = new TcpNettyModbusClient(new NettyTcpModbusClientConfig("127.0.0.1", server.getPort()),
				pending, pendingMessages, idSupplier::incrementAndGet);
		client.setWireLogging(true);
	}

	@AfterEach
	public void teardown() {
		if ( client != null ) {
			client.stop();
		}
		if ( server != null ) {
			server.stop();
		}
	}

	private int freePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} finally {
			try {
				if ( socket != null ) {
					socket.close();
				}
			} catch ( IOException e ) {
				// ignore
			}
		}
	}

	@Test
	public void send_recv() throws Exception {
		// GIVEN
		final List<ModbusMessage> serverIn = new ArrayList<>(1);
		server.setMessageHandler((msg, sender) -> {
			serverIn.add(msg);
			net.solarnetwork.io.modbus.RegistersModbusMessage reg = msg
					.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
			if ( reg != null ) {
				sender.accept(RegistersModbusMessage.readHoldingsResponse(msg.getUnitId(),
						reg.getAddress(), new short[] { 1, 2, 3 }));
			} else {
				log.error("Expected a RegistersModbusMessage, got {}", msg);
				sender.accept(new BaseModbusMessage(msg.getUnitId(), msg.getFunction(),
						ModbusErrorCode.IllegalFunction));
			}
		});
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start().get(10, TimeUnit.SECONDS);
		Future<ModbusMessage> f = client.sendAsync(req);
		ModbusMessage res = f.get(10, TimeUnit.SECONDS);

		// THEN
		assertThat("Response received", res, is(notNullValue()));
		assertThat("Request should not be pending", pending.keySet(), hasSize(0));

		ModbusMessage resp = f.get();
		assertThat("Response is not an error", resp.getError(), is(nullValue()));
		net.solarnetwork.io.modbus.RegistersModbusMessage respReg = resp
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("Response is Registers", respReg, is(notNullValue()));
		assertThat("Address preserved", respReg.getAddress(), is(equalTo(addr)));
		assertThat("Count decoded", respReg.getCount(), is(equalTo(3)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(respReg.dataCopy(), new byte[] {
				(byte)0x00,
				(byte)0x01,
				(byte)0x00,
				(byte)0x02,
				(byte)0x00,
				(byte)0x03,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(respReg.dataDecode(), new short[] {
				(short)0x0001,
				(short)0x0002,
				(short)0x0003
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(respReg.dataDecodeUnsigned(), new int[] {
				0x0001,
				0x0002,
				0x0003
		}), is(equalTo(true)));
		// @formatter:on
	}

}
