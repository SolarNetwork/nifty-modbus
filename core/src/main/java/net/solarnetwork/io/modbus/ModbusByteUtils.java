/* ==================================================================
 * ModbusByteUtils.java - 27/11/2022 7:24:35 am
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
 * Utilities for Modbus byte manipulation.
 *
 * @author matt
 * @version 1.0
 */
public final class ModbusByteUtils {

	private ModbusByteUtils() {
		// not available
	}

	/**
	 * Get raw data from unsigned 16-bit values.
	 * 
	 * @param registers
	 *        the 16-bit register values, treated as unsigned values
	 * @return the raw data, or {@literal null} if {@code registers} is
	 *         {@literal null}
	 */
	public static byte[] encode(short[] registers) {
		if ( registers == null ) {
			return null;
		}
		final int len = registers.length;
		byte[] data = new byte[len * 2];
		encode(registers, data, 0);
		return data;
	}

	/**
	 * Get raw data from unsigned 16-bit values.
	 * 
	 * @param registers
	 *        the 16-bit register values, treated as unsigned values
	 * @param dest
	 *        the destination data
	 * @param offset
	 *        the offset within {@code dest} to start at
	 */
	public static void encode(short[] registers, byte[] dest, int offset) {
		if ( registers == null ) {
			return;
		}
		final int len = registers.length;
		for ( int i = 0, d = offset; i < len; i++, d += 2 ) {
			dest[d] = (byte) (registers[i] >>> 8 & 0xFF);
			dest[d + 1] = (byte) (registers[i] & 0xFF);
		}
	}

	/**
	 * Get raw data from unsigned 16-bit values.
	 * 
	 * @param registers
	 *        the 16-bit register values, treated as unsigned 16-bit values
	 * @return the raw data, or {@literal null} if {@code registers} is
	 *         {@literal null}
	 */
	public static byte[] encodeUnsigned(int[] registers) {
		if ( registers == null ) {
			return null;
		}
		final int len = registers.length;
		byte[] data = new byte[len * 2];
		for ( int i = 0, d = 0; i < len; i++, d += 2 ) {
			data[d] = (byte) (registers[i] >>> 8 & 0xFF);
			data[d + 1] = (byte) (registers[i] & 0xFF);
		}
		return data;
	}

	/**
	 * Get raw data as signed 16-bit values.
	 * 
	 * @param data
	 *        the data
	 * @return a copy of the data as an array of signed 16-bit values in
	 *         most-to-least (big endian) byte order, or {@literal null} if
	 *         {@code data} is {@literal null}
	 */
	public static short[] decode(byte[] data) {
		if ( data == null ) {
			return null;
		}
		if ( data.length % 2 != 0 ) {
			throw new IllegalArgumentException("The byte data has an odd length, but it must be even.");
		}
		short[] r = new short[data.length / 2];
		for ( int i = 0, d = 0; i < r.length; i++, d += 2 ) {
			r[i] = (short) (((data[d] & 0xFF) << 8) | data[d + 1] & 0xFF);
		}
		return r;
	}

	/**
	 * Get raw data as unsigned 16-bit values.
	 * 
	 * @param data
	 *        the data
	 * @return a copy of the data as an array of unsigned 16-bit values in
	 *         most-to-least (big endian) byte order, represented as 32-bit int
	 *         values to maintain as unsigned values, or {@literal null} if
	 *         {@code data} is {@literal null}
	 */
	public static int[] decodeUnsigned(byte[] data) {
		if ( data == null ) {
			return null;
		}
		if ( data.length % 2 != 0 ) {
			throw new IllegalArgumentException("The byte data has an odd length, but it must be even.");
		}
		int[] r = new int[data.length / 2];
		for ( int i = 0, d = 0; i < r.length; i++, d += 2 ) {
			r[i] = (((data[d] & 0xFF) << 8) | data[d + 1] & 0xFF);
		}
		return r;
	}

	/**
	 * Reverse a byte array.
	 * <p>
	 * This modifies the contents of the array directly.
	 * </p>
	 * 
	 * @param data
	 *        the data to reverse
	 */
	public static void reverse(final byte[] data) {
		if ( data == null ) {
			return;
		}
		for ( int i = 0, max = data.length / 2; i < max; i++ ) {
			byte temp = data[i];
			data[i] = data[data.length - i - 1];
			data[data.length - i - 1] = temp;
		}
	}

	/**
	 * Encode a 16-bit value into a byte array.
	 * 
	 * @param data
	 *        the data array
	 * @param offset
	 *        the offset within {@code data} to start populating
	 * @param value
	 *        the value to populate
	 */
	public static void encode16(final byte[] data, final int offset, final int value) {
		data[offset] = (byte) ((value >>> 8) & 0xFF);
		data[offset + 1] = (byte) (value & 0xFF);
	}

}
