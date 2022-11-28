/* ==================================================================
 * AddressedModbusMessageTests.java - 27/11/2022 7:09:03 am
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

import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_COILS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.netty.msg.AddressedModbusMessage;

/**
 * Test cases for the {@link AddressedModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class AddressedModbusMessageTests {

	@Test
	public void base() {
		// WHEN
		int unitId = 1;
		byte fn = READ_COILS;
		AddressedModbusMessage msg = new AddressedModbusMessage(unitId, fn, 1, 2);

		// THEN
		assertThat("Unit ID saved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function saved", msg.getFunction(), is(equalTo(ModbusFunctionCode.ReadCoils)));
	}

	@Test
	public void address() {
		// WHEN
		int addr = 2;
		int count = 3;
		AddressedModbusMessage msg = new AddressedModbusMessage(1, READ_COILS, addr, count);

		// THEN
		assertThat("Address saved", msg.getAddress(), is(equalTo(addr)));
		assertThat("Count saved", msg.getCount(), is(equalTo(count)));
	}

}
