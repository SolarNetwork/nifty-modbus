/* ==================================================================
 * MaskWriteRegisterMessageTests.java - 28/11/2022 4:39:50 pm
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

package net.solarnetwork.io.modbus.msg.test;

import static net.solarnetwork.io.modbus.netty.test.ModbusTestUtils.byteObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.msg.MaskWriteRegisterMessage;

/**
 * Test cases for the {@link MaskWriteRegisterMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class MaskWriteRegisterMessageTests {

	@Test
	public void encode_maskWriteRegisters_request() {
		MaskWriteRegisterMessage msg = MaskWriteRegisterMessage.maskWriteRequest(1, 4, 0x00F2, 0x0025);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER,
						(byte)0x00,
						(byte)0x04,
						(byte)0x00,
						(byte)0xF2,
						(byte)0x00,
						(byte)0x25,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(7)));
	}

	@Test
	public void encode_maskWriteRegisters_response() {
		MaskWriteRegisterMessage msg = MaskWriteRegisterMessage.maskWriteResponse(1, 0x1A, 0x01F3,
				0x0227);

		// WHEN
		ByteBuf buf = Unpooled.buffer();
		msg.encodeModbusPayload(buf);

		// THEN
		// @formatter:off
		assertThat("Message encoded", byteObjectArray(ByteBufUtil.getBytes(buf)), arrayContaining(
				byteObjectArray(new byte[] {
						ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER,
						(byte)0x00,
						(byte)0x1A,
						(byte)0x01,
						(byte)0xF3,
						(byte)0x02,
						(byte)0x27,
				})));
		// @formatter:on
		assertThat("Message length", msg.payloadLength(), is(equalTo(7)));
	}

}
