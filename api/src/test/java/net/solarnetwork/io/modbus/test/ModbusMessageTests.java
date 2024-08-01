/* ==================================================================
 * ModbusMessageTests.java - 17/05/2024 7:15:24 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * Test cases for the {@link ModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageTests {

	@Test
	public void defaultIsNotException() {
		// GIVEN
		ModbusMessage msg = new ModbusMessage() {

			@Override
			public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
				return null;
			}

			@Override
			public boolean isSameAs(ModbusMessage obj) {
				return false;
			}

			@Override
			public int getUnitId() {
				return 0;
			}

			@Override
			public ModbusFunction getFunction() {
				return null;
			}

			@Override
			public ModbusError getError() {
				return null;
			}
		};

		// WHEN
		boolean result = msg.isException();

		// THEN
		assertThat("Is not exception if no error provided", result, is(equalTo(false)));
	}

	@Test
	public void defaultIsException() {
		// GIVEN
		ModbusMessage msg = new ModbusMessage() {

			@Override
			public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
				return null;
			}

			@Override
			public boolean isSameAs(ModbusMessage obj) {
				return false;
			}

			@Override
			public int getUnitId() {
				return 0;
			}

			@Override
			public ModbusFunction getFunction() {
				return null;
			}

			@Override
			public ModbusError getError() {
				return ModbusErrorCode.IllegalFunction;
			}
		};

		// WHEN
		boolean result = msg.isException();

		// THEN
		assertThat("Is exception if error provided", result, is(equalTo(true)));
	}

	@Test
	public void defaultValid() {
		// GIVEN
		ModbusMessage msg = new ModbusMessage() {

			@Override
			public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
				return null;
			}

			@Override
			public boolean isSameAs(ModbusMessage obj) {
				return false;
			}

			@Override
			public int getUnitId() {
				return 0;
			}

			@Override
			public ModbusFunction getFunction() {
				return null;
			}

			@Override
			public ModbusError getError() {
				return ModbusErrorCode.IllegalFunction;
			}
		};

		// WHEN
		ModbusMessage validated = assertDoesNotThrow(() -> {
			return msg.validate();
		}, "Default validation does not throw any exception.");
		assertThat("Vaildated is same instance", validated, is(sameInstance(msg)));
	}

}
