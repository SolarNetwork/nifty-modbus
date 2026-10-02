/* ==================================================================
 * SerialPortChannel.java - 5/12/2022 12:18:24 pm
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

package net.solarnetwork.io.modbus.netty.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.netty.util.internal.StringUtil;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * Channel for a {@link SerialPortProvider}.
 *
 * <p>
 * Reading from the serial port is a blocking operation, so each connected
 * channel uses a dedicated daemon thread to read from the serial port's
 * {@link InputStream} and hand the data over to the channel's event loop. This
 * means the event loop is never blocked waiting for data to arrive, so writes
 * are not delayed by the serial port's read timeout. Writing is performed
 * directly on the event loop.
 * </p>
 *
 * <p>
 * The serial port is expected to follow these {@link InputStream} semantics:
 * </p>
 *
 * <ul>
 * <li>{@link InputStream#read(byte[], int, int)} returns {@literal 0} if the
 * configured read timeout expires before any data is available</li>
 * <li>{@link InputStream#read(byte[], int, int)} returns {@literal -1}, or
 * either that method or {@link InputStream#available()} throws an exception, if
 * the serial port is no longer usable, for example the device has been
 * disconnected; the channel will be closed when this happens</li>
 * </ul>
 *
 * <p>
 * If the configured read timeout is not greater than {@literal 0} then blocking
 * reads are not used; the serial port is polled for available data instead.
 * </p>
 *
 * <p>
 * Any single-threaded event loop can be used with this channel. As writing to
 * the serial port blocks the event loop, a dedicated event loop is recommended,
 * such as one created by
 * {@link net.solarnetwork.io.modbus.netty.channel.LocalIoEventLoopGroupFactory}.
 * If the event loop is shut down without the channel having been closed, the
 * serial port is closed.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public class SerialPortChannel extends AbstractChannel {

	private static final ChannelMetadata METADATA = new ChannelMetadata(true);

	private static final SerialAddress LOCAL_ADDRESS = new SerialAddress("localhost");

	/** The maximum number of bytes to read from the serial port at once. */
	private static final int READ_BUFFER_SIZE = 1024;

	/**
	 * The minimum time a read attempt that produces no data is allowed to take,
	 * so a serial port that does not block for data cannot consume a CPU.
	 */
	private static final long IDLE_READ_MIN_NANOS = TimeUnit.MILLISECONDS.toNanos(10);

	/**
	 * How often to check the event loop is still running while waiting to be
	 * asked to read, in milliseconds.
	 */
	private static final long EVENT_LOOP_CHECK_MS = 1000L;

	private final SerialPortProvider serialPortProvider;
	private final SerialPortChannelConfig config;

	/** Granted by the event loop each time a read is wanted. */
	private final Semaphore readPermits = new Semaphore(0);

	private volatile boolean open;
	private volatile @Nullable SerialAddress deviceAddress;
	private volatile @Nullable SerialPort serialPort;

	// the following are only accessed from the event loop

	private @Nullable InputStream serialPortIn;
	private @Nullable OutputStream serialPortOut;
	private @Nullable SerialReader reader;
	private boolean readPending;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The stream provider is passed a serial device name, and should return a
	 * stream for that device.
	 * </p>
	 * 
	 * @param serialPortProvider
	 *        the serial port provider
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	@SuppressWarnings("this-escape")
	public SerialPortChannel(SerialPortProvider serialPortProvider) {
		super(null);
		if ( serialPortProvider == null ) {
			throw new IllegalArgumentException("The serialPortProvider argument must not be null.");
		}
		this.serialPortProvider = serialPortProvider;
		config = new DefaultSerialPortChannelConfig(this);
		open = true;
	}

	@Override
	public ChannelMetadata metadata() {
		return METADATA;
	}

	@Override
	public SerialPortChannelConfig config() {
		return config;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public boolean isActive() {
		final SerialPort p = this.serialPort;
		return (p != null && p.isOpen());
	}

	@Override
	protected AbstractUnsafe newUnsafe() {
		return new SerialUnsafe();
	}

	private void doConnect(SocketAddress remoteAddress) throws Exception {
		SerialAddress remote = (SerialAddress) remoteAddress;
		serialPort = serialPortProvider.getSerialPort(remote.name());
		deviceAddress = remote;
	}

	/**
	 * Open the serial port and start reading from it.
	 * 
	 * @throws Exception
	 *         if the serial port cannot be opened
	 */
	protected void doInit() throws Exception {
		final SerialPort p = this.serialPort;
		if ( p == null || !isOpen() ) {
			throw new ClosedChannelException();
		}
		final SerialParameters params = config();
		try {
			p.open(params);
			final InputStream in = p.getInputStream();
			serialPortIn = in;
			serialPortOut = p.getOutputStream();

			final SerialAddress addr = this.deviceAddress;
			final SerialReader r = new SerialReader(in, params.getReadTimeout() > 0,
					(addr != null ? addr.name() : p.getName()));
			reader = r;
			r.start();
		} catch ( Exception e ) {
			try {
				doDisconnect();
			} catch ( Exception e2 ) {
				// ignore, to throw original exception
			}
			throw e;
		}
	}

	@Override
	public SerialAddress localAddress() {
		return (SerialAddress) super.localAddress();
	}

	@Nullable
	@Override
	public SerialAddress remoteAddress() {
		return (SerialAddress) super.remoteAddress();
	}

	@Override
	protected SerialAddress localAddress0() {
		return LOCAL_ADDRESS;
	}

	@Nullable
	@Override
	protected SerialAddress remoteAddress0() {
		return deviceAddress;
	}

	@Override
	protected void doBind(SocketAddress localAddress) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doDisconnect() throws Exception {
		final SerialReader r = this.reader;
		if ( r != null ) {
			reader = null;
			r.stop();
		}
		readPending = false;
		readPermits.drainPermits();
		if ( serialPortIn != null ) {
			try {
				serialPortIn.close();
			} catch ( Exception e ) {
				// ignore
			} finally {
				serialPortIn = null;
			}
		}
		if ( serialPortOut != null ) {
			try {
				serialPortOut.close();
			} catch ( Exception e ) {
				// ignore
			} finally {
				serialPortOut = null;
			}
		}
		final SerialPort p = this.serialPort;
		if ( p != null ) {
			try {
				p.close();
			} finally {
				serialPort = null;
			}
		}
	}

	@Override
	protected void doClose() throws Exception {
		open = false;
		doDisconnect();
	}

	@Override
	protected final Object filterOutboundMessage(Object msg) throws Exception {
		if ( msg instanceof ByteBuf ) {
			return msg;
		}

		throw new UnsupportedOperationException(
				"Unsupported message type: " + StringUtil.simpleClassName(msg) + " (expected: "
						+ StringUtil.simpleClassName(ByteBuf.class) + ')');
	}

	@Override
	protected void doBeginRead() throws Exception {
		if ( readPending ) {
			return;
		}
		readPending = true;
		readPermits.release();
	}

	/**
	 * Handle data read from the serial port.
	 * 
	 * <p>
	 * This must be called on the event loop.
	 * </p>
	 * 
	 * @param source
	 *        the reader that read the data
	 * @param data
	 *        the data
	 */
	private void handleRead(SerialReader source, ByteBuf data) {
		if ( source != this.reader ) {
			// serial port has been closed since the data was read
			data.release();
			return;
		}
		readPending = false;
		final ChannelPipeline pipeline = pipeline();
		pipeline.fireChannelRead(data);
		pipeline.fireChannelReadComplete();
	}

	/**
	 * Handle a failure to read from the serial port, by closing the channel.
	 * 
	 * <p>
	 * This must be called on the event loop.
	 * </p>
	 * 
	 * @param source
	 *        the reader that failed
	 * @param cause
	 *        the cause of the failure
	 */
	private void handleReadFailure(SerialReader source, Throwable cause) {
		if ( source != this.reader ) {
			// serial port has been closed already
			return;
		}
		readPending = false;
		pipeline().fireExceptionCaught(cause);
		if ( isOpen() ) {
			unsafe().close(unsafe().voidPromise());
		}
	}

	@Override
	protected void doWrite(ChannelOutboundBuffer in) throws Exception {
		for ( ;; ) {
			Object msg = in.current();
			if ( msg == null ) {
				// nothing left to write
				break;
			}

			// only expect ByteBuf here because of filterOutputMessage() implementation
			ByteBuf buf = (ByteBuf) msg;
			int readableBytes = buf.readableBytes();
			while ( readableBytes > 0 ) {
				doWriteBytes(buf);
				int newReadableBytes = buf.readableBytes();
				in.progress(readableBytes - newReadableBytes);
				readableBytes = newReadableBytes;
			}
			in.remove();
		}
	}

	/**
	 * Write the data which is hold by the {@link ByteBuf} to the underlying
	 * serial port.
	 *
	 * @param buf
	 *        the {@link ByteBuf} which holds the data to transfer
	 * @throws Exception
	 *         is thrown if an error occurred
	 */
	protected void doWriteBytes(ByteBuf buf) throws Exception {
		final OutputStream out = this.serialPortOut;
		if ( out == null ) {
			throw new IOException("Serial port is not open.");
		}
		buf.readBytes(out, buf.readableBytes());
	}

	@Override
	protected boolean isCompatible(EventLoop loop) {
		return (loop instanceof SingleThreadEventExecutor);
	}

	/**
	 * Task to read from the serial port on a dedicated thread, handing data
	 * over to the event loop.
	 * 
	 * <p>
	 * One read is performed for each permit granted to {@code readPermits}, so
	 * the serial port is only read from when the channel wants data.
	 * </p>
	 */
	private final class SerialReader implements Runnable {

		private final InputStream in;
		private final boolean blocking;
		private final String name;
		private final Thread thread;
		private final byte[] buffer = new byte[READ_BUFFER_SIZE];
		private volatile boolean stopped;

		private SerialReader(InputStream in, boolean blocking, String name) {
			super();
			this.in = in;
			this.blocking = blocking;
			this.name = name;
			this.thread = new Thread(this, "SerialPortChannel-Reader-" + name);
			this.thread.setDaemon(true);
		}

		private void start() {
			thread.start();
		}

		private void stop() {
			stopped = true;
			thread.interrupt();
		}

		@SuppressWarnings("FutureReturnValueIgnored")
		@Override
		public void run() {
			try {
				while ( !stopped ) {
					while ( !readPermits.tryAcquire(EVENT_LOOP_CHECK_MS, TimeUnit.MILLISECONDS) ) {
						if ( abandoned() ) {
							return;
						}
					}
					int len = 0;
					while ( len == 0 ) {
						if ( stopped || abandoned() ) {
							return;
						}
						len = read();
					}
					if ( len < 0 ) {
						throw new IOException("Serial port [" + name + "] is no longer available.");
					}
					final ByteBuf data = alloc().buffer(len).writeBytes(buffer, 0, len);
					try {
						eventLoop().execute(() -> handleRead(this, data));
					} catch ( RejectedExecutionException e ) {
						data.release();
						closeAbandonedSerialPort();
						return;
					}
				}
			} catch ( Throwable t ) {
				if ( stopped ) {
					return;
				}
				try {
					eventLoop().execute(() -> handleReadFailure(this, t));
				} catch ( RejectedExecutionException e ) {
					closeAbandonedSerialPort();
				}
			}
		}

		/**
		 * Test if the event loop has shut down without closing the channel,
		 * closing the serial port if so.
		 * 
		 * @return {@code true} if the event loop has shut down
		 */
		private boolean abandoned() {
			if ( !eventLoop().isShutdown() ) {
				return false;
			}
			closeAbandonedSerialPort();
			return true;
		}

		/**
		 * Close the serial port, because the event loop is no longer able to.
		 */
		private void closeAbandonedSerialPort() {
			final SerialPort p = serialPort;
			if ( p != null && !stopped ) {
				try {
					p.close();
				} catch ( Exception e ) {
					// ignore
				}
			}
		}

		/**
		 * Read from the serial port.
		 * 
		 * @return the number of bytes read into {@code buffer}, {@literal 0} if
		 *         no data is available yet, or {@literal -1} if the serial port
		 *         is no longer available
		 */
		private int read() throws IOException, InterruptedException {
			final long start = System.nanoTime();
			final int available = in.available();
			int len;
			if ( available > 0 ) {
				len = in.read(buffer, 0, Math.min(available, buffer.length));
			} else if ( blocking ) {
				// wait for data, up to the serial port's read timeout
				len = in.read(buffer, 0, 1);
			} else {
				len = (available < 0 ? -1 : 0);
			}
			if ( len == 0 ) {
				final long remaining = IDLE_READ_MIN_NANOS - (System.nanoTime() - start);
				if ( remaining > 0 ) {
					TimeUnit.NANOSECONDS.sleep(remaining);
				}
			}
			return len;
		}

	}

	private final class SerialUnsafe extends AbstractUnsafe {

		@SuppressWarnings("FutureReturnValueIgnored")
		@Override
		public void connect(final SocketAddress remoteAddress, final SocketAddress localAddress,
				final ChannelPromise promise) {
			if ( !promise.setUncancellable() || !ensureOpen(promise) ) {
				return;
			}

			final boolean wasActive = isActive();

			Runnable task = new Runnable() {

				@Override
				public void run() {
					try {
						doInit();
						safeSetSuccess(promise);
						if ( !wasActive && isActive() ) {
							pipeline().fireChannelActive();
						}
					} catch ( Throwable t ) {
						safeSetFailure(promise, t);
						closeIfClosed();
					}
				}
			};

			try {
				doConnect(remoteAddress);
				int waitTime = config().getOption(SerialPortChannelOption.WAIT_TIME);
				if ( waitTime > 0 ) {
					eventLoop().schedule(task, waitTime, TimeUnit.MILLISECONDS);
				} else {
					task.run();
				}
			} catch ( Throwable t ) {
				safeSetFailure(promise, t);
				closeIfClosed();
			}
		}
	}

}
