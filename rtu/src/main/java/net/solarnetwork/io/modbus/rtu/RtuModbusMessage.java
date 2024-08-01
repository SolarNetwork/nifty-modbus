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

import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;

/**
 * RTU encapsulated Modbus message API.
 *
 * @author matt
 * @version 1.0
 */
public interface RtuModbusMessage extends ModbusMessage {

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
	 * @return {@literal true} if {@link #getCrc()} and {@link #computeCrc()}
	 *         return the same value
	 */
	default boolean isCrcValid() {
		final short provided = getCrc();
		final short computed = computeCrc();
		return (provided == computed);
	}

	@Override
	default void validate() throws ModbusValidationException {
		final short provided = getCrc();
		final short computed = computeCrc();
		if ( provided != computed ) {
			throw new ModbusValidationException(String.format(
					"CRC mismatch: got 0x%X but computed 0x%X from message data.", provided, computed));
		}
	}

}
