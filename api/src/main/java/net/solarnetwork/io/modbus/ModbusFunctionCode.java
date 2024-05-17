/* ==================================================================
 * ModbusFunctionCode.java - 25/11/2022 5:13:41 pm
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

package net.solarnetwork.io.modbus;

import static net.solarnetwork.io.modbus.ModbusFunctionCodes.DIAGNOSTICS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.GET_COMM_EVENT_COUNTER;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.GET_COMM_EVENT_LOG;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_COILS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_DISCRETE_INPUTS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_EXCEPTION_STATUS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_FIFO_QUEUE;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_FILE_RECORD;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_HOLDING_REGISTERS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_INPUT_REGISTERS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.REPORT_SERVER_ID;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.WRITE_COIL;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.WRITE_COILS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.WRITE_FILE_RECORD;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.WRITE_HOLDING_REGISTER;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.WRITE_HOLDING_REGISTERS;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Modbus function codes.
 *
 * @author matt
 * @version 1.0
 */
public enum ModbusFunctionCode implements ModbusFunction {

	/** Read coil. */
	ReadCoils(READ_COILS, ModbusBlockType.Coil, true),

	/** Write a coil. */
	WriteCoil(WRITE_COIL, ModbusBlockType.Coil, false),

	/** Write multiple coils. */
	WriteCoils(WRITE_COILS, ModbusBlockType.Coil, false),

	/** Read discreet input. */
	ReadDiscreteInputs(READ_DISCRETE_INPUTS, ModbusBlockType.Discrete, true),

	/** Read input registers. */
	ReadInputRegisters(READ_INPUT_REGISTERS, ModbusBlockType.Input, true),

	/** Read holding registers. */
	ReadHoldingRegisters(READ_HOLDING_REGISTERS, ModbusBlockType.Holding, true),

	/** Write a holding register. */
	WriteHoldingRegister(WRITE_HOLDING_REGISTER, ModbusBlockType.Holding, false),

	/** Write multiple holding registers. */
	WriteHoldingRegisters(WRITE_HOLDING_REGISTERS, ModbusBlockType.Holding, false),

	/** Write then read multiple holding registers. */
	ReadWriteHoldingRegisters(READ_WRITE_HOLDING_REGISTERS, ModbusBlockType.Holding, false),

	/** Mask write a holding register. */
	MaskWriteHoldingRegister(MASK_WRITE_HOLDING_REGISTER, ModbusBlockType.Holding, false),

	/** Read FIFO queue. */
	ReadFifoQueue(READ_FIFO_QUEUE, ModbusBlockType.Holding, true),

	/** Read file record. */
	ReadFileRecord(READ_FILE_RECORD, ModbusBlockType.Diagnostic, true),

	/** Write file record. */
	WriteFileRecord(WRITE_FILE_RECORD, ModbusBlockType.Diagnostic, false),

	/** Read exception status. */
	ReadExceptionStatus(READ_EXCEPTION_STATUS, ModbusBlockType.Diagnostic, true),

	/** Diagnostics info. */
	Diagnostics(DIAGNOSTICS, ModbusBlockType.Diagnostic, false),

	/** Get the communication event counter. */
	GetCommEventCounter(GET_COMM_EVENT_COUNTER, ModbusBlockType.Diagnostic, true),

	/** The the communication event log. */
	GetCommEventLog(GET_COMM_EVENT_LOG, ModbusBlockType.Diagnostic, true),

	/** Get the report server ID. */
	ReportServerId(REPORT_SERVER_ID, ModbusBlockType.Diagnostic, true),

	/** Encapsulated interface transport (read device identification). */
	EncapsulatedInterfaceTransport(ENCAPSULATED_INTERFACE_TRANSPORT, ModbusBlockType.Diagnostic, true),

	;

	/** A mapping of function read-write opposites. */
	public static final Map<ModbusFunctionCode, ModbusFunctionCode> OPPOSITES;
	static {
		Map<ModbusFunctionCode, ModbusFunctionCode> m = new HashMap<>(8);
		m.put(ModbusFunctionCode.ReadCoils, ModbusFunctionCode.WriteCoils);
		m.put(ModbusFunctionCode.WriteCoil, ModbusFunctionCode.ReadCoils);
		m.put(ModbusFunctionCode.WriteCoils, ModbusFunctionCode.ReadCoils);

		m.put(ModbusFunctionCode.ReadHoldingRegisters, ModbusFunctionCode.WriteHoldingRegisters);
		m.put(ModbusFunctionCode.WriteHoldingRegister, ModbusFunctionCode.ReadHoldingRegisters);
		m.put(ModbusFunctionCode.WriteHoldingRegisters, ModbusFunctionCode.ReadHoldingRegisters);
		m.put(ModbusFunctionCode.MaskWriteHoldingRegister, ModbusFunctionCode.ReadHoldingRegisters);
		m.put(ModbusFunctionCode.ReadWriteHoldingRegisters, ModbusFunctionCode.ReadHoldingRegisters);

		m.put(ModbusFunctionCode.ReadFileRecord, ModbusFunctionCode.WriteFileRecord);
		m.put(ModbusFunctionCode.WriteFileRecord, ModbusFunctionCode.ReadFileRecord);

		OPPOSITES = Collections.unmodifiableMap(m);
	}

	private final byte code;
	private final ModbusBlockType blockType;
	private final boolean read;
	private final String displayName;

	ModbusFunctionCode(byte code, ModbusBlockType blockType, boolean read) {
		this.code = code;
		this.blockType = blockType;
		this.read = read;
		this.displayName = name().replaceAll("([a-z])([A-Z])", "$1 $2") + " (" + (code & 0xFF) + ")";
	}

	@Override
	public byte getCode() {
		return code;
	}

	@Override
	public ModbusFunctionCode functionCode() {
		return this;
	}

	@Override
	public String toDisplayString() {
		return displayName;
	}

	@Override
	public boolean isReadFunction() {
		return read;
	}

	@Override
	public ModbusFunctionCode oppositeFunction() {
		return OPPOSITES.get(this);
	}

	@Override
	public ModbusBlockType blockType() {
		return blockType;
	}

	/**
	 * Get a function instance for a code value.
	 * 
	 * <p>
	 * Error values will be handled to return the non-error function code
	 * equivalent value.
	 * </p>
	 * 
	 * @param code
	 *        the code
	 * @return the function instance; may be an enumeration value of a
	 *         user-defined custom function instance
	 */
	public static ModbusFunction valueOf(byte code) {
		if ( code < 0 ) {
			code &= (byte) 0x7F;
		}
		switch (code) {
			case READ_COILS:
				return ModbusFunctionCode.ReadCoils;

			case READ_DISCRETE_INPUTS:
				return ModbusFunctionCode.ReadDiscreteInputs;

			case READ_HOLDING_REGISTERS:
				return ModbusFunctionCode.ReadHoldingRegisters;

			case READ_INPUT_REGISTERS:
				return ModbusFunctionCode.ReadInputRegisters;

			case WRITE_COIL:
				return ModbusFunctionCode.WriteCoil;

			case WRITE_HOLDING_REGISTER:
				return ModbusFunctionCode.WriteHoldingRegister;

			case READ_EXCEPTION_STATUS:
				return ModbusFunctionCode.ReadExceptionStatus;

			case DIAGNOSTICS:
				return ModbusFunctionCode.Diagnostics;

			case GET_COMM_EVENT_COUNTER:
				return ModbusFunctionCode.GetCommEventCounter;

			case GET_COMM_EVENT_LOG:
				return ModbusFunctionCode.GetCommEventLog;

			case WRITE_COILS:
				return ModbusFunctionCode.WriteCoils;

			case WRITE_HOLDING_REGISTERS:
				return ModbusFunctionCode.WriteHoldingRegisters;

			case REPORT_SERVER_ID:
				return ModbusFunctionCode.ReportServerId;

			case READ_FILE_RECORD:
				return ModbusFunctionCode.ReadFileRecord;

			case WRITE_FILE_RECORD:
				return ModbusFunctionCode.WriteFileRecord;

			case MASK_WRITE_HOLDING_REGISTER:
				return ModbusFunctionCode.MaskWriteHoldingRegister;

			case READ_WRITE_HOLDING_REGISTERS:
				return ModbusFunctionCode.ReadWriteHoldingRegisters;

			case READ_FIFO_QUEUE:
				return ModbusFunctionCode.ReadFifoQueue;

			case ENCAPSULATED_INTERFACE_TRANSPORT:
				return ModbusFunctionCode.EncapsulatedInterfaceTransport;

			default:
				return new UserModbusFunction(code);

		}
	}

	/**
	 * Get an enum instance for a code value.
	 * 
	 * <p>
	 * Error values will be handled to return the non-error function code
	 * equivalent value.
	 * </p>
	 * 
	 * @param code
	 *        the code
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not a valid value
	 */
	public static ModbusFunctionCode forCode(byte code) {
		ModbusFunction fn = valueOf(code);
		if ( fn instanceof ModbusFunctionCode ) {
			return (ModbusFunctionCode) fn;
		}
		throw new IllegalArgumentException("Unknown Modbus function code [" + (code & 0xFF) + "]");
	}

}
