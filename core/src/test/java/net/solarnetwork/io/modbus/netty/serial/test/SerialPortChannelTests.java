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

package net.solarnetwork.io.modbus.netty.serial.test;

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import net.solarnetwork.io.modbus.netty.serial.SerialAddress;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannel;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * Test cases for the {@link SerialPortChannel} class.
 *
 * @author matt
 * @version 1.1
 */
public class SerialPortChannelTests {

	private ExecutorService executor;
	private ByteArrayOutputStream out;
	private PipedOutputStream pout = new PipedOutputStream();
	private PipedInputStream in;

	/** How long the simulated serial port blocks waiting for data. */
	private long simulatedReadTimeout;

	/** Flag to simulate the serial port device going away. */
	private AtomicBoolean disconnected;

	private AtomicInteger availableCount;
	private AtomicInteger blockingReadCount;

	@BeforeEach
	public void setup() throws Exception {
		executor = Executors.newCachedThreadPool();
		out = new ByteArrayOutputStream();
		pout = new PipedOutputStream();
		in = new PipedInputStream(pout);
		simulatedReadTimeout = 50;
		disconnected = new AtomicBoolean(false);
		availableCount = new AtomicInteger();
		blockingReadCount = new AtomicInteger();
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
		}, "Null argument is not allowed");
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
		return simulatedSerialPort(writeLatch, null, inCloseException, outCloseException, closeException,
				null, null);
	}

	private SerialPort simulatedSerialPort(CountDownLatch writeLatch, Supplier<Exception> openException,
			Supplier<IOException> inCloseException, Supplier<IOException> outCloseException,
			Supplier<IOException> closeException, Supplier<IOException> availException,
			Supplier<Exception> readException) {
		return new SerialPort() {

			private boolean open = false;

			@Override
			public String getName() {
				return "Test Port";
			}

			@Override
			public void open(SerialParameters parameters) throws IOException {
				Exception e = (openException != null ? openException.get() : null);
				if ( e instanceof IOException ) {
					throw (IOException) e;
				} else if ( e instanceof RuntimeException ) {
					throw (RuntimeException) e;
				} else if ( e != null ) {
					throw new RuntimeException(e);
				}
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
						availableCount.incrementAndGet();
						IOException e = (availException != null ? availException.get() : null);
						if ( e != null ) {
							throw e;
						}
						if ( disconnected.get() ) {
							return -1;
						}
						return in.available();
					}

					private void throwReadException() throws IOException {
						Exception e = (readException != null ? readException.get() : null);
						if ( e instanceof IOException ) {
							throw (IOException) e;
						} else if ( e instanceof RuntimeException ) {
							throw (RuntimeException) e;
						} else if ( e != null ) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public int read() throws IOException {
						throwReadException();
						if ( in.available() < 1 ) {
							return -1;
						}
						return in.read();
					}

					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						throwReadException();
						if ( disconnected.get() ) {
							return -1;
						}
						if ( in.available() < 1 ) {
							// like a real serial port: block until the read timeout, then return 0
							blockingReadCount.incrementAndGet();
							final long end = System.currentTimeMillis() + simulatedReadTimeout;
							while ( in.available() < 1 && !disconnected.get() ) {
								long remaining = end - System.currentTimeMillis();
								if ( remaining <= 0 ) {
									return 0;
								}
								try {
									Thread.sleep(Math.min(remaining, 5));
								} catch ( InterruptedException e ) {
									throw new InterruptedIOException();
								}
							}
							if ( disconnected.get() ) {
								return -1;
							}
						}
						return in.read(b, off, Math.min(len, in.available()));
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
	public void connect_withWait() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));
		ch.config().setWaitTime(200);

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
				provider(simulatedSerialPort(writeLatch, null, null, null, null, () -> {
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

			// THEN
			assertThat("Channel closed because of read failure",
					ch.closeFuture().await(2, TimeUnit.SECONDS), is(equalTo(true)));
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Available threw exception", thrown.get(), is(equalTo(true)));
	}

	@Test
	public void read_throwsIOException() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(4);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, null, null, null, null, null, () -> {
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

			// THEN
			assertThat("Channel closed because of read failure",
					ch.closeFuture().await(2, TimeUnit.SECONDS), is(equalTo(true)));
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Read threw exception", thrown.get(), is(equalTo(true)));
	}

	@Test
	public void read_throwsException() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(4);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, null, null, null, null, null, () -> {
					thrown.set(true);
					return new RuntimeException();
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

			// THEN
			assertThat("Channel closed because of read failure",
					ch.closeFuture().await(2, TimeUnit.SECONDS), is(equalTo(true)));
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Read threw exception", thrown.get(), is(equalTo(true)));
	}

	@Test
	public void open_throwsException() throws Exception {
		// GIVEN
		final RuntimeException t = new RuntimeException();
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(4);
		final AtomicBoolean thrown = new AtomicBoolean(false);
		final SerialPortChannel ch = new SerialPortChannel(
				provider(simulatedSerialPort(writeLatch, () -> {
					return t;
				}, null, null, null, null, null)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		assertThrows(RuntimeException.class, () -> {
			try {
				eventLoopGroup.register(ch).sync();

				// WHEN
				ch.connect(remote).sync();
			} catch ( Exception e ) {
				if ( e == t ) {
					thrown.set(true);
				}
				throw e;
			} finally {
				ch.close().sync();
				eventLoopGroup.shutdownGracefully();
			}
		}, "Connect throws exception from channel.open() method.");
		// THEN
		assertThat("Open threw exception", thrown.get(), is((true)));
	}

	private static Thread readerThread(String portName) {
		for ( Thread t : Thread.getAllStackTraces().keySet() ) {
			if ( t.getName().equals("SerialPortChannel-Reader-" + portName) ) {
				return t;
			}
		}
		return null;
	}

	@Test
	public void connect_closedChannel() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.close().sync();

			// WHEN
			ChannelFuture connectFuture = ch.connect(remote);

			// THEN
			assertThat("Connect future completes", connectFuture.await(2, TimeUnit.SECONDS),
					is(equalTo(true)));
			assertThat("Connect failed", connectFuture.isSuccess(), is(equalTo(false)));
			assertThat("Connect failed because channel closed", connectFuture.cause(),
					is(instanceOf(ClosedChannelException.class)));
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void connect_inputStreamThrowsException() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final IOException t = new IOException("No stream for you.");
		final SerialPort delegate = simulatedSerialPort(new CountDownLatch(0));
		final SerialPort port = new SerialPort() {

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
				throw t;
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				return delegate.getOutputStream();
			}
		};
		final SerialPortChannel ch = new SerialPortChannel(provider(port));

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();

			// WHEN
			ChannelFuture connectFuture = ch.connect(remote);

			// THEN
			assertThat("Connect future completes", connectFuture.await(2, TimeUnit.SECONDS),
					is(equalTo(true)));
			assertThat("Connect failed with stream exception", connectFuture.cause(),
					is(sameInstance(t)));
			assertThat("Serial port closed again", port.isOpen(), is(equalTo(false)));
			assertThat("Channel is not active", ch.isActive(), is(equalTo(false)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void close_stopsReader() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM-close-stops-reader");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		// a read timeout far longer than the test, so reader is blocked when closed
		simulatedReadTimeout = 60_000;

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		Thread reader = null;
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();
			reader = readerThread(remote.name());
			assertThat("Reader thread started", reader, is(notNullValue()));
			assertThat("Reader thread is daemon", reader.isDaemon(), is(equalTo(true)));

			// WHEN
			ch.close().sync();

			// THEN
			reader.join(2000);
			assertThat("Reader thread stopped", reader.isAlive(), is(equalTo(false)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void write_notDelayedByRead() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(4);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		// a read timeout far longer than the test, so a read is blocked when writing
		simulatedReadTimeout = 60_000;

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();

			// wait for reader to be blocked waiting for data
			for ( int i = 0; i < 200 && blockingReadCount.get() < 1; i++ ) {
				Thread.sleep(10);
			}
			assertThat("Read is blocked waiting for data", blockingReadCount.get(), is(equalTo(1)));

			// WHEN
			ChannelFuture writeFuture = ch.writeAndFlush(Unpooled.wrappedBuffer(new byte[] { 1, 2, 3, 4 }));

			// THEN
			assertThat("Write completes while read is still blocked",
					writeFuture.await(2, TimeUnit.SECONDS), is(equalTo(true)));
			assertThat("Write succeeded", writeFuture.isSuccess(), is(equalTo(true)));
			assertThat("Output written", writeLatch.await(2, TimeUnit.SECONDS), is(equalTo(true)));
			assertThat("Read is still blocked", blockingReadCount.get(), is(equalTo(1)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Output written", byteObjectArray(out.toByteArray()),
				is(equalTo(byteObjectArray(new byte[] { 1, 2, 3, 4 }))));
	}

	@Test
	public void read_disconnected() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));

		final AtomicReference<Throwable> exception = new AtomicReference<>();
		final CountDownLatch inactiveLatch = new CountDownLatch(1);
		ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				exception.set(cause);
			}

			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				inactiveLatch.countDown();
			}
		});

		@SuppressWarnings("deprecation")
		final EventLoopGroup eventLoopGroup = new io.netty.channel.oio.OioEventLoopGroup();
		try {
			eventLoopGroup.register(ch).sync();
			ch.connect(remote).sync();
			assertThat("Channel is active", ch.isActive(), is(equalTo(true)));

			// WHEN
			disconnected.set(true);

			// THEN
			assertThat("Channel closed because serial port disconnected",
					ch.closeFuture().await(2, TimeUnit.SECONDS), is(equalTo(true)));
			assertThat("Channel is no longer active", ch.isActive(), is(equalTo(false)));
			assertThat("Channel is no longer open", ch.isOpen(), is(equalTo(false)));
			assertThat("Inactive event fired", inactiveLatch.await(2, TimeUnit.SECONDS),
					is(equalTo(true)));
			assertThat("Exception fired for disconnection", exception.get(),
					is(instanceOf(IOException.class)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void read_disconnected_doesNotSpin() throws Exception {
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
			disconnected.set(true);
			ch.closeFuture().await(2, TimeUnit.SECONDS);
			final int count = availableCount.get();
			Thread.sleep(200);

			// THEN
			assertThat("Serial port not read from again after disconnection", availableCount.get(),
					is(equalTo(count)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
	}

	@Test
	public void read_noReadTimeout() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));
		ch.config().setReadTimeout(0);

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
			Thread.sleep(500);
			final int idleAvailableCount = availableCount.get();
			pout.write(new byte[] { 4, 3, 2, 1, 0 });

			// THEN
			assertThat("Input read", readLatch.await(2, TimeUnit.SECONDS), is(equalTo(true)));
			assertThat("Serial port polled at a limited rate while idle", idleAvailableCount,
					is(lessThan(100)));
			assertThat("Blocking read not used without a read timeout", blockingReadCount.get(),
					is(equalTo(0)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Input read", byteObjectArray(read.toByteArray()),
				is(equalTo(byteObjectArray(new byte[] { 4, 3, 2, 1, 0 }))));
	}

	@Test
	public void read_autoReadDisabled() throws Exception {
		// GIVEN
		final SerialAddress remote = new SerialAddress("COM1");
		final CountDownLatch writeLatch = new CountDownLatch(0);
		final SerialPortChannel ch = new SerialPortChannel(provider(simulatedSerialPort(writeLatch)));
		ch.config().setAutoRead(false);

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
			pout.write(new byte[] { 4, 3, 2, 1, 0 });
			Thread.sleep(200);
			assertThat("Serial port not read from until asked", availableCount.get(), is(equalTo(0)));
			assertThat("Nothing read until asked", read.size(), is(equalTo(0)));

			// WHEN
			ch.read();

			// THEN
			assertThat("Input read", readLatch.await(2, TimeUnit.SECONDS), is(equalTo(true)));
		} finally {
			ch.close().sync();
			eventLoopGroup.shutdownGracefully();
		}
		assertThat("Input read", byteObjectArray(read.toByteArray()),
				is(equalTo(byteObjectArray(new byte[] { 4, 3, 2, 1, 0 }))));
	}

}
