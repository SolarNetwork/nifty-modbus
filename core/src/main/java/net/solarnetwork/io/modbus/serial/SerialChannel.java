/* ==================================================================
 * SerialChannel.java - 2/12/2022 8:51:48 am
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

package net.solarnetwork.io.modbus.serial;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

/**
 * Netty channel based on a {@link SerialPort}.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class SerialChannel extends io.netty.channel.oio.OioByteStreamChannel {

	private static final SerialAddress LOCAL_ADDRESS = new SerialAddress("localhost");

	private final SerialPortProvider serialPortProvider;
	private final SerialChannelConfig config;

	private boolean open;
	private SerialAddress deviceAddress;
	private SerialPort serialPort;

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
	 */
	public SerialChannel(SerialPortProvider serialPortProvider) {
		super(null);
		if ( serialPortProvider == null ) {
			throw new IllegalArgumentException("The serialPortProvider argument must not be null.");
		}
		this.serialPortProvider = serialPortProvider;
		config = new DefaultSerialChannelConfig(this);
		open = true;
	}

	@Override
	public SerialChannelConfig config() {
		return config;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	protected AbstractUnsafe newUnsafe() {
		return new SerialUnsafe();
	}

	@Override
	protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
		SerialAddress remote = (SerialAddress) remoteAddress;
		serialPort = serialPortProvider.getSerialPort(remote.name());
		deviceAddress = remote;
	}

	protected void doInit() throws Exception {
		SerialParameters params = config();
		serialPort.open(params);
		activate(serialPort.getInputStream(), serialPort.getOutputStream());
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
		doClose();
	}

	@Override
	protected void doClose() throws Exception {
		open = false;
		try {
			super.doClose();
		} finally {
			if ( serialPort != null ) {
				serialPort.close();
				serialPort = null;
			}
		}
	}

	@Override
	protected boolean isInputShutdown() {
		return !open;
	}

	@Override
	protected ChannelFuture shutdownInput() {
		return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
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
				doConnect(remoteAddress, localAddress);
				int waitTime = config().getOption(SerialChannelOption.WAIT_TIME);
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
