/* ==================================================================
 * ModbusMessageEncoderTests.java - 24/03/2024 2:09:26 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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

package net.solarnetwork.io.modbus.netty.handler.test;

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.ModbusMessageEncoder;

/**
 * Test cases for the {@link ModbusMessageEncoder} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageEncoderTests {

	private EmbeddedChannel controllerChannel;

	@BeforeEach
	public void setup() {
		controllerChannel = new EmbeddedChannel(new ModbusMessageEncoder());
	}

	@Test
	public void request_readInputs() {
		// GIVEN
		final RegistersModbusMessage req = net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage
				.readInputsRequest(1, 0x8, 2);

		// WHEN
		boolean result = controllerChannel.writeOutbound(req);

		// THEN
		assertThat("Encoder produced", result, is(equalTo(true)));
		ByteBuf msg = controllerChannel.readOutbound();

		assertThat("Message encoded to bytes", msg, is(notNullValue()));
		// @formatter:off
		assertThat("Message bytes encoded", byteObjectArray(ByteBufUtil.getBytes(msg)),
				arrayContaining(byteObjectArray(new byte[] { 
						ModbusFunctionCodes.READ_INPUT_REGISTERS,
						(byte)0x00,
						(byte)0x08,
						(byte)0x00,
						(byte)0x02,
		})));
	}

	@Test
	public void request_notEncodable() {
		// GIVEN
		final ModbusMessage notModbusPayloadEncoder = new ModbusMessage() {
			
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


		assertThrows(EncoderException.class, () -> {
			controllerChannel.writeOutbound(notModbusPayloadEncoder);
		});
	}
}
