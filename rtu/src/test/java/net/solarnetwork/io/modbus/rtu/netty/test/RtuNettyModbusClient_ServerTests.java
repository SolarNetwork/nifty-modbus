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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
 * @version 1.0
 */
public class RtuNettyModbusClient_ServerTests {

	private ConcurrentMap<ModbusMessage, PendingMessage> pending;
	private NettyRtuModbusClientConfig config;
	private RtuNettyModbusClient client;
	private SerialPort serialPort;
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

}
