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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusMessage;
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
 * @version 1.0
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

	private EmbeddedChannel channel;
	private NettyRtuModbusServer server;

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

}
