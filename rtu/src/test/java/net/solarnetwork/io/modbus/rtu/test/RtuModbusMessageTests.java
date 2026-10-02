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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;
import net.solarnetwork.io.modbus.UserModbusFunction;
import net.solarnetwork.io.modbus.rtu.RtuModbusMessage;

/**
 * Test cases for the {@link RtuModbusMessage} class.
 *
 * @author matt
 * @version 1.1
 */
public class RtuModbusMessageTests {

	private RtuModbusMessage msg(short crc, short computedCrc) {
		return msg(0, null, crc, computedCrc);
	}

	private RtuModbusMessage msg(int unitId, ModbusFunction function) {
		return msg(unitId, function, (short) 0, (short) 0);
	}

	private RtuModbusMessage msg(int unitId, ModbusFunction function, short crc, short computedCrc) {
		return new RtuModbusMessage() {

			@Nullable
			@Override
			public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
				return null;
			}

			@Override
			public boolean isSameAs(@Nullable ModbusMessage obj) {
				return false;
			}

			@Override
			public int getUnitId() {
				return unitId;
			}

			@Override
			public ModbusFunction getFunction() {
				return function;
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
		ModbusMessage validated = assertDoesNotThrow(() -> {
			return rtu.validate();
		}, "No exception thrown when CRC is valid");
		assertThat("Validated is same instance", validated, is(sameInstance(rtu)));
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

	/**
	 * A plain message, that is not a {@link RtuModbusMessage}.
	 */
	private ModbusMessage plainMsg(int unitId, ModbusFunction function) {
		return new ModbusMessage() {

			@Nullable
			@Override
			public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
				return null;
			}

			@Override
			public boolean isSameAs(@Nullable ModbusMessage obj) {
				return false;
			}

			@Override
			public int getUnitId() {
				return unitId;
			}

			@Override
			public ModbusFunction getFunction() {
				return function;
			}

			@Nullable
			@Override
			public ModbusError getError() {
				return null;
			}
		};
	}

	@Test
	public void broadcastUnitId() {
		assertThat("Broadcast unit ID", RtuModbusMessage.BROADCAST_UNIT_ID, is(equalTo(0)));
	}

	@Test
	public void isBroadcast_writeFunctions() {
		for ( ModbusFunctionCode fn : new ModbusFunctionCode[] { ModbusFunctionCode.WriteCoil,
				ModbusFunctionCode.WriteCoils, ModbusFunctionCode.WriteHoldingRegister,
				ModbusFunctionCode.WriteHoldingRegisters, ModbusFunctionCode.MaskWriteHoldingRegister,
				ModbusFunctionCode.WriteFileRecord } ) {
			assertThat(fn + " to unit 0 is a broadcast", msg(0, fn).isBroadcast(), is(equalTo(true)));
			assertThat(fn + " to unit 1 is not a broadcast", msg(1, fn).isBroadcast(),
					is(equalTo(false)));
			assertThat(fn + " to unit 255 is not a broadcast", msg(255, fn).isBroadcast(),
					is(equalTo(false)));
		}
	}

	@Test
	public void isBroadcast_readFunctions() {
		for ( ModbusFunctionCode fn : ModbusFunctionCode.values() ) {
			if ( !fn.isReadFunction() ) {
				continue;
			}
			assertThat(fn + " to unit 0 is not a broadcast, as it needs a response",
					msg(0, fn).isBroadcast(), is(equalTo(false)));
			assertThat(fn + " to unit 1 is not a broadcast", msg(1, fn).isBroadcast(),
					is(equalTo(false)));
		}
	}

	@Test
	public void isBroadcast_readWriteFunction() {
		assertThat("Read/write to unit 0 is not a broadcast, as it needs a response",
				msg(0, ModbusFunctionCode.ReadWriteHoldingRegisters).isBroadcast(), is(equalTo(false)));
	}

	@Test
	public void isBroadcast_userFunctions() {
		assertThat("User write function to unit 0 is a broadcast",
				msg(0, new UserModbusFunction(null, (byte) 0x41, null, false, null)).isBroadcast(),
				is(equalTo(true)));
		assertThat("User read function to unit 0 is not a broadcast",
				msg(0, new UserModbusFunction(null, (byte) 0x42, null, true, null)).isBroadcast(),
				is(equalTo(false)));
		assertThat("User write function to unit 1 is not a broadcast",
				msg(1, new UserModbusFunction(null, (byte) 0x41, null, false, null)).isBroadcast(),
				is(equalTo(false)));
	}

	@Test
	public void isBroadcast_anyMessage() {
		assertThat("Plain write to unit 0 is a broadcast",
				RtuModbusMessage.isBroadcast(plainMsg(0, ModbusFunctionCode.WriteHoldingRegister)),
				is(equalTo(true)));
		assertThat("Plain write to unit 1 is not a broadcast",
				RtuModbusMessage.isBroadcast(plainMsg(1, ModbusFunctionCode.WriteHoldingRegister)),
				is(equalTo(false)));
		assertThat("Plain read from unit 0 is not a broadcast",
				RtuModbusMessage.isBroadcast(plainMsg(0, ModbusFunctionCode.ReadHoldingRegisters)),
				is(equalTo(false)));
	}

}
