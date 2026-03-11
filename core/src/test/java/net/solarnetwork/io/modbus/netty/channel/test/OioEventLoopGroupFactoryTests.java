/* ==================================================================
 * OioEventLoopGroupFactoryTests.java - 12/03/2026 12:04:34 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.io.modbus.netty.channel.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;
import io.netty.channel.EventLoopGroup;
import net.solarnetwork.io.modbus.netty.channel.OioEventLoopGroupFactory;

/**
 * Test cases for the {@link OioEventLoopGroupFactory} class.
 *
 * @author matt
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class OioEventLoopGroupFactoryTests {

	@Test
	public void constructed() throws Exception {
		// WHEN
		final OioEventLoopGroupFactory factory = new OioEventLoopGroupFactory();
		final EventLoopGroup result = factory.apply(null, null);

		// THEN
		assertThat("EventLoopGroup created", result, is(not(nullValue())));
		assertThat("EventLoopGroup is OioEventLoopGroup", result,
				is(instanceOf(io.netty.channel.oio.OioEventLoopGroup.class)));
	}

	@Test
	public void staticInstance() throws Exception {
		// WHEN
		final EventLoopGroup result = OioEventLoopGroupFactory.INSTANCE.apply(null, null);

		// THEN
		assertThat("EventLoopGroup created", result, is(not(nullValue())));
		assertThat("EventLoopGroup is OioEventLoopGroup", result,
				is(instanceOf(io.netty.channel.oio.OioEventLoopGroup.class)));
	}

}
