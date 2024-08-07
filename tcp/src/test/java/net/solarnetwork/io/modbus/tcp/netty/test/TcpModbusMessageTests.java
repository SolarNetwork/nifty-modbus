/* ==================================================================
 * TcpModbusMessageTests.java - 28/11/2022 4:15:37 pm
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

package net.solarnetwork.io.modbus.tcp.netty.test;

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;

/**
 * Test cases for the {@link TcpModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusMessageTests {

	@Test
	public void construct_nullBody() {
		assertThrows(IllegalArgumentException.class, () -> {
			new TcpModbusMessage(4, null);
		}, "Null body not allowed");
	}

	@Test
	public void construct_nonEncoder() {
		class NonPayloadEncoder implements ModbusMessage {

			@Override
			public int getUnitId() {
				return 0;
			}

			@Override
			public ModbusFunction getFunction() {
				return ModbusFunctionCode.ReadCoils;
			}

			@Override
			public ModbusError getError() {
				return null;
			}

			@Override
			public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isSameAs(ModbusMessage obj) {
				return false;
			}

		}

		assertThrows(IllegalArgumentException.class, () -> {
			new TcpModbusMessage(4, new NonPayloadEncoder());
		}, "Body that does not implement ModbusPayloadEncoder not allowed");
	}

	@Test
	public void getFunction() {
		// GIVEN
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.ReadCoils,
				ModbusErrorCode.IllegalFunction);
		TcpModbusMessage tcp = new TcpModbusMessage(4, msg);

		// THEN
		assertThat("Function getter delegates", tcp.getFunction(), is(sameInstance(msg.getFunction())));
	}

	@Test
	public void getError() {
		// GIVEN
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.ReadCoils,
				ModbusErrorCode.IllegalFunction);
		TcpModbusMessage tcp = new TcpModbusMessage(4, msg);

		// THEN
		assertThat("Error getter delegates", tcp.getError(), is(sameInstance(msg.getError())));
	}

	@Test
	public void getTimestamp() {
		// GIVEN
		final long ts = System.currentTimeMillis();
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.ReadCoils,
				ModbusErrorCode.IllegalFunction);
		TcpModbusMessage tcp = new TcpModbusMessage(ts, 4, msg);

		// THEN
		assertThat("Provided timestamp returned", tcp.getTimestamp(), is(equalTo(ts)));
	}

	@Test
	public void getBody() {
		// GIVEN
		final long ts = System.currentTimeMillis();
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.ReadCoils,
				ModbusErrorCode.IllegalFunction);
		TcpModbusMessage tcp = new TcpModbusMessage(ts, 4, msg);

		// THEN
		assertThat("Provided timestamp returned", tcp.getBody(), is(sameInstance(msg)));
	}

	@Test
	public void getTimestamp_default() {
		// GIVEN
		final long start = System.currentTimeMillis();
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.ReadCoils,
				ModbusErrorCode.IllegalFunction);
		TcpModbusMessage tcp = new TcpModbusMessage(4, msg);

		// THEN
		assertThat("System timestamp returned (within sec tolerance)", tcp.getTimestamp(),
				is(allOf(greaterThanOrEqualTo(start), lessThanOrEqualTo(start + 1000))));
	}

	@Test
	public void encode_request() {
		// GIVEN
		RegistersModbusMessage msg = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		TcpModbusMessage tcp = new TcpModbusMessage(4, msg);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		tcp.encodeModbusPayload(buf);

		// THEN
		assertThat("Message length", tcp.payloadLength(), is(equalTo(7 + msg.payloadLength())));
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)0x00,
						(byte)0x04,
						(byte)0x00,
						(byte)0x00,
						(byte)0x00,
						(byte)0x06,
						(byte)0x01,
						ModbusFunctionCodes.READ_HOLDING_REGISTERS,
						(byte)0x00,
						(byte)0x02,
						(byte)0x00,
						(byte)0x03,
				})));
		// @formatter:on
	}

	@Test
	public void unwrap() {
		// GIVEN
		RegistersModbusMessage msg = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		TcpModbusMessage tcp = new TcpModbusMessage(4, msg);

		// THEN
		assertThat("Can unwrap as Registers",
				tcp.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class),
				is(sameInstance(msg)));
		assertThat("Can not unwrap as Bits",
				tcp.unwrap(net.solarnetwork.io.modbus.BitsModbusMessage.class), is(nullValue()));
	}

	@Test
	public void equals() {
		// GIVEN
		RegistersModbusMessage body1 = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		TcpModbusMessage msg1 = new TcpModbusMessage(4, body1);

		// THEN
		assertThat("Sameness is based on properties", msg1.isSameAs(msg1), is(equalTo(true)));
		assertThat("Equality is based on instance", msg1, is(equalTo(msg1)));
	}

	@Test
	public void isSameAs() {
		// GIVEN
		RegistersModbusMessage body1 = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage body2 = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		TcpModbusMessage msg1 = new TcpModbusMessage(1, body1);
		TcpModbusMessage msg2 = new TcpModbusMessage(1, body2);

		// THEN
		assertThat("Sameness is based on properties", msg1.isSameAs(msg2), is(equalTo(true)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void isNotSameAs() {
		// GIVEN
		RegistersModbusMessage body1 = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage body2 = RegistersModbusMessage.readHoldingsRequest(2, 2, 3);
		TcpModbusMessage msg1 = new TcpModbusMessage(1, body1);
		TcpModbusMessage msg2 = new TcpModbusMessage(2, body1);
		TcpModbusMessage msg3 = new TcpModbusMessage(1, body2);
		BaseModbusMessage msg4 = new BaseModbusMessage(1, ModbusFunctionCodes.READ_COILS);

		// THEN
		assertThat("Difference is based on properties", msg1.isSameAs(msg2), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));

		assertThat("Difference is based on properties", msg1.isSameAs(msg3), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));

		assertThat("Difference is based on properties", msg1.isSameAs(msg4), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void stringValue() {
		// GIVEN
		RegistersModbusMessage body = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		TcpModbusMessage msg = new TcpModbusMessage(1, body);

		// THEN
		assertThat("String produced", msg.toString(), matchesPattern("^TcpModbusMessage\\{.*\\}$"));
	}

	@Test
	public void validate() {
		// GIVEN
		ModbusValidationException ex = new ModbusValidationException("test");
		ModbusMessage msg = new BaseModbusMessage(0, ModbusFunctionCodes.READ_COILS) {

			@Override
			public ModbusMessage validate() throws ModbusValidationException {
				throw ex;
			}

		};
		TcpModbusMessage tcp = new TcpModbusMessage(4, msg);

		// THEN
		ModbusValidationException mve = assertThrows(ModbusValidationException.class, () -> {
			tcp.validate();
		}, "TcpModbusMessage validate() delegates to reply message validate()");
		assertThat("Delegated exception is returned", mve, is(sameInstance(ex)));
	}

}
