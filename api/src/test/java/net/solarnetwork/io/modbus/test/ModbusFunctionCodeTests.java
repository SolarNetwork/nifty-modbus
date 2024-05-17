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
import static org.hamcrest.Matchers.sameInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.UserModbusFunction;

/**
 * Test cases for the {@link ModbusFunctionCode} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusFunctionCodeTests {

	@Test
	public void getCode() {
		assertThat(ModbusFunctionCode.ReadCoils.getCode(), is(equalTo(ModbusFunctionCodes.READ_COILS)));
		assertThat(ModbusFunctionCode.ReadDiscreteInputs.getCode(),
				is(equalTo(ModbusFunctionCodes.READ_DISCRETE_INPUTS)));
		assertThat(ModbusFunctionCode.ReadHoldingRegisters.getCode(),
				is(equalTo(ModbusFunctionCodes.READ_HOLDING_REGISTERS)));
		assertThat(ModbusFunctionCode.ReadInputRegisters.getCode(),
				is(equalTo(ModbusFunctionCodes.READ_INPUT_REGISTERS)));
		assertThat(ModbusFunctionCode.WriteCoil.getCode(), is(equalTo(ModbusFunctionCodes.WRITE_COIL)));
		assertThat(ModbusFunctionCode.WriteHoldingRegister.getCode(),
				is(equalTo(ModbusFunctionCodes.WRITE_HOLDING_REGISTER)));
		assertThat(ModbusFunctionCode.ReadExceptionStatus.getCode(),
				is(equalTo(ModbusFunctionCodes.READ_EXCEPTION_STATUS)));
		assertThat(ModbusFunctionCode.Diagnostics.getCode(),
				is(equalTo(ModbusFunctionCodes.DIAGNOSTICS)));
		assertThat(ModbusFunctionCode.GetCommEventCounter.getCode(),
				is(equalTo(ModbusFunctionCodes.GET_COMM_EVENT_COUNTER)));
		assertThat(ModbusFunctionCode.GetCommEventLog.getCode(),
				is(equalTo(ModbusFunctionCodes.GET_COMM_EVENT_LOG)));
		assertThat(ModbusFunctionCode.WriteCoils.getCode(),
				is(equalTo(ModbusFunctionCodes.WRITE_COILS)));
		assertThat(ModbusFunctionCode.WriteHoldingRegisters.getCode(),
				is(equalTo(ModbusFunctionCodes.WRITE_HOLDING_REGISTERS)));
		assertThat(ModbusFunctionCode.ReportServerId.getCode(),
				is(equalTo(ModbusFunctionCodes.REPORT_SERVER_ID)));
		assertThat(ModbusFunctionCode.ReadFileRecord.getCode(),
				is(equalTo(ModbusFunctionCodes.READ_FILE_RECORD)));
		assertThat(ModbusFunctionCode.WriteFileRecord.getCode(),
				is(equalTo(ModbusFunctionCodes.WRITE_FILE_RECORD)));
		assertThat(ModbusFunctionCode.MaskWriteHoldingRegister.getCode(),
				is(equalTo(ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER)));
		assertThat(ModbusFunctionCode.ReadWriteHoldingRegisters.getCode(),
				is(equalTo(ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS)));
		assertThat(ModbusFunctionCode.ReadFifoQueue.getCode(),
				is(equalTo(ModbusFunctionCodes.READ_FIFO_QUEUE)));
		assertThat(ModbusFunctionCode.EncapsulatedInterfaceTransport.getCode(),
				is(equalTo(ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT)));
	}

	@Test
	public void functionCode() {
		for ( ModbusFunction fn : ModbusFunctionCode.values() ) {
			assertThat(format("ModbusFunctionCode %s is ModbusFunctionCode", fn), fn.functionCode(),
					is(sameInstance(fn)));
		}
	}

	@Test
	public void toDisplayString() {
		assertThat(ModbusFunctionCode.ReadCoils.toDisplayString(),
				is(equalTo(format("Read Coils (%d)", ModbusFunctionCodes.READ_COILS))));
		assertThat(ModbusFunctionCode.ReadDiscreteInputs.toDisplayString(), is(
				equalTo(format("Read Discrete Inputs (%d)", ModbusFunctionCodes.READ_DISCRETE_INPUTS))));
		assertThat(ModbusFunctionCode.ReadHoldingRegisters.toDisplayString(), is(equalTo(
				format("Read Holding Registers (%d)", ModbusFunctionCodes.READ_HOLDING_REGISTERS))));
		assertThat(ModbusFunctionCode.ReadInputRegisters.toDisplayString(), is(
				equalTo(format("Read Input Registers (%d)", ModbusFunctionCodes.READ_INPUT_REGISTERS))));
		assertThat(ModbusFunctionCode.WriteCoil.toDisplayString(),
				is(equalTo(format("Write Coil (%d)", ModbusFunctionCodes.WRITE_COIL))));
		assertThat(ModbusFunctionCode.WriteHoldingRegister.toDisplayString(), is(equalTo(
				format("Write Holding Register (%d)", ModbusFunctionCodes.WRITE_HOLDING_REGISTER))));
		assertThat(ModbusFunctionCode.ReadExceptionStatus.toDisplayString(), is(equalTo(
				format("Read Exception Status (%d)", ModbusFunctionCodes.READ_EXCEPTION_STATUS))));
		assertThat(ModbusFunctionCode.Diagnostics.toDisplayString(),
				is(equalTo(format("Diagnostics (%d)", ModbusFunctionCodes.DIAGNOSTICS))));
		assertThat(ModbusFunctionCode.GetCommEventCounter.toDisplayString(), is(equalTo(
				format("Get Comm Event Counter (%d)", ModbusFunctionCodes.GET_COMM_EVENT_COUNTER))));
		assertThat(ModbusFunctionCode.GetCommEventLog.toDisplayString(),
				is(equalTo(format("Get Comm Event Log (%d)", ModbusFunctionCodes.GET_COMM_EVENT_LOG))));
		assertThat(ModbusFunctionCode.WriteCoils.toDisplayString(),
				is(equalTo(format("Write Coils (%d)", ModbusFunctionCodes.WRITE_COILS))));
		assertThat(ModbusFunctionCode.WriteHoldingRegisters.toDisplayString(), is(equalTo(
				format("Write Holding Registers (%d)", ModbusFunctionCodes.WRITE_HOLDING_REGISTERS))));
		assertThat(ModbusFunctionCode.ReportServerId.toDisplayString(),
				is(equalTo(format("Report Server Id (%d)", ModbusFunctionCodes.REPORT_SERVER_ID))));
		assertThat(ModbusFunctionCode.ReadFileRecord.toDisplayString(),
				is(equalTo(format("Read File Record (%d)", ModbusFunctionCodes.READ_FILE_RECORD))));
		assertThat(ModbusFunctionCode.WriteFileRecord.toDisplayString(),
				is(equalTo(format("Write File Record (%d)", ModbusFunctionCodes.WRITE_FILE_RECORD))));
		assertThat(ModbusFunctionCode.MaskWriteHoldingRegister.toDisplayString(),
				is(equalTo(format("Mask Write Holding Register (%d)",
						ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER))));
		assertThat(ModbusFunctionCode.ReadWriteHoldingRegisters.toDisplayString(),
				is(equalTo(format("Read Write Holding Registers (%d)",
						ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS))));
		assertThat(ModbusFunctionCode.ReadFifoQueue.toDisplayString(),
				is(equalTo(format("Read Fifo Queue (%d)", ModbusFunctionCodes.READ_FIFO_QUEUE))));
		assertThat(ModbusFunctionCode.EncapsulatedInterfaceTransport.toDisplayString(),
				is(equalTo(format("Encapsulated Interface Transport (%d)",
						ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT))));
	}

	@Test
	public void valueOf() {
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_COILS),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_DISCRETE_INPUTS),
				is(equalTo(ModbusFunctionCode.ReadDiscreteInputs)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_HOLDING_REGISTERS),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_INPUT_REGISTERS),
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.WRITE_COIL),
				is(equalTo(ModbusFunctionCode.WriteCoil)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.WRITE_HOLDING_REGISTER),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegister)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_EXCEPTION_STATUS),
				is(equalTo(ModbusFunctionCode.ReadExceptionStatus)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.DIAGNOSTICS),
				is(equalTo(ModbusFunctionCode.Diagnostics)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.GET_COMM_EVENT_COUNTER),
				is(equalTo(ModbusFunctionCode.GetCommEventCounter)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.GET_COMM_EVENT_LOG),
				is(equalTo(ModbusFunctionCode.GetCommEventLog)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.WRITE_COILS),
				is(equalTo(ModbusFunctionCode.WriteCoils)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.WRITE_HOLDING_REGISTERS),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegisters)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.REPORT_SERVER_ID),
				is(equalTo(ModbusFunctionCode.ReportServerId)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_FILE_RECORD),
				is(equalTo(ModbusFunctionCode.ReadFileRecord)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.WRITE_FILE_RECORD),
				is(equalTo(ModbusFunctionCode.WriteFileRecord)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.READ_FIFO_QUEUE),
				is(equalTo(ModbusFunctionCode.ReadFifoQueue)));
		assertThat(ModbusFunctionCode.valueOf(ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT),
				is(equalTo(ModbusFunctionCode.EncapsulatedInterfaceTransport)));
	}

	@Test
	public void valueOf_user() {
		assertThat(ModbusFunctionCode.valueOf((byte) 0x65),
				is(equalTo(new UserModbusFunction((byte) 0x65))));
	}

	@Test
	public void valueOf_negative() {
		// WHEN
		ModbusFunction fn = ModbusFunctionCode.valueOf((byte) (ModbusFunctionCodes.ERROR_OFFSET
				+ ModbusFunctionCode.ReadHoldingRegisters.getCode()));

		// THEN
		assertThat("ModbusFunctionCode %s is decoded from error offset", fn.functionCode(),
				is(ModbusFunctionCode.ReadHoldingRegisters));
	}

	@Test
	public void forCode() {
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_COILS),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_DISCRETE_INPUTS),
				is(equalTo(ModbusFunctionCode.ReadDiscreteInputs)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_HOLDING_REGISTERS),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_INPUT_REGISTERS),
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.WRITE_COIL),
				is(equalTo(ModbusFunctionCode.WriteCoil)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.WRITE_HOLDING_REGISTER),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegister)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_EXCEPTION_STATUS),
				is(equalTo(ModbusFunctionCode.ReadExceptionStatus)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.DIAGNOSTICS),
				is(equalTo(ModbusFunctionCode.Diagnostics)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.GET_COMM_EVENT_COUNTER),
				is(equalTo(ModbusFunctionCode.GetCommEventCounter)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.GET_COMM_EVENT_LOG),
				is(equalTo(ModbusFunctionCode.GetCommEventLog)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.WRITE_COILS),
				is(equalTo(ModbusFunctionCode.WriteCoils)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.WRITE_HOLDING_REGISTERS),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegisters)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.REPORT_SERVER_ID),
				is(equalTo(ModbusFunctionCode.ReportServerId)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_FILE_RECORD),
				is(equalTo(ModbusFunctionCode.ReadFileRecord)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.WRITE_FILE_RECORD),
				is(equalTo(ModbusFunctionCode.WriteFileRecord)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.READ_FIFO_QUEUE),
				is(equalTo(ModbusFunctionCode.ReadFifoQueue)));
		assertThat(ModbusFunctionCode.forCode(ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT),
				is(equalTo(ModbusFunctionCode.EncapsulatedInterfaceTransport)));
	}

	@Test
	public void forCode_user() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ModbusFunctionCode.forCode((byte) 0x65);
		}, "forCode throws IllegalArgumentException for user-defined function codes");
	}

	@Test
	public void readFunction() {
		assertThat(ModbusFunctionCode.ReadCoils.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.ReadDiscreteInputs.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.ReadHoldingRegisters.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.ReadInputRegisters.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.WriteCoil.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.WriteHoldingRegister.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.ReadExceptionStatus.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.Diagnostics.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.GetCommEventCounter.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.GetCommEventLog.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.WriteCoils.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.WriteHoldingRegisters.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.ReportServerId.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.ReadFileRecord.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.WriteFileRecord.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.MaskWriteHoldingRegister.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.ReadWriteHoldingRegisters.isReadFunction(), is(equalTo(false)));
		assertThat(ModbusFunctionCode.ReadFifoQueue.isReadFunction(), is(equalTo(true)));
		assertThat(ModbusFunctionCode.EncapsulatedInterfaceTransport.isReadFunction(),
				is(equalTo(true)));
	}

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
