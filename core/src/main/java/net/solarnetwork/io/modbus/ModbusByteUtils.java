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

	/* Table of CRC values for high-order bytes. */
	private final static short[] CRC_HI = { 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
			0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00,
			0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
			0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80,
			0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
			0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01,
			0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
			0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80,
			0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
			0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00,
			0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
			0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
			0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
			0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00,
			0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
			0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81,
			0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
			0x80, 0x41, 0x00, 0xC1, 0x81, 0x40 };

	/* Table of CRC values for low-order bytes. */
	private final static short[] CRC_LO = { 0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06,
			0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04, 0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E, 0x0A,
			0xCA, 0xCB, 0x0B, 0xC9, 0x09, 0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A,
			0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC, 0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16,
			0xD6, 0xD2, 0x12, 0x13, 0xD3, 0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3,
			0xF2, 0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4, 0x3C, 0xFC, 0xFD, 0x3D, 0xFF,
			0x3F, 0x3E, 0xFE, 0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29,
			0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25,
			0xE5, 0x27, 0xE7, 0xE6, 0x26, 0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60,
			0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4, 0x6C,
			0xAC, 0xAD, 0x6D, 0xAF, 0x6F, 0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
			0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC,
			0x7C, 0xB4, 0x74, 0x75, 0xB5, 0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71,
			0x70, 0xB0, 0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92, 0x96, 0x56, 0x57, 0x97, 0x55,
			0x95, 0x94, 0x54, 0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B,
			0x99, 0x59, 0x58, 0x98, 0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F,
			0x4F, 0x8D, 0x4D, 0x4C, 0x8C, 0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42,
			0x43, 0x83, 0x41, 0x81, 0x80, 0x40 };

	/**
	 * Compute a 16-bit cyclic redundancy check (CRC) value from a range of
	 * bytes.
	 * 
	 * @param data
	 *        the data to compute the CRC value
	 * @param start
	 *        the starting index (inclusive)
	 * @param end
	 *        the ending index (exclusive)
	 * @return the computed CRC value
	 */
	public static final short computeCrc(byte[] data, int start, int end) {
		int hi = 0xFF;
		int lo = 0xFF;
		int next = 0;
		int uIndex;
		for ( int i = start, len = data.length; i < end && i < len; i++ ) {
			next = 0xFF & data[i];
			uIndex = lo ^ next;
			lo = hi ^ CRC_HI[uIndex];
			hi = CRC_LO[uIndex];
		}
		return (short) ((hi << 8) | lo);
	}

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
		for ( int i = fromIndex, len = data.length; i < toIndex && i < len; i++ ) {
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
