/* ==================================================================
 * SimpleModbusMessageReplyTests.java - 29/11/2022 9:04:43 am
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

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
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
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;

/**
 * Test cases for the {@link SimpleModbusMessageReply} class.
 *
 * @author matt
 * @version 1.0
 */
public class SimpleModbusMessageReplyTests {

	@Test
	public void construct() {
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(4, 5,
				new short[] { 1, 2, 3 });

		// WHEN
		SimpleModbusMessageReply r = new SimpleModbusMessageReply(req, res);

		// THEN
		assertThat("Request saved", r.getRequest(), is(sameInstance(req)));
		assertThat("Reply saved", r.getReply(), is(sameInstance(res)));
		assertThat("Unit ID from reply", r.getUnitId(), is(equalTo(res.getUnitId())));
		assertThat("Function from reply", r.getFunction(), is(sameInstance(res.getFunction())));
		assertThat("Error from reply", r.getError(), is(nullValue()));
		assertThat("Is exception from reply", r.isException(), is(res.isException()));
	}

	@Test
	public void construct_error() {
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		BaseModbusMessage res = new BaseModbusMessage(4, ModbusFunctionCode.ReadHoldingRegisters,
				ModbusErrorCode.IllegalDataAddress);

		// WHEN
		SimpleModbusMessageReply r = new SimpleModbusMessageReply(req, res);

		// THEN
		assertThat("Request saved", r.getRequest(), is(sameInstance(req)));
		assertThat("Reply saved", r.getReply(), is(sameInstance(res)));
		assertThat("Unit ID from reply", r.getUnitId(), is(equalTo(res.getUnitId())));
		assertThat("Function from reply", r.getFunction(), is(sameInstance(res.getFunction())));
		assertThat("Error from reply", r.getError(), is(sameInstance(res.getError())));
		assertThat("Is exception from reply", r.isException(), is(res.isException()));
	}

	@Test
	public void construct_null_req() {
		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });

		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			new SimpleModbusMessageReply(null, res);
		}, "Request is required.");
	}

	@Test
	public void construct_null_reply() {
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);

		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			new SimpleModbusMessageReply(req, null);
		}, "Reply is required.");
	}

	@Test
	public void construct_reply_notEncoder() {
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		ModbusMessage res = new ModbusMessage() {

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

		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			new SimpleModbusMessageReply(req, res);
		}, "Reply must implement ModbusPayloadEncoder.");
	}

	@Test
	public void unwrap() {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });

		// WHEN
		SimpleModbusMessageReply r = new SimpleModbusMessageReply(req, res);

		// THEN
		assertThat("Can unwrap as Registers",
				r.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class),
				is(sameInstance(res)));
		assertThat("Can unwrap as Reply", r.unwrap(net.solarnetwork.io.modbus.ModbusMessageReply.class),
				is(sameInstance(r)));
		assertThat("Can not unwrap as Bits",
				r.unwrap(net.solarnetwork.io.modbus.BitsModbusMessage.class), is(nullValue()));
	}

	@Test
	public void stringValue() {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });
		SimpleModbusMessageReply r = new SimpleModbusMessageReply(req, res);

		// THEN
		assertThat("String value", r.toString(), matchesRegex("ModbusMessageReply\\{.*\\}"));

	}

	@Test
	public void isSameAs() {
		// GIVEN
		RegistersModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage res1 = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });
		SimpleModbusMessageReply msg1 = new SimpleModbusMessageReply(req1, res1);

		RegistersModbusMessage msg2 = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });

		// THEN
		assertThat("Sameness is based on reply properties", msg1.isSameAs(msg2), is(equalTo(true)));
		assertThat("Sameness ok for instance", msg1.isSameAs(msg1), is(equalTo(true)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void isNotSameAs() {
		// GIVEN
		RegistersModbusMessage req1 = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage res1 = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });
		SimpleModbusMessageReply msg1 = new SimpleModbusMessageReply(req1, res1);

		RegistersModbusMessage msg2 = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 4 });

		// THEN
		assertThat("Difference is based on reply properties", msg1.isSameAs(msg2), is(equalTo(false)));
		assertThat("Equality is based on instance", msg1, is(not(equalTo(msg2))));
	}

	@Test
	public void encodeModbusPayload() {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		RegistersModbusMessage res = RegistersModbusMessage.readHoldingsResponse(1, 2,
				new short[] { 1, 2, 3 });
		SimpleModbusMessageReply r = new SimpleModbusMessageReply(req, res);

		// WHEN
		ByteBuf out = Unpooled.buffer();
		r.encodeModbusPayload(out);

		// THEN

		ByteBuf replyOut = Unpooled.buffer();
		res.encodeModbusPayload(replyOut);
		assertThat("Reply is encoded", byteObjectArray(ByteBufUtil.getBytes(out)),
				arrayContaining(byteObjectArray(ByteBufUtil.getBytes(replyOut))));
		assertThat("Payload length from reply", r.payloadLength(), is(equalTo(res.payloadLength())));
	}

	@Test
	public void validate() {
		// GIVEN
		RegistersModbusMessage req = RegistersModbusMessage.readHoldingsRequest(1, 2, 3);
		ModbusValidationException ex = new ModbusValidationException("test");
		ModbusMessage res = new BaseModbusMessage(0, ModbusFunctionCodes.READ_COILS) {

			@Override
			public ModbusMessage validate() throws ModbusValidationException {
				throw ex;
			}

		};

		// WHEN
		SimpleModbusMessageReply r = new SimpleModbusMessageReply(req, res);

		// THEN
		ModbusValidationException mve = assertThrows(ModbusValidationException.class, () -> {
			r.validate();
		}, "SimpleModbusMessageReply validate() delegates to reply message validate()");
		assertThat("Delegated exception is returned", mve, is(sameInstance(ex)));
	}

}
