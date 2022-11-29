/* ==================================================================
 * BaseModbusMessageTests.java - 27/11/2022 7:13:36 am
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

import static net.solarnetwork.io.modbus.ModbusErrorCodes.ILLEGAL_DATA_ADDRESS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_COILS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;

/**
 * Test cases for the {@link BaseModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class BaseModbusMessageTests {

	@Test
	public void base() {
		// WHEN
		int unitId = 1;
		byte fn = READ_COILS;
		BaseModbusMessage msg = new BaseModbusMessage(unitId, fn);

		// THEN
		assertThat("Unit ID saved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function saved", msg.getError(), is(nullValue()));
	}

	@Test
	public void error() {
		// WHEN
		int unitId = 1;
		byte fn = READ_COILS;
		byte err = ILLEGAL_DATA_ADDRESS;
		BaseModbusMessage msg = new BaseModbusMessage(unitId, fn, err);

		// THEN
		assertThat("Unit ID saved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function saved", msg.getFunction(), is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Error saved", msg.getError(), is(equalTo(ModbusErrorCode.IllegalDataAddress)));
	}

	@Test
	public void error_functionOffset() {
		// WHEN
		int unitId = 1;
		byte fn = READ_COILS + ModbusFunctionCodes.ERROR_OFFSET;
		byte err = ILLEGAL_DATA_ADDRESS;
		BaseModbusMessage msg = new BaseModbusMessage(unitId, fn, err);

		// THEN
		assertThat("Unit ID saved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function decoded from error offset", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Error saved", msg.getError(), is(equalTo(ModbusErrorCode.IllegalDataAddress)));
	}

}
