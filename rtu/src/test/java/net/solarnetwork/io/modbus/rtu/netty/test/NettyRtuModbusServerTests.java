/* ==================================================================
 * NettyRtuModbusServerTests.java - 12/01/2026 12:37:03 pm
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

import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readInputsResponse;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusUnsupportedFunctionException;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusServer;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessage;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * Test cases for the {@link NettyRtuModbusServer} class.
 *
 * @author matt
 * @version 1.1
 */
public class NettyRtuModbusServerTests {

	private static final class TestRtuNettyModbusServer extends NettyRtuModbusServer {

		private final EmbeddedChannel channel;

		private TestRtuNettyModbusServer(String device, SerialParameters serialParameters,
				SerialPortProvider serialPortProvider, EmbeddedChannel channel) {
			super(device, serialParameters, serialPortProvider, channel.eventLoop());
			this.channel = channel;
			setWireLogging(true);
		}

		@Override
		public void start() {
			super.initChannel(channel);
		}

	}

	private static class TestSerialPortProvider implements SerialPortProvider {

		private final SerialPort serialPort;

		private TestSerialPortProvider(SerialPort serialPort) {
			super();
			this.serialPort = serialPort;
		}

		@Override
		public SerialPort getSerialPort(String name) {
			return serialPort;
		}

	}

	private static BiConsumer<ModbusMessage, Consumer<ModbusMessage>> inputMessageHandler() {
		return (msg, sender) -> {
			// this handler only supports read input registers requests
			RegistersModbusMessage req = msg.unwrap(RegistersModbusMessage.class);
			if ( req != null && req.getFunction().blockType() == ModbusBlockType.Input ) {

				// generate some fake data that matches the request register count
				short[] resultData = new short[req.getCount()];
				for ( int i = 0; i < resultData.length; i++ ) {
					resultData[i] = (short) i;
				}

				// respond with the fake data
				sender.accept(readInputsResponse(req.getUnitId(), req.getAddress(), resultData));
			} else {
				// send back error that we don't handle that request
				sender.accept(new BaseModbusMessage(msg.getUnitId(), msg.getFunction(),
						ModbusErrorCode.IllegalFunction));
			}
		};
	}

	private @Nullable EmbeddedChannel channel;
	private @Nullable NettyRtuModbusServer server;

	@BeforeEach
	public void setup() {
		channel = new EmbeddedChannel();
	}

	@AfterEach
	public void teardown() {
		if ( server != null ) {
			server.stop();
		}
	}

	@Test
	public void construct_nulls() {
		assertThrows(IllegalArgumentException.class, () -> {
			new NettyRtuModbusServer(null, new BasicSerialParameters(),
					new TestSerialPortProvider(null));
		}, "Null device not allowed");
		assertThrows(IllegalArgumentException.class, () -> {
			new NettyRtuModbusServer("/dev/ttyUSB0", null, new TestSerialPortProvider(null));
		}, "Null serial parameters not allowed");
		assertThrows(IllegalArgumentException.class, () -> {
			new NettyRtuModbusServer("/dev/ttyUSB0", new BasicSerialParameters(), null);
		}, "Null serial port provider not allowed");
	}

	@Test
	public void start_twice() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);

		// WHEN
		server.start();
		server.start(); // should not cause exception
	}

	@Test
	public void receive() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		server.setMessageHandler(inputMessageHandler());

		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readInputsRequest(unitId, addr, count);

		ByteBuf buf = Unpooled.buffer();
		new RtuModbusMessage(unitId, req).encodeModbusPayload(buf);

		// WHEN
		server.start();
		channel.writeInbound(buf);

		// THEN
		ByteBuf channelResponse = channel.readOutbound();
		assertThat("Response bytes produced", channelResponse, is(notNullValue()));

		ByteBuf expectedResponse = Unpooled.buffer();
		new RtuModbusMessage(unitId,
				RegistersModbusMessage.readInputsResponse(unitId, addr, new short[] { 0, 1, 2 }))
						.encodeModbusPayload(expectedResponse);
		assertThat("Response encoded", byteObjectArray(ByteBufUtil.getBytes(channelResponse)),
				arrayContaining(byteObjectArray(ByteBufUtil.getBytes(expectedResponse))));
	}

	/**
	 * A simulated serial port that can be opened more than once.
	 */
	private static final class SimulatedSerialPort implements SerialPort {

		private final AtomicInteger openCount = new AtomicInteger();
		private volatile boolean open;
		private volatile boolean disconnected;
		private volatile @Nullable Exception openException;

		@Override
		public String getName() {
			return "Test Port";
		}

		@Override
		public void open(SerialParameters parameters) throws IOException {
			openCount.incrementAndGet();
			final Exception e = this.openException;
			if ( e instanceof IOException ) {
				throw (IOException) e;
			} else if ( e instanceof RuntimeException ) {
				throw (RuntimeException) e;
			}
			disconnected = false;
			open = true;
		}

		@Override
		public void close() throws IOException {
			open = false;
		}

		@Override
		public boolean isOpen() {
			return open;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new InputStream() {

				@Override
				public int available() throws IOException {
					return (disconnected ? -1 : 0);
				}

				@Override
				public int read() throws IOException {
					return -1;
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					if ( disconnected ) {
						return -1;
					}
					// like a real serial port: wait for the read timeout, then return 0
					try {
						Thread.sleep(20);
					} catch ( InterruptedException e ) {
						throw new InterruptedIOException();
					}
					return (disconnected ? -1 : 0);
				}
			};
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return new ByteArrayOutputStream();
		}

		private void awaitClosed() throws InterruptedException {
			final long end = System.currentTimeMillis() + 5000;
			while ( open && System.currentTimeMillis() < end ) {
				Thread.sleep(20);
			}
		}

	}

	@Test
	public void start_stop_start() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));

		// WHEN
		server.start();
		assertThat("Serial port opened", port.isOpen(), is(equalTo(true)));
		server.stop();
		assertThat("Serial port closed", port.isOpen(), is(equalTo(false)));
		server.start();

		// THEN
		assertThat("Serial port opened again", port.isOpen(), is(equalTo(true)));
		assertThat("Serial port opened twice", port.openCount.get(), is(equalTo(2)));
	}

	@Test
	public void start_afterSerialPortFailure() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));
		server.start();
		assertThat("Serial port opened", port.isOpen(), is(equalTo(true)));

		// serial port fails, which closes the connection
		port.disconnected = true;
		port.awaitClosed();
		assertThat("Serial port closed after failure", port.isOpen(), is(equalTo(false)));

		// WHEN
		server.start();

		// THEN
		assertThat("Serial port opened again", port.isOpen(), is(equalTo(true)));
		assertThat("Serial port opened twice", port.openCount.get(), is(equalTo(2)));
	}

	@Test
	public void exceptionHandler_unsupportedFunction() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		server.setMessageHandler(inputMessageHandler());
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		server.setExceptionHandler((ex, sender) -> {
			exception.set(ex);
			if ( ex instanceof ModbusUnsupportedFunctionException ) {
				ModbusUnsupportedFunctionException ufe = (ModbusUnsupportedFunctionException) ex;
				sender.accept(new BaseModbusMessage(ufe.getUnitId(), ufe.getCode(),
						ModbusErrorCode.IllegalFunction.getCode()));
			}
		});

		final int unitId = 1;
		ByteBuf buf = Unpooled.buffer();
		new RtuModbusMessage(unitId,
				new BaseModbusMessage(unitId, ModbusFunctionCodes.GET_COMM_EVENT_LOG))
						.encodeModbusPayload(buf);

		// WHEN
		server.start();
		channel.writeInbound(buf);

		// THEN
		assertThat("Exception handler given the cause of the decoding failure", exception.get(),
				is(instanceOf(ModbusUnsupportedFunctionException.class)));

		ByteBuf out = channel.readOutbound();
		assertThat("Error response written", out, is(notNullValue()));
		final short crc = RtuModbusMessage.computeCrc(unitId, new BaseModbusMessage(unitId,
				ModbusFunctionCodes.GET_COMM_EVENT_LOG, ModbusErrorCode.IllegalFunction.getCode()));
		// @formatter:off
		assertThat("Error response encoded", byteObjectArray(ByteBufUtil.getBytes(out)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)unitId,
						(byte)(ModbusFunctionCodes.GET_COMM_EVENT_LOG + ModbusFunctionCodes.ERROR_OFFSET),
						ModbusErrorCode.IllegalFunction.getCode(),
						(byte)(crc & 0xFF),
						(byte)(crc >>> 8 & 0xFF),
				})));
		// @formatter:on
	}

	private static byte[] requestFrame(int unitId, ModbusMessage req) {
		final ByteBuf buf = Unpooled.buffer();
		new RtuModbusMessage(unitId, req).encodeModbusPayload(buf);
		return ByteBufUtil.getBytes(buf);
	}

	private static ByteBuf readBuffer(byte[]... data) {
		// use a buffer with spare capacity, like the one a real channel reads into
		final ByteBuf buf = Unpooled.buffer(2048);
		for ( byte[] d : data ) {
			buf.writeBytes(d);
		}
		return buf;
	}

	private void assertReadInputsResponse(String msg, int unitId, int addr, short[] data) {
		final ByteBuf response = channel.readOutbound();
		assertThat(msg + " produced", response, is(notNullValue()));
		final ByteBuf expected = Unpooled.buffer();
		new RtuModbusMessage(unitId, readInputsResponse(unitId, addr, data))
				.encodeModbusPayload(expected);
		assertThat(msg + " encoded", byteObjectArray(ByteBufUtil.getBytes(response)),
				arrayContaining(byteObjectArray(ByteBufUtil.getBytes(expected))));
	}

	@Test
	public void receive_invalidCrc() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		final AtomicInteger handled = new AtomicInteger();
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		server.setMessageHandler((msg, sender) -> {
			handled.incrementAndGet();
			inputMessageHandler().accept(msg, sender);
		});
		server.setExceptionHandler((ex, sender) -> exception.set(ex));

		final byte[] frame = requestFrame(1, RegistersModbusMessage.readInputsRequest(1, 2, 3));
		frame[frame.length - 1] ^= 0x01;

		// WHEN
		server.start();
		channel.writeInbound(readBuffer(frame));

		// THEN
		assertThat("Request with invalid CRC not passed to message handler", handled.get(),
				is(equalTo(0)));
		Object response = channel.readOutbound();
		assertThat("No response sent for request with invalid CRC", response, is(nullValue()));
		assertThat("Invalid CRC is not treated as an exception", exception.get(), is(nullValue()));
	}

	@Test
	public void receive_corruptedData() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		final AtomicInteger handled = new AtomicInteger();
		server.setMessageHandler((msg, sender) -> {
			handled.incrementAndGet();
			inputMessageHandler().accept(msg, sender);
		});

		// a request whose address was corrupted after the CRC was calculated
		final byte[] frame = requestFrame(1, RegistersModbusMessage.readInputsRequest(1, 2, 3));
		frame[3] = (byte) 0x7F;

		// WHEN
		server.start();
		channel.writeInbound(readBuffer(frame));

		// THEN
		assertThat("Corrupted request not passed to message handler", handled.get(), is(equalTo(0)));
		Object response = channel.readOutbound();
		assertThat("No response sent for corrupted request", response, is(nullValue()));
	}

	@Test
	public void receive_invalidCrc_followingRequestHandled() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		final AtomicInteger handled = new AtomicInteger();
		server.setMessageHandler((msg, sender) -> {
			handled.incrementAndGet();
			inputMessageHandler().accept(msg, sender);
		});

		final byte[] bad = requestFrame(1, RegistersModbusMessage.readInputsRequest(1, 2, 3));
		bad[bad.length - 1] ^= 0x01;

		// WHEN
		server.start();
		// junk follows the request with the invalid CRC
		channel.writeInbound(readBuffer(bad, new byte[] { 0x01, 0x04 }));
		Object none = channel.readOutbound();
		assertThat("No response sent for request with invalid CRC", none, is(nullValue()));

		for ( int i = 1; i <= 3; i++ ) {
			channel.writeInbound(
					readBuffer(requestFrame(1, RegistersModbusMessage.readInputsRequest(1, 10 * i, 2))));

			// THEN
			assertReadInputsResponse("Response " + i, 1, 10 * i, new short[] { 0, 1 });
		}
		assertThat("Only valid requests passed to message handler", handled.get(), is(equalTo(3)));
	}

	@Test
	public void receive_broadcast() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		final List<ModbusMessage> handled = new ArrayList<>(2);
		server.setMessageHandler((msg, sender) -> {
			handled.add(msg);
			RegistersModbusMessage req = msg.unwrap(RegistersModbusMessage.class);
			if ( req.getFunction().isReadFunction() ) {
				sender.accept(readInputsResponse(req.getUnitId(), req.getAddress(),
						new short[req.getCount()]));
			} else {
				sender.accept(RegistersModbusMessage.writeHoldingResponse(req.getUnitId(),
						req.getAddress(), req.dataDecodeUnsigned()[0]));
			}
		});

		// WHEN
		server.start();
		channel.writeInbound(
				readBuffer(requestFrame(0, RegistersModbusMessage.writeHoldingRequest(0, 100, 9))));

		// THEN
		assertThat("Broadcast request passed to message handler", handled, hasSize(1));
		assertThat("Broadcast request unit ID", handled.get(0).getUnitId(), is(equalTo(0)));
		assertThat("Broadcast request is a write",
				handled.get(0).unwrap(RegistersModbusMessage.class).getAddress(), is(equalTo(100)));
		Object response = channel.readOutbound();
		assertThat("No response sent for broadcast request", response, is(nullValue()));

		// the same request addressed to a unit is responded to
		channel.writeInbound(
				readBuffer(requestFrame(1, RegistersModbusMessage.writeHoldingRequest(1, 100, 9))));
		assertThat("Addressed request passed to message handler", handled, hasSize(2));
		final ByteBuf out = channel.readOutbound();
		assertThat("Response sent for addressed request", out, is(notNullValue()));
		final ByteBuf expected = Unpooled.buffer();
		new RtuModbusMessage(1, RegistersModbusMessage.writeHoldingResponse(1, 100, 9))
				.encodeModbusPayload(expected);
		assertThat("Response encoded", byteObjectArray(ByteBufUtil.getBytes(out)),
				arrayContaining(byteObjectArray(ByteBufUtil.getBytes(expected))));
	}

	@Test
	public void receive_unitZero_read() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		server.setMessageHandler(inputMessageHandler());

		// WHEN
		server.start();
		channel.writeInbound(
				readBuffer(requestFrame(0, RegistersModbusMessage.readInputsRequest(0, 2, 3))));

		// THEN
		// a read is not a broadcast, so is responded to
		assertReadInputsResponse("Response", 0, 2, new short[] { 0, 1, 2 });
	}

	@Test
	public void exceptionHandler_broadcast_unsupportedFunction() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		server.setExceptionHandler((ex, sender) -> {
			exception.set(ex);
			ModbusUnsupportedFunctionException ufe = (ModbusUnsupportedFunctionException) ex;
			sender.accept(new BaseModbusMessage(ufe.getUnitId(), ufe.getCode(),
					ModbusErrorCode.IllegalFunction.getCode()));
		});

		// a write function, the decoder does not support, sent as a broadcast
		final int unitId = 0;
		ByteBuf buf = Unpooled.buffer();
		new RtuModbusMessage(unitId,
				new BaseModbusMessage(unitId, ModbusFunctionCodes.WRITE_FILE_RECORD))
						.encodeModbusPayload(buf);

		// WHEN
		server.start();
		channel.writeInbound(buf);

		// THEN
		assertThat("Exception handler invoked", exception.get(),
				is(instanceOf(ModbusUnsupportedFunctionException.class)));
		Object response = channel.readOutbound();
		assertThat("No error response sent for broadcast request", response, is(nullValue()));
	}

	@Test
	public void accessors() {
		// GIVEN
		final BasicSerialParameters params = new BasicSerialParameters();
		final TestSerialPortProvider provider = new TestSerialPortProvider(null);

		// WHEN
		NettyRtuModbusServer s = new NettyRtuModbusServer("COM1", params, provider);
		try {
			// THEN
			assertThat("Device", s.getDevice(), is(equalTo("COM1")));
			assertThat("Serial parameters", s.getSerialParameters(), is(sameInstance(params)));
			assertThat("Serial port provider", s.getSerialPortProvider(), is(sameInstance(provider)));
			assertThat("Wire logging off by default", s.isWireLogging(), is(equalTo(false)));
			assertThat("No message handler by default", s.getMessageHandler(), is(nullValue()));
			assertThat("No exception handler by default", s.getExceptionHandler(), is(nullValue()));
			assertThat("No connection listener by default", s.getClientConnectionListener(),
					is(nullValue()));

			s.setWireLogging(true);
			assertThat("Wire logging configured", s.isWireLogging(), is(equalTo(true)));
		} finally {
			s.stop();
		}
	}

	@Test
	public void start_twice_started() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));

		// WHEN
		server.start();
		server.start();

		// THEN
		assertThat("Serial port open", port.isOpen(), is(equalTo(true)));
		assertThat("Serial port opened once", port.openCount.get(), is(equalTo(1)));
	}

	@Test
	public void start_wireLogging() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));
		server.setWireLogging(true);

		// WHEN
		server.start();

		// THEN
		assertThat("Serial port open", port.isOpen(), is(equalTo(true)));
	}

	@Test
	public void start_openFails_ioException() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		final IOException t = new IOException("Port not available.");
		port.openException = t;
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));

		// WHEN
		IOException e = assertThrows(IOException.class, () -> {
			server.start();
		}, "Start fails when serial port cannot be opened");

		// THEN
		assertThat("IOException from serial port thrown", e, is(sameInstance(t)));
		assertThat("Serial port not open", port.isOpen(), is(equalTo(false)));

		// can start once the serial port is available
		port.openException = null;
		server.start();
		assertThat("Serial port open", port.isOpen(), is(equalTo(true)));
	}

	@Test
	public void start_openFails_runtimeException() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		final IllegalStateException t = new IllegalStateException("Port not available.");
		port.openException = t;
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));

		// WHEN
		RuntimeException e = assertThrows(RuntimeException.class, () -> {
			server.start();
		}, "Start fails when serial port cannot be opened");

		// THEN
		assertThat("Exception from serial port is cause", e.getCause(), is(sameInstance(t)));
		assertThat("Serial port not open", port.isOpen(), is(equalTo(false)));
	}

	@Test
	public void start_externalEventLoopGroupStopped() throws Exception {
		// GIVEN
		final io.netty.channel.EventLoopGroup group = net.solarnetwork.io.modbus.netty.channel.LocalIoEventLoopGroupFactory.INSTANCE
				.apply(null, false);
		group.shutdownGracefully(0, 1, java.util.concurrent.TimeUnit.SECONDS).sync();
		final SimulatedSerialPort port = new SimulatedSerialPort();
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port), group);

		// WHEN
		assertThrows(IOException.class, () -> {
			server.start();
		}, "Start fails when external event loop group has been stopped");

		// THEN
		assertThat("Serial port not opened", port.openCount.get(), is(equalTo(0)));
	}

	@Test
	public void connectionListener() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		final List<String> events = java.util.Collections.synchronizedList(new ArrayList<>(2));
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));
		server.setClientConnectionListener((device, connected) -> {
			events.add(device + "=" + connected);
			return true;
		});

		// WHEN
		server.start();
		assertThat("Serial port open after being accepted", port.isOpen(), is(equalTo(true)));
		server.stop();

		// the disconnection is reported from the event loop, which can be after stop() returns
		final long end = System.currentTimeMillis() + 5000;
		while ( events.size() < 2 && System.currentTimeMillis() < end ) {
			Thread.sleep(20);
		}

		// THEN
		assertThat("Listener told of connection and disconnection", events, hasSize(2));
		assertThat("Listener told of connection", events.get(0), is(equalTo("COM1=true")));
		assertThat("Listener told of disconnection", events.get(1), is(equalTo("COM1=false")));
	}

	@Test
	public void connectionListener_noResult() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));
		server.setClientConnectionListener((device, connected) -> null);

		// WHEN
		server.start();

		// THEN
		assertThat("Serial port open when listener has no opinion", port.isOpen(), is(equalTo(true)));
	}

	@Test
	public void connectionListener_deny() throws Exception {
		// GIVEN
		final SimulatedSerialPort port = new SimulatedSerialPort();
		final List<String> events = java.util.Collections.synchronizedList(new ArrayList<>(2));
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port));
		server.setClientConnectionListener((device, connected) -> {
			events.add(device + "=" + connected);
			return false;
		});

		// WHEN
		server.start();
		port.awaitClosed();

		// THEN
		assertThat("Serial port closed after being denied", port.isOpen(), is(equalTo(false)));
		assertThat("Listener told of connection", events.get(0), is(equalTo("COM1=true")));
	}

	@Test
	public void receive_noMessageHandler() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);

		// WHEN
		server.start();
		channel.writeInbound(
				readBuffer(requestFrame(1, RegistersModbusMessage.readInputsRequest(1, 2, 3))));

		// THEN
		Object response = channel.readOutbound();
		assertThat("No response sent without a message handler", response, is(nullValue()));
	}

	@Test
	public void exceptionHandler_otherException() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		server.setExceptionHandler((ex, sender) -> {
			exception.set(ex);
			sender.accept(new BaseModbusMessage(1, ModbusFunctionCodes.READ_INPUT_REGISTERS,
					ModbusErrorCode.ServerDeviceFailure.getCode()));
		});
		final IllegalStateException t = new IllegalStateException("Something else.");

		// WHEN
		server.start();
		channel.pipeline().fireExceptionCaught(t);

		// THEN
		assertThat("Exception handler given the exception", exception.get(), is(sameInstance(t)));
		final ByteBuf out = channel.readOutbound();
		assertThat("Response from exception handler written", out, is(notNullValue()));
		final ByteBuf expected = Unpooled.buffer();
		new RtuModbusMessage(1, new BaseModbusMessage(1, ModbusFunctionCodes.READ_INPUT_REGISTERS,
				ModbusErrorCode.ServerDeviceFailure.getCode())).encodeModbusPayload(expected);
		assertThat("Response encoded", byteObjectArray(ByteBufUtil.getBytes(out)),
				arrayContaining(byteObjectArray(ByteBufUtil.getBytes(expected))));
	}

	@Test
	public void exceptionHandler_decoderExceptionWithoutCause() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		server.setExceptionHandler((ex, sender) -> exception.set(ex));
		final io.netty.handler.codec.DecoderException t = new io.netty.handler.codec.DecoderException(
				"Bad data.");

		// WHEN
		server.start();
		channel.pipeline().fireExceptionCaught(t);

		// THEN
		assertThat("Exception handler given the decoder exception", exception.get(),
				is(sameInstance(t)));
	}

	@Test
	public void exception_noExceptionHandler() throws Exception {
		// GIVEN
		server = new TestRtuNettyModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(null), channel);

		// WHEN
		server.start();
		channel.pipeline().fireExceptionCaught(new IllegalStateException("Something else."));

		// THEN
		Object response = channel.readOutbound();
		assertThat("Nothing sent without an exception handler", response, is(nullValue()));
		channel.checkException();
	}

	@Test
	public void start_externalEventLoopGroup() throws Exception {
		// GIVEN
		final io.netty.channel.EventLoopGroup group = net.solarnetwork.io.modbus.netty.channel.LocalIoEventLoopGroupFactory.INSTANCE
				.apply(null, false);
		final SimulatedSerialPort port = new SimulatedSerialPort();
		server = new NettyRtuModbusServer("COM1", new BasicSerialParameters(),
				new TestSerialPortProvider(port), group);
		try {
			// WHEN
			server.start();
			assertThat("Serial port opened", port.isOpen(), is(equalTo(true)));

			// serial port fails, which closes the connection
			port.disconnected = true;
			port.awaitClosed();
			assertThat("External group left running after connection closed", group.isShuttingDown(),
					is(equalTo(false)));

			// THEN
			server.start();
			assertThat("Serial port opened again using external group", port.isOpen(),
					is(equalTo(true)));
			assertThat("Serial port opened twice", port.openCount.get(), is(equalTo(2)));

			server.stop();
			assertThat("Serial port closed", port.isOpen(), is(equalTo(false)));
			assertThat("External group left running after stop", group.isShuttingDown(),
					is(equalTo(false)));
		} finally {
			group.shutdownGracefully();
		}
	}

}
