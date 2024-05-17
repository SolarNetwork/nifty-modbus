/* ==================================================================
 * BitsModbusMessage.java - 27/11/2022 11:59:14 am
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

package net.solarnetwork.io.modbus;

import java.math.BigInteger;
import java.util.BitSet;

/**
 * A Modbus message related to a bit-based (coil/discrete) register address
 * range.
 *
 * @author matt
 * @version 1.0
 */
public interface BitsModbusMessage extends AddressedModbusMessage {

	/**
	 * Get a BigInteger for a BitSet.
	 * 
	 * @param set
	 *        the set
	 * @return the bit integer
	 */
	static BigInteger bitsForBitSet(BitSet set) {
		if ( set == null ) {
			return BigInteger.ZERO;
		}
		return new BigInteger(set.toByteArray());
	}

	/**
	 * Get the bits.
	 * 
	 * @return the bits, or {@literal null} if no data
	 */
	BigInteger getBits();

	/**
	 * Test if a specific bit is enabled.
	 * 
	 * @param idx
	 *        the index of the bit to test
	 * @return {@literal true} if that bit is set
	 */
	default boolean isBitEnabled(final int idx) {
		if ( idx < 0 ) {
			return false;
		}
		BigInteger bits = getBits();
		if ( bits == null ) {
			return false;
		}
		return bits.testBit(idx);
	}

	/**
	 * Get a {@code BitSet} value for the bits data.
	 * 
	 * @return the new set, or {@literal null} if {@link #getBits()} returns
	 *         {@literal null}
	 */
	default BitSet toBitSet() {
		BigInteger bits = getBits();
		if ( bits == null ) {
			return null;
		}
		return BitSet.valueOf(bits.toByteArray());
	}

}
