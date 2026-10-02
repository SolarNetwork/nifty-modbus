/* ==================================================================
 * RtuNettyModbusClientTests.java - 5/12/2022 6:43:24 am
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

package net.solarnetwork.io.modbus.rtu.netty.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusClientConfig;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.RtuNettyModbusClient;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * Test cases for the {@link RtuNettyModbusClient} class.
 *
 * @author matt
 * @version 1.1
 */
public class RtuNettyModbusClient_ServerTests {

	private ConcurrentMap<ModbusMessage, PendingMessage> pending;
	private NettyRtuModbusClientConfig config;
	private RtuNettyModbusClient client;
	private @Nullable SerialPort serialPort;
	private ExecutorService executor;
	private ByteArrayOutputStream out;
	private PipedOutputStream pout = new PipedOutputStream();
	private PipedInputStream in;

	@BeforeEach
	public void setup() throws Exception {
		pending = new ConcurrentHashMap<>(8, 0.9f, 2);
		config = new NettyRtuModbusClientConfig("COM1", new BasicSerialParameters());
		client = new RtuNettyModbusClient(config, new SerialPortProvider() {

			@Override
			public SerialPort getSerialPort(String name) {
				return serialPort;
			}
		});
		client.setWireLogging(true);

		executor = Executors.newCachedThreadPool();
		out = new ByteArrayOutputStream();
		pout = new PipedOutputStream();
		in = new PipedInputStream(pout);
	}

	@AfterEach
	public void teardown() {
		if ( client != null ) {
			client.stop();
		}
		if ( executor != null ) {
			executor.shutdownNow();
		}
		if ( in != null ) {
			try {
				in.close();
			} catch ( IOException e ) {
				// ignore
			}
		}
	}

	private SerialPort simulatedSerialPort(CountDownLatch reqLatch) {
		return new SerialPort() {

			private boolean open = false;

			@Override
			public String getName() {
				return "Test Port";
			}

			@Override
			public void open(SerialParameters parameters) throws IOException {
				open = true;
			}

			@Override
			public boolean isOpen() {
				return open;
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				return new OutputStream() {

					@Override
					public void write(int b) throws IOException {
						reqLatch.countDown();
						out.write(b);
					}

				};
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new InputStream() {

					@Override
					public int available() throws IOException {
						return in.available();
					}

					@Override
					public int read() throws IOException {
						if ( in.available() < 1 ) {
							return -1;
						}
						return in.read();
					}

					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						if ( in.available() < 1 ) {
							// like a real serial port: wait for the read timeout, then return 0
							try {
								Thread.sleep(20);
							} catch ( InterruptedException e ) {
								throw new InterruptedIOException();
							}
							if ( in.available() < 1 ) {
								return 0;
							}
						}
						return in.read(b, off, Math.min(len, in.available()));
					}

				};
			}

			@Override
			public void close() throws IOException {
				open = false;
			}
		};
	}

	@Test
	public void send_recv() throws Exception {
		// GIVEN
		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		final RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr,
				count);
		final RtuModbusMessage rtuReq = new RtuModbusMessage(unitId, req);
		ByteBuf reqBuf = Unpooled.buffer(rtuReq.payloadLength());
		rtuReq.encodeModbusPayload(reqBuf);
		final byte[] rtuReqFrame = reqBuf.array();

		final CountDownLatch reqLatch = new CountDownLatch(rtuReqFrame.length);
		serialPort = simulatedSerialPort(reqLatch);

		final RegistersModbusMessage response = RegistersModbusMessage.readHoldingsResponse(unitId, addr,
				new short[] { 1, 2, 3 });
		final RtuModbusMessage rtuResponse = new RtuModbusMessage(0, response);

		// WHEN
		client.start().get();
		Future<ModbusMessage> f = client.sendAsync(req);

		// send response
		executor.execute(() -> {
			try {
				reqLatch.await(5, TimeUnit.SECONDS);
			} catch ( InterruptedException e ) {
				// ignore;
			}

			try {
				ByteBuf buf = Unpooled.buffer(rtuResponse.payloadLength());
				rtuResponse.encodeModbusPayload(buf);
				pout.write(buf.array());
			} catch ( IOException e ) {
				throw new RuntimeException(e);
			}
		});

		ModbusMessage res = f.get(5, TimeUnit.SECONDS);

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

	/**
	 * Create a simulated serial port with a device attached that responds to
	 * every read holding registers request with one register, whose value is
	 * the address requested.
	 */
	private SerialPort respondingSerialPort() {
		final SerialPort delegate = simulatedSerialPort(new CountDownLatch(0));
		return new SerialPort() {

			private final ByteArrayOutputStream request = new ByteArrayOutputStream();

			@Override
			public String getName() {
				return delegate.getName();
			}

			@Override
			public void open(SerialParameters parameters) throws IOException {
				delegate.open(parameters);
			}

			@Override
			public void close() throws IOException {
				delegate.close();
			}

			@Override
			public boolean isOpen() {
				return delegate.isOpen();
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return delegate.getInputStream();
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				return new OutputStream() {

					@Override
					public void write(int b) throws IOException {
						request.write(b);
						if ( request.size() < 8 ) {
							return;
						}
						final byte[] req = request.toByteArray();
						request.reset();
						final int unitId = req[0] & 0xFF;
						final int addr = ((req[2] & 0xFF) << 8) | (req[3] & 0xFF);
						final RtuModbusMessage res = new RtuModbusMessage(unitId, RegistersModbusMessage
								.readHoldingsResponse(unitId, addr, new short[] { (short) addr }));
						final ByteBuf buf = Unpooled.buffer(res.payloadLength());
						res.encodeModbusPayload(buf);
						pout.write(buf.array(), 0, buf.readableBytes());
					}

				};
			}
		};
	}

	@Test
	public void send_recv_concurrentThreads() throws Exception {
		// GIVEN
		final int threadCount = 4;
		final int requestCount = 25;
		serialPort = respondingSerialPort();
		client.setWireLogging(false);
		client.setReplyTimeout(5000);
		client.start().get();

		// WHEN
		final List<Future<List<String>>> results = new ArrayList<>();
		for ( int t = 0; t < threadCount; t++ ) {
			final int base = (t + 1) * 1000;
			results.add(executor.submit(new Callable<List<String>>() {

				@Override
				public List<String> call() throws Exception {
					final List<String> problems = new ArrayList<>();
					for ( int i = 0; i < requestCount; i++ ) {
						final int addr = base + i;
						try {
							ModbusMessage res = client
									.send(RegistersModbusMessage.readHoldingsRequest(1, addr, 1));
							res.validate();
							int value = res
									.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class)
									.dataDecodeUnsigned()[0];
							if ( value != addr ) {
								problems.add("Request for " + addr + " got response for " + value);
							}
						} catch ( RuntimeException e ) {
							problems.add("Request for " + addr + " failed: " + e);
						}
					}
					return problems;
				}
			}));
		}

		// THEN
		for ( Future<List<String>> result : results ) {
			assertThat("Every request received its own response", result.get(30, TimeUnit.SECONDS),
					is(empty()));
		}
	}

	@Test
	public void start_eventLoopGroupProvider() throws Exception {
		// GIVEN
		serialPort = simulatedSerialPort(new CountDownLatch(0));
		final List<Object> contexts = new ArrayList<>(1);
		client.setEventLoopGroupProvider((context, parent) -> {
			contexts.add(context);
			return net.solarnetwork.io.modbus.netty.channel.LocalIoEventLoopGroupFactory.INSTANCE
					.apply(context, parent);
		});

		// WHEN
		client.start().get(5, TimeUnit.SECONDS);

		// THEN
		assertThat("Connected using event loop group from provider", client.isConnected(),
				is(equalTo(true)));
		assertThat("Provider asked for event loop group", contexts, hasSize(1));
		assertThat("Provider given the client as context", contexts.get(0) == client, is(equalTo(true)));
	}

	@Test
	public void start_stop_start() throws Exception {
		// GIVEN
		serialPort = simulatedSerialPort(new CountDownLatch(0));

		// WHEN
		client.start().get(5, TimeUnit.SECONDS);
		assertThat("Connected", client.isConnected(), is(equalTo(true)));
		client.stop().get(15, TimeUnit.SECONDS);
		assertThat("Not connected after stop", client.isConnected(), is(equalTo(false)));
		assertThat("Serial port closed", serialPort.isOpen(), is(equalTo(false)));
		client.start().get(5, TimeUnit.SECONDS);

		// THEN
		assertThat("Connected again", client.isConnected(), is(equalTo(true)));
		assertThat("Serial port open again", serialPort.isOpen(), is(equalTo(true)));
	}

}
