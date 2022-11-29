/* ==================================================================
 * ModbusFunctionCodeTests.java - 26/11/2022 7:25:19 am
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

package net.solarnetwork.io.modbus.test;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusFunctionCode;

/**
 * Test cases for the {@link ModbusFunctionCode} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusFunctionCodeTests {

	@Test
	public void opposite_read_coils() {
		assertThat(ModbusFunctionCode.ReadCoils.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.WriteCoils)));
	}

	@Test
	public void opposite_read_discretes() {
		assertThat(ModbusFunctionCode.ReadDiscreteInputs.oppositeFunction(), is(nullValue()));
	}

	@Test
	public void opposite_read_inputs() {
		assertThat(ModbusFunctionCode.ReadInputRegisters.oppositeFunction(), is(nullValue()));
	}

	@Test
	public void opposite_read_holdings() {
		assertThat(ModbusFunctionCode.ReadHoldingRegisters.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegisters)));
	}

	@Test
	public void opposite_write_coil() {
		assertThat(ModbusFunctionCode.WriteCoil.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
	}

	@Test
	public void opposite_write_coils() {
		assertThat(ModbusFunctionCode.WriteCoils.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
	}

	@Test
	public void opposite_write_holding() {
		assertThat(ModbusFunctionCode.WriteHoldingRegister.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
	}

	@Test
	public void opposite_write_holdings() {
		assertThat(ModbusFunctionCode.WriteHoldingRegisters.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
	}

	@Test
	public void opposite_write_holding_mask() {
		assertThat(ModbusFunctionCode.MaskWriteHoldingRegister.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
	}

	@Test
	public void opposite_write_holding_read() {
		assertThat(ModbusFunctionCode.ReadWriteHoldingRegisters.oppositeFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
	}

	@Test
	public void blockType_coils() {
		for ( ModbusFunctionCode fn : ModbusFunctionCode.values() ) {
			switch (fn) {
				case ReadCoils:
				case WriteCoil:
				case WriteCoils:
					assertThat(format("%s is coil type", fn), fn.blockType(),
							is(equalTo(ModbusBlockType.Coil)));
					break;

				default:
					assertThat(format("%s is not coil type", fn), fn.blockType(),
							is(not(equalTo(ModbusBlockType.Coil))));
			}
		}
	}

	@Test
	public void blockType_discrete() {
		for ( ModbusFunctionCode fn : ModbusFunctionCode.values() ) {
			switch (fn) {
				case ReadDiscreteInputs:
					assertThat(format("%s is discrete type", fn), fn.blockType(),
							is(equalTo(ModbusBlockType.Discrete)));
					break;

				default:
					assertThat(format("%s is not discrete type", fn), fn.blockType(),
							is(not(equalTo(ModbusBlockType.Discrete))));
			}
		}
	}

	@Test
	public void blockType_input() {
		for ( ModbusFunctionCode fn : ModbusFunctionCode.values() ) {
			switch (fn) {
				case ReadInputRegisters:
					assertThat(format("%s is input type", fn), fn.blockType(),
							is(equalTo(ModbusBlockType.Input)));
					break;

				default:
					assertThat(format("%s is not input type", fn), fn.blockType(),
							is(not(equalTo(ModbusBlockType.Input))));
			}
		}
	}

	@Test
	public void blockType_holding() {
		for ( ModbusFunctionCode fn : ModbusFunctionCode.values() ) {
			switch (fn) {
				case ReadHoldingRegisters:
				case WriteHoldingRegister:
				case WriteHoldingRegisters:
				case MaskWriteHoldingRegister:
				case ReadWriteHoldingRegisters:
				case ReadFifoQueue:
					assertThat(format("%s is holding type", fn), fn.blockType(),
							is(equalTo(ModbusBlockType.Holding)));
					break;

				default:
					assertThat(format("%s is not holding type", fn), fn.blockType(),
							is(not(equalTo(ModbusBlockType.Holding))));
			}
		}
	}

	@Test
	public void blockType_diagnostic() {
		for ( ModbusFunctionCode fn : ModbusFunctionCode.values() ) {
			switch (fn) {
				case ReadFileRecord:
				case WriteFileRecord:
				case ReadExceptionStatus:
				case Diagnostics:
				case GetCommEventCounter:
				case GetCommEventLog:
				case ReportServerId:
				case EncapsulatedInterfaceTransport:
					assertThat(format("%s is diagnostic type", fn), fn.blockType(),
							is(equalTo(ModbusBlockType.Diagnostic)));
					break;

				default:
					assertThat(format("%s is not diagnostic type", fn), fn.blockType(),
							is(not(equalTo(ModbusBlockType.Diagnostic))));
			}
		}
	}

}
