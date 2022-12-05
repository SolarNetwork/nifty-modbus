/* ==================================================================
 * DefaultSerialPortChannelConfig.java - 2/12/2022 7:00:58 am
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

import static net.solarnetwork.io.modbus.serial.SerialPortChannelOption.BAUD_RATE;
import static net.solarnetwork.io.modbus.serial.SerialPortChannelOption.DATA_BITS;
import static net.solarnetwork.io.modbus.serial.SerialPortChannelOption.PARITY;
import static net.solarnetwork.io.modbus.serial.SerialPortChannelOption.READ_TIMEOUT;
import static net.solarnetwork.io.modbus.serial.SerialPortChannelOption.STOP_BITS;
import static net.solarnetwork.io.modbus.serial.SerialPortChannelOption.WAIT_TIME;
import java.util.Map;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;

/**
 * Default implementation of {@link SerialPortChannelConfig}.
 *
 * @author matt
 * @version 1.0
 */
public class DefaultSerialPortChannelConfig extends DefaultChannelConfig
		implements SerialPortChannelConfig {

	private volatile int baudRate = DEFAULT_BAUD_RATE;
	private volatile SerialStopBits stopBits = DEFAULT_STOP_BITS;
	private volatile int dataBits = DEFAULT_DATA_BITS;
	private volatile SerialParity parity = DEFAULT_PARITY;
	private volatile int waitTime;
	private volatile int readTimeout = DEFAULT_READ_TIMEOUT;

	DefaultSerialPortChannelConfig(SerialPortChannel channel) {
		super(channel);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("SerialPortChannelConfig{");
		buf.append(baudRate);
		buf.append(" ");
		buf.append(bitsShortcut());
		buf.append("}");
		return buf.toString();
	}

	@Override
	public Map<ChannelOption<?>, Object> getOptions() {
		return getOptions(super.getOptions(), BAUD_RATE, STOP_BITS, DATA_BITS, PARITY, WAIT_TIME);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOption(ChannelOption<T> option) {
		if ( option == BAUD_RATE ) {
			return (T) Integer.valueOf(getBaudRate());
		}
		if ( option == STOP_BITS ) {
			return (T) getStopBits();
		}
		if ( option == DATA_BITS ) {
			return (T) Integer.valueOf(getDataBits());
		}
		if ( option == PARITY ) {
			return (T) getParity();
		}
		if ( option == WAIT_TIME ) {
			return (T) Integer.valueOf(getWaitTime());
		}
		if ( option == READ_TIMEOUT ) {
			return (T) Integer.valueOf(getReadTimeout());
		}
		return super.getOption(option);
	}

	@Override
	public <T> boolean setOption(ChannelOption<T> option, T value) {
		validate(option, value);

		if ( option == BAUD_RATE ) {
			setBaudRate((Integer) value);
		} else if ( option == STOP_BITS ) {
			setStopBits((SerialStopBits) value);
		} else if ( option == DATA_BITS ) {
			setDataBits((Integer) value);
		} else if ( option == PARITY ) {
			setParity((SerialParity) value);
		} else if ( option == WAIT_TIME ) {
			setWaitTime((Integer) value);
		} else if ( option == READ_TIMEOUT ) {
			setReadTimeout((Integer) value);
		} else {
			return super.setOption(option, value);
		}
		return true;
	}

	@Override
	public int getBaudRate() {
		return baudRate;
	}

	@Override
	public SerialPortChannelConfig setBaudRate(final int baudRate) {
		this.baudRate = baudRate;
		return this;
	}

	@Override
	public SerialStopBits getStopBits() {
		return stopBits;
	}

	@Override
	public SerialPortChannelConfig setStopBits(final SerialStopBits stopBits) {
		this.stopBits = stopBits;
		return this;
	}

	@Override
	public int getDataBits() {
		return dataBits;
	}

	@Override
	public SerialPortChannelConfig setDataBits(final int dataBits) {
		this.dataBits = dataBits;
		return this;
	}

	@Override
	public SerialParity getParity() {
		return parity;
	}

	@Override
	public SerialPortChannelConfig setParity(final SerialParity parity) {
		this.parity = parity;
		return this;
	}

	@Override
	public int getWaitTime() {
		return waitTime;
	}

	@Override
	public SerialPortChannelConfig setWaitTime(final int waitTime) {
		if ( waitTime < 0 ) {
			throw new IllegalArgumentException("The wait time must be >= 0");
		}
		this.waitTime = waitTime;
		return this;
	}

	@Override
	public int getReadTimeout() {
		return readTimeout;
	}

	@Override
	public SerialPortChannelConfig setReadTimeout(int readTimeout) {
		if ( readTimeout < 0 ) {
			throw new IllegalArgumentException("The read timeout must be >= 0");
		}
		this.readTimeout = readTimeout;
		return this;
	}

	@Override
	public SerialPortChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
		super.setConnectTimeoutMillis(connectTimeoutMillis);
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public SerialPortChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
		super.setMaxMessagesPerRead(maxMessagesPerRead);
		return this;
	}

	@Override
	public SerialPortChannelConfig setWriteSpinCount(int writeSpinCount) {
		super.setWriteSpinCount(writeSpinCount);
		return this;
	}

	@Override
	public SerialPortChannelConfig setAllocator(ByteBufAllocator allocator) {
		super.setAllocator(allocator);
		return this;
	}

	@Override
	public SerialPortChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
		super.setRecvByteBufAllocator(allocator);
		return this;
	}

	@Override
	public SerialPortChannelConfig setAutoRead(boolean autoRead) {
		super.setAutoRead(autoRead);
		return this;
	}

	@Override
	public SerialPortChannelConfig setAutoClose(boolean autoClose) {
		super.setAutoClose(autoClose);
		return this;
	}

	@Override
	public SerialPortChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
		super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
		return this;
	}

	@Override
	public SerialPortChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
		super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
		return this;
	}

	@Override
	public SerialPortChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
		super.setMessageSizeEstimator(estimator);
		return this;
	}

}
