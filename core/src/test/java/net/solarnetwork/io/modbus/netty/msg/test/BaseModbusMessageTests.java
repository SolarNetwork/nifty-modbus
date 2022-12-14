/* ==================================================================
 * BaseModbusMessageTests.java - 27/11/2022 7:13:36 am
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

package net.solarnetwork.io.modbus.netty.msg.test;

import static net.solarnetwork.io.modbus.ModbusErrorCodes.ILLEGAL_DATA_ADDRESS;
import static net.solarnetwork.io.modbus.ModbusFunctionCodes.READ_COILS;
import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusErrorCodes;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.UserModbusFunction;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;

/**
 * Test cases for the {@link BaseModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class BaseModbusMessageTests {

	@Test
	public void base() {
		// GIVEN
		int unitId = 1;
		byte fn = READ_COILS;
		BaseModbusMessage msg = new BaseModbusMessage(unitId, fn);

		// THEN
		assertThat("Unit ID saved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function saved", msg.getError(), is(nullValue()));
	}

	@Test
	public void construct_null() {
		assertThrows(IllegalArgumentException.class, () -> {
			new BaseModbusMessage(1, null, null);
		}, "Function is required");

	}

	@Test
	public void error() {
		// GIVEN
		int unitId = 1;
		byte fn = READ_COILS;
		byte err = ILLEGAL_DATA_ADDRESS;
		BaseModbusMessage msg = new BaseModbusMessage(unitId, fn, err);

		// THEN
		assertThat("Unit ID saved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function saved", msg.getFunction(), is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Error saved", msg.getError(), is(equalTo(ModbusErrorCode.IllegalDataAddress)));
	}

	@Test
	public void error_functionOffset() {
		// GIVEN
		int unitId = 1;
		byte fn = READ_COILS + ModbusFunctionCodes.ERROR_OFFSET;
		byte err = ILLEGAL_DATA_ADDRESS;
		BaseModbusMessage msg = new BaseModbusMessage(unitId, fn, err);

		// THEN
		assertThat("Unit ID saved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function decoded from error offset", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Error saved", msg.getError(), is(equalTo(ModbusErrorCode.IllegalDataAddress)));
	}

	@Test
	public void equals() {
		// GIVEN
		BaseModbusMessage msg1 = new BaseModbusMessage(1, READ_COILS);

		// THEN
		assertThat("Equality is based on instance", msg1, is(equalTo(msg1)));
	}

	@Test
	public void isSameAs() {
		// GIVEN
		BaseModbusMessage msg1 = new BaseModbusMessage(1, READ_COILS);
		BaseModbusMessage msg2 = new BaseModbusMessage(1, READ_COILS);

		// THEN
		assertThat("Sameness is based on properties", msg1.isSameAs(msg2), is(equalTo(true)));
		assertThat("Sameness ok for instance", msg1.isSameAs(msg1), is(equalTo(true)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void isNotSameAs() {
		// GIVEN
		BaseModbusMessage msg1 = new BaseModbusMessage(1, READ_COILS);
		BaseModbusMessage msg2 = new BaseModbusMessage(2, READ_COILS);

		// THEN
		assertThat("Difference is based on properties", msg1.isSameAs(msg2), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void isNotSameAs_otherClass() {
		// GIVEN
		BaseModbusMessage msg1 = new BaseModbusMessage(1, READ_COILS);
		ModbusMessage msg2 = new ModbusMessage() {

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
		// THEN
		assertThat("Difference is based on properties", msg1.isSameAs(msg2), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void stringValue() {
		// GIVEN
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.ReadInputRegisters, null);

		// THEN
		assertThat("String value", msg.toString(), matchesRegex("ModbusMessage\\{.*\\}"));
	}

	@Test
	public void stringValue_error() {
		// GIVEN
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.ReadInputRegisters,
				ModbusErrorCode.IllegalDataAddress);

		// THEN
		assertThat("String value", msg.toString(), matchesRegex("ModbusMessage\\{.*\\}"));
	}

	@Test
	public void encodeModbusPayload() {
		// GIVEN
		BaseModbusMessage msg = new BaseModbusMessage(1, new UserModbusFunction((byte) 0x56), null);

		// WHEN
		ByteBuf buf = Unpooled.buffer(8);
		msg.encodeModbusPayload(buf);

		// THEN
		assertThat("Payload encoded as function code", byteObjectArray(ByteBufUtil.getBytes(buf)),
				is(arrayContaining(byteObjectArray(new byte[] { (byte) 0x56 }))));
		assertThat("Payload length", msg.payloadLength(), is(equalTo(1)));
	}

	@Test
	public void encodeModbusPayload_exception() {
		// GIVEN
		BaseModbusMessage msg = new BaseModbusMessage(1, new UserModbusFunction((byte) 0x56),
				ModbusErrorCode.IllegalFunction);

		// WHEN
		ByteBuf buf = Unpooled.buffer(8);
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Payload encoded as exception", byteObjectArray(ByteBufUtil.getBytes(buf)),
				is(arrayContaining(byteObjectArray(new byte[] { 
						(byte) 0x56 + ModbusFunctionCodes.ERROR_OFFSET,
						ModbusErrorCodes.ILLEGAL_FUNCTION,
		}))));
		// @formatter:on
		assertThat("Payload length", msg.payloadLength(), is(equalTo(2)));
	}

}
