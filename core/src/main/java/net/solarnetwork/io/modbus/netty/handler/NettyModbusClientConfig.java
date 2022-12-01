/* ==================================================================
 * NettyModbusClientConfig.java - 29/11/2022 4:10:47 pm
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

package net.solarnetwork.io.modbus.netty.handler;

import net.solarnetwork.io.modbus.ModbusClientConfig;

/**
 * Netty implementation of {@link ModbusClientConfig}.
 *
 * @author matt
 * @version 1.0
 */
public abstract class NettyModbusClientConfig implements ModbusClientConfig {

	private boolean autoReconnect = DEFAULT_AUTO_RECONNECT;
	private long autoReconnectDelaySeconds = DEFAULT_RECONNECT_DELAY_SECS;

	@Override
	public boolean isAutoReconnect() {
		return autoReconnect;
	}

	@Override
	public long getAutoReconnectDelaySeconds() {
		return autoReconnectDelaySeconds;
	}

	/**
	 * Set the automatic reconnection setting.
	 * 
	 * @param autoReconnect
	 *        {@literal true} to automatically reconnect
	 */
	public void setAutoReconnect(boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
	}

	/**
	 * Set the automatic reconnection delay seconds.
	 * 
	 * @param autoReconnectDelaySeconds
	 *        the delay (in seconds) to set
	 */
	public void setAutoReconnectDelaySeconds(long autoReconnectDelaySeconds) {
		this.autoReconnectDelaySeconds = autoReconnectDelaySeconds;
	}

}
