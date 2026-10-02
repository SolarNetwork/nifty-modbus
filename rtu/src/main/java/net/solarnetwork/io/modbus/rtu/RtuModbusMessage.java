/* ==================================================================
 * RtuModbusMessage.java - 1/12/2022 2:53:32 pm
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

import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;

/**
 * RTU encapsulated Modbus message API.
 *
 * @author matt
 * @version 1.1
 */
public interface RtuModbusMessage extends ModbusMessage {

	/**
	 * The unit ID used to address all devices on a serial network.
	 * 
	 * @since 1.1
	 */
	int BROADCAST_UNIT_ID = 0;

	/**
	 * A {@link ModbusValidationException} message template.
	 * 
	 * <p>
	 * The message accepts two short values: the provided CRC and the computed
	 * CRC.
	 * </p>
	 */
	String CRC_MISMATCH_VALIDATION_MESSAGE = "CRC mismatch: got 0x%X but computed 0x%X from message data.";

	/**
	 * Get a message creation date.
	 * 
	 * @return the message creation date
	 */
	long getTimestamp();

	/**
	 * Get the 16-bit cyclic redundancy check value presented in the RTU message
	 * frame.
	 * 
	 * @return the provided CRC value
	 */
	short getCrc();

	/**
	 * Compute the 16-bit cyclic redundancy check value from the message data.
	 * 
	 * <p>
	 * If the {@link #getCrc()} and this value differ, the message should be
	 * considered corrupted.
	 * </p>
	 * 
	 * @return the computed CRC value
	 */
	short computeCrc();

	/**
	 * Test if the provided and computed CRC values match.
	 * 
	 * @return {@code true} if {@link #getCrc()} and {@link #computeCrc()}
	 *         return the same value
	 */
	default boolean isCrcValid() {
		final short provided = getCrc();
		final short computed = computeCrc();
		return (provided == computed);
	}

	/**
	 * Test if this message is a broadcast.
	 * 
	 * @return {@code true} if this message is a broadcast
	 * @see #isBroadcast(ModbusMessage)
	 * @since 1.1
	 */
	default boolean isBroadcast() {
		return isBroadcast(this);
	}

	/**
	 * Test if a message is a broadcast.
	 * 
	 * <p>
	 * A broadcast is a request to write that is addressed to all devices on a
	 * serial network, by using the {@link #BROADCAST_UNIT_ID} unit ID. Every
	 * device acts on a broadcast request and none of them respond to it. A
	 * request to read, including one that reads and writes, is never a
	 * broadcast as it requires a response.
	 * </p>
	 * 
	 * <p>
	 * As no device responds to a broadcast request, a client can provide a
	 * reply that echoes the request in place of a response. This method returns
	 * {@code true} for such a reply as well, so it can be used to tell that no
	 * device actually responded.
	 * </p>
	 * 
	 * <p>
	 * The message does not have to be a {@link RtuModbusMessage}, so this
	 * method can be used with any request intended for a serial network, or any
	 * response received from one.
	 * </p>
	 * 
	 * @param message
	 *        the message to test
	 * @return {@code true} if the unit ID of {@code message} is
	 *         {@link #BROADCAST_UNIT_ID} and its function is one that only
	 *         writes
	 * @since 1.1
	 */
	static boolean isBroadcast(ModbusMessage message) {
		if ( message.getUnitId() != BROADCAST_UNIT_ID ) {
			return false;
		}
		final ModbusFunction fn = message.getFunction();
		return !fn.isReadFunction() && fn.functionCode() != ModbusFunctionCode.ReadWriteHoldingRegisters;
	}

	@Override
	default RtuModbusMessage validate() throws ModbusValidationException {
		final short provided = getCrc();
		final short computed = computeCrc();
		if ( provided != computed ) {
			throw new ModbusValidationException(
					String.format(CRC_MISMATCH_VALIDATION_MESSAGE, provided, computed));
		}
		return this;
	}

}
