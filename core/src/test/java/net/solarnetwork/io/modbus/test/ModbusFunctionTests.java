/* ==================================================================
 * ModbusFunctionTests.java - 23/06/2023 12:06:13 pm
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
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;

/**
 * Test cases for the {@link ModbusFunction} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusFunctionTests {

	private static class TestModbusFunction implements ModbusFunction {

		private final byte code;

		private TestModbusFunction(byte code) {
			super();
			this.code = code;
		}

		@Override
		public byte getCode() {
			return code;
		}

		@Override
		public String toDisplayString() {
			return null;
		}

		@Override
		public boolean isReadFunction() {
			return false;
		}

		@Override
		public ModbusFunction oppositeFunction() {
			return null;
		}

		@Override
		public ModbusBlockType blockType() {
			return null;
		}

	}

	@Test
	public void defaultCode_ok() {
		// GIVEN
		ModbusFunction f = new TestModbusFunction(ModbusFunctionCodes.READ_INPUT_REGISTERS);

		// WHEN
		ModbusFunctionCode c = f.functionCode();

		// THEN
		assertThat("Function code returned for code value", c,
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
	}

}
