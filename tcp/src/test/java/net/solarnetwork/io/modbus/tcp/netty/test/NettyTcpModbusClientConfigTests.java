/* ==================================================================
 * NettyTcpModbusClientConfigTests.java - 4/12/2022 7:23:29 am
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

package net.solarnetwork.io.modbus.tcp.netty.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.tcp.TcpModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusClientConfig;

/**
 * Test cases for the {@link NettyTcpModbusClientConfig} class.
 *
 * @author matt
 * @version 1.0
 */
public class NettyTcpModbusClientConfigTests {

	@Test
	public void defaults() {
		// GIVEN
		final NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig();

		// THEN
		assertThat("No default host", config.getHost(), is(nullValue()));
		assertThat("Default port", config.getPort(), is(equalTo(TcpModbusClientConfig.DEFAULT_PORT)));
	}

	@Test
	public void setHost() {
		// GIVEN
		final NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig();

		final String host = UUID.randomUUID().toString();

		// WHEN
		config.setHost(host);

		// THEN
		assertThat("Set host returned", config.getHost(), is(equalTo(host)));

	}

	@Test
	public void setPort() {
		// GIVEN
		final NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig();

		final int port = (int) UUID.randomUUID().getMostSignificantBits();

		// WHEN
		config.setPort(port);

		// THEN
		assertThat("Set port returned", config.getPort(), is(equalTo(port)));

	}

}
