/* ==================================================================
 * SerialAddress.java - 2/12/2022 8:54:48 am
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

import java.net.SocketAddress;

/**
 * A {@link SocketAddress} subclass to wrap a serial port device such as
 * {@literal COM1} or {@literal /dev/ttyUSB0}.
 *
 * @author matt
 * @version 1.0
 */
public class SerialAddress extends SocketAddress {

	private static final long serialVersionUID = -1162052149683962663L;

	/** The serial port name. */
	private final String name;

	/**
	 * Creates an address representing the name of a serial port.
	 *
	 * @param name
	 *        the name of the device, such as {@literal COM1} or
	 *        {@literal /dev/ttyUSB0}
	 */
	public SerialAddress(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the serial port.
	 * 
	 * @return the serial port name
	 */
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
