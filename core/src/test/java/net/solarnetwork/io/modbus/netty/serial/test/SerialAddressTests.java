/* ==================================================================
 * SerialAddressTests.java - 6/12/2022 12:27:30 pm
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

package net.solarnetwork.io.modbus.netty.serial.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.netty.serial.SerialAddress;

/**
 * Test cases for the {@link SerialAddress} class.
 *
 * @author matt
 * @version 1.0
 */
public class SerialAddressTests {

	@Test
	public void construct() {
		// GIVEN
		final String name = "foo";
		SerialAddress addr = new SerialAddress(name);

		// THEN
		assertThat("Name saved", addr.name(), is(equalTo(name)));
	}

	@Test
	public void stringValue() {
		final String name = "foo";
		SerialAddress addr = new SerialAddress(name);

		// THEN
		assertThat("String value", addr.toString(), is(equalTo(name)));
	}

}
