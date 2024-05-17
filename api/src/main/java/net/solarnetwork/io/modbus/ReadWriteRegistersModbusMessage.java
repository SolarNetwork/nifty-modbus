/* ==================================================================
 * MaskWriteRegisterModbusMessage.java - 27/11/2022 2:43:30 pm
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
 * A Modbus message related to a holding register read/write.
 * 
 * <p>
 * The {@link AddressedModbusMessage#getAddress()} of this message refers to the
 * read address, and {@link AddressedModbusMessage#getCount()} refers to the
 * read count. The {@link RegistersModbusMessage#dataDecode()} and
 * {@link RegistersModbusMessage#dataDecodeUnsigned()} refer to the read
 * registers (only available in responses).
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public interface ReadWriteRegistersModbusMessage extends RegistersModbusMessage {

	/**
	 * Get the write starting address.
	 * 
	 * @return the write starting address
	 */
	int getWriteAddress();

	/**
	 * Get the write register data as signed 16-bit values.
	 * 
	 * @return a copy of the register data as an array of signed 16-bit values,
	 *         or {@literal null} if there is no data
	 */
	short[] writeDataDecode();

	/**
	 * Get the write register data as unsigned 16-bit values.
	 * 
	 * @return a copy of the register data as an array of unsigned 16-bit
	 *         values, or {@literal null} if there is no data
	 */
	int[] writeDataDecodeUnsigned();

}
