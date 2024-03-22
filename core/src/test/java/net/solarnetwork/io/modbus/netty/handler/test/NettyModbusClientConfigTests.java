/* ==================================================================
 * NettyModbusClientConfigTests.java - 21/12/2022 4:31:13 pm
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

package net.solarnetwork.io.modbus.netty.handler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusClientConfig;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClientConfig;

/**
 * Test cases for the {@link NettyModbusClientConfig} class.
 *
 * @author matt
 * @version 1.1
 */
public class NettyModbusClientConfigTests {

	private static final String DESC = UUID.randomUUID().toString();

	private static final class TestConfig extends NettyModbusClientConfig {

		@Override
		public String getDescription() {
			return DESC;
		}

	}

	@Test
	public void stringValue() {
		// GIVEN
		NettyModbusClientConfig config = new TestConfig();

		// THEN
		assertThat("String value is description", config.toString(), is(equalTo(DESC)));
	}

	@Test
	public void defaults() {
		// GIVEN
		NettyModbusClientConfig config = new TestConfig();

		// THEN
		assertThat("Default autoReconnect from API", config.isAutoReconnect(),
				is(equalTo(ModbusClientConfig.DEFAULT_AUTO_RECONNECT)));
		assertThat("Default autoReconnectDelay from API", config.getAutoReconnectDelaySeconds(),
				is(equalTo(ModbusClientConfig.DEFAULT_RECONNECT_DELAY_SECS)));
		assertThat("Default sendMinimumDelayMs is 0", config.getSendMinimumDelayMs(), is(equalTo(0L)));
	}

	@Test
	public void setters() {
		// GIVEN
		NettyModbusClientConfig config = new TestConfig();

		// WHEN
		final boolean autoReconnect = false;
		final long autoReconnectDelaySeconds = 123456L;
		final long sendMinimumDelayMs = 321L;
		config.setAutoReconnect(autoReconnect);
		config.setAutoReconnectDelaySeconds(autoReconnectDelaySeconds);
		config.setSendMinimumDelayMs(sendMinimumDelayMs);

		assertThat("autoReconnect saved", config.isAutoReconnect(), is(equalTo(autoReconnect)));
		assertThat("autoReconnectDelay saved", config.getAutoReconnectDelaySeconds(),
				is(equalTo(autoReconnectDelaySeconds)));
		assertThat("sendMinimumDelayMs saved", config.getSendMinimumDelayMs(),
				is(equalTo(sendMinimumDelayMs)));
	}

}
