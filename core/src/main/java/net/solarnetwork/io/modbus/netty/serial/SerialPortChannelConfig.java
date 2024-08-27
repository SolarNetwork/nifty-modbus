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

package net.solarnetwork.io.modbus.netty.serial;

import java.util.Set;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import net.solarnetwork.io.modbus.serial.SerialFlowControl;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

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
		setFlowControl(serialParameters.getFlowControl());
		setRs485ModeEnabled(serialParameters.getRs485ModeEnabled());
		setRs485RtsHighEnabled(serialParameters.isRs485RtsHighEnabled());
		setRs485TerminationEnabled(serialParameters.isRs485TerminationEnabled());
		setRs485EchoEnabled(serialParameters.isRs485EchoEnabled());
		setRs485BeforeSendDelay(serialParameters.getRs485BeforeSendDelay());
		setRs485AfterSendDelay(serialParameters.getRs485AfterSendDelay());
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
	 * Set the flow control.
	 *
	 * @param flowControl
	 *        the flow control, or {@literal null} for none
	 * @return this instance
	 */
	SerialPortChannelConfig setFlowControl(Set<SerialFlowControl> flowControl);

	/**
	 * Set the RS-485 mode.
	 *
	 * <p>
	 * When this is set to {@literal true} then the other {@code getRs485*}
	 * settings are used.
	 * </p>
	 *
	 * @param rs485ModeEnabled
	 *        {@literal true} to enable RS-485 mode
	 * @return this instance
	 */
	SerialPortChannelConfig setRs485ModeEnabled(Boolean rs485ModeEnabled);

	/**
	 * Set the RS-485 RTS "high" mode.
	 *
	 * @param rs485RtsHighEnabled
	 *        {@literal true} to set the RTS line high (to 1) when transmitting
	 * @return this instance
	 */
	SerialPortChannelConfig setRs485RtsHighEnabled(boolean rs485RtsHighEnabled);

	/**
	 * Set the RS-485 termination mode.
	 *
	 * @param rs485TerminationEnabled
	 *        {@literal true} to enable RS-485 bus termination
	 * @return this instance
	 */
	SerialPortChannelConfig setRs485TerminationEnabled(boolean rs485TerminationEnabled);

	/**
	 * Set the RS-485 "echo" mode.
	 *
	 * @param rs485EchoEnabled
	 *        {@literal true} to enable receive during transmit
	 * @return this instance
	 */
	SerialPortChannelConfig setRs485EchoEnabled(boolean rs485EchoEnabled);

	/**
	 * Set a time to wait after enabling transmit mode before sending data when
	 * in RS-485 mode.
	 *
	 * @param rs485BeforeSendDelay
	 *        the delay, in microseconds
	 * @return this instance
	 */
	SerialPortChannelConfig setRs485BeforeSendDelay(int rs485BeforeSendDelay);

	/**
	 * Set a time to wait after sending data before disabling transmit mode when
	 * in RS-485 mode
	 *
	 * @param rs485AfterSendDelay
	 *        the delay, in microseconds
	 * @return this instance
	 */
	SerialPortChannelConfig setRs485AfterSendDelay(int rs485AfterSendDelay);

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
