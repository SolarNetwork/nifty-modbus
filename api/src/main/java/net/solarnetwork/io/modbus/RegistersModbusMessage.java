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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A Modbus message related to a 16-bit register-based (input/holding) register
 * address range.
 *
 * @author matt
 * @version 1.1
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

	/**
	 * Get the register data as a string.
	 *
	 * @param charset
	 *        the encoding to interpret the bytes as
	 * @return the new string, or {@literal null} if there is no data
	 * @since 1.1
	 */
	default String dataDecodeString(Charset charset) {
		byte[] data = dataCopy();
		if ( data == null ) {
			return null;
		}
		return new String(data, charset);
	}

	/**
	 * Get the register data as a {@code US-ASCII} string.
	 *
	 * @return the new string, or {@literal null} if there is no data
	 * @since 1.1
	 */
	default String dataDecodeAsciiString() {
		return dataDecodeString(StandardCharsets.US_ASCII);
	}

	/**
	 * Get the register data as a {@code ISO-8859-1} (Latin1) string.
	 *
	 * @return the new string, or {@literal null} if there is no data
	 * @since 1.1
	 */
	default String dataDecodeIso88591String() {
		return dataDecodeString(StandardCharsets.ISO_8859_1);
	}

	/**
	 * Get the register data as a {@code UTF-8} string.
	 *
	 * @return the new string, or {@literal null} if there is no data
	 * @since 1.1
	 */
	default String dataDecodeUtf8String() {
		return dataDecodeString(StandardCharsets.UTF_8);
	}

	/**
	 * Get the register data as a {@code UTF-16} (byte-order mark) string.
	 *
	 * @return the new string, or {@literal null} if there is no data
	 * @since 1.1
	 */
	default String dataDecodeUtf16String() {
		return dataDecodeString(StandardCharsets.UTF_16);
	}

	/**
	 * Get the register data as a {@code UTF-16} (big-endian) string.
	 *
	 * @return the new string, or {@literal null} if there is no data
	 * @since 1.1
	 */
	default String dataDecodeUtf16BeString() {
		return dataDecodeString(StandardCharsets.UTF_16BE);
	}

	/**
	 * Get the register data as a {@code UTF-16} (little-endian) string.
	 *
	 * @return the new string, or {@literal null} if there is no data
	 * @since 1.1
	 */
	default String dataDecodeUtf16LeString() {
		return dataDecodeString(StandardCharsets.UTF_16LE);
	}

}
