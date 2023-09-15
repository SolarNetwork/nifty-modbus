/* ==================================================================
 * ModbusClientConfigTests.java - 16/09/2023 9:23:31 am
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
import net.solarnetwork.io.modbus.ModbusClientConfig;

/**
 * Test cases for the {@link ModbusClientConfig} interface.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusClientConfigTests {

	private static class TestModbusClientConfig implements ModbusClientConfig {

		@Override
		public String getDescription() {
			return null;
		}

	}

	@Test
	public void autoReconnectDelaySeconds() {
		// GIVEN
		ModbusClientConfig config = new TestModbusClientConfig();

		// WHEN
		long result = config.getAutoReconnectDelaySeconds();

		// THEN
		assertThat("Result from default method is interface constant.", result,
				is(equalTo(ModbusClientConfig.DEFAULT_RECONNECT_DELAY_SECS)));
	}

	@Test
	public void autoReconnect() {
		// GIVEN
		ModbusClientConfig config = new TestModbusClientConfig();

		// WHEN
		boolean result = config.isAutoReconnect();

		// THEN
		assertThat("Result from default method is interface constant.", result,
				is(equalTo(ModbusClientConfig.DEFAULT_AUTO_RECONNECT)));
	}

}
