/* ==================================================================
 * TcpModbusClientConfigTests.java - 4/12/2022 7:09:54 am
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

package net.solarnetwork.io.modbus.tcp.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.tcp.TcpModbusClientConfig;

/**
 * Test cases for the {@link TcpModbusClientConfig} class.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusClientConfigTests {

	private TcpModbusClientConfig config(String host) {
		return new TcpModbusClientConfig() {

			@Override
			public String getHost() {
				return host;
			}
		};
	}

	@Test
	public void defaultPort() {
		// GIVEN
		TcpModbusClientConfig config = config("localhost");

		// THEN
		assertThat("Default port provided", config.getPort(), is(equalTo(502)));
	}

	@Test
	public void defaultDescription() {
		// GIVEN
		TcpModbusClientConfig config = config("localhost");

		// THEN
		assertThat("Default description provided", config.getDescription(),
				is(equalTo("localhost:502")));
	}

	@Test
	public void defaultDescription_nullHost() {
		// GIVEN
		TcpModbusClientConfig config = config(null);

		// THEN
		assertThat("Default description provided", config.getDescription(), is(equalTo(":502")));
	}

}
