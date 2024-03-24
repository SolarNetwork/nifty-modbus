/* ==================================================================
 * NettyModbusClientTests.java - 30/11/2022 6:12:32 am
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

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusClientConfig;
import net.solarnetwork.io.modbus.ModbusClientConnectionObserver;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.ModbusMessageDecoder;
import net.solarnetwork.io.modbus.netty.handler.ModbusMessageEncoder;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClientConfig;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;

/**
 * Test cases for the {@link NettyModbusClient} class.
 *
 * @author matt
 * @version 1.1
 */
public class NettyModbusClientTests {

	private static final class TestNettyModbusClient extends NettyModbusClient<ModbusClientConfig> {

		private final EmbeddedChannel testChannel;

		private TestNettyModbusClient(ModbusClientConfig clientConfig, EmbeddedChannel channel,
				ConcurrentMap<ModbusMessage, PendingMessage> pending) {
			super(clientConfig, channel.eventLoop(), pending);
			this.testChannel = channel;
			setWireLogging(true);
		}

		private TestNettyModbusClient(ModbusClientConfig clientConfig, EmbeddedChannel channel) {
			super(clientConfig, channel.eventLoop());
			this.testChannel = channel;
			setWireLogging(true);
		}

		private TestNettyModbusClient(ModbusClientConfig clientConfig,
				ScheduledExecutorService scheduler, EmbeddedChannel channel) {
			super(clientConfig, scheduler);
			this.testChannel = channel;
			setWireLogging(true);
		}

		private TestNettyModbusClient(ModbusClientConfig clientConfig,
				ScheduledExecutorService scheduler, EmbeddedChannel channel,
				ConcurrentMap<ModbusMessage, PendingMessage> pending) {
			super(clientConfig, scheduler, pending);
			this.testChannel = channel;
			setWireLogging(true);
		}

		@Override
		protected ChannelFuture connect() {
			testChannel.pipeline().addLast(new ModbusMessageEncoder(), new ModbusMessageDecoder(true));
			super.initChannel(testChannel);

			return testChannel.newSucceededFuture();
		}

	}

	private ConcurrentMap<ModbusMessage, PendingMessage> pending;
	private EmbeddedChannel channel;
	private TestNettyModbusClient client;

	@BeforeEach
	public void setup() {
		pending = new ConcurrentHashMap<>(8, 0.9f, 2);
		channel = new EmbeddedChannel();
		client = new TestNettyModbusClient(new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test";
			}
		}, channel, pending);
	}

	@AfterEach
	public void teardown() {
		try {
			client.stop().get(5, TimeUnit.SECONDS);
		} catch ( Throwable t ) {
			// ignore and continue
		}
	}

	@Test
	public void construct_internalPending() {
		// WHEN
		TestNettyModbusClient c = new TestNettyModbusClient(new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test Construct";
			}
		}, channel);

		// THEN
		assertThat("Constructed with internal pending map", c, is(notNullValue()));
	}

	@Test
	public void construct_nullConfig() {
		// WHEN
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new TestNettyModbusClient(null, channel);
		}, "Null config not allowed");
	}

	@Test
	public void construct_nullPending() {
		// WHEN
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new TestNettyModbusClient(new NettyModbusClientConfig() {

				@Override
				public String getDescription() {
					return "Test Construct";
				}
			}, channel, null);
		}, "Null pending map not allowed");
	}

	@Test
	public void construct_privateScheduler() {
		// WHEN
		TestNettyModbusClient c = new TestNettyModbusClient(new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test Construct";
			}
		}, null, channel);

		// THEN
		assertThat("Client with private scheduler created", c, is(notNullValue()));
	}

	@Test
	public void isStarted_no() {
		assertThat("Client has not been started", client.isStarted(), is(equalTo(false)));
	}

	@Test
	public void isStarted_yes() {
		// GIVEN
		client.start();

		// THEN
		assertThat("Client has been started", client.isStarted(), is(equalTo(true)));
	}

	@Test
	public void isStarted_afterStop() throws Exception {
		// GIVEN
		client.start().get();
		client.stop();

		// THEN
		assertThat("Client is no longer started", client.isStarted(), is(equalTo(false)));
	}

	@Test
	public void isStarted_privateScheduler_yes() {
		// GIVEN
		TestNettyModbusClient client = new TestNettyModbusClient(new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test Construct";
			}
		}, null, channel);

		// WHEN
		client.start();

		// THEN
		try {
			assertThat("Client with private scheduler has been started", client.isStarted(),
					is(equalTo(true)));
		} finally {
			try {
				client.stop().get(5, TimeUnit.SECONDS);
			} catch ( Throwable t ) {
				// ignore and continue
			}
		}
	}

	@Test
	public void startStopStart_privateSchedule()
			throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN
		TestNettyModbusClient client = new TestNettyModbusClient(new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test Construct";
			}
		}, null, channel);

		// WHEN
		CompletableFuture<?> f = client.start().thenCompose(o -> {
			assertThat("Client is started", client.isStarted(), is(true));
			return client.stop();
		}).thenCompose(o -> {
			assertThat("Client is stopped", client.isStarted(), is(false));
			return client.start();
		});

		// THEN
		assertThat("Future provided", f, is(notNullValue()));
		f.get(5L, TimeUnit.SECONDS);

		assertThat("Client with private scheduler has been started, stopped, and started again",
				client.isStarted(), is(equalTo(true)));
		try {
			client.stop().get(5, TimeUnit.SECONDS);
		} catch ( Throwable t ) {
			// ignore and continue
		}
	}

	@Test
	public void send() {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start();
		Future<ModbusMessage> f = client.sendAsync(req);

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should be pending", pending.keySet(), hasSize(1));
		Entry<ModbusMessage, PendingMessage> pendingMessage = pending.entrySet().iterator().next();
		assertThat("Pending entry key is message", pendingMessage.getKey(), is(sameInstance(req)));

		ByteBuf buf = channel.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on
	}

	@Test
	public void send_recv() throws InterruptedException, ExecutionException {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start();
		Future<ModbusMessage> f = client.sendAsync(req);

		// provide response
		// @formatter:off
		final byte[] responseData = new byte[] {
				ModbusFunctionCodes.READ_HOLDING_REGISTERS,
				(byte)0x06,
				(byte)0x02,
				(byte)0x2B,
				(byte)0x00,
				(byte)0x00,
				(byte)0x00,
				(byte)0x64,
		};
		ByteBuf response = Unpooled.copiedBuffer(responseData);
		// @formatter:on
		channel.writeOneInbound(response).sync();

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should no longer be pending", pending.keySet(), hasSize(0));

		ByteBuf requestData = channel.readOutbound();
		assertThat("Request bytes produced", requestData, is(notNullValue()));

		// @formatter:off
		assertThat("Request message encoded", byteObjectArray(ByteBufUtil.getBytes(requestData)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on

		assertThat("Response has been received and processed", f.isDone(), is(equalTo(true)));
		ModbusMessage resp = f.get();
		assertThat("Response is not an error", resp.getError(), is(nullValue()));
		net.solarnetwork.io.modbus.RegistersModbusMessage respReg = resp
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("Response is Registers", respReg, is(notNullValue()));
	}

	@Test
	public void send_recvError() throws InterruptedException, ExecutionException {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start();
		Future<ModbusMessage> f = client.sendAsync(req);

		// provide response
		// @formatter:off
		final byte[] responseData = new byte[] {
				ModbusFunctionCodes.READ_HOLDING_REGISTERS + ModbusFunctionCodes.ERROR_OFFSET,
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
		};
		ByteBuf response = Unpooled.copiedBuffer(responseData);
		// @formatter:on
		channel.writeOneInbound(response).sync();

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should no longer be pending", pending.keySet(), hasSize(0));

		ByteBuf requestData = channel.readOutbound();
		assertThat("Request bytes produced", requestData, is(notNullValue()));

		// @formatter:off
		assertThat("Request message encoded", byteObjectArray(ByteBufUtil.getBytes(requestData)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
				})));
		// @formatter:on

		assertThat("Response has been received and processed", f.isDone(), is(equalTo(true)));
		ModbusMessage resp = f.get();
		assertThat("Response is an error", resp.getError(), is(ModbusErrorCode.IllegalDataAddress));
	}

	@Test
	public void send_recvTimeout() throws InterruptedException, ExecutionException {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.setReplyTimeout(200);
		client.start();

		RuntimeException e = assertThrows(RuntimeException.class, () -> {
			client.send(req);
		});

		// THEN
		assertThat("Exception is timeout", e.getCause(), is(instanceOf(TimeoutException.class)));
	}

	@Test
	public void send_recv_withDelay() throws InterruptedException, ExecutionException {
		// GIVEN
		final long sendDelay = 900L;
		final long manualDelay = 1200L;
		((NettyModbusClientConfig) client.getClientConfig()).setSendMinimumDelayMs(sendDelay);

		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start();
		List<Long> executionTimes = new ArrayList<>(3);
		List<Future<ModbusMessage>> futures = new ArrayList<>(3);

		for ( int i = 0; i < 3; i++ ) {
			if ( i == 2 ) {
				// pause longer than throttle, to verify message sends right away
				Thread.sleep(manualDelay);
			}
			final long start = System.currentTimeMillis();
			Future<ModbusMessage> f = client.sendAsync(req);
			executionTimes.add(System.currentTimeMillis() - start);

			// provide response
			// @formatter:off
			final byte[] responseData = new byte[] {
					ModbusFunctionCodes.READ_HOLDING_REGISTERS,
					(byte)0x06,
					(byte)0x02,
					(byte)0x2B,
					(byte)0x00,
					(byte)0x00,
					(byte)0x00,
					(byte)0x64,
			};
			// @formatter:on
			ByteBuf response = Unpooled.copiedBuffer(responseData);
			channel.writeOneInbound(response).sync();
			futures.add(f);
		}

		// THEN
		assertThat("3 futures returned", futures, hasSize(3));
		for ( int i = 0; i < 3; i++ ) {
			Future<ModbusMessage> f = futures.get(i);
			assertThat("Future returned", f, is(notNullValue()));
			assertThat("Request should no longer be pending", pending.keySet(), hasSize(0));

			ByteBuf requestData = channel.readOutbound();
			assertThat("Request bytes produced", requestData, is(notNullValue()));

			// @formatter:off
			assertThat("Request message encoded", byteObjectArray(ByteBufUtil.getBytes(requestData)), arrayContaining(
					byteObjectArray(new byte[] {
							ModbusFunctionCodes.READ_HOLDING_REGISTERS,
							(byte)(addr >>> 8 & 0xFF),
							(byte)(addr & 0xFF),
							(byte)(count >>> 8 & 0xFF),
							(byte)(count & 0xFF),
					})));
			// @formatter:on

			assertThat("Response has been received and processed", f.isDone(), is(equalTo(true)));
			ModbusMessage resp = f.get();
			assertThat("Response is not an error", resp.getError(), is(nullValue()));
			net.solarnetwork.io.modbus.RegistersModbusMessage respReg = resp
					.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
			assertThat("Response is Registers", respReg, is(notNullValue()));

			long execTime = executionTimes.get(i);
			if ( i == 0 || i == 2 ) {
				// last execution time should be negligable
				assertThat(
						"First execution time, or after manual delay, should be close to 0 (within 200ms)",
						execTime, is(lessThan(200L)));
			} else if ( i == 1 ) {
				assertThat("Execution time immeidately after send must be roughly delay (within 200ms)",
						sendDelay - execTime, is(lessThan(200L)));
			}
		}
	}

	private static final class TestObservingNettyModbusClient
			extends NettyModbusClient<ModbusClientConfig> {

		private final AtomicReference<EmbeddedChannel> channelRef;

		private TestObservingNettyModbusClient(ModbusClientConfig config,
				AtomicReference<EmbeddedChannel> channelRef) {
			super(config, null);
			this.channelRef = channelRef;
		}

		@Override
		protected ChannelFuture connect() throws IOException {
			return channelRef.get().newSucceededFuture();
		}

		@Override
		public ChannelHandler newModbusChannelHandler() {
			return super.newModbusChannelHandler();
		}

	}

	@Test
	public void startStop_observer() throws InterruptedException, ExecutionException, TimeoutException {
		AtomicReference<EmbeddedChannel> channelRef = new AtomicReference<>();
		ModbusClientConfig config = new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test";
			}
		};
		TestObservingNettyModbusClient testClient = new TestObservingNettyModbusClient(config,
				channelRef);

		AtomicInteger openCount = new AtomicInteger();
		AtomicInteger closeCount = new AtomicInteger();

		// GIVEN
		testClient.setConnectionObserver(new ModbusClientConnectionObserver() {

			@Override
			public void connectionOpened(ModbusClient client, ModbusClientConfig config) {
				assertThat("Client is own client", client, is(sameInstance(testClient)));
				openCount.incrementAndGet();
			}

			@Override
			public void connectionClosed(ModbusClient client, ModbusClientConfig config,
					Throwable exception, boolean willReconnect) {
				assertThat("Client is own client", client, is(sameInstance(testClient)));
				assertThat("Will not reconnect after explicit close", willReconnect, is(false));
				closeCount.incrementAndGet();
			}
		});

		// WHEN
		channelRef.set(new EmbeddedChannel(testClient.newModbusChannelHandler()));
		CompletableFuture<?> f = testClient.start().thenCompose(o -> {
			assertThat("Client is started", testClient.isStarted(), is(true));
			return testClient.stop();
		});

		// THEN
		assertThat("Future provided", f, is(notNullValue()));
		f.get(5L, TimeUnit.SECONDS);

		// THEN
		assertThat("Client has been stopped", testClient.isStarted(), is(equalTo(false)));
		assertThat("Opened callabck called", openCount.get(), is(equalTo(1)));
		assertThat("Opened callabck called", closeCount.get(), is(equalTo(1)));
	}

	@Test
	public void pendingMessageTimeoutCleaner()
			throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN
		TestNettyModbusClient client = new TestNettyModbusClient(new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test Construct";
			}
		}, null, channel, pending);

		client.setReplyTimeout(500);
		client.setPendingMessageTtl(700);
		client.start().get(5, TimeUnit.SECONDS);

		// WHEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.sendAsync(req);

		PendingMessage msg = pending.values().iterator().next();
		assertThat("Pending message available", msg, is(notNullValue()));

		// THEN
		Thread.sleep(1600L);

		assertThat("Pending message has been cleaned", pending.isEmpty(), is(true));
		client.stop().get(5, TimeUnit.SECONDS);
	}

	@Test
	public void pendingMessageTimeoutCleaner_multiPass()
			throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN
		TestNettyModbusClient client = new TestNettyModbusClient(new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test Construct";
			}
		}, null, channel, pending);

		client.setReplyTimeout(500);
		client.setPendingMessageTtl(700);
		client.start().get(5, TimeUnit.SECONDS);

		// WHEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.sendAsync(req);

		assertThat("Pending message available", pending.keySet(), hasSize(1));

		// THEN
		Thread.sleep(900L);

		RegistersModbusMessage req2 = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);
		client.sendAsync(req2);

		assertThat("Pending messages available", pending.keySet(), hasSize(2));

		Thread.sleep(900L);
		assertThat("One pending messages pruned", pending.keySet(), hasSize(1));

		Thread.sleep(1400L);

		assertThat("Pending messages have been cleaned", pending.isEmpty(), is(true));
		client.stop().get(5, TimeUnit.SECONDS);
	}

}
