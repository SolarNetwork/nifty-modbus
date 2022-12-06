/* ==================================================================
 * SerialPortChannelConfig.java - 2/12/2022 6:33:26 am
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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;

/**
 * A configuration class for serial device connections.
 *
 * @author matt
 * @version 1.0
 */
public interface SerialPortChannelConfig extends ChannelConfig, SerialParameters {

	/**
	 * Copy serial parameters.
	 * 
	 * @param serialParameters
	 *        the parameters to copy
	 * @return this instance
	 */
	default SerialPortChannelConfig setSerialParameters(SerialParameters serialParameters) {
		setBaudRate(serialParameters.getBaudRate());
		setDataBits(serialParameters.getDataBits());
		setParity(serialParameters.getParity());
		setStopBits(serialParameters.getStopBits());
		setWaitTime(serialParameters.getWaitTime());
		setReadTimeout(serialParameters.getReadTimeout());
		return this;
	}

	/**
	 * Sets the baud rate (bits per second) for communication with the serial
	 * device.
	 * 
	 * <p>
	 * The baud rate will include bits for framing (in the form of stop bits and
	 * parity), such that the effective data rate will be lower than this value.
	 * </p>
	 *
	 * @param baudRate
	 *        the baud rate to use (in bits per second)
	 * @return this instance
	 */
	SerialPortChannelConfig setBaudRate(int baudRate);

	/**
	 * Sets the number of stop bits to include at the end of every character.
	 *
	 * @param stopBits
	 *        the number of stop bits to use
	 * @return this instance
	 */
	SerialPortChannelConfig setStopBits(SerialStopBits stopBits);

	/**
	 * Sets the number of data bits to use to make up each character sent to the
	 * serial device.
	 *
	 * @param dataBits
	 *        the number of data bits to use
	 * @return this instance
	 */
	SerialPortChannelConfig setDataBits(int dataBits);

	/**
	 * Sets the type of parity to be used when communicating with the serial
	 * device.
	 *
	 * @param parity
	 *        the type of parity to be used
	 * @return this instance
	 */
	SerialPortChannelConfig setParity(SerialParity parity);

	/**
	 * Set the time to wait after opening the serial port and before sending it
	 * any configuration information or data.
	 * 
	 * <p>
	 * A value of 0 indicates that no waiting should occur.
	 * </p>
	 *
	 * @param waitTime
	 *        the maximum number of milliseconds to wait
	 * @return this instance
	 * @throws IllegalArgumentException
	 *         if the supplied value is &lt; 0
	 */
	SerialPortChannelConfig setWaitTime(int waitTime);

	/**
	 * Set the maximal time to block while try to read from the serial port.
	 * 
	 * @param readTimeout
	 *        the maximum time to wait, in milliseconds
	 * @return this instance
	 */
	SerialPortChannelConfig setReadTimeout(int readTimeout);

	@Override
	SerialPortChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

	@Override
	@Deprecated
	SerialPortChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

	@Override
	SerialPortChannelConfig setWriteSpinCount(int writeSpinCount);

	@Override
	SerialPortChannelConfig setAllocator(ByteBufAllocator allocator);

	@Override
	SerialPortChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

	@Override
	SerialPortChannelConfig setAutoRead(boolean autoRead);

	@Override
	SerialPortChannelConfig setAutoClose(boolean autoClose);

	@Override
	SerialPortChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

	@Override
	SerialPortChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

	@Override
	SerialPortChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

}
