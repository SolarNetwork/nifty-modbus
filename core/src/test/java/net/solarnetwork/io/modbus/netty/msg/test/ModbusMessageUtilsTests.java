/* ==================================================================
 * ModbusMessageUtilsTests.java - 6/12/2022 9:45:58 am
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;

/**
 * Test cases for the {@link ModbusMessageUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageUtilsTests {

	@Test
	public void encodePayload_null() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);

		// WHEN
		ModbusMessageUtils.encodePayload(null, buf);

		// THEN
		assertThat("Nothing written for null message", buf.writerIndex(), is(equalTo(0)));
	}

	@Test
	public void encodePayload_notEncodable() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		final ModbusMessage msg = new ModbusMessage() {

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
			ModbusMessageUtils.encodePayload(msg, buf);
		}, "IllegalArgumentException thrown when message does not implement ModbusPayloadEncoder");
	}

	@Test
	public void encodePayload() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		BaseModbusMessage msg = new BaseModbusMessage(1, ModbusFunctionCode.Diagnostics, null);

		// WHEN
		ModbusMessageUtils.encodePayload(msg, buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)),
				arrayContaining(byteObjectArray(new byte[] { 
						ModbusFunctionCodes.DIAGNOSTICS,
		})));
		// @formatter:on
	}

}
