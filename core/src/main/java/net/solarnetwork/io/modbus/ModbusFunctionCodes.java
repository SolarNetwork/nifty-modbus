/* ==================================================================
 * ModbusFunctionCodes.java - 26/11/2022 6:47:10 am
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

/**
 * Function code utilities and constants.
 *
 * @author matt
 * @version 1.0
 */
public final class ModbusFunctionCodes {

	private ModbusFunctionCodes() {
		// not available
	}

	/** Read coils. */
	public static final byte READ_COILS = 0x01;

	/** Read discrete inputs. */
	public static final byte READ_DISCRETE_INPUTS = 0x02;

	/** Read holding registers. */
	public static final byte READ_HOLDING_REGISTERS = 0x03;

	/** Read input registers. */
	public static final byte READ_INPUT_REGISTERS = 0x04;

	/** Write coil. */
	public static final byte WRITE_COIL = 0x05;

	/** Write holding register. */
	public static final byte WRITE_HOLDING_REGISTER = 0x06;

	/** Read exception status. */
	public static final byte READ_EXCEPTION_STATUS = 0x07;

	/** Diagnostics. */
	public static final byte DIAGNOSTICS = 0x08;

	/** Get the communication event counter. */
	public static final byte GET_COMM_EVENT_COUNTER = 0x0B;

	/** Get the communication event log. */
	public static final byte GET_COMM_EVENT_LOG = 0x0C;

	/** Write coils. */
	public static final byte WRITE_COILS = 0x0F;

	/** Write holding registers. */
	public static final byte WRITE_HOLDING_REGISTERS = 0x10;

	/** Report server ID. */
	public static final byte REPORT_SERVER_ID = 0x11;

	/** Read file record. */
	public static final byte READ_FILE_RECORD = 0x14;

	/** Write file record. */
	public static final byte WRITE_FILE_RECORD = 0x15;

	/** Mask write holding register. */
	public static final byte MASK_WRITE_HOLDING_REGISTER = 0x16;

	/** Write then read multiple holding registers. */
	public static final byte READ_WRITE_HOLDING_REGISTERS = 0x17;

	/** Read FIFO queue. */
	public static final byte READ_FIFO_QUEUE = 0x18;

	/** Encapsulated interface transport. */
	public static final byte ENCAPSULATED_INTERFACE_TRANSPORT = 0x2B;

}
