/* ==================================================================
 * ModbusErrorTests.java - 16/09/2023 9:27:50 am
 *
 * Copyright 2023 SolarNetwork.net Dev Team
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
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;

/**
 * Test cases for the {@link ModbusError} interface.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusErrorTests {

	private static class TestModbusError implements ModbusError {

		private final byte code;

		private TestModbusError(byte code) {
			super();
			this.code = code;
		}

		@Override
		public byte getCode() {
			return code;
		}

	}

	@Test
	public void errorCode() {
		for ( ModbusErrorCode code : ModbusErrorCode.values() ) {

			// GIVEN
			ModbusError err = new TestModbusError(code.getCode());

			// WHEN
			ModbusErrorCode result = err.errorCode();

			// THEN
			assertThat("Result from default method is from getCode() result.", result,
					is(equalTo(code)));
		}
	}

	@Test
	public void errorCode_notAvailable() {
		// GIVEN
		ModbusError err = new TestModbusError((byte) 123);

		// WHEN
		ModbusErrorCode result = err.errorCode();

		// THEN
		assertThat("Result from default method is null when cannot be mapped to ModbusErrorCode value",
				result, is(nullValue()));
	}

}
