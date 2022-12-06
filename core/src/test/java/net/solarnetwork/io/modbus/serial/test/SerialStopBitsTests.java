/* ==================================================================
 * SerialStopBitsTests.java - 6/12/2022 3:25:42 pm
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

package net.solarnetwork.io.modbus.serial.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Test cases for the {@link SerialStopBitsTests} class.
 *
 * @author matt
 * @version 1.0
 */
public class SerialStopBitsTests {

	@Test
	public void getCode() {
		assertThat(SerialStopBits.One.getCode(), is(equalTo(SerialStopBits.ONE_STOP_BIT)));
		assertThat(SerialStopBits.OnePointFive.getCode(),
				is(equalTo(SerialStopBits.ONE_POINT_FIVE_STOP_BITS)));
		assertThat(SerialStopBits.Two.getCode(), is(equalTo(SerialStopBits.TWO_STOP_BITS)));
	}

	@Test
	public void forCode() {
		assertThat(SerialStopBits.forCode(SerialStopBits.ONE_STOP_BIT), is(equalTo(SerialStopBits.One)));
		assertThat(SerialStopBits.forCode(SerialStopBits.ONE_POINT_FIVE_STOP_BITS),
				is(equalTo(SerialStopBits.OnePointFive)));
		assertThat(SerialStopBits.forCode(SerialStopBits.TWO_STOP_BITS),
				is(equalTo(SerialStopBits.Two)));
	}

	@Test
	public void forCode_unknown() {
		assertThrows(IllegalArgumentException.class, () -> {
			SerialStopBits.forCode(-1);
		}, "Unknown stop bits code throws exception");
	}

}
