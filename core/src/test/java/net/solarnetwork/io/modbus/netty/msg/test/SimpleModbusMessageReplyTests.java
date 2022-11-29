/* ==================================================================
 * SimpleModbusMessageReplyTests.java - 29/11/2022 9:04:43 am
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

package net.solarnetwork.io.modbus.netty.msg.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;

/**
 * Test cases for the {@link SimpleModbusMessageReply} class.
 *
 * @author matt
 * @version 1.0
 */
public class SimpleModbusMessageReplyTests {

	@Test
	public void unwrap() {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });

		// WHEN
		SimpleModbusMessageReply r = new SimpleModbusMessageReply(req, res);

		// THEN
		assertThat("Can unwrap as Registers",
				r.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class),
				is(sameInstance(res)));
		assertThat("Can not unwrap as Bits",
				r.unwrap(net.solarnetwork.io.modbus.BitsModbusMessage.class), is(nullValue()));
	}

}
