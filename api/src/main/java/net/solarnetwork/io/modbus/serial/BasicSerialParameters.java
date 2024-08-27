/* ==================================================================
 * BasicSerialParameters.java - 2/12/2022 12:32:25 pm
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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic implementation of {@link SerialParameters}.
 *
 * @author matt
 * @version 1.0
 */
public class BasicSerialParameters implements SerialParameters {

	private int baudRate = DEFAULT_BAUD_RATE;
	private int dataBits = DEFAULT_DATA_BITS;
	private SerialStopBits stopBits = DEFAULT_STOP_BITS;
	private SerialParity parity = DEFAULT_PARITY;
	private Set<SerialFlowControl> flowControl;
	private int waitTime;
	private int readTimeout = DEFAULT_READ_TIMEOUT;
	private Boolean rs485ModeEnabled;
	private boolean rs485RtsHighEnabled = DEFAULT_RS485_RTS_HIGH_ENABLED;
	private boolean rs485TerminationEnabled;
	private boolean rs485EchoEnabled;
	private int rs485BeforeSendDelay = DEFAULT_RS485_BEFORE_SEND_DELAY;
	private int rs485AfterSendDelay = DEFAULT_RS485_AFTER_SEND_DELAY;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SerialParameters{");
		builder.append(baudRate);
		builder.append(" ");
		builder.append(bitsShortcut());
		if ( flowControl != null && !flowControl.isEmpty() ) {
			builder.append(", flowControl=");
			builder.append(flowControl);
		}
		if ( waitTime > 0 ) {
			builder.append(", waitTime=");
			builder.append(waitTime);
		}
		if ( readTimeout > 0 ) {
			builder.append(", readTimeout=");
			builder.append(readTimeout);
		}
		if ( rs485ModeEnabled != null && rs485ModeEnabled.booleanValue() ) {
			builder.append(", RS-485");
			String flags = rs485Flags();
			if ( flags != null ) {
				builder.append("=[");
				builder.append(flags);
				builder.append("]");
			}
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Parse a comma-delimited list of RS-485 flags and configure the associated
	 * settings on this instance.
	 *
	 * <p>
	 * Each boolean-style flag can be prefixed with {@code !} to disable the
	 * associated setting, for example {@code "!RtsHigh"} would disable the RTS
	 * High setting.
	 * </p>
	 *
	 * @param flags
	 *        an RS-485 flags string as returned from
	 *        {@link SerialParameters#rs485Flags()}
	 */
	public void populateRs485Flags(String flags) {
		if ( flags == null || flags.isEmpty() ) {
			return;
		}
		String[] list = flags.split("\\s*,\\s*");
		Pattern numArgPat = Pattern.compile("(\\w+)=(\\d+)");
		for ( String flag : list ) {
			boolean enable = true;
			if ( flag.startsWith("!") ) {
				enable = false;
				flag = flag.substring(1);
			}
			if ( RS485_RTS_HIGH_FLAG.equalsIgnoreCase(flag) ) {
				setRs485RtsHighEnabled(enable);
			} else if ( RS485_TERMINATION_FLAG.equalsIgnoreCase(flag) ) {
				setRs485TerminationEnabled(enable);
			} else if ( RS485_ECHO_FLAG.equalsIgnoreCase(flag) ) {
				setRs485EchoEnabled(enable);
			} else {
				Matcher m = numArgPat.matcher(flag);
				if ( m.matches() ) {
					flag = m.group(1);
					int num = Integer.parseInt(m.group(2));
					if ( RS485_BEFORE_SEND_DELAY_FLAG.equalsIgnoreCase(flag) ) {
						setRs485BeforeSendDelay(num);
					} else if ( RS485_AFTER_SEND_DELAY_FLAG.equalsIgnoreCase(flag) ) {
						setRs485AfterSendDelay(num);
					}
				}
			}
		}
	}

	@Override
	public int getBaudRate() {
		return baudRate;
	}

	/**
	 * Set the baud rate.
	 *
	 * @param baudRate
	 *        the baud rate to set
	 */
	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	@Override
	public int getDataBits() {
		return dataBits;
	}

	/**
	 * Set the data bits.
	 *
	 * @param dataBits
	 *        the data bits to set
	 */
	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	@Override
	public SerialStopBits getStopBits() {
		return stopBits;
	}

	/**
	 * Set the stop bits.
	 *
	 * @param stopBits
	 *        the stop bits to set
	 */
	public void setStopBits(SerialStopBits stopBits) {
		this.stopBits = stopBits;
	}

	@Override
	public SerialParity getParity() {
		return parity;
	}

	/**
	 * Set the parity.
	 *
	 * @param parity
	 *        the parity to set
	 */
	public void setParity(SerialParity parity) {
		this.parity = parity;
	}

	@Override
	public Set<SerialFlowControl> getFlowControl() {
		return flowControl;
	}

	/**
	 * Set the flow control.
	 *
	 * @param flowControl
	 *        the flow control, or {@literal null} for none
	 */
	public void setFlowControl(Set<SerialFlowControl> flowControl) {
		this.flowControl = flowControl;
	}

	@Override
	public int getWaitTime() {
		return waitTime;
	}

	/**
	 * Set the wait time.
	 *
	 * @param waitTime
	 *        the wait time to set, in milliseconds
	 */
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	@Override
	public int getReadTimeout() {
		return readTimeout;
	}

	/**
	 * Set the read timeout.
	 *
	 * @param readTimeout
	 *        the read timeout to set, in milliseconds
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public Boolean getRs485ModeEnabled() {
		return rs485ModeEnabled;
	}

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
	 */
	public void setRs485ModeEnabled(Boolean rs485ModeEnabled) {
		this.rs485ModeEnabled = rs485ModeEnabled;
	}

	@Override
	public boolean isRs485RtsHighEnabled() {
		return rs485RtsHighEnabled;
	}

	/**
	 * Set the RS-485 RTS "high" mode.
	 *
	 * @param rs485RtsHighEnabled
	 *        {@literal true} to set the RTS line high (to 1) when transmitting
	 */
	public void setRs485RtsHighEnabled(boolean rs485RtsHighEnabled) {
		this.rs485RtsHighEnabled = rs485RtsHighEnabled;
	}

	@Override
	public boolean isRs485TerminationEnabled() {
		return rs485TerminationEnabled;
	}

	/**
	 * Set the RS-485 termination mode.
	 *
	 * @param rs485TerminationEnabled
	 *        {@literal true} to enable RS-485 bus termination
	 */
	public void setRs485TerminationEnabled(boolean rs485TerminationEnabled) {
		this.rs485TerminationEnabled = rs485TerminationEnabled;
	}

	@Override
	public boolean isRs485EchoEnabled() {
		return rs485EchoEnabled;
	}

	/**
	 * Set the RS-485 "echo" mode.
	 *
	 * @param rs485EchoEnabled
	 *        {@literal true} to enable receive during transmit
	 */
	public void setRs485EchoEnabled(boolean rs485EchoEnabled) {
		this.rs485EchoEnabled = rs485EchoEnabled;
	}

	@Override
	public int getRs485BeforeSendDelay() {
		return rs485BeforeSendDelay;
	}

	/**
	 * Set a time to wait after enabling transmit mode before sending data when
	 * in RS-485 mode.
	 *
	 * @param rs485BeforeSendDelay
	 *        the delay, in microseconds
	 */
	public void setRs485BeforeSendDelay(int rs485BeforeSendDelay) {
		this.rs485BeforeSendDelay = rs485BeforeSendDelay;
	}

	@Override
	public int getRs485AfterSendDelay() {
		return rs485AfterSendDelay;
	}

	/**
	 * Set a time to wait after sending data before disabling transmit mode when
	 * in RS-485 mode
	 *
	 * @param rs485AfterSendDelay
	 *        the delay, in microseconds
	 */
	public void setRs485AfterSendDelay(int rs485AfterSendDelay) {
		this.rs485AfterSendDelay = rs485AfterSendDelay;
	}

}
