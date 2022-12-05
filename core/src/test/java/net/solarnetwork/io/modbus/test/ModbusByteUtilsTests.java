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
	public void encodeHexString_null() {
		byte[] b = new byte[0];
		String r = ModbusByteUtils.encodeHexString(b, 0, b.length, false);
		assertThat("Empty range encodes empty string", r, equalTo(""));
	}

	@Test
	public void encodeHexString_basic() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 0, b.length, false);
		assertThat("Bytes encode to hex string", r, equalTo("1199FF"));
	}

	@Test
	public void encodeHexString_basicLowerCase() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 0, b.length, false, true);
		assertThat("Bytes encode to hex string", r, equalTo("1199ff"));
	}

	@Test
	public void encodeHexString_basicWithSpace() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 0, b.length, true);
		assertThat("Bytes encode to hex string", r, equalTo("11 99 FF"));
	}

	@Test
	public void encodeHexString_subset() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 1, b.length);
		assertThat("Bytes subrange encode to hex string", r, equalTo("99FF"));
	}

	@Test
	public void encodeHexString_subset_null() {
		String r = ModbusByteUtils.encodeHexString(null, 1, 2);
		assertThat("Bytes subrange encode to hex string null returns empty", r, equalTo(""));
	}

	@Test
	public void encodeHexString_subset_empty() {
		String r = ModbusByteUtils.encodeHexString(new byte[0], 1, 2);
		assertThat("Bytes subrange encode to hex string empty returns empty", r, equalTo(""));
	}

	@Test
	public void encodeHexString_subset_lower() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 1, b.length, false, true);
		assertThat("Bytes subrange encode to hex string lowercase", r, equalTo("99ff"));
	}

	@Test
	public void encodeHexString_subsetWithSpace() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 1, b.length, true);
		assertThat("Bytes subrange encode to hex string spaced", r, equalTo("99 FF"));
	}

	@Test
	public void encodeHexString_subsetWithSpace_lower() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 1, b.length, true, true);
		assertThat("Bytes subrange encode to hex string spaced lowercase", r, equalTo("99 ff"));
	}

	@Test
	public void encodeHexString_subset_overshoot() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 1, 99);
		assertThat("Bytes subrange encode to hex string overshoot returns subset", r, equalTo("99FF"));
	}

	@Test
	public void encodeHexString_subset_negative_start() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, -1, 99);
		assertThat("Bytes subrange encode to hex string negative start returns empty", r, equalTo(""));
	}

	@Test
	public void encodeHexString_subset_overshoot_start() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 99, 99);
		assertThat("Bytes subrange encode to hex string overshoot start returns empty", r, equalTo(""));
	}

	@Test
	public void encodeHexString_subset_negative_end() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 1, -1);
		assertThat("Bytes subrange encode to hex string negative end returns empty", r, equalTo(""));
	}

	@Test
	public void encodeHexString_subset_undershoot_end() {
		byte[] b = new byte[] { (byte) 0x11, (byte) 0x99, (byte) 0xFF };
		String r = ModbusByteUtils.encodeHexString(b, 2, 1);
		assertThat("Bytes subrange encode to hex string undershoot end returns empty", r, equalTo(""));
	}

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
	public void decode_range() {
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
		short[] r = ModbusByteUtils.decode(data, 2, data.length);

		// THEN
		// @formatter:off
		assertThat("Signed data range extracted", Arrays.equals(r, new short[] {
				(short)0x0012,
				(short)0x3400,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decode_range_null() {
		// WHEN
		short[] r = ModbusByteUtils.decode(null, 0, 1);

		// THEN
		assertThat("Null signed data when null input data", r, is(nullValue()));
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
	public void decodeUnsigned_range() {
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
		int[] r = ModbusByteUtils.decodeUnsigned(data, 2, data.length);

		// THEN
		// @formatter:off
		assertThat("Unsigned data range extracted", Arrays.equals(r, new int[] {
				0x0012,
				0x3400,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeUnsigned_range_null() {
		// WHEN
		int[] r = ModbusByteUtils.decodeUnsigned(null, 0, 1);

		// THEN
		assertThat("Null unsigned data when no data", r, is(nullValue()));
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
	public void encode_range() {
		// GIVEN
		// @formatter:off
		short[] r = new short[] {
				(short)0xABCD,
				(short)0x0012,
				(short)0x3400,
		};
		// @formatter:on
		byte[] dest = new byte[8];

		// WHEN
		ModbusByteUtils.encode(r, dest, 2);

		// THEN
		// @formatter:off
		byte[] expected = new byte[] {
				0x00,
				0x00,
				(byte)0xAB,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x12,
				(byte)0x34,
				(byte)0x00,
		};
		assertThat("Data encoded", Arrays.equals(dest, expected), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void encode_range_null() {
		// GIVEN
		byte[] dest = new byte[] { (byte) 0xFF, (byte) 0xFF };

		// WHEN
		ModbusByteUtils.encode(null, dest, 1);

		// THEN
		// @formatter:off
		byte[] expected = new byte[] {
				(byte)0xFF,
				(byte)0xFF,
		};
		assertThat("Null encoded", Arrays.equals(dest, expected), is(equalTo(true)));
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

	@Test
	public void computeCrc() {
		// GIVEN
		final byte[] data = new byte[] { 0x01, 0x04, 0x02, (byte) 0xFF, (byte) 0xFF };
		assertThat("CRC computed", ModbusByteUtils.computeCrc(data, 0, data.length),
				is(equalTo((short) 0x80B8)));
	}

	@Test
	public void computeCrc_range() {
		// GIVEN
		final byte[] data = new byte[] { 0x01, 0x04, 0x02, (byte) 0xFF, (byte) 0xFF, 0x01, 0x02 };
		assertThat("CRC computed range", ModbusByteUtils.computeCrc(data, 0, 5),
				is(equalTo((short) 0x80B8)));
	}

	@Test
	public void computeCrc_range_cutshort() {
		// GIVEN
		final byte[] data = new byte[] { 0x01, 0x04, 0x02, (byte) 0xFF, (byte) 0xFF };
		assertThat("CRC computed range", ModbusByteUtils.computeCrc(data, 0, 99),
				is(equalTo((short) 0x80B8)));
	}

	@Test
	public void reverse() {
		// GIVEN
		final byte[] data = new byte[] { 0x01, 0x04, 0x02, (byte) 0xFF, (byte) 0xFF };

		// WHEN
		ModbusByteUtils.reverse(data);

		// THEN
		assertThat("Data reversed",
				Arrays.equals(data, new byte[] { (byte) 0xFF, (byte) 0xFF, 0x02, 0x04, 0x01 }),
				is(equalTo(true)));
	}

	@Test
	public void reverse_empty() {
		// GIVEN
		final byte[] data = new byte[0];

		// WHEN
		ModbusByteUtils.reverse(data);

		// THEN
		assertThat("Data reversed", Arrays.equals(data, new byte[0]), is(equalTo(true)));
	}

	@Test
	public void reverse_null() {
		ModbusByteUtils.reverse(null);
	}

}
