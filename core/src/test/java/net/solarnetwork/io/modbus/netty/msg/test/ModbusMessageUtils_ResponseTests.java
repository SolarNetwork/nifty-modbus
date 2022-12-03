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
import net.solarnetwork.io.modbus.MaskWriteRegisterModbusMessage;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ReadWriteRegistersModbusMessage;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.UserModbusError;
import net.solarnetwork.io.modbus.UserModbusFunction;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;

/**
 * Test cases for the decode methods of the {@link ModbusMessageUtils} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageUtils_ResponseTests {

	@Test
	public void decodeResponse_readCoils() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.READ_COILS,
				(byte)0x03,
				(byte)0xCD,
				(byte)0x6B,
				(byte)0x05,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 3;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(ModbusFunctionCode.ReadCoils)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address preserved", bmm.getAddress(), is(equalTo(address)));
		assertThat("Count preserved", bmm.getCount(), is(equalTo(count)));
		assertThat("Bits decoded", bmm.getBits(), is(equalTo(new BigInteger("56BCD", 16))));
	}

	@Test
	public void decodeResponse_readDiscretes() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.READ_DISCRETE_INPUTS,
				(byte)0x03,
				(byte)0xAC,
				(byte)0xDB,
				(byte)0x35,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 3;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadDiscreteInputs)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address preserved", bmm.getAddress(), is(equalTo(address)));
		assertThat("Count preserved", bmm.getCount(), is(equalTo(count)));
		assertThat("Bits decoded", bmm.getBits(), is(equalTo(new BigInteger("35DBAC", 16))));
	}

	@Test
	public void decodeResponse_readInputs() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.READ_INPUT_REGISTERS,
				(byte)0x02,
				(byte)0x00,
				(byte)0x0A,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadInputRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address preserved", rmm.getAddress(), is(equalTo(address)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(1)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x00,
				(byte)0x0A,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x000A,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x000A,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeResponse_readHoldings() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.READ_HOLDING_REGISTERS,
				(byte)0x06,
				(byte)0x02,
				(byte)0x2B,
				(byte)0x00,
				(byte)0x00,
				(byte)0x00,
				(byte)0x64,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadHoldingRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address preserved", rmm.getAddress(), is(equalTo(address)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(3)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x02,
				(byte)0x2B,
				(byte)0x00,
				(byte)0x00,
				(byte)0x00,
				(byte)0x64,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x022B,
				(short)0x0000,
				(short)0x0064
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x022B,
				0x0000,
				0x0064
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeResponse_writeCoil_on() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.WRITE_COIL,
				(byte)0x00,
				(byte)0xA1,
				(byte)0xFF,
				(byte)0x00,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(ModbusFunctionCode.WriteCoil)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address decoded", bmm.getAddress(), is(equalTo(0xA1)));
		assertThat("Count fixed", bmm.getCount(), is(equalTo(1)));
		assertThat("Bit decoded", bmm.getBits(), is(equalTo(BigInteger.ONE)));
	}

	@Test
	public void decodeResponse_writeCoil_off() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.WRITE_COIL,
				(byte)0x00,
				(byte)0xA1,
				(byte)0x00,
				(byte)0x00,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(), is(equalTo(ModbusFunctionCode.WriteCoil)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Bits", msg, instanceOf(BitsModbusMessage.class));
		BitsModbusMessage bmm = (BitsModbusMessage) msg;
		assertThat("Address decoded", bmm.getAddress(), is(equalTo(0xA1)));
		assertThat("Count fixed", bmm.getCount(), is(equalTo(1)));
		assertThat("Bit decoded", bmm.getBits(), is(equalTo(BigInteger.ZERO)));
	}

	@Test
	public void decodeResponse_writeHolding() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.WRITE_HOLDING_REGISTER,
				(byte)0x00,
				(byte)0xA1,
				(byte)0x05,
				(byte)0xFF,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.WriteHoldingRegister)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address decoded", rmm.getAddress(), is(equalTo(0xA1)));
		assertThat("Count fixed", rmm.getCount(), is(equalTo(1)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x05,
				(byte)0xFF,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x05FF,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x05FF,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeResponse_writeCoils() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.WRITE_COILS,
				(byte)0x00,
				(byte)0x13,
				(byte)0x00,
				(byte)0x0A,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

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
		assertThat("No bits", bmm.getBits(), is(nullValue()));
	}

	@Test
	public void decodeResponse_writeHoldings() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.WRITE_HOLDING_REGISTERS,
				(byte)0x00,
				(byte)0x01,
				(byte)0x00,
				(byte)0x02,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

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
		assertThat("No data", rmm.dataCopy(), is(nullValue()));
		assertThat("No data (shorts)", rmm.dataDecode(), is(nullValue()));
		assertThat("No data (int)", rmm.dataDecodeUnsigned(), is(nullValue()));
	}

	@Test
	public void decodeResponse_maskWriteHolding() {
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
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.MaskWriteHoldingRegister)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is MaskWriteRegisterModbusMessage", msg,
				instanceOf(MaskWriteRegisterModbusMessage.class));
		MaskWriteRegisterModbusMessage rmm = (MaskWriteRegisterModbusMessage) msg;
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
	public void decodeResponse_readWriteHoldings() {
		// GIVEN
		// @formatter:off
		ByteBuf buf = Unpooled.copiedBuffer(new byte[] {
				ModbusFunctionCodes.READ_WRITE_HOLDING_REGISTERS,
				(byte)0x0C,
				(byte)0x00,
				(byte)0xFE,
				(byte)0x0A,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x01,
				(byte)0x00,
				(byte)0x03,
				(byte)0x00,
				(byte)0x0D,
				(byte)0x00,
				(byte)0xFF,
		});
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 0;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadWriteHoldingRegisters)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is ReadWriteRegisters", msg, instanceOf(ReadWriteRegistersModbusMessage.class));
		ReadWriteRegistersModbusMessage rwm = (ReadWriteRegistersModbusMessage) msg;
		assertThat("Address preserved", rwm.getAddress(), is(equalTo(address)));
		assertThat("Count decoded", rwm.getCount(), is(equalTo(6)));
		// @formatter:off
		assertThat("Read data decoded", Arrays.equals(rwm.dataCopy(), new byte[] {
				(byte)0x00,
				(byte)0xFE,
				(byte)0x0A,
				(byte)0xCD,
				(byte)0x00,
				(byte)0x01,
				(byte)0x00,
				(byte)0x03,
				(byte)0x00,
				(byte)0x0D,
				(byte)0x00,
				(byte)0xFF,
		}), is(equalTo(true)));
		assertThat("Read data decoded (shorts)", Arrays.equals(rwm.dataDecode(), new short[] {
				(short)0x00FE,
				(short)0x0ACD,
				(short)0x0001,
				(short)0x0003,
				(short)0x000D,
				(short)0x00FF,
		}), is(equalTo(true)));
		assertThat("Read data decoded (ints)", Arrays.equals(rwm.dataDecodeUnsigned(), new int[] {
				0x00FE,
				0x0ACD,
				0x0001,
				0x0003,
				0x000D,
				0x00FF,
		}), is(equalTo(true)));
		assertThat("Write address unknown (0)", rwm.getWriteAddress(), is(equalTo(0x0000)));
		assertThat("Write data null", rwm.writeDataDecode(), is(nullValue()));
		assertThat("Write data null (ints)", rwm.writeDataDecodeUnsigned(),  is(nullValue()));
		// @formatter:on
	}

	@Test
	public void decodeResponse_readFifoQueue() {
		// GIVEN
		// @formatter:off
		final byte[] data = new byte[] {
				ModbusFunctionCodes.READ_FIFO_QUEUE,
				(byte)0x00,
				(byte)0x06,
				(byte)0x00,
				(byte)0x02,
				(byte)0x01,
				(byte)0xB8,
				(byte)0x12,
				(byte)0x84,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded", msg.getFunction(),
				is(equalTo(ModbusFunctionCode.ReadFifoQueue)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));

		assertThat("Type is Registers", msg, instanceOf(RegistersModbusMessage.class));
		RegistersModbusMessage rmm = (RegistersModbusMessage) msg;
		assertThat("Address preserved", rmm.getAddress(), is(equalTo(address)));
		assertThat("Count decoded", rmm.getCount(), is(equalTo(2)));
		// @formatter:off
		assertThat("Data decoded", Arrays.equals(rmm.dataCopy(), new byte[] {
				(byte)0x01,
				(byte)0xB8,
				(byte)0x12,
				(byte)0x84,
		}), is(equalTo(true)));
		assertThat("Data decoded (shorts)", Arrays.equals(rmm.dataDecode(), new short[] {
				(short)0x01B8,
				(short)0x1284,
		}), is(equalTo(true)));
		assertThat("Data decoded (ints)", Arrays.equals(rmm.dataDecodeUnsigned(), new int[] {
				0x01B8,
				0x1284,
		}), is(equalTo(true)));
		// @formatter:on
	}

	@Test
	public void decodeResponse_userFunction() {
		// GIVEN
		// @formatter:off
		final byte userFn = (byte)0x65;
		final byte[] data = new byte[] {
				userFn,
				(byte)0x04,
				(byte)0xDE,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded as user function", msg.getFunction(),
				is(instanceOf(UserModbusFunction.class)));
		assertThat("Not an exception", msg.isException(), is(equalTo(false)));
		assertThat("No error", msg.getError(), is(nullValue()));
		assertThat("Function is preserved", msg.getFunction().getCode(), is(equalTo(userFn)));
	}

	@Test
	public void decodeResponse_userFunctionError() {
		// GIVEN
		// @formatter:off
		final byte userFn = (byte)0x65;
		final byte errorCode = (byte)0xDD;
		final byte[] data = new byte[] {
				userFn + (byte)0x80,
				errorCode,
		};
		ByteBuf buf = Unpooled.copiedBuffer(data);
		// @formatter:on

		// WHEN
		final int unitId = 1;
		final int address = 2;
		final int count = 0;
		ModbusMessage msg = ModbusMessageUtils.decodeResponsePayload(unitId, address, count, buf);

		// THEN
		assertThat("Message decoded", msg, is(notNullValue()));
		assertThat("Unit ID preserved", msg.getUnitId(), is(equalTo(unitId)));
		assertThat("Function is decoded as user function", msg.getFunction(),
				is(instanceOf(UserModbusFunction.class)));
		assertThat("Function is preserved", msg.getFunction().getCode(), is(equalTo(userFn)));
		assertThat("Is an exception", msg.isException(), is(equalTo(true)));
		assertThat("Error decoded as user error", msg.getError(), is(instanceOf(UserModbusError.class)));
		assertThat("Error is preserved", msg.getError().getCode(), is(equalTo(errorCode)));
	}

}
