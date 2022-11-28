/* ==================================================================
 * ModbusErrorCodes.java - 26/11/2022 10:49:33 am
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
 * Modbus error codes utilities and constants.
 *
 * @author matt
 * @version 1.0
 */
public final class ModbusErrorCodes {

	private ModbusErrorCodes() {
		// not available
	}

	/**
	 * Function code received in the query is not recognized or allowed by
	 * server.
	 */
	public static final byte ILLEGAL_FUNCTION = 0x01;

	/**
	 * Data address of some or all the required entities are not allowed or do
	 * not exist in server.
	 */
	public static final byte ILLEGAL_DATA_ADDRESS = 0x02;

	/** Value is not accepted by server. */
	public static final byte ILLEGAL_DATA_VALUE = 0x03;

	/**
	 * Unrecoverable error occurred while server was attempting to perform
	 * requested action.
	 */
	public static final byte SERVER_DEVICE_FAILURE = 0x04;

	/**
	 * Server has accepted request and is processing it, but a long duration of
	 * time is required. This response is returned to prevent a timeout error
	 * from occurring in the client. Client can next issue a Poll Program
	 * Complete message to determine whether processing is completed.
	 */
	public static final byte ACKNOWLEDGE = 0x05;

	/**
	 * Server is engaged in processing a long-duration command. client should
	 * retry later.
	 */
	public static final byte SERVER_DEVICE_BUSY = 0x06;

	/**
	 * Server cannot perform the programming functions. Client should request
	 * diagnostic or error information from server.
	 */
	public static final byte NEGATIVE_ACKNOWLEDGE = 0x07;

	/**
	 * Server detected a parity error in memory. Client can retry the request,
	 * but service may be required on the server device.
	 */
	public static final byte MEMORY_PARITY_ERROR = 0x08;

	/** Specialized for Modbus gateways. Indicates a misconfigured gateway. */
	public static final byte GATEWAY_PATH_UNAVAILABLE = 0x0A;

	/** Specialized for Modbus gateways. Sent when server fails to respond. */
	public static final byte GATEWAY_TIMEOUT = 0x0B;

}
