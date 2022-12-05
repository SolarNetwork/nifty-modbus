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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.BitsModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;

/**
 * Test cases for the {@link ModbusMessageUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageUtils_ResponseDiscoveryTests {

	@Test
	public void discoverResponsePayloadLength_noReadable() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);

		// THEN
		assertThat("Nothing to read cannot be discovered",
				ModbusMessageUtils.discoverResponsePayloadLength(buf), is(equalTo(-1)));
	}

	@Test
	public void discoverResponsePayloadLength_error() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		new BaseModbusMessage(1, ModbusFunctionCode.ReadCoils, ModbusErrorCode.IllegalDataAddress)
				.encodeModbusPayload(buf);

		// THEN
		assertThat("Error discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(2)));
	}

	@Test
	public void discoverResponsePayloadLength_user() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		new BaseModbusMessage(1, (byte) 0x56).encodeModbusPayload(buf);

		// THEN
		assertThat("User function cannot be discovered",
				ModbusMessageUtils.discoverResponsePayloadLength(buf), is(equalTo(-1)));
	}

	@Test
	public void discoverResponsePayloadLength_readCoils() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		BitsModbusMessage.readCoilsResponse(1, 2, 3, new BigInteger("101", 2)).encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(3)));
	}

	@Test
	public void discoverResponsePayloadLength_readDiscretes() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		BitsModbusMessage.readDiscretesResponse(1, 2, 3, new BigInteger("101", 2))
				.encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(3)));
	}

	@Test
	public void discoverResponsePayloadLength_readInputs() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		RegistersModbusMessage.readInputsResponse(1, 2, new short[] { 1, 2, 3 })
				.encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(8)));
	}

	@Test
	public void discoverResponsePayloadLength_readHoldings() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		RegistersModbusMessage.readHoldingsResponse(1, 2, new short[] { 1, 2, 3 })
				.encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(8)));
	}

	@Test
	public void discoverResponsePayloadLength_readHoldings_missingData() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.READ_HOLDING_REGISTERS,
		});
		// @formatter:on

		// THEN
		assertThat("Message is not discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(-1)));
	}

	@Test
	public void discoverResponsePayloadLength_writeCoil() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		BitsModbusMessage.writeCoilResponse(1, 2, true).encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(5)));
	}

	@Test
	public void discoverResponsePayloadLength_writeHolding() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		RegistersModbusMessage.writeHoldingResponse(1, 2, 3).encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(5)));
	}

	@Test
	public void discoverResponsePayloadLength_readExceptionStatus() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		new BaseModbusMessage(1, ModbusFunctionCodes.READ_EXCEPTION_STATUS).encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(2)));
	}

	@Test
	public void discoverResponsePayloadLength_diagnostics_noData() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.DIAGNOSTICS,
		});
		// @formatter:on

		// THEN
		assertThat("Message is not discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(-1)));
	}

	@Test
	public void discoverResponsePayloadLength_diagnostics_sub() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.DIAGNOSTICS,
				(byte)0x00,
				(byte)0x01,
				(byte)0x02,
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered with readable bytes",
				ModbusMessageUtils.discoverResponsePayloadLength(buf), is(equalTo(4)));
	}

	@Test
	public void discoverResponsePayloadLength_diagnostics_pub() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.DIAGNOSTICS,
				(byte)0x01,
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(5)));
	}

	@Test
	public void discoverResponsePayloadLength_getCommEventCounter() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.GET_COMM_EVENT_COUNTER,
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(5)));
	}

	@Test
	public void discoverResponsePayloadLength_getCommEventLog() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.GET_COMM_EVENT_LOG,
				(byte)0x1a
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(0x1a + 2)));
	}

	@Test
	public void discoverResponsePayloadLength_reportServerId() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.REPORT_SERVER_ID,
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(1)));
	}

	@Test
	public void discoverResponsePayloadLength_writeCoils() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		BitsModbusMessage.writeCoilsResponse(1, 2, 3).encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(5)));
	}

	@Test
	public void discoverResponsePayloadLength_writeHoldings() {
		// GIVEN
		final ByteBuf buf = Unpooled.buffer(8);
		RegistersModbusMessage.writeHoldingsResponse(1, 2, 3).encodeModbusPayload(buf);

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(5)));
	}

	@Test
	public void discoverResponsePayloadLength_readFileRecord_missingData() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.READ_FILE_RECORD,
		});
		// @formatter:on

		// THEN
		assertThat("Message is not discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(-1)));
	}

	@Test
	public void discoverResponsePayloadLength_readFileRecord() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.READ_FILE_RECORD,
				(byte)0x12
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(0x12 + 2)));
	}

	@Test
	public void discoverResponsePayloadLength_writeFileRecord_missingData() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.READ_FILE_RECORD,
		});
		// @formatter:on

		// THEN
		assertThat("Message is not discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(-1)));
	}

	@Test
	public void discoverResponsePayloadLength_writeFileRecord() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.WRITE_FILE_RECORD,
				(byte)0x09
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(0x09 + 2)));
	}

	@Test
	public void discoverResponsePayloadLength_maskWriteHoldingRegister() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER,
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(7)));
	}

	@Test
	public void discoverResponsePayloadLength_readWriteHoldingRegister_missingData() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS,
		});
		// @formatter:on

		// THEN
		assertThat("Message is not discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(-1)));
	}

	@Test
	public void discoverResponsePayloadLength_readWriteHoldingRegister() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS,
				(byte)0x02,
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(0x02 + 2)));
	}

	@Test
	public void discoverResponsePayloadLength_readFifoQueue() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.READ_FIFO_QUEUE,
				(byte)0x32
		});
		// @formatter:on

		// THEN
		assertThat("Message is discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(0x32 + 2)));
	}

	@Test
	public void discoverResponsePayloadLength_encapsulatedInterfaceTransport() {
		// GIVEN
		// @formatter:off
		final ByteBuf buf = Unpooled.wrappedBuffer(new byte[] { 
				ModbusFunctionCodes.ENCAPSULATED_INTERFACE_TRANSPORT,
		});
		// @formatter:on

		// THEN
		assertThat("Message is not discovered", ModbusMessageUtils.discoverResponsePayloadLength(buf),
				is(equalTo(-1)));
	}

}
