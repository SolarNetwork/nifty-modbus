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

}
