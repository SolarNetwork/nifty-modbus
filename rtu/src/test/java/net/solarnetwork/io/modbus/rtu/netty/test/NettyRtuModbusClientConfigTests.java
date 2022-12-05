/* ==================================================================
 * NettyRtuModbusClientConfigTests.java - 5/12/2022 6:22:25 am
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

package net.solarnetwork.io.modbus.rtu.netty.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusClientConfig;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;

/**
 * Test cases for the {@link NettyRtuModbusClientConfig} class.
 *
 * @author matt
 * @version 1.0
 */
public class NettyRtuModbusClientConfigTests {

	@Test
	public void construct_params() {
		// GIVEN
		final String name = UUID.randomUUID().toString();
		final BasicSerialParameters params = new BasicSerialParameters();

		// WHEN
		final NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig(name, params);

		// THEN
		assertThat("Provided name returned", config.getName(), is(equalTo(name)));
		assertThat("Provided serial parameters returned", config.getSerialParameters(),
				is(sameInstance(params)));
	}

	@Test
	public void setName() {
		// GIVEN
		final NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig();

		final String name = UUID.randomUUID().toString();

		// WHEN
		config.setName(name);

		// THEN
		assertThat("Set name returned", config.getName(), is(equalTo(name)));

	}

	@Test
	public void setSerialParameters() {
		// GIVEN
		final NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig();

		final BasicSerialParameters params = new BasicSerialParameters();

		// WHEN
		config.setSerialParameters(params);

		// THEN
		assertThat("Set serial parameters returned", config.getSerialParameters(),
				is(sameInstance(params)));

	}

}
