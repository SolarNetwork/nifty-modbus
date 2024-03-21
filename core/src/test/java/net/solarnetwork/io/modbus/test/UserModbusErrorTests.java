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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.UserModbusError;

/**
 * Test cases for the {@link UserModbusError} class.
 *
 * @author matt
 * @version 1.1
 */
public class UserModbusErrorTests {

	@Test
	public void hash() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusError err = new UserModbusError(code);

		// THEN
		assertThat("Hash code is based on code value", err.hashCode(),
				is(equalTo(Byte.hashCode((code)))));
	}

	@Test
	public void equals() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusError err1 = new UserModbusError(code);
		UserModbusError err2 = new UserModbusError(code);

		// THEN
		assertThat("Equality is based on code value", err1, is(equalTo(err2)));
	}

	@Test
	public void equals_ident() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusError err1 = new UserModbusError(code);

		// THEN
		assertThat("Equality works for identity", err1, is(equalTo(err1)));
	}

	@Test
	public void equals_different() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusError err1 = new UserModbusError(code);
		UserModbusError err2 = new UserModbusError((byte) 0x55);

		// THEN
		assertThat("Difference is based on code value", err1, is(not(equalTo(err2))));
	}

	@Test
	public void equals_different_class() {
		// GIVEN
		final byte code = (byte) 0x56;
		UserModbusError err1 = new UserModbusError(code);

		// THEN
		assertThat("Difference is based on code value", err1, is(not(equalTo("foo"))));
	}

	@Test
	public void accessors() {
		// GIVEN
		final byte code = (byte) 0x55;

		// WHEN
		UserModbusError err = new UserModbusError(code);

		// THEN
		assertThat("Getter code is from constructor", err.getCode(), is(equalTo(code)));
	}

	@Test
	public void toStringValue() {
		// GIVEN 
		final byte code = (byte) 0x56;

		// WHEN
		UserModbusError err = new UserModbusError(code);

		// THEN
		assertThat("String formatted", err.toString(), is(equalTo("UserModbusError{0x56}")));
	}
}
