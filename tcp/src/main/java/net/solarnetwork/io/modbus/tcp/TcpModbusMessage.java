/* ==================================================================
 * TcpModbusMessage.java - 25/11/2022 3:31:56 pm
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

package net.solarnetwork.io.modbus.tcp;

import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;

/**
 * TCP encapsulated Modbus message API.
 *
 * @author matt
 * @version 1.0
 */
public interface TcpModbusMessage extends ModbusMessage {

	/** The maximum transaction ID value. */
	int MAX_TRANSACTION_ID = 0xFFFF;

	/**
	 * Get a message creation date.
	 * 
	 * @return the message creation date
	 */
	long getTimestamp();

	/**
	 * Get the transaction identifier.
	 * 
	 * @return the transaction identifier
	 */
	int getTransactionId();

	/**
	 * Get the protocol identifier.
	 * 
	 * <p>
	 * This implementation returns {@literal 0} for Modbus/TCP.
	 * </p>
	 * 
	 * @return the protocol identifier
	 */
	default int getProtocolId() {
		return 0;
	}

	@Override
	default TcpModbusMessage validate() throws ModbusValidationException {
		return this;
	}

}
