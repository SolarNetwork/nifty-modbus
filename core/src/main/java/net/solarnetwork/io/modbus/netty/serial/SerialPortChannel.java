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
import java.util.concurrent.TimeUnit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.netty.util.internal.StringUtil;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * Channel for a {@link SerialPortProvider}.
 *
 * @author matt
 * @version 1.0
 */
public class SerialPortChannel extends AbstractChannel {

	private static final ChannelMetadata METADATA = new ChannelMetadata(true);

	private static final SerialAddress LOCAL_ADDRESS = new SerialAddress("localhost");

	private final SerialPortProvider serialPortProvider;
	private final SerialPortChannelConfig config;

	private boolean open;
	private SerialAddress deviceAddress;
	private SerialPort serialPort;

	private InputStream serialPortIn;
	private OutputStream serialPortOut;

	boolean readPending;
	private final Runnable readTask = new Runnable() {

		@Override
		public void run() {
			doRead();
		}
	};

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
	 *         if any argument is {@literal null}
	 */
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

	protected void doInit() throws Exception {
		SerialParameters params = config();
		serialPort.open(params);
	}

	@Override
	public SerialAddress localAddress() {
		return (SerialAddress) super.localAddress();
	}

	@Override
	public SerialAddress remoteAddress() {
		return (SerialAddress) super.remoteAddress();
	}

	@Override
	protected SerialAddress localAddress0() {
		return LOCAL_ADDRESS;
	}

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
		if ( serialPort != null ) {
			try {
				serialPort.close();
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
		eventLoop().execute(readTask);
	}

	private InputStream serialIn() throws IOException {
		if ( serialPortIn != null ) {
			return serialPortIn;
		}
		final SerialPort p = this.serialPort;
		InputStream in = (p != null ? p.getInputStream() : null);
		this.serialPortIn = in;
		return in;
	}

	@SuppressWarnings("deprecation")
	protected void doRead() {
		if ( !readPending ) {
			// We have to check readPending here because the Runnable to read could have been scheduled and later
			// during the same read loop readPending was set to false.
			return;
		}
		// In OIO we should set readPending to false even if the read was not successful so we can schedule
		// another read on the event loop if no reads are done.
		readPending = false;

		final ChannelConfig config = config();
		final ChannelPipeline pipeline = pipeline();
		final ByteBufAllocator allocator = config.getAllocator();
		final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
		allocHandle.reset(config);

		ByteBuf byteBuf = null;
		boolean readData = false;
		try {
			byteBuf = allocHandle.allocate(allocator);
			do {
				allocHandle.lastBytesRead(doReadBytes(byteBuf));
				if ( allocHandle.lastBytesRead() <= 0 ) {
					if ( !byteBuf.isReadable() ) { // nothing was read. release the buffer.
						byteBuf.release();
						byteBuf = null;
					}
					break;
				} else {
					readData = true;
				}

				final int available = available();
				if ( available <= 0 ) {
					break;
				}

				// Oio collects consecutive read operations into 1 ByteBuf before propagating up the pipeline.
				if ( !byteBuf.isWritable() ) {
					final int capacity = byteBuf.capacity();
					final int maxCapacity = byteBuf.maxCapacity();
					if ( capacity == maxCapacity ) {
						allocHandle.incMessagesRead(1);
						readPending = false;
						pipeline.fireChannelRead(byteBuf);
						byteBuf = allocHandle.allocate(allocator);
					} else {
						final int writerIndex = byteBuf.writerIndex();
						if ( writerIndex + available > maxCapacity ) {
							byteBuf.capacity(maxCapacity);
						} else {
							byteBuf.ensureWritable(available);
						}
					}
				}
			} while ( allocHandle.continueReading() );

			if ( byteBuf != null ) {
				// It is possible we allocated a buffer because the previous one was not writable, but then didn't use
				// it because allocHandle.continueReading() returned false.
				if ( byteBuf.isReadable() ) {
					readPending = false;
					pipeline.fireChannelRead(byteBuf);
				} else {
					byteBuf.release();
				}
				byteBuf = null;
			}

			if ( readData ) {
				allocHandle.readComplete();
				pipeline.fireChannelReadComplete();
			}
		} catch ( Throwable t ) {
			handleReadException(pipeline, byteBuf, t, allocHandle);
		} finally {
			if ( (config.isAutoRead() || !readData) && isActive() ) {
				// Reading 0 bytes could mean there is a SocketTimeout and no data was actually read, so we
				// should execute read() again because no data may have been read.
				read();
			}
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
	 * Return the number of bytes ready to read from the underlying Socket.
	 */
	protected int available() {
		try {
			final InputStream in = serialIn();
			if ( in == null ) {
				return 0;
			}
			return in.available();
		} catch ( IOException e ) {
			// TODO: log? ignore
			return 0;
		}
	}

	/**
	 * Read bytes from the underlying Socket.
	 *
	 * @param buf
	 *        the {@link ByteBuf} into which the read bytes will be written
	 * @return the number of bytes read. This may return a negative amount if
	 *         the underlying Socket was closed
	 * @throws Exception
	 *         is thrown if an error occurred
	 */
	protected int doReadBytes(ByteBuf buf) throws Exception {
		try {
			InputStream in = serialIn();
			if ( in == null ) {
				return 0;
			}
			int avail = in.available();
			if ( avail > 0 ) {
				try {
					return buf.writeBytes(in, avail);
				} finally {
					Thread.sleep(25); // TODO: make configurable
				}
			} else if ( config.getReadTimeout() > 0 ) {
				// use blocking read w/timeout
				return buf.writeBytes(in, 1);
			} else {
				return 0;
			}
		} catch ( IOException e ) {
			// TODO: log? ignore
			return 0;
		}
	}

	private OutputStream serialOut() throws IOException {
		if ( serialPortOut != null ) {
			return serialPortOut;
		}
		final SerialPort p = this.serialPort;
		OutputStream out = (p != null ? p.getOutputStream() : null);
		this.serialPortOut = out;
		return out;
	}

	/**
	 * Write the data which is hold by the {@link ByteBuf} to the underlying
	 * Socket.
	 *
	 * @param buf
	 *        the {@link ByteBuf} which holds the data to transfer
	 * @throws Exception
	 *         is thrown if an error occurred
	 */
	protected void doWriteBytes(ByteBuf buf) throws Exception {
		final OutputStream out = serialOut();
		if ( out != null ) {
			buf.readBytes(out, buf.readableBytes());
		}
	}

	@SuppressWarnings("deprecation")
	private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause,
			RecvByteBufAllocator.Handle allocHandle) {
		if ( byteBuf != null ) {
			if ( byteBuf.isReadable() ) {
				readPending = false;
				pipeline.fireChannelRead(byteBuf);
			} else {
				byteBuf.release();
			}
		}
		allocHandle.readComplete();
		pipeline.fireChannelReadComplete();
		pipeline.fireExceptionCaught(cause);
	}

	@Override
	protected boolean isCompatible(EventLoop loop) {
		return (loop instanceof SingleThreadEventExecutor);
	}

	private final class SerialUnsafe extends AbstractUnsafe {

		@Override
		public void connect(final SocketAddress remoteAddress, final SocketAddress localAddress,
				final ChannelPromise promise) {
			if ( !promise.setUncancellable() || !isOpen() ) {
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
