/* ==================================================================
 * ModbusByteUtilsTests.java - 27/11/2022 7:28:59 am
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

package net.solarnetwork.io.modbus.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusByteUtils;

/**
 * Test cases for the {@link ModbusByteUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusByteUtilsTests {

	@Test
	public void decode_null() {
		// WHEN
		short[] r = ModbusByteUtils.decode(null);

		// THEN
		assertThat("Null signed data when null input data", r, is(nullValue()));
	}

	@Test
	public void decode_empty() {
		// WHEN
		short[] r = ModbusByteUtils.decode(new byte[0]);

		// THEN
		assertThat("Empty signed data when empty input data", Arrays.equals(r, new short[0]),
				is(equalTo(true)));
	}

	@Test
	public void decode_odd() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModbusByteUtils.decode(new byte[] { 1 });
		}, "Not allowed to decode with an odd-length data array");
	}

	@Test
	public void decode() {
		// GIVEN
		// @formatter:off
		byte[] data = new byte[] {
				(byte)0xAB,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x12,
				(byte)0x34,
				(byte)0x00,
		};
		// @formatter:on

		// WHEN
		short[] r = ModbusByteUtils.decode(data);

		// THEN
		// @formatter:off
		assertThat("Signed data extracted", Arrays.equals(r, new short[] {
				(short)0xABCD,
				(short)0x0012,
				(short)0x3400,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeUnsigned_null() {
		// WHEN
		int[] r = ModbusByteUtils.decodeUnsigned(null);

		// THEN
		assertThat("Null unsigned data when no data", r, is(nullValue()));
	}

	@Test
	public void decodeUnsigned_empty() {
		// WHEN
		int[] r = ModbusByteUtils.decodeUnsigned(new byte[0]);

		// THEN
		assertThat("Null unsigned data when no data", Arrays.equals(r, new int[0]), is(equalTo(true)));
	}

	@Test
	public void decodeUnsigned_odd() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModbusByteUtils.decodeUnsigned(new byte[] { 1 });
		}, "Not allowed to decode with an odd-length data array");
	}

	@Test
	public void decodeUnsigned() {
		// GIVEN
		// @formatter:off
		byte[] data = new byte[] {
				(byte)0xAB,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x12,
				(byte)0x34,
				(byte)0x00,
		};
		// @formatter:on

		// WHEN
		int[] r = ModbusByteUtils.decodeUnsigned(data);

		// THEN
		// @formatter:off
		assertThat("Unsigned data extracted", Arrays.equals(r, new int[] {
				0xABCD,
				0x0012,
				0x3400,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void encode_null() {
		// WHEN
		byte[] d = ModbusByteUtils.encode(null);

		// THEN
		assertThat("Null data when null input", d, is(nullValue()));
	}

	@Test
	public void encode_empty() {
		// WHEN
		byte[] d = ModbusByteUtils.encode(new short[0]);

		// THEN
		assertThat("Empty data when empty input", Arrays.equals(d, new byte[0]), is(equalTo(true)));
	}

	@Test
	public void encode() {
		// GIVEN
		// @formatter:off
		short[] r = new short[] {
				(short)0xABCD,
				(short)0x0012,
				(short)0x3400,
		};
		// @formatter:on

		// WHEN
		byte[] data = ModbusByteUtils.encode(r);

		// THEN
		// @formatter:off
		byte[] expected = new byte[] {
				(byte)0xAB,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x12,
				(byte)0x34,
				(byte)0x00,
		};
		assertThat("Data encoded", Arrays.equals(data, expected), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void encodeUnsigned_null() {
		// WHEN
		byte[] d = ModbusByteUtils.encodeUnsigned(null);

		// THEN
		assertThat("Null data when null input", d, is(nullValue()));
	}

	@Test
	public void encodeUnsigned_empty() {
		// WHEN
		byte[] d = ModbusByteUtils.encodeUnsigned(new int[0]);

		// THEN
		assertThat("Empty data when empty input", Arrays.equals(d, new byte[0]), is(equalTo(true)));
	}

	@Test
	public void encodeUnsigned() {
		// GIVEN
		// @formatter:off
		int[] r = new int[] {
				0xABCD,
				0x0012,
				0x3400,
		};
		// @formatter:on

		// WHEN
		byte[] data = ModbusByteUtils.encodeUnsigned(r);

		// THEN
		// @formatter:off
		byte[] expected = new byte[] {
				(byte)0xAB,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x12,
				(byte)0x34,
				(byte)0x00,
		};
		assertThat("Data encoded", Arrays.equals(data, expected), is(equalTo(true)));
		// @formatter:on
	}

}
