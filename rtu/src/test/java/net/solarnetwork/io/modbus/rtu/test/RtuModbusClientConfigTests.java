/* ==================================================================
 * RtuModbusClientConfigTests.java - 5/12/2022 6:16:07 am
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

package net.solarnetwork.io.modbus.rtu.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.rtu.RtuModbusClientConfig;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParameters;

/**
 * Test cases for the {@link RtuModbusClientConfig} class.
 *
 * @author matt
 * @version 1.0
 */
public class RtuModbusClientConfigTests {

	private RtuModbusClientConfig config(String name, SerialParameters serialParameters) {
		return new RtuModbusClientConfig() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public SerialParameters getSerialParameters() {
				return serialParameters;
			}

		};
	}

	@Test
	public void defaultDescription() {
		// GIVEN
		final String name = "foo";
		final SerialParameters params = new BasicSerialParameters();
		RtuModbusClientConfig config = config(name, params);

		// THEN
		assertThat("Default description provided", config.getDescription(), is(
				equalTo(String.format("%s %d %s", name, params.getBaudRate(), params.bitsShortcut()))));
	}

	@Test
	public void defaultDescription_nullParameter() {
		// GIVEN
		final String name = "foo";
		RtuModbusClientConfig config = config(name, null);

		// THEN
		assertThat("Default description provided", config.getDescription(), is(equalTo(name)));
	}

}
