/* ==================================================================
 * SerialParametersTests.java - 6/12/2022 3:49:22 pm
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
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Test cases for the {@link SerialParameters} class.
 *
 * @author matt
 * @version 1.0
 */
public class SerialParametersTests {

	private SerialParameters params(int dataBits, SerialParity parity, SerialStopBits stopBits) {
		return new SerialParameters() {

			@Override
			public int getWaitTime() {
				return 0;
			}

			@Override
			public SerialStopBits getStopBits() {
				return stopBits;
			}

			@Override
			public int getReadTimeout() {
				return 0;
			}

			@Override
			public SerialParity getParity() {
				return parity;
			}

			@Override
			public int getDataBits() {
				return dataBits;
			}

			@Override
			public int getBaudRate() {
				return 1200;
			}
		};
	}

	@Test
	public void bitsShortcut() {
		assertThat("Bits shortcut 1", params(1, SerialParity.None, SerialStopBits.One).bitsShortcut(),
				is(equalTo("1N1")));
		assertThat("Bits shortcut 2", params(2, SerialParity.Even, SerialStopBits.Two).bitsShortcut(),
				is(equalTo("2E2")));
		assertThat("Bits shortcut 3",
				params(3, SerialParity.Odd, SerialStopBits.OnePointFive).bitsShortcut(),
				is(equalTo("3O3")));
	}

	@Test
	public void bitsShortcut_nulls() {
		assertThat("Bits shortcut 1", params(1, null, SerialStopBits.One).bitsShortcut(),
				is(equalTo("1N1")));
		assertThat("Bits shortcut 2", params(2, SerialParity.Even, null).bitsShortcut(),
				is(equalTo("2E1")));
		assertThat("Bits shortcut 3", params(3, null, null).bitsShortcut(), is(equalTo("3N1")));
	}

}
