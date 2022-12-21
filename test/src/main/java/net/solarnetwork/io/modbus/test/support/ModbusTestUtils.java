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

package net.solarnetwork.io.modbus.test.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities to aid with testing.
 *
 * @author matt
 * @version 1.0
 */
public final class ModbusTestUtils {

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

	/**
	 * A pattern for matching the data portion of a Netty ByteBuf wire log
	 * message.
	 * 
	 * <p>
	 * The messages appear like:
	 * </p>
	 * 
	 * <pre>{@code
	 * |00000000| 00 00 3f 7f be 77 3f 7f be 77 3f 7f be 77 00 00 |..?..w?..w?..w..|
	 * }</pre>
	 */
	public static final Pattern WIRE_LOG_LINE_PATTERN = Pattern
			.compile("\\|\\d+\\|([0-9A-Fa-f ]+)\\|.*");

	/**
	 * Decode the byte content from Netty ByteBuf wire log messages.
	 * 
	 * @param in
	 *        the wire log messages to parse
	 * @param hexDecoder
	 *        a function that decodes hex strings into bytes
	 * @return the decoded data
	 * @throws IOException
	 *         if an IO error occurs
	 */
	public static List<byte[]> parseWireLogLines(BufferedReader in, Function<String, byte[]> hexDecoder)
			throws IOException {
		String line = null;
		List<byte[]> result = new ArrayList<>(8);
		while ( (line = in.readLine()) != null ) {
			Matcher m = WIRE_LOG_LINE_PATTERN.matcher(line);
			if ( m.matches() ) {
				String hex = m.group(1).replace(" ", "");
				result.add(hexDecoder.apply(hex));
			}
		}
		return result;
	}

}
