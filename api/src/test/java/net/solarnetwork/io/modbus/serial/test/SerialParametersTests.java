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
import static org.hamcrest.Matchers.nullValue;
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

	private static SerialParameters params(int dataBits, SerialParity parity, SerialStopBits stopBits) {
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

	private static SerialParameters defaultParams() {
		return new SerialParameters() {
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

	@Test
	public void defaults() {
		// GIVEN
		SerialParameters p = defaultParams();

		// THEN
		assertThat("Default baud rate", p.getBaudRate(),
				is(equalTo(SerialParameters.DEFAULT_BAUD_RATE)));
		assertThat("Default data bits", p.getDataBits(),
				is(equalTo(SerialParameters.DEFAULT_DATA_BITS)));
		assertThat("Default stop bits", p.getStopBits(),
				is(equalTo(SerialParameters.DEFAULT_STOP_BITS)));
		assertThat("Default parity", p.getParity(), is(equalTo(SerialParameters.DEFAULT_PARITY)));
		assertThat("Default flow control", p.getFlowControl(), is(nullValue()));
		assertThat("Default wait time", p.getWaitTime(), is(equalTo(0)));
		assertThat("Default read timeout", p.getReadTimeout(),
				is(equalTo(SerialParameters.DEFAULT_READ_TIMEOUT)));
		assertThat("Default RS-485 mode", p.getRs485ModeEnabled(), is(nullValue()));
		assertThat("Default RS-485 RTS high", p.isRs485RtsHighEnabled(),
				is(equalTo(SerialParameters.DEFAULT_RS485_RTS_HIGH_ENABLED)));
		assertThat("Default RS-485 termination", p.isRs485TerminationEnabled(), is(false));
		assertThat("Default RS-485 echo", p.isRs485EchoEnabled(), is(false));
		assertThat("Default RS-485 before send delay", p.getRs485BeforeSendDelay(),
				is(equalTo(SerialParameters.DEFAULT_RS485_BEFORE_SEND_DELAY)));
		assertThat("Default RS-485 after send delay", p.getRs485AfterSendDelay(),
				is(equalTo(SerialParameters.DEFAULT_RS485_AFTER_SEND_DELAY)));
	}

	@Test
	public void rs485Flags_default() {
		// GIVEN
		SerialParameters p = defaultParams();

		// THEN
		assertThat("Default RS-485 flags", p.rs485Flags(),
				is(equalTo(String.format("%s,%s=%d,%s=%d", SerialParameters.RS485_RTS_HIGH_FLAG,
						SerialParameters.RS485_BEFORE_SEND_DELAY_FLAG,
						SerialParameters.DEFAULT_RS485_BEFORE_SEND_DELAY,
						SerialParameters.RS485_AFTER_SEND_DELAY_FLAG,
						SerialParameters.DEFAULT_RS485_AFTER_SEND_DELAY))));
	}

	@Test
	public void rs485Flags_custom() {
		// GIVEN
		SerialParameters p = new SerialParameters() {

			@Override
			public boolean isRs485RtsHighEnabled() {
				return false;
			}

			@Override
			public boolean isRs485TerminationEnabled() {
				return true;
			}

			@Override
			public boolean isRs485EchoEnabled() {
				return true;
			}

			@Override
			public int getRs485BeforeSendDelay() {
				return 0;
			}

			@Override
			public int getRs485AfterSendDelay() {
				return 0;
			}

		};

		// THEN
		assertThat("Custom RS-485 flags", p.rs485Flags(), is(equalTo(String.format("%s,%s",
				SerialParameters.RS485_TERMINATION_FLAG, SerialParameters.RS485_ECHO_FLAG))));
	}

}
