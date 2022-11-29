/* ==================================================================
 * ModbusMessageReply.java - 29/11/2022 8:00:34 am
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

package net.solarnetwork.io.modbus;

/**
 * A Modbus request/response pair.
 * 
 * <p>
 * Instances of this API should be used when replying to a Modbus request.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public interface ModbusMessageReply extends ModbusMessage {

	/**
	 * Get the original request message this message is a reply to.
	 * 
	 * @return the request message
	 */
	ModbusMessage getRequest();

}
