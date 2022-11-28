/* ==================================================================
 * ModbusMessageUtils_ResponseTests.java - 27/11/2022 11:42:26 am
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.math.BigInteger;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.BitsModbusMessage;
import net.solarnetwork.io.modbus.MaskWriteRegisterMessage;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;

/**
 * Test cases for the decode methods of the {@link ModbusMessageUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageUtils_RequestTests {

	@Test
	public void decodeRequest_readCoils() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.READ_COILS,
				(byte)0x00,
				(byte)0x13,
				(byte)0x00,
				(byte)0x14,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 3;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address decoded", bmm.getAddress(), is(equalTo(0x013)));
		assertThat("Count decoded", bmm.getCount(), is(equalTo(0x14)));
		assertThat("No bits", bmm.getBits(), is(nullValue()));
	}

	@Test
	public void decodeRequest_readDiscretes() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.READ_DISCRETE_INPUTS,
				(byte)0x00,
				(byte)0xC4,
				(byte)0x00,
				(byte)0x16,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 3;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadDiscreteInputs)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address decoded", bmm.getAddress(), is(equalTo(0xC4)));
		assertThat("Count decoded", bmm.getCount(), is(equalTo(0x16)));
		assertThat("No bits", bmm.getBits(), is(nullValue()));
	}

	@Test
	public void decodeRequest_readInputs() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.READ_INPUT_REGISTERS,
				(byte)0x00,
				(byte)0x08,
				(byte)0x00,
				(byte)0x01,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x0008)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(1)));
		assertThat("No data", rmm.dataCopy(), is(nullValue()));
		assertThat("No data (shorts)", rmm.dataDecode(), is(nullValue()));
		assertThat("No data (ints)", rmm.dataDecodeUnsigned(), is(nullValue()));
	}

	@Test
	public void decodeRequest_readHoldings() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.READ_HOLDING_REGISTERS,
				(byte)0x00,
				(byte)0x6B,
				(byte)0x00,
				(byte)0x03,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x6B)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(3)));
		assertThat("No data", rmm.dataCopy(), is(nullValue()));
		assertThat("No data (shorts)", rmm.dataDecode(), is(nullValue()));
		assertThat("No data (ints)", rmm.dataDecodeUnsigned(), is(nullValue()));
	}

	@Test
	public void decodeRequest_writeCoil_on() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.WRITE_COIL,
				(byte)0x00,
				(byte)0xAC,
				(byte)0xFF,
				(byte)0x00,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(ModbusFunctionCode.WriteCoil)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address decoded", bmm.getAddress(), is(equalTo(0xAC)));
		assertThat("Count fixed", bmm.getCount(), is(equalTo(1)));
		assertThat("Bit decoded", bmm.getBits(), is(equalTo(BigInteger.ONE)));
	}

	@Test
	public void decodeRequest_writeCoil_off() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.WRITE_COIL,
				(byte)0x00,
				(byte)0xAC,
				(byte)0x00,
				(byte)0x00,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(ModbusFunctionCode.WriteCoil)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address decoded", bmm.getAddress(), is(equalTo(0xAC)));
		assertThat("Count fixed", bmm.getCount(), is(equalTo(1)));
		assertThat("Bit decoded", bmm.getBits(), is(equalTo(BigInteger.ZERO)));
	}

	@Test
	public void decodeRequest_writeHolding() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.WRITE_HOLDING_REGISTER,
				(byte)0x00,
				(byte)0x01,
				(byte)0x00,
				(byte)0x03,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegister)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x01)));
		assertThat("Count fixed", rmm.getCount(), is(equalTo(1)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x00,
				(byte)0x03,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x0003,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x0003,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeRequest_writeCoils() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.WRITE_COILS,
				(byte)0x00,
				(byte)0x13,
				(byte)0x00,
				(byte)0x0A,
				(byte)0x02,
				(byte)0xCD,
				(byte)0x01,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(ModbusFunctionCode.WriteCoils)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address decoded", bmm.getAddress(), is(equalTo(0x13)));
		assertThat("Count decoded", bmm.getCount(), is(equalTo(0x0A)));
		assertThat("Bits decoded", bmm.getBits(), is(equalTo(new BigInteger("01CD", 16))));
	}

	@Test
	public void decodeRequest_writeHoldings() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.WRITE_HOLDING_REGISTERS,
				(byte)0x00,
				(byte)0x01,
				(byte)0x00,
				(byte)0x02,
				(byte)0x04,
				(byte)0x00,
				(byte)0x0A,
				(byte)0x01,
				(byte)0x02,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x01)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(0x02)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x00,
				(byte)0x0A,
				(byte)0x01,
				(byte)0x02,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x000A,
				(short)0x0102,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x000A,
				0x0102,
		}), is(equalTo(true)));
	}

	@Test
	public void decodeRequest_maskWriteHolding() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.MASK_WRITE_HOLDING_REGISTER,
				(byte)0x00,
				(byte)0x04,
				(byte)0x01,
				(byte)0xF2,
				(byte)0x02,
				(byte)0x25,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is MaskWriteRegisterMessage", msg, instanceOf(MaskWriteRegisterMessage.class));
		MaskWriteRegisterMessage rmm = (MaskWriteRegisterMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x04)));
		assertThat("Count fixed", rmm.getCount(), is(equalTo(1)));
		assertThat("And mask decoded", rmm.getAndMask(), is(equalTo(0x01F2)));
		assertThat("Or mask decoded", rmm.getOrMask(), is(equalTo(0x0225)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x01,
				(byte)0xF2,
				(byte)0x02,
				(byte)0x25,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x01F2,
				(short)0x0225,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x01F2,
				0x0225,
		}), is(equalTo(true)));
	}

	@Test
	public void decodeRequest_readWriteHoldings() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS,
				(byte)0x00,
				(byte)0x03,
				(byte)0x00,
				(byte)0x06,
				(byte)0x00,
				(byte)0x0E,
				(byte)0x00,
				(byte)0x03,
				(byte)0x06,
				(byte)0x00,
				(byte)0xFF,
				(byte)0x00,
				(byte)0xFD,
				(byte)0x00,
				(byte)0xFC,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(3)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(6)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x00,
				(byte)0x0E,
				(byte)0x00,
				(byte)0xFF,
				(byte)0x00,
				(byte)0xFD,
				(byte)0x00,
				(byte)0xFC,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x000E,
				(short)0x00FF,
				(short)0x00FD,
				(short)0x00FC,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x000E,
				0x00FF,
				0x00FD,
				0x00FC,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeRequest_readFifoQueue() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.READ_FIFO_QUEUE,
				(byte)0x04,
				(byte)0xDE,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeRequestPayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadFifoQueue)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0x04DE)));
		assertThat("Count preserved", rmm.getCount(), is(equalTo(0)));
		assertThat("No data", rmm.dataCopy(), is(nullValue()));
		assertThat("No data (shorts)", rmm.dataDecode(), is(nullValue()));
		assertThat("No data (ints)", rmm.dataDecodeUnsigned(), is(nullValue()));
	}

}
