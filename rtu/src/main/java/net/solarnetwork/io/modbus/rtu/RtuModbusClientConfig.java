/* ==================================================================
 * RtuModbusClientConfig.java - 2/12/2022 10:33:13 am
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

package net.solarnetwork.io.modbus.rtu;

import net.solarnetwork.io.modbus.ModbusClientConfig;
import net.solarnetwork.io.modbus.serial.SerialParameters;

/**
 * RTU Modbus client configuration.
 *
 * @author matt
 * @version 1.0
 */
public interface RtuModbusClientConfig extends ModbusClientConfig {

	/**
	 * Get the name of the serial device to connect to, such as
	 * {@literal /dev/ttyUSB0} or {@literal COM1}.
	 * 
	 * @return the serial device name
	 */
	String getName();

	/**
	 * Get the serial device parameters to use.
	 * 
	 * @return the serial parameters
	 */
	SerialParameters getSerialParameters();

	/**
	 * Get the serial device name.
	 * 
	 * <p>
	 * This implementation returns {@link #getName()}.
	 * </p>
	 */
	@Override
	default String getDescription() {
		return getName();
	}

}
