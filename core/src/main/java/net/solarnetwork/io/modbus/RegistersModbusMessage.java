/* ==================================================================
 * RegistersModbusMessage.java - 27/11/2022 12:12:01 pm
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
 * A Modbus message related to a 16-bit register-based (input/holding) register
 * address range.
 *
 * @author matt
 * @version 1.0
 */
public interface RegistersModbusMessage extends AddressedModbusMessage {

	/**
	 * Get a copy of the raw register data.
	 * 
	 * <p>
	 * This returns a new copy of the register data.
	 * </p>
	 * 
	 * @return the raw register data copy, or {@literal null} if there is no
	 *         data
	 */
	byte[] dataCopy();

	/**
	 * Get the register data as signed 16-bit values.
	 * 
	 * @return a copy of the register data as an array of signed 16-bit values,
	 *         or {@literal null} if there is no data
	 */
	short[] dataDecode();

	/**
	 * Get the register data as unsigned 16-bit values.
	 * 
	 * @return a copy of the register data as an array of unsigned 16-bit
	 *         values, or {@literal null} if there is no data
	 */
	int[] dataDecodeUnsigned();

}
