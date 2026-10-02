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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusClientConfig;
import net.solarnetwork.io.modbus.ModbusClientConnectionObserver;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.ModbusException;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.ModbusMessageDecoder;
import net.solarnetwork.io.modbus.netty.handler.ModbusMessageEncoder;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClientConfig;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;

/**
 * Test cases for the {@link NettyModbusClient} class.
 *
 * @author matt
 * @version 1.2
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

		@Override
		public void enforceSendDelay() {
			super.enforceSendDelay();
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
	public void configure_eventLoopGroupProvider() {
		// WHEN
		BiFunction<Object, Boolean, EventLoopGroup> provider = new BiFunction<Object, Boolean, EventLoopGroup>() {

			@Override
			public EventLoopGroup apply(Object context, Boolean connected) {
				// nadda
				return null;
			}
		};
		client.setEventLoopGroupProvider(provider);

		// THEN
		assertThat("Getter returns set value", client.getEventLoopGroupProvider(),
				is(sameInstance(provider)));
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

		PendingMessage p = pending.get(req);
		assertThat("Request still pending", p, is(notNullValue()));
		assertThat("Response future cancelled so request is known to be abandoned",
				p.getFuture().isCancelled(), is(equalTo(true)));
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
				// last execution time should be negligible
				assertThat(
						"First execution time, or after manual delay, should be close to 0 (within 200ms)",
						execTime, is(lessThan(200L)));
			} else if ( i == 1 ) {
				assertThat("Execution time immediately after send must be roughly delay (within 200ms)",
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
		AtomicReference<@Nullable EmbeddedChannel> channelRef = new AtomicReference<>();
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
					@Nullable Throwable exception, boolean willReconnect) {
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
		assertThat("Opened callback called", openCount.get(), is(equalTo(1)));
		assertThat("Opened callback called", closeCount.get(), is(equalTo(1)));
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

	/**
	 * A client whose connection attempts are controlled by the test.
	 */
	private static final class TestConnectingNettyModbusClient
			extends NettyModbusClient<ModbusClientConfig> {

		private final AtomicInteger connectCount = new AtomicInteger();
		private volatile Supplier<ChannelFuture> connector;

		private TestConnectingNettyModbusClient(ModbusClientConfig config,
				@Nullable ScheduledExecutorService scheduler) {
			super(config, scheduler);
		}

		private TestConnectingNettyModbusClient(ModbusClientConfig config,
				@Nullable ScheduledExecutorService scheduler,
				ConcurrentMap<ModbusMessage, PendingMessage> pending) {
			super(config, scheduler, pending);
		}

		@Override
		protected ChannelFuture connect() throws IOException {
			connectCount.incrementAndGet();
			return connector.get();
		}

		private EmbeddedChannel newChannel() {
			return new EmbeddedChannel(newModbusChannelHandler());
		}

	}

	private static NettyModbusClientConfig reconnectingConfig(long delaySeconds) {
		NettyModbusClientConfig config = new NettyModbusClientConfig() {

			@Override
			public String getDescription() {
				return "Test Reconnect";
			}
		};
		config.setAutoReconnect(true);
		config.setAutoReconnectDelaySeconds(delaySeconds);
		return config;
	}

	private static void awaitCount(AtomicInteger counter, int expected, long maxWaitMs)
			throws InterruptedException {
		final long end = System.currentTimeMillis() + maxWaitMs;
		while ( counter.get() < expected && System.currentTimeMillis() < end ) {
			Thread.sleep(20);
		}
	}

	@Test
	public void reconnect_afterConnectionClosed() throws Exception {
		// GIVEN
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null);
		final EmbeddedChannel ch1 = c.newChannel();
		final EmbeddedChannel ch2 = c.newChannel();
		try {
			c.connector = () -> ch1.newSucceededFuture();
			c.start().get(5, TimeUnit.SECONDS);
			assertThat("Connected", c.isConnected(), is(equalTo(true)));

			// WHEN
			c.connector = () -> ch2.newSucceededFuture();
			ch1.close();

			// THEN
			assertThat("No longer connected", c.isConnected(), is(equalTo(false)));
			awaitCount(c.connectCount, 2, 5000);
			assertThat("Reconnected after connection closed", c.connectCount.get(), is(equalTo(2)));
			assertThat("Connected again", c.isConnected(), is(equalTo(true)));
		} finally {
			c.stop().get(5, TimeUnit.SECONDS);
		}
		assertThat("Connection closed by stop", ch2.isOpen(), is(equalTo(false)));
	}

	@Test
	public void stop_cancelsScheduledReconnect() throws Exception {
		// GIVEN
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null);
		c.connector = () -> channel.newFailedFuture(new IOException("Not available."));
		assertThrows(ExecutionException.class, () -> {
			c.start().get(5, TimeUnit.SECONDS);
		}, "Connection fails");
		assertThat("Connection attempted", c.connectCount.get(), is(equalTo(1)));

		// WHEN
		final long start = System.currentTimeMillis();
		c.stop().get(5, TimeUnit.SECONDS);
		final long stopTime = System.currentTimeMillis() - start;

		// wait for when reconnect would have happened
		Thread.sleep(1500);

		// THEN
		assertThat("Stop does not wait for scheduled reconnect", stopTime, is(lessThan(5000L)));
		assertThat("Connection not attempted again after stop", c.connectCount.get(), is(equalTo(1)));
		assertThat("Not started", c.isStarted(), is(equalTo(false)));
	}

	@Test
	public void stop_cancelsScheduledReconnect_externalScheduler() throws Exception {
		// GIVEN
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		try {
			TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(
					reconnectingConfig(1), scheduler);
			final EmbeddedChannel ch = c.newChannel();
			c.connector = () -> ch.newSucceededFuture();
			c.start().get(5, TimeUnit.SECONDS);

			// connection closes, so reconnect is scheduled
			ch.close();
			assertThat("Connection attempted", c.connectCount.get(), is(equalTo(1)));

			// WHEN
			c.stop().get(5, TimeUnit.SECONDS);

			// wait for when reconnect would have happened
			Thread.sleep(1500);

			// THEN
			assertThat("Connection not attempted again after stop", c.connectCount.get(),
					is(equalTo(1)));
		} finally {
			scheduler.shutdownNow();
		}
	}

	@Test
	public void stop_whileConnecting() throws Exception {
		// GIVEN
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null);
		final EmbeddedChannel ch = c.newChannel();
		final io.netty.channel.ChannelPromise connectPromise = ch.newPromise();
		c.connector = () -> connectPromise;
		final CompletableFuture<?> startFuture = c.start();
		assertThat("Connection in progress", startFuture.isDone(), is(equalTo(false)));

		// WHEN
		c.stop().get(5, TimeUnit.SECONDS);
		connectPromise.setSuccess();

		// THEN
		assertThat("Connection that completed after stop is closed", ch.isOpen(), is(equalTo(false)));
		assertThat("Not connected", c.isConnected(), is(equalTo(false)));
		assertThat("Start future cancelled", startFuture.isCancelled(), is(equalTo(true)));

		// and no reconnect is attempted for that closed connection
		Thread.sleep(1500);
		assertThat("Connection not attempted again after stop", c.connectCount.get(), is(equalTo(1)));
	}

	@Test
	public void restart_afterStopWhileReconnectScheduled() throws Exception {
		// GIVEN
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null);
		c.connector = () -> channel.newFailedFuture(new IOException("Not available."));
		assertThrows(ExecutionException.class, () -> {
			c.start().get(5, TimeUnit.SECONDS);
		}, "Connection fails");
		c.stop().get(5, TimeUnit.SECONDS);

		// WHEN
		final EmbeddedChannel ch = c.newChannel();
		c.connector = () -> ch.newSucceededFuture();
		try {
			c.start().get(5, TimeUnit.SECONDS);

			// wait for when reconnect from before the stop would have happened
			Thread.sleep(1500);

			// THEN
			assertThat("Only one connection attempted after restart", c.connectCount.get(),
					is(equalTo(2)));
			assertThat("Connected", c.isConnected(), is(equalTo(true)));
		} finally {
			c.stop().get(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void connectionClosed_failsPendingRequests() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		client.start().get(5, TimeUnit.SECONDS);
		CompletableFuture<ModbusMessage> f = client.sendAsync(req);
		assertThat("Request pending", pending.keySet(), hasSize(1));

		// WHEN
		channel.close();

		// THEN
		assertThat("Request completed when connection closed", f.isDone(), is(equalTo(true)));
		ExecutionException e = assertThrows(ExecutionException.class, () -> {
			f.get();
		}, "Request failed");
		assertThat("Request failed because connection closed", e.getCause(),
				is(instanceOf(IOException.class)));
		assertThat("Nothing pending", pending.keySet(), hasSize(0));
	}

	@Test
	public void sendDelay_multipleThreads() throws Exception {
		// GIVEN
		final long sendDelay = 300L;
		final int threadCount = 4;
		((NettyModbusClientConfig) client.getClientConfig()).setSendMinimumDelayMs(sendDelay);
		final java.util.concurrent.ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		final java.util.concurrent.CountDownLatch go = new java.util.concurrent.CountDownLatch(1);
		final List<Long> sendTimes = java.util.Collections.synchronizedList(new ArrayList<>());

		// WHEN
		try {
			List<Future<?>> tasks = new ArrayList<>(threadCount);
			for ( int i = 0; i < threadCount; i++ ) {
				tasks.add(executor.submit(() -> {
					try {
						go.await();
					} catch ( InterruptedException e ) {
						return;
					}
					client.enforceSendDelay();
					sendTimes.add(System.currentTimeMillis());
				}));
			}
			go.countDown();
			for ( Future<?> task : tasks ) {
				task.get(10, TimeUnit.SECONDS);
			}
		} finally {
			executor.shutdownNow();
		}

		// THEN
		assertThat("All threads allowed to send", sendTimes, hasSize(threadCount));
		java.util.Collections.sort(sendTimes);
		for ( int i = 1; i < threadCount; i++ ) {
			long gap = sendTimes.get(i) - sendTimes.get(i - 1);
			assertThat("Thread " + i + " allowed to send no sooner than the delay after thread "
					+ (i - 1) + " (within 50ms): " + gap, gap >= sendDelay - 50L, is(equalTo(true)));
		}
	}

	private static final byte[] READ_HOLDINGS_RESPONSE = new byte[] {
			ModbusFunctionCodes.READ_HOLDING_REGISTERS, (byte) 0x06, (byte) 0x02, (byte) 0x2B,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, };

	/**
	 * Run a task on another thread once a request has been sent.
	 */
	private Thread whenRequestSent(Runnable task) {
		Thread t = new Thread(() -> {
			final long end = System.currentTimeMillis() + 5000;
			while ( channel.attr(NettyModbusClient.LAST_ENCODED_MESSAGE).get() == null
					&& System.currentTimeMillis() < end ) {
				try {
					Thread.sleep(10);
				} catch ( InterruptedException e ) {
					return;
				}
			}
			task.run();
		}, "Test Responder");
		t.setDaemon(true);
		t.start();
		return t;
	}

	@Test
	public void start_twice() throws Exception {
		// WHEN
		CompletableFuture<?> f1 = client.start();
		CompletableFuture<?> f2 = client.start();

		// THEN
		assertThat("Same future returned when already started", f2, is(sameInstance(f1)));
	}

	@Test
	public void start_connectThrowsException() throws Exception {
		// GIVEN
		final IllegalStateException t = new IllegalStateException("Not today.");
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null);
		c.connector = () -> {
			throw t;
		};

		// WHEN
		try {
			ExecutionException e = assertThrows(ExecutionException.class, () -> {
				c.start().get(5, TimeUnit.SECONDS);
			}, "Start fails");

			// THEN
			assertThat("Start failed with exception thrown by connect", e.getCause(),
					is(sameInstance(t)));
			assertThat("Not connected", c.isConnected(), is(equalTo(false)));
		} finally {
			c.stop().get(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void start_schedulerShutDown() throws Exception {
		// GIVEN
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.shutdownNow();
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				scheduler);
		final EmbeddedChannel ch = c.newChannel();
		c.connector = () -> ch.newSucceededFuture();

		// WHEN
		try {
			c.start().get(5, TimeUnit.SECONDS);

			// THEN
			assertThat("Connected even though cleaner task could not be scheduled", c.isConnected(),
					is(equalTo(true)));
		} finally {
			c.stop().get(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void reconnect_schedulerShutDown() throws Exception {
		// GIVEN
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				scheduler);
		final EmbeddedChannel ch = c.newChannel();
		c.connector = () -> ch.newSucceededFuture();
		try {
			c.start().get(5, TimeUnit.SECONDS);
			scheduler.shutdownNow();

			// WHEN
			ch.close();
			Thread.sleep(1500);

			// THEN
			assertThat("Reconnect not attempted as it could not be scheduled", c.connectCount.get(),
					is(equalTo(1)));
			assertThat("Not connected", c.isConnected(), is(equalTo(false)));
		} finally {
			c.stop().get(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void reconnect_taskRunsAfterStop() throws Exception {
		// GIVEN
		// a scheduler whose reconnect tasks are not able to be cancelled, and are run by the test
		final List<Runnable> scheduled = new java.util.concurrent.CopyOnWriteArrayList<>();
		final java.util.concurrent.ScheduledThreadPoolExecutor scheduler = new java.util.concurrent.ScheduledThreadPoolExecutor(
				1) {

			@Override
			public java.util.concurrent.ScheduledFuture<?> schedule(Runnable command, long delay,
					TimeUnit unit) {
				scheduled.add(command);
				return super.schedule(() -> {
				}, delay, unit);
			}
		};
		try {
			TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(
					reconnectingConfig(1), scheduler);
			final EmbeddedChannel ch = c.newChannel();
			c.connector = () -> ch.newSucceededFuture();
			c.start().get(5, TimeUnit.SECONDS);

			// connection closes, so reconnect is scheduled
			ch.close();
			assertThat("Reconnect scheduled", scheduled, hasSize(1));
			c.stop().get(5, TimeUnit.SECONDS);

			// WHEN
			scheduled.get(0).run();

			// THEN
			assertThat("Reconnect task from before stop does not connect", c.connectCount.get(),
					is(equalTo(1)));
		} finally {
			scheduler.shutdownNow();
		}
	}

	@Test
	public void stop_interrupted() throws Exception {
		// GIVEN
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null);
		final EmbeddedChannel ch = c.newChannel();
		c.connector = () -> ch.newSucceededFuture();
		c.start().get(5, TimeUnit.SECONDS);

		// WHEN
		Thread.currentThread().interrupt();
		try {
			CompletableFuture<?> f = c.stop();

			// THEN
			assertThat("Stop completes when interrupted", f.isDone(), is(equalTo(true)));
			assertThat("Connection closed", ch.isOpen(), is(equalTo(false)));
			assertThat("Not started", c.isStarted(), is(equalTo(false)));
		} finally {
			// clear interrupted status
			Thread.interrupted();
		}
	}

	@Test
	public void sendAsync_notStarted() {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);

		// WHEN
		CompletableFuture<ModbusMessage> f = client.sendAsync(req);

		// THEN
		assertThat("Request completed", f.isDone(), is(equalTo(true)));
		ExecutionException e = assertThrows(ExecutionException.class, () -> {
			f.get();
		}, "Request failed");
		assertThat("Request failed because not connected", e.getCause(),
				is(instanceOf(IOException.class)));
		assertThat("Nothing pending", pending.keySet(), hasSize(0));
	}

	@Test
	public void sendAsync_connectionClosed() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		client.start().get(5, TimeUnit.SECONDS);
		channel.close();

		// WHEN
		CompletableFuture<ModbusMessage> f = client.sendAsync(req);

		// THEN
		assertThat("Request completed", f.isDone(), is(equalTo(true)));
		ExecutionException e = assertThrows(ExecutionException.class, () -> {
			f.get();
		}, "Request failed");
		assertThat("Request failed because connection closed", e.getCause(),
				is(instanceOf(IOException.class)));
		assertThat("Nothing pending", pending.keySet(), hasSize(0));
	}

	@Test
	public void send_sync() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		client.start().get(5, TimeUnit.SECONDS);
		whenRequestSent(() -> channel.writeOneInbound(Unpooled.copiedBuffer(READ_HOLDINGS_RESPONSE)));

		// WHEN
		ModbusMessage res = client.send(req);

		// THEN
		assertThat("Response returned", res, is(notNullValue()));
		assertThat("Response is not an error", res.getError(), is(nullValue()));
		assertThat("Nothing pending", pending.keySet(), hasSize(0));
	}

	@Test
	public void send_noReplyTimeout() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		client.setReplyTimeout(0);
		client.start().get(5, TimeUnit.SECONDS);
		whenRequestSent(() -> channel.writeOneInbound(Unpooled.copiedBuffer(READ_HOLDINGS_RESPONSE)));

		// WHEN
		ModbusMessage res = client.send(req);

		// THEN
		assertThat("Response returned", res, is(notNullValue()));
		assertThat("Reply timeout configured", client.getReplyTimeout(), is(equalTo(0L)));
	}

	@Test
	public void send_interrupted() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		client.start().get(5, TimeUnit.SECONDS);

		// WHEN
		Thread.currentThread().interrupt();
		try {
			ModbusException e = assertThrows(ModbusException.class, () -> {
				client.send(req);
			}, "Send fails when interrupted");

			// THEN
			assertThat("Exception caused by interruption", e.getCause(),
					is(instanceOf(InterruptedException.class)));
			PendingMessage p = pending.get(req);
			assertThat("Request still pending", p, is(notNullValue()));
			assertThat("Response future cancelled so request is known to be abandoned",
					p.getFuture().isCancelled(), is(equalTo(true)));
		} finally {
			// clear interrupted status
			Thread.interrupted();
		}
	}

	@Test
	public void send_failsWithRuntimeException() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		final IllegalStateException t = new IllegalStateException("Not today.");
		client.start().get(5, TimeUnit.SECONDS);
		whenRequestSent(() -> pending.get(req).getFuture().completeExceptionally(t));

		// WHEN
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			client.send(req);
		}, "Send fails");

		// THEN
		assertThat("Runtime exception thrown as is", e, is(sameInstance(t)));
	}

	@Test
	public void send_connectionClosed() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		client.start().get(5, TimeUnit.SECONDS);
		channel.close();

		// WHEN
		ModbusException e = assertThrows(ModbusException.class, () -> {
			client.send(req);
		}, "Send fails");

		// THEN
		assertThat("Checked exception wrapped in Modbus exception", e.getCause(),
				is(instanceOf(IOException.class)));
	}

	@Test
	public void recv_replyMessage() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 1);
		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 7 });
		client.start().get(5, TimeUnit.SECONDS);
		CompletableFuture<ModbusMessage> f = client.sendAsync(req);

		// WHEN
		// a reply that identifies its own request
		SimpleModbusMessageReply reply = new SimpleModbusMessageReply(req, res);
		channel.writeOneInbound(reply);

		// THEN
		assertThat("Request completed", f.isDone(), is(equalTo(true)));
		assertThat("Request completed with reply", f.get(), is(sameInstance(reply)));
		assertThat("Nothing pending", pending.keySet(), hasSize(0));
	}

	@Test
	public void recv_replyMessage_requestNotPending() throws Exception {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 1);
		RegistersModbusMessage other = RegistersModbusMessage.readHoldingsRequest(1, 3, 1);
		client.start().get(5, TimeUnit.SECONDS);
		CompletableFuture<ModbusMessage> f = client.sendAsync(req);

		// WHEN
		// a reply to some other request
		channel.writeOneInbound(new SimpleModbusMessageReply(other,
				RegistersModbusMessage.readHoldingsResponse(1, 3, new short[] { 7 })));

		// THEN
		assertThat("Request not completed by reply to other request", f.isDone(), is(equalTo(false)));
		assertThat("Request still pending", pending.keySet(), hasSize(1));
	}

	@Test
	public void recv_noRequest() throws Exception {
		// GIVEN
		client.start().get(5, TimeUnit.SECONDS);

		// WHEN
		// a response arrives without any request having been sent
		channel.writeOneInbound(Unpooled.copiedBuffer(READ_HOLDINGS_RESPONSE));

		// THEN
		assertThat("Nothing pending", pending.keySet(), hasSize(0));
		channel.checkException();
	}

	@Test
	public void observer_exceptionsIgnored() throws Exception {
		// GIVEN
		AtomicReference<@Nullable EmbeddedChannel> channelRef = new AtomicReference<>();
		TestObservingNettyModbusClient testClient = new TestObservingNettyModbusClient(
				new NettyModbusClientConfig() {

					@Override
					public String getDescription() {
						return "Test";
					}
				}, channelRef);
		AtomicInteger openCount = new AtomicInteger();
		AtomicInteger closeCount = new AtomicInteger();
		testClient.setConnectionObserver(new ModbusClientConnectionObserver() {

			@Override
			public void connectionOpened(ModbusClient client, ModbusClientConfig config) {
				openCount.incrementAndGet();
				throw new IllegalStateException("Observer failed.");
			}

			@Override
			public void connectionClosed(ModbusClient client, ModbusClientConfig config,
					@Nullable Throwable exception, boolean willReconnect) {
				closeCount.incrementAndGet();
				throw new IllegalStateException("Observer failed.");
			}
		});

		// WHEN
		EmbeddedChannel ch = new EmbeddedChannel(testClient.newModbusChannelHandler());
		channelRef.set(ch);
		testClient.start().get(5, TimeUnit.SECONDS);
		testClient.stop().get(5, TimeUnit.SECONDS);

		// THEN
		assertThat("Opened callback called", openCount.get(), is(equalTo(1)));
		assertThat("Closed callback called", closeCount.get(), is(equalTo(1)));
		assertThat("Connection closed", ch.isOpen(), is(equalTo(false)));
		ch.checkException();
	}

	@Test
	public void pendingMessageCleaner_nothingPending() throws Exception {
		// GIVEN
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null);
		c.setPendingMessageTtl(25);
		final EmbeddedChannel ch = c.newChannel();
		c.connector = () -> ch.newSucceededFuture();

		// WHEN
		try {
			c.start().get(5, TimeUnit.SECONDS);

			// wait for cleaner to run
			Thread.sleep(300);

			// THEN
			assertThat("Still connected", c.isConnected(), is(equalTo(true)));
		} finally {
			c.stop().get(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void pendingMessageCleaner_exception() throws Exception {
		// GIVEN
		final AtomicInteger valuesCount = new AtomicInteger();
		final ConcurrentMap<ModbusMessage, PendingMessage> badMap = new ConcurrentHashMap<ModbusMessage, PendingMessage>() {

			private static final long serialVersionUID = 4298371652039187642L;

			@Override
			public java.util.Collection<PendingMessage> values() {
				valuesCount.incrementAndGet();
				throw new IllegalStateException("No values for you.");
			}
		};
		TestConnectingNettyModbusClient c = new TestConnectingNettyModbusClient(reconnectingConfig(1),
				null, badMap);
		c.setPendingMessageTtl(25);
		final EmbeddedChannel ch = c.newChannel();
		c.connector = () -> ch.newSucceededFuture();

		// WHEN
		try {
			c.start().get(5, TimeUnit.SECONDS);

			// wait for cleaner to run more than once
			awaitCount(valuesCount, 2, 5000);

			// THEN
			assertThat("Cleaner keeps running after an exception", valuesCount.get() >= 2,
					is(equalTo(true)));
			assertThat("Still connected", c.isConnected(), is(equalTo(true)));
		} finally {
			// stop without closing the channel, which would use the bad map
			c.connector = null;
			ch.pipeline().removeFirst();
			c.stop().get(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void configure_wireLogging() {
		// THEN
		assertThat("Wire logging enabled by test setup", client.isWireLogging(), is(equalTo(true)));

		// WHEN
		client.setWireLogging(false);

		// THEN
		assertThat("Wire logging disabled", client.isWireLogging(), is(equalTo(false)));
	}

	@Test
	public void configure_replyTimeout() {
		// THEN
		assertThat("Default reply timeout", client.getReplyTimeout(),
				is(equalTo(NettyModbusClient.DEFAULT_REPLY_TIMEOUT)));

		// WHEN
		client.setReplyTimeout(123L);

		// THEN
		assertThat("Reply timeout configured", client.getReplyTimeout(), is(equalTo(123L)));
	}

	@Test
	public void sendDelay_interrupted() throws Exception {
		// GIVEN
		((NettyModbusClientConfig) client.getClientConfig()).setSendMinimumDelayMs(60_000L);

		// first call does not wait
		client.enforceSendDelay();

		// WHEN
		final AtomicReference<Long> waited = new AtomicReference<>();
		Thread t = new Thread(() -> {
			final long start = System.currentTimeMillis();
			client.enforceSendDelay();
			waited.set(System.currentTimeMillis() - start);
		});
		t.start();
		Thread.sleep(200);
		t.interrupt();
		t.join(5000);

		// THEN
		assertThat("Waiting thread stopped waiting when interrupted", t.isAlive(), is(equalTo(false)));
		assertThat("Waiting thread did not wait for whole delay", waited.get(), is(lessThan(30_000L)));
	}

}
