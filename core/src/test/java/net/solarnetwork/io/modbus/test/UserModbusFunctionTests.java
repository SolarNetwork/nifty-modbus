/* ==================================================================
 * UserModbusFunctionTests.java - 4/12/2022 1:56:40 pm
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

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.UserModbusFunction;

/**
 * Test cases for the {@link UserModbusFunction} class.
 *
 * @author matt
 * @version 1.0
 */
public class UserModbusFunctionTests {

	@Test
	public void hash() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusFunction fn = new UserModbusFunction(code);

		// THEN
		assertThat("Hash code is based on code value", fn.hashCode(),
				is(equalTo(Byte.hashCode((code)))));
	}

	@Test
	public void equals() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusFunction fn1 = new UserModbusFunction("Name 1", code);
		UserModbusFunction fn2 = new UserModbusFunction("Name 2", code);

		// THEN
		assertThat("Equality is based on code value", fn1, is(equalTo(fn2)));
	}

	@Test
	public void equals_ident() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusFunction fn1 = new UserModbusFunction("Name 1", code);

		// THEN
		assertThat("Equality works for identity", fn1, is(equalTo(fn1)));
	}

	@Test
	public void equals_different() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusFunction fn1 = new UserModbusFunction(code);
		UserModbusFunction fn2 = new UserModbusFunction((byte) 0x55);

		// THEN
		assertThat("Difference is based on code value", fn1, is(not(equalTo(fn2))));
	}

	@Test
	public void equals_different_class() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusFunction fn1 = new UserModbusFunction(code);

		// THEN
		assertThat("Difference is based on code value", fn1, is(not(equalTo("foo"))));
	}

	@Test
	public void toDisplayString() {
		// GIVEN
		final String displayName = UUID.randomUUID().toString();
		final byte code = (byte) 0x55;
		final ModbusBlockType blockType = ModbusBlockType.Coil;
		final boolean readFunction = true;
		final ModbusFunction oppositeFunction = ModbusFunctionCode.WriteFileRecord;

		// WHEN
		UserModbusFunction fn = new UserModbusFunction(displayName, code, blockType, readFunction,
				oppositeFunction);

		// THEN
		assertThat("Display string is from display name", fn.toDisplayString(),
				is(equalTo(displayName)));
	}

	@Test
	public void toDisplayString_null() {
		// GIVEN
		final byte code = (byte) 0x55;
		final ModbusBlockType blockType = ModbusBlockType.Coil;
		final boolean readFunction = true;
		final ModbusFunction oppositeFunction = ModbusFunctionCode.WriteFileRecord;

		// WHEN
		UserModbusFunction fn = new UserModbusFunction(null, code, blockType, readFunction,
				oppositeFunction);

		// THEN
		assertThat("Display string is from code when display name is null", fn.toDisplayString(),
				is(equalTo(format("UserModbusFunction{%d}", Byte.toUnsignedInt(code)))));
	}

	@Test
	public void toDisplayString_empty() {
		// GIVEN
		final byte code = (byte) 0x55;
		final ModbusBlockType blockType = ModbusBlockType.Coil;
		final boolean readFunction = true;
		final ModbusFunction oppositeFunction = ModbusFunctionCode.WriteFileRecord;

		// WHEN
		UserModbusFunction fn = new UserModbusFunction("", code, blockType, readFunction,
				oppositeFunction);

		// THEN
		assertThat("Display string is from code when display name is empty", fn.toDisplayString(),
				is(equalTo(format("UserModbusFunction{%d}", Byte.toUnsignedInt(code)))));
	}

	@Test
	public void accessors() {
		// GIVEN
		final String displayName = UUID.randomUUID().toString();
		final byte code = (byte) 0x55;
		final ModbusBlockType blockType = ModbusBlockType.Coil;
		final boolean readFunction = true;
		final ModbusFunction oppositeFunction = ModbusFunctionCode.WriteFileRecord;

		// WHEN
		UserModbusFunction fn = new UserModbusFunction(displayName, code, blockType, readFunction,
				oppositeFunction);

		// THEN
		assertThat("Getter code is from constructor", fn.getCode(), is(equalTo(code)));
		assertThat("Getter blockType is from constructor", fn.blockType(), is(equalTo(blockType)));
		assertThat("Getter readFunction is from constructor", fn.isReadFunction(),
				is(equalTo(readFunction)));
		assertThat("Getter oppositeFunction is from constructor", fn.oppositeFunction(),
				is(equalTo(oppositeFunction)));
	}

	@Test
	public void stringValue() {
		// GIVEN
		byte code = (byte) 0x65;
		UserModbusFunction fn = new UserModbusFunction(code);
		assertThat("String value", fn.toString(),
				is(equalTo(format("UserModbusFunction{%d}", Byte.toUnsignedInt(code)))));
	}
}
