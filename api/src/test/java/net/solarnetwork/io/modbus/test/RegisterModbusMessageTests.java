/* ==================================================================
 * RegisterModbusMessageTests.java - 15/10/2025 3:09:50 pm
 *
 * Copyright 2025 SolarNetwork.net Dev Team
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
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusByteUtils;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.RegistersModbusMessage;

/**
 * Test cases for the {@link RegistersModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class RegisterModbusMessageTests {

	private RegistersModbusMessage msg(final byte[] data) {
		return new RegistersModbusMessage() {

			@Override
			public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
				return null;
			}

			@Override
			public boolean isSameAs(ModbusMessage obj) {
				return false;
			}

			@Override
			public int getUnitId() {
				return 0;
			}

			@Override
			public ModbusFunction getFunction() {
				return null;
			}

			@Override
			public ModbusError getError() {
				return null;
			}

			@Override
			public int getCount() {
				return (data != null ? data.length / 2 : 0);
			}

			@Override
			public int getAddress() {
				return 0;
			}

			@Override
			public int[] dataDecodeUnsigned() {
				return ModbusByteUtils.decodeUnsigned(data);
			}

			@Override
			public short[] dataDecode() {
				return ModbusByteUtils.decode(data);
			}

			@Override
			public byte[] dataCopy() {
				if ( data == null ) {
					return null;
				}
				byte[] copy = new byte[data.length];
				System.arraycopy(data, 0, copy, 0, copy.length);
				return copy;
			}
		};
	}

	@Test
	public void dataDecodeString_null() {
		// GIVEN
		RegistersModbusMessage msg = msg(null);

		// WHEN
		String r = msg.dataDecodeString(StandardCharsets.US_ASCII);

		// THEN
		assertThat("Null string when no data", r, is(nullValue()));
	}

	@Test
	public void dataDecodeString_empty() {
		// GIVEN
		RegistersModbusMessage msg = msg(new byte[0]);

		// WHEN
		String r = msg.dataDecodeString(StandardCharsets.US_ASCII);

		// THEN
		assertThat("Empty string when empty data", r, is(equalTo("")));
	}

	@Test
	public void dataDecodeString_ascii() {
		// GIVEN
		final String src = "hello!";
		byte[] data = src.getBytes(StandardCharsets.US_ASCII);
		RegistersModbusMessage msg = msg(data);

		// WHEN
		String r = msg.dataDecodeAsciiString();

		// THEN
		assertThat("US-ASCII string extracted", r, is(equalTo(src)));
	}

	@Test
	public void dataDecodeString_iso88591() {
		// GIVEN
		final String src = "hello!";
		byte[] data = src.getBytes(StandardCharsets.ISO_8859_1);
		RegistersModbusMessage msg = msg(data);

		// WHEN
		String r = msg.dataDecodeIso88591String();

		// THEN
		assertThat("ISO-8859-1 string extracted", r, is(equalTo(src)));
	}

	@Test
	public void dataDecodeString_utf8() {
		// GIVEN
		final String src = "hello!";
		byte[] data = src.getBytes(StandardCharsets.UTF_8);
		RegistersModbusMessage msg = msg(data);

		// WHEN
		String r = msg.dataDecodeUtf8String();

		// THEN
		assertThat("UTF-8 string extracted", r, is(equalTo(src)));
	}

	@Test
	public void dataDecodeString_utf16() {
		// GIVEN
		final String src = "hello!";
		byte[] data = src.getBytes(StandardCharsets.UTF_16);
		RegistersModbusMessage msg = msg(data);

		// WHEN
		String r = msg.dataDecodeUtf16String();

		// THEN
		assertThat("UTF-16 string extracted", r, is(equalTo(src)));
	}

	@Test
	public void dataDecodeString_utf16be() {
		// GIVEN
		final String src = "hello!";
		byte[] data = src.getBytes(StandardCharsets.UTF_16BE);
		RegistersModbusMessage msg = msg(data);

		// WHEN
		String r = msg.dataDecodeUtf16BeString();

		// THEN
		assertThat("UTF-16BE string extracted", r, is(equalTo(src)));
	}

	@Test
	public void dataDecodeString_utf16le() {
		// GIVEN
		final String src = "hello!";
		byte[] data = src.getBytes(StandardCharsets.UTF_16LE);
		RegistersModbusMessage msg = msg(data);

		// WHEN
		String r = msg.dataDecodeUtf16LeString();

		// THEN
		assertThat("UTF-16LE string extracted", r, is(equalTo(src)));
	}

}
