/* ==================================================================
 * NettyTcpModbusServerTests.java - 4/12/2022 7:42:39 am
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusServer;
import net.solarnetwork.io.modbus.tcp.netty.test.support.TcpTestUtils;

/**
 * Test cases for the {@link NettyTcpModbusServer} class.
 *
 * @author matt
 * @version 1.0
 */
public class NettyTcpModbusServerTests {

	@Test
	public void construct_nulls() {
		assertThrows(IllegalArgumentException.class, () -> {
			new NettyTcpModbusServer(null, 502);
		}, "Null bindAddress not allowed");
		assertThrows(IllegalArgumentException.class, () -> {
			new NettyTcpModbusServer(502, null, SimpleTransactionIdSupplier.INSTANCE);
		}, "Null pendingMessages not allowed");
		assertThrows(IllegalArgumentException.class, () -> {
			new NettyTcpModbusServer(502, new ConcurrentHashMap<>(), null);
		}, "Null transactionIdSupplier not allowed");
	}

	@Test
	public void construct_customBindAddress() {
		// GIVEN
		final int port = 5502;
		final String addr = "127.0.0.1";
		NettyTcpModbusServer s = new NettyTcpModbusServer(addr, port);

		assertThat("Port getter returns set value", s.getPort(), is(port));
		assertThat("Bind getter returns set value", s.getBindAddress(), is(addr));
	}

	@Test
	public void configure_messageHandler() {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(502);

		// WHEN
		BiConsumer<ModbusMessage, Consumer<ModbusMessage>> handler = new BiConsumer<ModbusMessage, Consumer<ModbusMessage>>() {

			@Override
			public void accept(ModbusMessage t, Consumer<ModbusMessage> u) {
				// nadda
			}
		};
		s.setMessageHandler(handler);

		// THEN
		assertThat("Getter returns set value", s.getMessageHandler(), is(sameInstance(handler)));
	}

	@Test
	public void configure_connectionListener() {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(502);

		// WHEN
		BiFunction<InetSocketAddress, Boolean, Boolean> listener = new BiFunction<InetSocketAddress, Boolean, Boolean>() {

			@Override
			public Boolean apply(InetSocketAddress address, Boolean connected) {
				// nadda
				return false;
			}
		};
		s.setClientConnectionListener(listener);

		// THEN
		assertThat("Getter returns set value", s.getClientConnectionListener(),
				is(sameInstance(listener)));
	}

	@Test
	public void default_wireLogging() {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(502);

		// THEN
		assertThat("Default value returned", s.isWireLogging(), is(false));
	}

	@Test
	public void configure_wireLogging() {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(502);

		// WHEN
		s.setWireLogging(true);

		// THEN
		assertThat("Getter returns set value", s.isWireLogging(), is(true));
	}

	@Test
	public void default_pendingMessageTtl() {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(502);

		// THEN
		assertThat("Default value returned", s.getPendingMessageTtl(),
				is(NettyTcpModbusServer.DEFAULT_PENDING_MESSAGE_TTL));
	}

	@Test
	public void configure_pendingMessageTtl() {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(502);

		// WHEN
		final long ttl = 123L;
		s.setPendingMessageTtl(ttl);

		// THEN
		assertThat("Getter returns set value", s.getPendingMessageTtl(), is(ttl));
	}

	@Test
	public void start_twice() throws IOException {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(TcpTestUtils.freePort());
		try {
			s.start();
			s.start(); // should not cause exception
		} finally {
			s.stop();
		}
	}

	@Test
	public void start_portInUse() throws IOException {
		// GIVEN
		NettyTcpModbusServer s = new NettyTcpModbusServer(TcpTestUtils.freePort());
		try {
			s.start();
			NettyTcpModbusServer s2 = new NettyTcpModbusServer(s.getPort());
			assertThrows(BindException.class, () -> {
				s2.start();
			}, "Cannot start server when port in use");
		} finally {
			s.stop();
		}
	}

}
