/* ==================================================================
 * SerialParameters.java - 2/12/2022 9:04:37 am
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

/**
 * Serial configuration parameters.
 *
 * @author matt
 * @version 1.0
 */
public interface SerialParameters {

	/** A default baud rate. */
	int DEFAULT_BAUD_RATE = 115200;

	/** The default stopbits value. */
	SerialStopBits DEFAULT_STOP_BITS = SerialStopBits.One;

	/** The default databits value. */
	int DEFAULT_DATA_BITS = 8;

	/** The default parity value. */
	SerialParity DEFAULT_PARITY = SerialParity.None;

	/** The default read timeout. */
	int DEFAULT_READ_TIMEOUT = 1000;

	/** The default RS-485 RTS high enabled value. */
	boolean DEFAULT_RS485_RTS_HIGH_ENABLED = true;

	/** The default RS-485 RTS before send delay. */
	int DEFAULT_RS485_BEFORE_SEND_DELAY = 5000;

	/** The default RS-485 RTS before send delay. */
	int DEFAULT_RS485_AFTER_SEND_DELAY = 2000;

	/** The RS-485 RTS high flag. */
	String RS485_RTS_HIGH_FLAG = "RtsHigh";

	/** The RS-485 termination flag. */
	String RS485_TERMINATION_FLAG = "Termination";

	/** The RS-485 echo flag. */
	String RS485_ECHO_FLAG = "Echo";

	/** The RS-485 before send delay flag. */
	String RS485_BEFORE_SEND_DELAY_FLAG = "BeforeSendDelay";

	/** The RS-485 after send delay flag. */
	String RS485_AFTER_SEND_DELAY_FLAG = "AfterSendDelay";

	/**
	 * Get the baud rate.
	 *
	 * @return the baud rate; defaults to {@link #DEFAULT_BAUD_RATE}
	 */
	default int getBaudRate() {
		return DEFAULT_BAUD_RATE;
	}

	/**
	 * Get the data bits.
	 *
	 * @return the data bits; defaults to {@link #DEFAULT_DATA_BITS}
	 */
	default int getDataBits() {
		return DEFAULT_DATA_BITS;
	}

	/**
	 * Get the stop bits.
	 *
	 * @return the stop bits; defaults to {@link #DEFAULT_STOP_BITS}
	 */
	default SerialStopBits getStopBits() {
		return DEFAULT_STOP_BITS;
	}

	/**
	 * Get the parity.
	 *
	 * @return the parity; defaults to {@link #DEFAULT_PARITY}
	 */
	default SerialParity getParity() {
		return DEFAULT_PARITY;
	}

	/**
	 * Get the flow control.
	 *
	 * @return the flow control set, or {@literal null} for no flow control
	 */
	default Set<SerialFlowControl> getFlowControl() {
		return null;
	}

	/**
	 * Get the wait time.
	 *
	 * @return the amount of time to wait between opening the serial port and
	 *         configuring its settings, in milliseconds; defaults to
	 *         {@literal 0}
	 */
	default int getWaitTime() {
		return 0;
	}

	/**
	 * Get the read timeout.
	 *
	 * @return the maximum amount of time to wait for data, in milliseconds;
	 *         defaults to {@link #DEFAULT_READ_TIMEOUT}
	 */
	default int getReadTimeout() {
		return DEFAULT_READ_TIMEOUT;
	}

	/**
	 * Get a "shortcut" bits value in the form {@literal DPS} for data bits,
	 * parity, and stop bits.
	 *
	 * @return the shortcut
	 */
	default String bitsShortcut() {
		SerialParity p = getParity();
		SerialStopBits s = getStopBits();
		return String.format("%d%s%d", getDataBits(), (p != null ? p : DEFAULT_PARITY).getAbbreviation(),
				(s != null ? s : DEFAULT_STOP_BITS).getCode());
	}

	/**
	 * Get the RS-485 mode.
	 *
	 * <p>
	 * When this is set to {@literal true} then the other {@code getRs485*}
	 * settings are used. This is only needed for some RS-485 hardware, and the
	 * other {@code getRs485*} settings may not be supported across all
	 * platforms.
	 * </p>
	 *
	 * @return {@literal true} to enable RS-485 mode
	 */
	default Boolean getRs485ModeEnabled() {
		return null;
	}

	/**
	 * Get the RS-485 RTS "high" mode.
	 *
	 * <p>
	 * This setting is only applicable when {@link #getRs485ModeEnabled()} is
	 * {@literal true}.
	 * </p>
	 *
	 * @return {@literal true} to set the RTS line high (to 1) when
	 *         transmitting; defaults to {@link #DEFAULT_RS485_RTS_HIGH_ENABLED}
	 */
	default boolean isRs485RtsHighEnabled() {
		return DEFAULT_RS485_RTS_HIGH_ENABLED;
	}

	/**
	 * Get the RS-485 termination mode.
	 *
	 * <p>
	 * This setting is only applicable when {@link #getRs485ModeEnabled()} is
	 * {@literal true}.
	 * </p>
	 *
	 * @return {@literal true} to enable RS-485 bus termination; defaults to
	 *         {@literal false}
	 */
	default boolean isRs485TerminationEnabled() {
		return false;
	}

	/**
	 * Get the RS-485 "echo" mode.
	 *
	 * <p>
	 * This setting is only applicable when {@link #getRs485ModeEnabled()} is
	 * {@literal true}.
	 * </p>
	 *
	 * @return {@literal true} to enable receive during transmit; defaults to
	 *         {@literal false}
	 */
	default boolean isRs485EchoEnabled() {
		return false;
	}

	/**
	 * Get a time to wait after enabling transmit mode before sending data when
	 * in RS-485 mode.
	 *
	 * <p>
	 * This setting is only applicable when {@link #getRs485ModeEnabled()} is
	 * {@literal true}.
	 * </p>
	 *
	 * @return the delay, in microseconds; defaults to
	 *         {@link #DEFAULT_RS485_BEFORE_SEND_DELAY}
	 */
	default int getRs485BeforeSendDelay() {
		return DEFAULT_RS485_BEFORE_SEND_DELAY;
	}

	/**
	 * Get a time to wait after sending data before disabling transmit mode when
	 * in RS-485 mode
	 *
	 * <p>
	 * This setting is only applicable when {@link #getRs485ModeEnabled()} is
	 * {@literal true}.
	 * </p>
	 *
	 * @return the delay, in microseconds; defaults to
	 *         {@link #DEFAULT_RS485_AFTER_SEND_DELAY}
	 */
	default int getRs485AfterSendDelay() {
		return DEFAULT_RS485_AFTER_SEND_DELAY;
	}

	/**
	 * Get a comma-delimited list of the enabled RS-485 settings, using the
	 * {@code RS485_*_FLAG} constants.
	 *
	 * @return the delimited list, or {@literal null} if no settings are enabled
	 */
	default String rs485Flags() {
		StringBuilder tmp = new StringBuilder();
		if ( isRs485RtsHighEnabled() ) {
			tmp.append(RS485_RTS_HIGH_FLAG);
		}
		if ( isRs485TerminationEnabled() ) {
			if ( tmp.length() > 0 ) {
				tmp.append(",");
			}
			tmp.append(RS485_TERMINATION_FLAG);
		}
		if ( isRs485EchoEnabled() ) {
			if ( tmp.length() > 0 ) {
				tmp.append(",");
			}
			tmp.append(RS485_ECHO_FLAG);
		}
		if ( getRs485BeforeSendDelay() > 0 ) {
			if ( tmp.length() > 0 ) {
				tmp.append(",");
			}
			tmp.append(RS485_BEFORE_SEND_DELAY_FLAG).append('=').append(getRs485BeforeSendDelay());
		}
		if ( getRs485AfterSendDelay() > 0 ) {
			if ( tmp.length() > 0 ) {
				tmp.append(",");
			}
			tmp.append(RS485_AFTER_SEND_DELAY_FLAG).append('=').append(getRs485AfterSendDelay());
		}
		return (tmp.length() > 0 ? tmp.toString() : null);
	}

}
