/* ==================================================================
 * NettyRtuModbusClientConfig.java - 2/12/2022 10:32:39 am
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

package net.solarnetwork.io.modbus.rtu.netty;

import net.solarnetwork.io.modbus.netty.handler.NettyModbusClientConfig;
import net.solarnetwork.io.modbus.rtu.RtuModbusClientConfig;
import net.solarnetwork.io.modbus.serial.SerialParameters;

/**
 * Netty implementation of {@link RtuModbusClientConfig}.
 *
 * @author matt
 * @version 1.0
 */
public class NettyRtuModbusClientConfig extends NettyModbusClientConfig
		implements RtuModbusClientConfig {

	private String name;
	private SerialParameters serialParameters;

	/**
	 * Constructor.
	 */
	public NettyRtuModbusClientConfig() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        the serial device name
	 * @param serialParameters
	 *        the serial device parameters
	 */
	public NettyRtuModbusClientConfig(String name, SerialParameters serialParameters) {
		super();
		setName(name);
		setSerialParameters(serialParameters);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the serial device name.
	 * 
	 * @param name
	 *        the name to set, for example {@literal /dev/ttyUSB0} or
	 *        {@literal COM1}
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public SerialParameters getSerialParameters() {
		return serialParameters;
	}

	/**
	 * Set the serial parameters.
	 * 
	 * @param serialParameters
	 *        the serial parameters to set
	 */
	public void setSerialParameters(SerialParameters serialParameters) {
		this.serialParameters = serialParameters;
	}

}
