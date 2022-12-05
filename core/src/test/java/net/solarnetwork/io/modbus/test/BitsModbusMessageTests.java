/* ==================================================================
 * BitsModbusMessageTests.java - 26/11/2022 11:42:06 am
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

package net.solarnetwork.io.modbus.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.math.BigInteger;
import java.util.BitSet;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.BitsModbusMessage;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusMessage;

/**
 * Test cases for the {@link BitsModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class BitsModbusMessageTests {

	@Test
	public void bitsForBitSet() {
		// GIVEN
		BitSet s = new BitSet(8);
		s.set(0);
		s.set(3);
		s.set(5);

		// WHEN
		BigInteger bi = BitsModbusMessage.bitsForBitSet(s);

		// THEN
		BigInteger expected = new BigInteger("00101001", 2);
		assertThat("BigInteger created from BitSet", bi, is(equalTo(expected)));
	}

	@Test
	public void bitsForBitSet_null() {
		// WHEN
		BigInteger bi = BitsModbusMessage.bitsForBitSet(null);

		// THEN
		assertThat("BigInteger created from null BitSet", bi, is(equalTo(BigInteger.ZERO)));
	}

	private BitsModbusMessage msg(BigInteger bits) {
		return new BitsModbusMessage() {

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

			@Override
			public int getCount() {
				return 0;
			}

			@Override
			public int getAddress() {
				return 0;
			}

			@Override
			public BigInteger getBits() {
				return bits;
			}
		};
	}

	@Test
	public void isBitEnabled() {
		// GIVEN
		BigInteger bi = new BigInteger("0100101", 2);
		BitsModbusMessage msg = msg(bi);

		// THEN
		assertThat("BigInteger bit tests", msg.isBitEnabled(-1), is(equalTo(false)));
		assertThat("BigInteger bit tests", msg.isBitEnabled(0), is(equalTo(true)));
		assertThat("BigInteger bit tests", msg.isBitEnabled(1), is(equalTo(false)));
		assertThat("BigInteger bit tests", msg.isBitEnabled(2), is(equalTo(true)));
		assertThat("BigInteger bit tests", msg.isBitEnabled(3), is(equalTo(false)));
		assertThat("BigInteger bit tests", msg.isBitEnabled(4), is(equalTo(false)));
		assertThat("BigInteger bit tests", msg.isBitEnabled(5), is(equalTo(true)));
		assertThat("BigInteger bit tests", msg.isBitEnabled(6), is(equalTo(false)));
	}

	@Test
	public void isBitEnabled_null() {
		// GIVEN
		BitsModbusMessage msg = msg(null);

		// THEN
		assertThat("BigInteger bit tests", msg.isBitEnabled(0), is(equalTo(false)));
	}

	@Test
	public void toBitSet() {
		// GIVEN
		BigInteger bi = new BigInteger("0100101", 2);
		BitsModbusMessage msg = msg(bi);

		// WHEN
		BitSet set = msg.toBitSet();

		// THEN		
		assertThat("BitSet generated", set, is(notNullValue()));
		assertThat("BitSet bit tests", set.get(0), is(equalTo(true)));
		assertThat("BitSet bit tests", set.get(1), is(equalTo(false)));
		assertThat("BitSet bit tests", set.get(2), is(equalTo(true)));
		assertThat("BitSet bit tests", set.get(3), is(equalTo(false)));
		assertThat("BitSet bit tests", set.get(4), is(equalTo(false)));
		assertThat("BitSet bit tests", set.get(5), is(equalTo(true)));
		assertThat("BitSet bit tests", set.get(6), is(equalTo(false)));
	}

	@Test
	public void toBitSet_null() {
		// GIVEN
		BitsModbusMessage msg = msg(null);

		// WHEN
		BitSet set = msg.toBitSet();

		// THEN		
		assertThat("BitSet not generated if bits are null", set, is(nullValue()));
	}

}
