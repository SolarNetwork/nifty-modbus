/* ==================================================================
 * SerialPortChannelTests.java - 6/12/2022 12:32:02 pm
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

package net.solarnetwork.io.modbus.serial.test;

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import net.solarnetwork.io.modbus.serial.SerialAddress;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortChannel;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * Test cases for the {@link SerialPortChannel} class.
 *
 * @author matt
 * @version 1.0
 */
public class SerialPortChannelTests {

	private ExecutorService executor;
	private ByteArrayOutputStream out;
	private PipedOutputStream pout = new PipedOutputStream();
	private PipedInputStream in;

	@BeforeEach
	public void setup() throws Exception {
		executor = Executors.newCachedThreadPool();
		out = new ByteArrayOutputStream();
		pout = new PipedOutputStream();
		in = new PipedInputStream(pout);
	}

	@AfterEach
	public void teardown() {
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

	private SerialPortProvider provider(SerialPort port) {
		return new SerialPortProvider() {

			@Override
			public SerialPort getSerialPort(String name) {
				return port;
			}
		};
	}

	@Test
	public void construct() {
		// GIVEN
		SerialPortChannel ch = new SerialPortChannel(provider(null));

		assertThat("Channel created", ch, is(notNullValue()));
		assertThat("Starts inactive", ch.isActive(), is(equalTo(false)));
		assertThat("Starts open", ch.isOpen(), is(equalTo(true)));
	}

	@Test
	public void construct_null() {
		assertThrows(IllegalArgumentException.class, () -> {
			new SerialPortChannel(null);
			;
		}, "Null argument is not alowed");
	}

	@Test
	public void config() {
		// GIVEN
		SerialPortChannel ch = new SerialPortChannel(provider(null));

		// THEN
		assertThat("Config available", ch.config(), is(notNullValue()));
	}

	@Test
	public void metadata() {
		// GIVEN
		SerialPortChannel ch = new SerialPortChannel(provider(null));

		// THEN
		assertThat("Metadata available", ch.metadata(), is(notNullValue()));
		assertThat("Metadata disconnect", ch.metadata().hasDisconnect(), is(equalTo(true)));
	}

	@Test
	public void localAddress() {
		SerialPortChannel ch = new SerialPortChannel(provider(null));

		// THEN
		assertThat("Local address available", ch.localAddress(), is(notNullValue()));
		assertThat("Local address name", ch.localAddress().name(), is("localhost"));
	}

	private SerialPort simulatedSerialPort(CountDownLatch writeLatch) {
		return simulatedSerialPort(writeLatch, null, null, null);
	}

	private SerialPort simulatedSerialPort(CountDownLatch writeLatch,
			Supplier<IOException> inCloseException, Supplier<IOException> outCloseException,
			Supplier<IOException> closeException) {
		return simulatedSerialPort(writeLatch, inCloseException, outCloseException, closeException, null,
				null);
	}

	private SerialPort simulatedSerialPort(CountDownLatch writeLatch,
			Supplier<IOException> inCloseException, Supplier<IOException> outCloseException,
			Supplier<IOException> closeException, Supplier<IOException> availException,
			Supplier<IOException> readException) {
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
						writeLatch.countDown();
						out.write(b);
					}

					@Override
					public void close() throws IOException {
						IOException e = (outCloseException != null ? outCloseException.get() : null);
						if ( e != null ) {
							throw e;
						}
					}

				};
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new InputStream() {

					@Override
					public int available() throws IOException {
						IOException e = (availException != null ? availException.get() : null);
						if ( e != null ) {
							throw e;
						}
						return in.available();
					}

					@Override
					public int read() throws IOException {
						IOException e = (readException != null ? readException.get() : null);
						if ( e != null ) {
							throw e;
						}
						if ( in.available() < 1 ) {
							return -1;
						}
						return in.read();
					}

					@Override
					public void close() throws IOException {
						IOException e = (inCloseException != null ? inCloseException.get() : null);
						if ( e != null ) {
							throw e;
						}
					}

				};
			}

			@Override
			public void close() throws IOException {
				open = false;
				IOException e = (closeException != null ? closeException.get() : null);
				if ( e != null ) {
					throw e;
				}
			}
		};
	}

	@Test
	public void bind() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();

			// THEN

			assertThrows(UnsupportedOperationException.class, () -> {
				ch.bind(remote).sync();
			}, "Cannot bind to serial channel");
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void connect() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();

			// WHEN

			ChannelFuture connectFuture = ch.connect(remote);

			// THEN
			assertThat("Connect future provided", connectFuture, is(notNullValue()));
			connectFuture.sync();
			assertThat("Channel is active", ch.isActive(), is(equalTo(true)));
			assertThat("Remote address matches", ch.remoteAddress(), is(sameInstance(remote)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void disconnect() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ChannelFuture disconnectFuture = ch.disconnect();

			// THEN
			assertThat("Disconnect future provided", disconnectFuture, is(notNullValue()));
			disconnectFuture.sync();
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
			assertThat("Channel is still open", ch.isOpen(), is(equalTo(true)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void close() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ChannelFuture close = ch.close();

			// THEN
			assertThat("Close future provided", close, is(notNullValue()));
			close.sync();
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
			assertThat("Channel is no longer open", ch.isOpen(), is(equalTo(false)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void close_withOutput() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(1);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ch.writeAndFlush(Unpooled.wrappedBuffer(new byte[] { 1 }));
			writeLatch.await(2L, TimeUnit.SECONDS);
			ChannelFuture close = ch.close();

			// THEN
			assertThat("Close future provided", close, is(notNullValue()));
			close.sync();
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
			assertThat("Channel is no longer open", ch.isOpen(), is(equalTo(false)));
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void close_exception_inputStream() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, () -> {
					thrown.set(true);
					return new IOException();
				}, null, null)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			Thread.sleep(20);
			ChannelFuture close = ch.close();

			// THEN
			assertThat("Close future provided", close, is(notNullValue()));
			close.sync();
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
			assertThat("Channel is no longer open", ch.isOpen(), is(equalTo(false)));
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Stream close threw exception", thrown.get(), is(equalTo(true)));
	}

	@Test
	public void close_exception_outputStream() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(1);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, null, () -> {
					thrown.set(true);
					return new IOException();
				}, null)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			// have to write something to open output stream
			ch.writeAndFlush(Unpooled.wrappedBuffer(new byte[] { 1 }));
			writeLatch.await(2L, TimeUnit.SECONDS);
			ChannelFuture close = ch.close();

			// THEN
			assertThat("Close future provided", close, is(notNullValue()));
			close.sync();
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
			assertThat("Channel is no longer open", ch.isOpen(), is(equalTo(false)));
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Stream close threw exception", thrown.get(), is(equalTo(true)));
	}

	@Test
	public void close_exception_port() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, null, null, () -> {
					thrown.set(true);
					return new IOException();
				})));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ChannelFuture close = ch.close();

			// THEN
			assertThat("Close future provided", close, is(notNullValue()));
			assertThrows(IOException.class, () -> {
				close.sync();
			}, "Close port throws exception");
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Stream close threw exception", thrown.get(), is(equalTo(true)));
	}

	@Test
	public void write_unsupported() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(1);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			assertThrows(UnsupportedOperationException.class, () -> {
				ch.writeAndFlush("can't write this").sync();
			}, "Can only write ByteBuf");

			// THEN
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void write() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(1);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ch.writeAndFlush(Unpooled.wrappedBuffer(new byte[] { 1 }));
			writeLatch.await(2L, TimeUnit.SECONDS);
			ChannelFuture close = ch.close();

			// THEN
			assertThat("Close future provided", close, is(notNullValue()));
			close.sync();
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
			assertThat("Channel is no longer open", ch.isOpen(), is(equalTo(false)));
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Output written", byteObjectArray(out.toByteArray()),
				is(equalTo(byteObjectArray(new byte[] { 1 }))));
	}

	@Test
	public void write_multi() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(8);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ch.writeAndFlush(Unpooled.wrappedBuffer(new byte[] { 1, 2, 3, 4 }));
			Thread.sleep(20);
			ch.writeAndFlush(Unpooled.wrappedBuffer(new byte[] { 5, 6, 7, 8 }));
			writeLatch.await(2L, TimeUnit.SECONDS);

		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}

		// THEN
		assertThat("Output written", byteObjectArray(out.toByteArray()),
				is(equalTo(byteObjectArray(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }))));
	}

	@Test
	public void read() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(4);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		final CountDownLatch readLatch = new CountDownLatch(5);
		final ByteArrayOutputStream read = new ByteArrayOutputStream();
		ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {

			@Override
			protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
				int len = msg.readableBytes();
				msg.readBytes(read, len);
				for ( int i = 0; i < len; i++ ) {
					readLatch.countDown();
				}
			}
		});

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ch.writeAndFlush(Unpooled.wrappedBuffer(new byte[] { 1, 2, 3, 4 }));

			// provide read data
			executor.execute(() -> {
				try {
					writeLatch.await(5, TimeUnit.SECONDS);
				} catch ( InterruptedException e ) {
					// ignore;
				}

				try {
					ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 4, 3, 2, 1, 0 });
					pout.write(buf.array());
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
			});

			readLatch.await(2, TimeUnit.SECONDS);

		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		// THEN
		assertThat("Output written", byteObjectArray(out.toByteArray()),
				is(equalTo(byteObjectArray(new byte[] { 1, 2, 3, 4 }))));
		assertThat("Input read", byteObjectArray(read.toByteArray()),
				is(equalTo(byteObjectArray(new byte[] { 4, 3, 2, 1, 0 }))));
	}

	@Test
	public void read_availableThrowsException() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(4);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, null, null, null, () -> {
					thrown.set(true);
					return new IOException();
				}, null)));

		final CountDownLatch readLatch = new CountDownLatch(5);
		final ByteArrayOutputStream read = new ByteArrayOutputStream();
		ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {

			@Override
			protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
				int len = msg.readableBytes();
				msg.readBytes(read, len);
				for ( int i = 0; i < len; i++ ) {
					readLatch.countDown();
				}
			}
		});

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// WHEN
			ch.read();
			Thread.sleep(200);
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		// THEN
		assertThat("Available threw exception", thrown.get(), is(equalTo(true)));
	}

	@Test
	public void read_throwsException() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(4);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, null, null, null, null, () -> {
					thrown.set(true);
					return new IOException();
				})));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// provide read data
			executor.execute(() -> {
				try {
					ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 4, 3, 2, 1, 0 });
					pout.write(buf.array());
				} catch ( IOException e ) {
					throw new RuntimeException(e);
				}
			});

			// WHEN
			ch.read();
			Thread.sleep(200);
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		// THEN
		assertThat("Read threw exception", thrown.get(), is(equalTo(true)));
	}

}
