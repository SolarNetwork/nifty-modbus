/* ==================================================================
 * ModbusTestUtils.java - 28/11/2022 5:36:40 am
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

package net.solarnetwork.io.modbus.netty.test;

/**
 * Utilities to aid with testing.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusTestUtils {

	/**
	 * Convert an array of bytes to Byte objects.
	 * 
	 * @param array
	 *        the array to convert
	 * @return the converted array, or {@literal null} if {@code array} is
	 *         {@literal null}
	 */
	public static Byte[] byteObjectArray(byte[] array) {
		if ( array == null ) {
			return null;
		}
		final int count = array.length;
		final Byte[] result = new Byte[count];
		for ( int i = 0; i < count; i++ ) {
			result[i] = array[i];
		}
		return result;
	}

}
