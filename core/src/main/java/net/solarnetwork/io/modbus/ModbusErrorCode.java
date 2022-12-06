/* ==================================================================
 * ModbusErrorCode.java - 25/11/2022 5:58:10 pm
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
 * Modbus error codes.
 *
 * @author matt
 * @version 1.0
 */
public enum ModbusErrorCode implements ModbusError {

	/**
	 * Function code received in the query is not recognized or allowed by
	 * server.
	 */
	IllegalFunction(ModbusErrorCodes.ILLEGAL_FUNCTION),

	/**
	 * Data address of some or all the required entities are not allowed or do
	 * not exist in server.
	 */
	IllegalDataAddress(ModbusErrorCodes.ILLEGAL_DATA_ADDRESS),

	/** Value is not accepted by server. */
	IllegalDataValue(ModbusErrorCodes.ILLEGAL_DATA_VALUE),

	/**
	 * Unrecoverable error occurred while server was attempting to perform
	 * requested action.
	 */
	ServerDeviceFailure(ModbusErrorCodes.SERVER_DEVICE_FAILURE),

	/**
	 * Server has accepted request and is processing it, but a long duration of
	 * time is required. This response is returned to prevent a timeout error
	 * from occurring in the client. Client can next issue a Poll Program
	 * Complete message to determine whether processing is completed.
	 */
	Acknowledge(ModbusErrorCodes.ACKNOWLEDGE),

	/**
	 * Server is engaged in processing a long-duration command. client should
	 * retry later.
	 */
	ServerDeviceBusy(ModbusErrorCodes.SERVER_DEVICE_BUSY),

	/**
	 * Server cannot perform the programming functions. Client should request
	 * diagnostic or error information from server.
	 */
	NegativeAcknowledge(ModbusErrorCodes.NEGATIVE_ACKNOWLEDGE),

	/**
	 * Server detected a parity error in memory. Client can retry the request,
	 * but service may be required on the server device.
	 */
	MemoryParityError(ModbusErrorCodes.MEMORY_PARITY_ERROR),

	/** Specialized for Modbus gateways. Indicates a misconfigured gateway. */
	GatewayPathUnavailable(ModbusErrorCodes.GATEWAY_PATH_UNAVAILABLE),

	/** Specialized for Modbus gateways. Sent when server fails to respond. */
	GatewayTimeout(ModbusErrorCodes.GATEWAY_TIMEOUT),

	;

	private byte code;

	ModbusErrorCode(byte code) {
		this.code = code;
	}

	@Override
	public byte getCode() {
		return code;
	}

	@Override
	public ModbusErrorCode errorCode() {
		return this;
	}

	public static ModbusError valueOf(byte code) {
		switch (code) {
			case ModbusErrorCodes.ILLEGAL_FUNCTION:
				return ModbusErrorCode.IllegalFunction;

			case ModbusErrorCodes.ILLEGAL_DATA_ADDRESS:
				return ModbusErrorCode.IllegalDataAddress;

			case ModbusErrorCodes.ILLEGAL_DATA_VALUE:
				return ModbusErrorCode.IllegalDataValue;

			case ModbusErrorCodes.SERVER_DEVICE_FAILURE:
				return ModbusErrorCode.ServerDeviceFailure;

			case ModbusErrorCodes.ACKNOWLEDGE:
				return ModbusErrorCode.Acknowledge;

			case ModbusErrorCodes.SERVER_DEVICE_BUSY:
				return ModbusErrorCode.ServerDeviceBusy;

			case ModbusErrorCodes.NEGATIVE_ACKNOWLEDGE:
				return ModbusErrorCode.NegativeAcknowledge;

			case ModbusErrorCodes.MEMORY_PARITY_ERROR:
				return ModbusErrorCode.MemoryParityError;

			case ModbusErrorCodes.GATEWAY_PATH_UNAVAILABLE:
				return ModbusErrorCode.GatewayPathUnavailable;

			case ModbusErrorCodes.GATEWAY_TIMEOUT:
				return ModbusErrorCode.GatewayTimeout;

			default:
				return new UserModbusError(code);
		}
	}

	/**
	 * Get an enum instance for a code value.
	 * 
	 * @param code
	 *        the code
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not a valid value
	 */
	public static ModbusErrorCode forCode(byte code) {
		ModbusError err = valueOf(code);
		if ( err instanceof ModbusErrorCode ) {
			return (ModbusErrorCode) err;
		}
		throw new IllegalArgumentException("Unknown Modbus error code [" + (code & 0xFF) + "]");
	}

}
