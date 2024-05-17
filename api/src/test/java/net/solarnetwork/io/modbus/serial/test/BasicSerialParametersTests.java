/* ==================================================================
 * BasicSerialParametersTests.java - 6/12/2022 4:05:23 pm
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

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Test cases for the {@link BasicSerialParameters} class.
 *
 * @author matt
 * @version 1.0
 */
public class BasicSerialParametersTests {

	@Test
	public void construct_defaults() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();

		// THEN
		assertThat("Baud rate default", p.getBaudRate(),
				is(equalTo(SerialParameters.DEFAULT_BAUD_RATE)));
		assertThat("Data bits default", p.getDataBits(),
				is(equalTo(SerialParameters.DEFAULT_DATA_BITS)));
		assertThat("Parity default", p.getParity(), is(equalTo(SerialParameters.DEFAULT_PARITY)));
		assertThat("Stop bits default", p.getStopBits(),
				is(equalTo(SerialParameters.DEFAULT_STOP_BITS)));
		assertThat("Wait time default", p.getWaitTime(), is(equalTo(0)));
		assertThat("Read timeout default", p.getReadTimeout(),
				is(equalTo(SerialParameters.DEFAULT_READ_TIMEOUT)));
	}

	@Test
	public void getters() {
		// GIVEN
		final int baudRate = 4;
		final int dataBits = 3;
		final SerialParity parity = SerialParity.Odd;
		final SerialStopBits stopBits = SerialStopBits.Two;
		final int waitTime = 1;
		final int readTimeout = 2;

		BasicSerialParameters p = new BasicSerialParameters();

		// WHEN
		p.setBaudRate(baudRate);
		p.setDataBits(dataBits);
		p.setParity(parity);
		p.setStopBits(stopBits);
		p.setWaitTime(waitTime);
		p.setReadTimeout(readTimeout);

		// THEN
		assertThat("Baud rate saved", p.getBaudRate(), is(equalTo(baudRate)));
		assertThat("Data bits saved", p.getDataBits(), is(equalTo(dataBits)));
		assertThat("Parity saved", p.getParity(), is(equalTo(parity)));
		assertThat("Stop bits saved", p.getStopBits(), is(equalTo(stopBits)));
		assertThat("Wait time saved", p.getWaitTime(), is(equalTo(waitTime)));
		assertThat("Read timeout saved", p.getReadTimeout(), is(equalTo(readTimeout)));
	}

	@Test
	public void stringValue() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();

		assertThat("String value", p.toString(), matchesRegex(
				format("SerialParameters\\{%d %s.*\\}", p.getBaudRate(), quote(p.bitsShortcut()))));
	}

	@Test
	public void stringValue_nulls() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setParity(null);
		p.setStopBits(null);

		assertThat("String value", p.toString(), matchesRegex(
				format("SerialParameters\\{%d %s.*\\}", p.getBaudRate(), quote(p.bitsShortcut()))));
	}

	@Test
	public void stringValue_nonDefaultWaitTimeReadTimeout() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setWaitTime(123);
		p.setReadTimeout(234);

		assertThat("String value", p.toString(),
				matchesRegex(format("SerialParameters\\{%d %s, waitTime=123, readTimeout=234\\}",
						p.getBaudRate(), quote(p.bitsShortcut()))));
	}

}
