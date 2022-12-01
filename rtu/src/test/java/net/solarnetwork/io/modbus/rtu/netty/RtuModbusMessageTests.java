/* ==================================================================
 * RtuModbusMessageTests.java - 1/12/2022 4:15:31 pm
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

package net.solarnetwork.io.modbus.rtu.netty;

import static net.solarnetwork.io.modbus.test.support.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;

/**
 * Test cases for the {@link RtuModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class RtuModbusMessageTests {

	@Test
	public void crc() {
		// GIVEN
		final short crc = (short) 0x80B8;
		RegistersModbusMessage msg = RegistersModbusMessage.readInputsResponse(1, 1,
				new short[] { (short) 0xFFFF });
		RtuModbusMessage rtu = new RtuModbusMessage(msg, crc);

		// WHEN
		short givenCrc = rtu.getCrc();
		short computedCrc = rtu.computeCrc();
		boolean valid = rtu.isCrcValid();

		// THEN
		assertThat("Given CRC preserved", givenCrc, is(equalTo(crc)));
		assertThat("Computed CRC same", computedCrc, is(equalTo(crc)));
		assertThat("Validated CRC", valid, is(equalTo(true)));
	}

	@Test
	public void encode_response() {
		// GIVEN
		RegistersModbusMessage msg = RegistersModbusMessage.readInputsResponse(1, 1,
				new short[] { (short) 0xFFFF });
		RtuModbusMessage rtu = new RtuModbusMessage(msg, (short) 0x80B8);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		rtu.encodeModbusPayload(buf);

		// THEN
		assertThat("Message length", rtu.payloadLength(), is(equalTo(3 + msg.payloadLength())));
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						(byte)0x01,
						ModbusFunctionCodes.READ_INPUT_REGISTERS,
						(byte)0x02,
						(byte)0xFF,
						(byte)0xFF,
						(byte)0xB8,
						(byte)0x80,
				})));
		// @formatter:on
	}

	@Test
	public void unwrap() {
		// GIVEN
		RegistersModbusMessage msg = RegistersModbusMessage.readInputsResponse(1, 1,
				new short[] { (short) 0xFFFF });
		RtuModbusMessage rtu = new RtuModbusMessage(msg, (short) 0x80B8);

		// THEN
		assertThat("Can unwrap as Registers",
				rtu.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class),
				is(sameInstance(msg)));
		assertThat("Can not unwrap as Bits",
				rtu.unwrap(net.solarnetwork.io.modbus.BitsModbusMessage.class), is(nullValue()));
	}

}
