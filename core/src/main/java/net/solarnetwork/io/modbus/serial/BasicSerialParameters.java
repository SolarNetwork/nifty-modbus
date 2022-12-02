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
	private int waitTime;
	private int readTimeout = DEFAULT_READ_TIMEOUT;

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

}
