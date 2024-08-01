/* ==================================================================
 * RtuModbusMessageTests.java - 5/12/2022 4:26:48 pm
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

package net.solarnetwork.io.modbus.rtu.test;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;
import net.solarnetwork.io.modbus.rtu.RtuModbusMessage;

/**
 * Test cases for the {@link RtuModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class RtuModbusMessageTests {

	private RtuModbusMessage msg(short crc, short computedCrc) {
		return new RtuModbusMessage() {

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

			@Override
			public long getTimestamp() {
				return 0;
			}

			@Override
			public short getCrc() {
				return crc;
			}

			@Override
			public short computeCrc() {
				return computedCrc;
			}
		};
	}

	@Test
	public void crc_valid() {
		// GIVEN
		final short crc = (short) 0xABCD;
		RtuModbusMessage rtu = msg(crc, crc);

		// WHEN
		boolean valid = rtu.isCrcValid();

		// THEN
		assertThat("Validated CRC", valid, is(equalTo(true)));
		assertDoesNotThrow(() -> {
			rtu.validate();
		}, "No exception thrown when CRC is valid");
	}

	@Test
	public void crc_invalid() {
		// GIVEN
		final short crc = (short) 0xABCD;
		final short computedCrc = (short) 0x1234;
		RtuModbusMessage rtu = msg(crc, computedCrc);

		// WHEN
		boolean valid = rtu.isCrcValid();

		// THEN
		assertThat("Validated CRC", valid, is(equalTo(false)));
		ModbusValidationException ex = assertThrows(ModbusValidationException.class, () -> {
			rtu.validate();
		}, "Validation exception thrown when CRC invalid");
		assertThat("Validation message is CRC mismatch", ex.getMessage(),
				is(equalTo(format(RtuModbusMessage.CRC_MISMATCH_VALIDATION_MESSAGE, crc, computedCrc))));
	}

}
