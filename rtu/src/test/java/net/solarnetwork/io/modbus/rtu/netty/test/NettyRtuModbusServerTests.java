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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
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

		@Override
		public String getName() {
			return "Test Port";
		}

		@Override
		public void open(SerialParameters parameters) throws IOException {
			openCount.incrementAndGet();
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
		new RtuModbusMessage(unitId, new BaseModbusMessage(unitId, ModbusFunctionCodes.GET_COMM_EVENT_LOG))
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

}
