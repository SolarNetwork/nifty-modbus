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

	private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F' };
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f' };

	/**
	 * Encode a single byte as a character.
	 * 
	 * @param b
	 *        the byte to encode
	 * @param toDigits
	 *        the alphabet to use
	 * @param dest
	 *        the destination character buffer to write the encoded char to
	 * @param destIndex
	 *        the index within {@code dest} to write the encoded char at, along
	 *        with {@code destIndex + 1}
	 * @return the {@code dest} array
	 */
	public static char[] encodeChar(final byte b, final char[] toDigits, final char[] dest,
			int destIndex) {
		dest[destIndex] = toDigits[(0xF0 & b) >>> 4];
		dest[destIndex + 1] = toDigits[0x0F & b];
		return dest;
	}

	/**
	 * Encode a byte array into a hex-encoded upper-case string without spaces.
	 * 
	 * @param data
	 *        the data to encode as hex strings
	 * @param fromIndex
	 *        the starting index within {@code data} to encode (inclusive)
	 * @param toIndex
	 *        the ending index within {@code data} to encode (exclusive)
	 * @return the string, never {@literal null}
	 */
	public static String encodeHexString(final byte[] data, final int fromIndex, final int toIndex) {
		return encodeHexString(data, fromIndex, toIndex, false, false);
	}

	/**
	 * Encode a byte array into a hex-encoded upper-case string.
	 * 
	 * @param data
	 *        the data to encode as hex strings
	 * @param fromIndex
	 *        the starting index within {@code data} to encode (inclusive)
	 * @param toIndex
	 *        the ending index within {@code data} to encode (exclusive)
	 * @param space
	 *        {@literal true} to add a single space character between each hex
	 * @return the string, never {@literal null}
	 */
	public static String encodeHexString(final byte[] data, final int fromIndex, final int toIndex,
			final boolean space) {
		return encodeHexString(data, fromIndex, toIndex, space, false);
	}

	/**
	 * Encode a byte array into a hex-encoded string.
	 * 
	 * @param data
	 *        the data to encode as hex strings
	 * @param fromIndex
	 *        the starting index within {@code data} to encode (inclusive)
	 * @param toIndex
	 *        the ending index within {@code data} to encode (exclusive)
	 * @param space
	 *        {@literal true} to add a single space character between each hex
	 * @param lowerCase
	 *        {@literal true} to use lower case, {@literal false} for upper case
	 *        pair
	 * @return the string, never {@literal null}
	 */
	public static String encodeHexString(final byte[] data, final int fromIndex, final int toIndex,
			final boolean space, final boolean lowerCase) {
		if ( data == null || data.length < 1 || fromIndex < 0 || fromIndex >= data.length || toIndex < 0
				|| toIndex <= fromIndex ) {
			return "";
		}
		final char[] digits = (lowerCase ? DIGITS_LOWER : DIGITS_UPPER);
		StringBuilder hexData = new StringBuilder(
				2 * (toIndex - fromIndex) + (space ? (toIndex - fromIndex) : 0));
		char[] buffer = new char[2];
		for ( int i = fromIndex; i < toIndex; i++ ) {
			if ( space && i > fromIndex ) {
				hexData.append(' ');
			}
			hexData.append(encodeChar(data[i], digits, buffer, 0));
		}
		return hexData.toString();
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
		return decode(data, 0, data.length);
	}

	/**
	 * Get raw data as signed 16-bit values.
	 * 
	 * @param data
	 *        the data
	 * @param start
	 *        the starting offset with data, inclusive
	 * @param end
	 *        the ending offset with data, exclusive
	 * @return a copy of the data as an array of signed 16-bit values in
	 *         most-to-least (big endian) byte order, or {@literal null} if
	 *         {@code data} is {@literal null}
	 */
	public static short[] decode(byte[] data, int start, int end) {
		if ( data == null ) {
			return null;
		}
		if ( (end - start) % 2 != 0 ) {
			throw new IllegalArgumentException("The byte range has an odd length, but it must be even.");
		}
		short[] r = new short[(end - start) / 2];
		for ( int i = 0, d = start; i < r.length; i++, d += 2 ) {
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
		return decodeUnsigned(data, 0, data.length);
	}

	/**
	 * Get raw data as unsigned 16-bit values.
	 * 
	 * @param data
	 *        the data
	 * @param start
	 *        the starting offset with data, inclusive
	 * @param end
	 *        the ending offset with data, exclusive
	 * @return a copy of the data as an array of unsigned 16-bit values in
	 *         most-to-least (big endian) byte order, represented as 32-bit int
	 *         values to maintain as unsigned values, or {@literal null} if
	 *         {@code data} is {@literal null}
	 */
	public static int[] decodeUnsigned(byte[] data, int start, int end) {
		if ( data == null ) {
			return null;
		}
		if ( (end - start) % 2 != 0 ) {
			throw new IllegalArgumentException("The byte data has an odd length, but it must be even.");
		}
		int[] r = new int[(end - start) / 2];
		for ( int i = 0, d = start; i < r.length; i++, d += 2 ) {
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
