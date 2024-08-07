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

import static net.solarnetwork.io.modbus.rtu.RtuModbusMessage.CRC_MISMATCH_VALIDATION_MESSAGE;
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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.RtuModbusClientConfig;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusClientConfig;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.RtuNettyModbusClient;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * Test cases for the {@link RtuNettyModbusClient} class.
 *
 * @author matt
 * @version 1.0
 */
public class RtuNettyModbusClientTests {

	private static final class TestRtuNettyModbusClient extends RtuNettyModbusClient {

		private final EmbeddedChannel channel;

		private TestRtuNettyModbusClient(RtuModbusClientConfig clientConfig, EmbeddedChannel channel,
				ConcurrentMap<ModbusMessage, PendingMessage> pending,
				SerialPortProvider serialPortProvider) {
			super(clientConfig, null, pending, channel.eventLoop(), serialPortProvider);
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
	private EmbeddedChannel channel;
	private RtuNettyModbusClient client;

	@BeforeEach
	public void setup() {
		pending = new ConcurrentHashMap<>(8, 0.9f, 2);
		channel = new EmbeddedChannel();
	}

	@AfterEach
	public void teardown() {
		if ( client != null ) {
			client.stop();
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

	@Test
	public void construct_defaults() {
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		RtuNettyModbusClient c = new RtuNettyModbusClient(config, new TestSerialPortProvider(null));

		assertThat("Provided client config returned", c.getClientConfig(), is(sameInstance(config)));
	}

	@Test
	public void construct_partial_defaults() {
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		RtuNettyModbusClient c = new RtuNettyModbusClient(config, null, null,
				new TestSerialPortProvider(null));

		assertThat("Provided client config returned", c.getClientConfig(), is(sameInstance(config)));
	}

	@Test
	public void construct_nulls() {
		assertThrows(IllegalArgumentException.class, () -> {
			new RtuNettyModbusClient(new NettyRtuModbusClientConfig("COM1", new BasicSerialParameters()),
					null);
		}, "Null serialPortProvider not allowed");
	}

	@Test
	public void start_nullDeviceName() {
		final RtuNettyModbusClient c = new RtuNettyModbusClient(
				new NettyRtuModbusClientConfig(null, new BasicSerialParameters()), null, pending,
				channel.eventLoop(), new TestSerialPortProvider(null));
		try {
			ExecutionException e = assertThrows(ExecutionException.class, () -> {
				c.start().get();
			}, "Null device name throws exception");
			assertThat("Null device name throws IllegalArgumentException", e.getCause(),
					is(instanceOf(IllegalArgumentException.class)));
		} finally {
			if ( c != null ) {
				c.stop();
			}
		}
	}

	@Test
	public void start_emptyDeviceName() {
		final RtuNettyModbusClient c = new RtuNettyModbusClient(
				new NettyRtuModbusClientConfig("", new BasicSerialParameters()), null, pending,
				channel.eventLoop(), new TestSerialPortProvider(null));
		try {
			ExecutionException e = assertThrows(ExecutionException.class, () -> {
				c.start().get();
			}, "Empty host throws exception");
			assertThat("Empty host throws IllegalArgumentException", e.getCause(),
					is(instanceOf(IllegalArgumentException.class)));
		} finally {
			if ( c != null ) {
				c.stop();
			}
		}
	}

	@Test
	public void start_twice() throws Exception {
		// GIVEN
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		client = new TestRtuNettyModbusClient(config, channel, pending,
				new TestSerialPortProvider(null));

		// WHEN
		client.start().get();
		client.start().get(); // should not cause exception
	}

	@Test
	public void send() throws Exception {
		// GIVEN
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		client = new TestRtuNettyModbusClient(config, channel, pending,
				new TestSerialPortProvider(null));

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

		final short crc = RtuModbusMessage.computeCrc(unitId, req);
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
						(byte)(crc & 0xFF),
						(byte)(crc >>> 8 & 0xFF),
				})));
		// @formatter:on
	}

	@Test
	public void responseTimeout() throws Exception {
		// GIVEN
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		client = new TestRtuNettyModbusClient(config, channel, pending,
				new TestSerialPortProvider(null));

		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.setPendingMessageTtl(200);
		client.start().get();
		Future<ModbusMessage> f = client.sendAsync(req);

		// sleep to wait for timeout and cleaner task to execute
		Thread.sleep(1000);

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Pending request should have been expunged by cleanup task", pending.keySet(),
				hasSize(0));

		ByteBuf buf = channel.readOutbound();
		assertThat("Bytes produced", buf, is(notNullValue()));

		final short crc = RtuModbusMessage.computeCrc(unitId, req);
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
						(byte)(crc & 0xFF),
						(byte)(crc >>> 8 & 0xFF),
				})));
		// @formatter:on
	}

	@Test
	public void send_recv() throws InterruptedException, ExecutionException {
		// GIVEN
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		client = new TestRtuNettyModbusClient(config, channel, pending,
				new TestSerialPortProvider(null));

		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start();
		Future<ModbusMessage> f = client.sendAsync(req);

		// provide response
		// @formatter:off
		final short resCrc = RtuModbusMessage.computeCrc(unitId,
				RegistersModbusMessage.readHoldingsResponse(unitId, addr, new short[] { 
						(short) 0xFFFE,
						(short) 0xFDFC,
						(short) 0xFBFA,
		}));
		final byte[] responseData = new byte[] {
				(byte)(unitId & 0xFF),
				ModbusFunctionCodes.READ_HOLDING_REGISTERS,
				(byte)0x06,
				(byte)0xFF,
				(byte)0xFE,
				(byte)0xFD,
				(byte)0xFC,
				(byte)0xFB,
				(byte)0xFA,
				(byte)(resCrc & 0xFF),
				(byte)(resCrc >>> 8 & 0xFF),
		};
		ByteBuf response = Unpooled.copiedBuffer(responseData);
		// @formatter:on
		channel.writeOneInbound(response).sync();

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should no longer be pending", pending.keySet(), hasSize(0));

		ByteBuf requestData = channel.readOutbound();
		assertThat("Request bytes produced", requestData, is(notNullValue()));

		final short crc = RtuModbusMessage.computeCrc(unitId, req);
		// @formatter:off
		assertThat("Request message encoded", byteObjectArray(ByteBufUtil.getBytes(requestData)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
						(byte)(crc & 0xFF),
						(byte)(crc >>> 8 & 0xFF),
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
	public void send_recv_invalidCrc() throws InterruptedException, ExecutionException {
		// GIVEN
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		client = new TestRtuNettyModbusClient(config, channel, pending,
				new TestSerialPortProvider(null));

		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start();
		Future<ModbusMessage> f = client.sendAsync(req);

		// provide response
		// @formatter:off
		final short expectedCrc = RtuModbusMessage.computeCrc(unitId,
				RegistersModbusMessage.readHoldingsResponse(unitId, addr, new short[] { 
						(short) 0xFFFE,
						(short) 0xFDFC,
						(short) 0xFBFA,
		}));
		final byte[] responseData = new byte[] {
				(byte)(unitId & 0xFF),
				ModbusFunctionCodes.READ_HOLDING_REGISTERS,
				(byte)0x06,
				(byte)0xFF,
				(byte)0xFE,
				(byte)0xFD,
				(byte)0xFC,
				(byte)0xFB,
				(byte)0xFA,
				(byte)0xCD, // BAD
				(byte)0XAB, // BAD
		};
		ByteBuf response = Unpooled.copiedBuffer(responseData);
		// @formatter:on
		channel.writeOneInbound(response).sync();

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should no longer be pending", pending.keySet(), hasSize(0));

		ByteBuf requestData = channel.readOutbound();
		assertThat("Request bytes produced", requestData, is(notNullValue()));

		final short crc = RtuModbusMessage.computeCrc(unitId, req);
		// @formatter:off
		assertThat("Request message encoded", byteObjectArray(ByteBufUtil.getBytes(requestData)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
						(byte)(crc & 0xFF),
						(byte)(crc >>> 8 & 0xFF),
				})));
		// @formatter:on

		assertThat("Response has been received and processed", f.isDone(), is(equalTo(true)));
		ModbusMessage resp = f.get();
		assertThat("Response is not an error", resp.getError(), is(nullValue()));
		net.solarnetwork.io.modbus.RegistersModbusMessage respReg = resp
				.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
		assertThat("Response is Registers", respReg, is(notNullValue()));

		ModbusValidationException ex = assertThrows(ModbusValidationException.class, () -> {
			resp.validate();
		});
		assertThat("Exception message", ex.getMessage(),
				is(equalTo(String.format(CRC_MISMATCH_VALIDATION_MESSAGE, 0xABCD, expectedCrc))));
	}

	@Test
	public void send_recvError() throws InterruptedException, ExecutionException {
		// GIVEN
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		client = new TestRtuNettyModbusClient(config, channel, pending,
				new TestSerialPortProvider(null));

		final int unitId = 1;
		final int addr = 2;
		final int count = 3;
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(unitId, addr, count);

		// WHEN
		client.start();
		Future<ModbusMessage> f = client.sendAsync(req);

		// provide response
		final short resCrc = RtuModbusMessage.computeCrc(unitId, new BaseModbusMessage(unitId,
				ModbusFunctionCode.ReadHoldingRegisters, ModbusErrorCode.IllegalDataAddress));
		// @formatter:off
		final byte[] responseData = new byte[] {
				(byte)(unitId & 0xFF),
				ModbusFunctionCodes.READ_HOLDING_REGISTERS + ModbusFunctionCodes.ERROR_OFFSET,
				ModbusErrorCodes.ILLEGAL_DATA_ADDRESS,
				(byte)(resCrc & 0xFF),
				(byte)(resCrc >>> 8 & 0xFF),
		};
		ByteBuf response = Unpooled.copiedBuffer(responseData);
		// @formatter:on
		channel.writeOneInbound(response).sync();

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should no longer be pending", pending.keySet(), hasSize(0));

		ByteBuf requestData = channel.readOutbound();
		assertThat("Request bytes produced", requestData, is(notNullValue()));

		final short crc = RtuModbusMessage.computeCrc(unitId, req);
		// @formatter:off
		assertThat("Request message encoded", byteObjectArray(ByteBufUtil.getBytes(requestData)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
						(byte)(crc & 0xFF),
						(byte)(crc >>> 8 & 0xFF),
				})));
		// @formatter:on

		assertThat("Response has been received and processed", f.isDone(), is(equalTo(true)));
		ModbusMessage resp = f.get();
		assertThat("Response is an error", resp.getError(), is(ModbusErrorCode.IllegalDataAddress));
	}

	@Test
	public void send_recvJunk() throws InterruptedException, ExecutionException {
		// GIVEN
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("COM1",
				new BasicSerialParameters());
		client = new TestRtuNettyModbusClient(config, channel, pending,
				new TestSerialPortProvider(null));

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
				(byte)0x00,
				(byte)0x65,
				(byte)0x02,
				(byte)0x03,
				(byte)0x04,
				(byte)0x05,
				(byte)0x06,
				(byte)0x07,
		};
		ByteBuf response = Unpooled.copiedBuffer(responseData);
		// @formatter:on
		channel.writeOneInbound(response).sync();

		// THEN
		assertThat("Future returned", f, is(notNullValue()));
		assertThat("Request should still be pending", pending.keySet(), hasSize(1));

		ByteBuf requestData = channel.readOutbound();
		assertThat("Request bytes produced", requestData, is(notNullValue()));

		final short crc = RtuModbusMessage.computeCrc(unitId, req);
		// @formatter:off
		assertThat("Request message encoded", byteObjectArray(ByteBufUtil.getBytes(requestData)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)(unitId & 0xFF),
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)(addr >>> 8 & 0xFF),
						(byte)(addr & 0xFF),
						(byte)(count >>> 8 & 0xFF),
						(byte)(count & 0xFF),
						(byte)(crc & 0xFF),
						(byte)(crc >>> 8 & 0xFF),
				})));
		// @formatter:on

		assertThat("Response has been received and processed", f.isDone(), is(equalTo(false)));
	}

}
