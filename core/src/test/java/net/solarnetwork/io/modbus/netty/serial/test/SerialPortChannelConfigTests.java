/* ==================================================================
 * SerialPortChannelConfigTests.java - 6/12/2022 3:56:41 pm
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

package net.solarnetwork.io.modbus.netty.serial.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannel;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannelConfig;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannelOption;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Test cases for the {@link SerialPortChannelConfig} class.
 *
 * @author matt
 * @version 1.0
 */
public class SerialPortChannelConfigTests {

	@Test
	public void setSerialParameters() {
		// GIVEN
		SerialPortChannel ch = new SerialPortChannel(new SerialPortProvider() {

			@Override
			public SerialPort getSerialPort(String name) {
				return null;
			}
		});
		SerialPortChannelConfig config = ch.config();

		// WHEN
		final int waitTime = 1;
		final SerialStopBits stopBits = SerialStopBits.Two;
		final int readTimeout = 2;
		final SerialParity parity = SerialParity.Odd;
		final int dataBits = 3;
		final int baudRate = 4;
		config.setSerialParameters(new SerialParameters() {

			@Override
			public int getWaitTime() {
				return waitTime;
			}

			@Override
			public SerialStopBits getStopBits() {
				return stopBits;
			}

			@Override
			public int getReadTimeout() {
				return readTimeout;
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
				return baudRate;
			}
		});

		// THEN
		assertThat("Wait time set", config.getOption(SerialPortChannelOption.WAIT_TIME),
				is(equalTo(waitTime)));
		assertThat("Stop bits set", config.getOption(SerialPortChannelOption.STOP_BITS),
				is(equalTo(stopBits)));
		assertThat("Read timeout set", config.getOption(SerialPortChannelOption.READ_TIMEOUT),
				is(equalTo(readTimeout)));
		assertThat("Parity set", config.getOption(SerialPortChannelOption.PARITY), is(equalTo(parity)));
		assertThat("Data bits set", config.getOption(SerialPortChannelOption.DATA_BITS),
				is(equalTo(dataBits)));
		assertThat("Baud rate set", config.getOption(SerialPortChannelOption.BAUD_RATE),
				is(equalTo(baudRate)));
	}

}
