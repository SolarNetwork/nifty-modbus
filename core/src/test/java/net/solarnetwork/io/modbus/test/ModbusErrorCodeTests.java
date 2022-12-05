/* ==================================================================
 * ModbusErrorCodeTests.java - 5/12/2022 5:18:03 pm
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
import static org.hamcrest.Matchers.sameInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.UserModbusError;

/**
 * Test cases for the {@link ModbudErrorCode} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusErrorCodeTests {

	@Test
	public void getCode() {
		assertThat(ModbusErrorCode.IllegalFunction.getCode(),
				is(equalTo(ModbusErrorCodes.ILLEGAL_FUNCTION)));
		assertThat(ModbusErrorCode.IllegalDataAddress.getCode(),
				is(equalTo(ModbusErrorCodes.ILLEGAL_DATA_ADDRESS)));
		assertThat(ModbusErrorCode.IllegalDataValue.getCode(),
				is(equalTo(ModbusErrorCodes.ILLEGAL_DATA_VALUE)));
		assertThat(ModbusErrorCode.ServerDeviceFailure.getCode(),
				is(equalTo(ModbusErrorCodes.SERVER_DEVICE_FAILURE)));
		assertThat(ModbusErrorCode.Acknowledge.getCode(), is(equalTo(ModbusErrorCodes.ACKNOWLEDGE)));
		assertThat(ModbusErrorCode.ServerDeviceBusy.getCode(),
				is(equalTo(ModbusErrorCodes.SERVER_DEVICE_BUSY)));
		assertThat(ModbusErrorCode.NegativeAcknowledge.getCode(),
				is(equalTo(ModbusErrorCodes.NEGATIVE_ACKNOWLEDGE)));
		assertThat(ModbusErrorCode.MemoryParityError.getCode(),
				is(equalTo(ModbusErrorCodes.MEMORY_PARITY_ERROR)));
		assertThat(ModbusErrorCode.GatewayPathUnavailable.getCode(),
				is(equalTo(ModbusErrorCodes.GATEWAY_PATH_UNAVAILABLE)));
		assertThat(ModbusErrorCode.GatewayTimeout.getCode(),
				is(equalTo(ModbusErrorCodes.GATEWAY_TIMEOUT)));
	}

	@Test
	public void errorCode() {
		for ( ModbusError err : ModbusErrorCode.values() ) {
			assertThat(format("ModbusErrorCode %s is ModbusErrorCode", err), err.errorCode(),
					is(sameInstance(err)));
		}
	}

	@Test
	public void valueOf() {
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.ILLEGAL_FUNCTION),
				is(equalTo(ModbusErrorCode.IllegalFunction)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.ILLEGAL_DATA_ADDRESS),
				is(equalTo(ModbusErrorCode.IllegalDataAddress)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.ILLEGAL_DATA_VALUE),
				is(equalTo(ModbusErrorCode.IllegalDataValue)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.SERVER_DEVICE_FAILURE),
				is(equalTo(ModbusErrorCode.ServerDeviceFailure)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.ACKNOWLEDGE),
				is(equalTo(ModbusErrorCode.Acknowledge)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.SERVER_DEVICE_BUSY),
				is(equalTo(ModbusErrorCode.ServerDeviceBusy)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.NEGATIVE_ACKNOWLEDGE),
				is(equalTo(ModbusErrorCode.NegativeAcknowledge)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.MEMORY_PARITY_ERROR),
				is(equalTo(ModbusErrorCode.MemoryParityError)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.GATEWAY_PATH_UNAVAILABLE),
				is(equalTo(ModbusErrorCode.GatewayPathUnavailable)));
		assertThat(ModbusErrorCode.valueOf(ModbusErrorCodes.GATEWAY_TIMEOUT),
				is(equalTo(ModbusErrorCode.GatewayTimeout)));
	}

	@Test
	public void valueOf_user() {
		assertThat(ModbusErrorCode.valueOf((byte) 0x65), is(equalTo(new UserModbusError((byte) 0x65))));
	}

	@Test
	public void forCode() {
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.ILLEGAL_FUNCTION),
				is(equalTo(ModbusErrorCode.IllegalFunction)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.ILLEGAL_DATA_ADDRESS),
				is(equalTo(ModbusErrorCode.IllegalDataAddress)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.ILLEGAL_DATA_VALUE),
				is(equalTo(ModbusErrorCode.IllegalDataValue)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.SERVER_DEVICE_FAILURE),
				is(equalTo(ModbusErrorCode.ServerDeviceFailure)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.ACKNOWLEDGE),
				is(equalTo(ModbusErrorCode.Acknowledge)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.SERVER_DEVICE_BUSY),
				is(equalTo(ModbusErrorCode.ServerDeviceBusy)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.NEGATIVE_ACKNOWLEDGE),
				is(equalTo(ModbusErrorCode.NegativeAcknowledge)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.MEMORY_PARITY_ERROR),
				is(equalTo(ModbusErrorCode.MemoryParityError)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.GATEWAY_PATH_UNAVAILABLE),
				is(equalTo(ModbusErrorCode.GatewayPathUnavailable)));
		assertThat(ModbusErrorCode.forCode(ModbusErrorCodes.GATEWAY_TIMEOUT),
				is(equalTo(ModbusErrorCode.GatewayTimeout)));
	}

	@Test
	public void forCode_user() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ModbusErrorCode.forCode((byte) 0x65);
		}, "forCode throws IllegalArgumentException for user-defined error codes");
	}

}
