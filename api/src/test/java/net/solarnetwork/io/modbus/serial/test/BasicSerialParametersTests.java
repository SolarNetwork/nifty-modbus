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
import static org.hamcrest.Matchers.nullValue;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialFlowControl;
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
		assertThat("Flow control default", p.getFlowControl(), is(nullValue()));
		assertThat("Stop bits default", p.getStopBits(),
				is(equalTo(SerialParameters.DEFAULT_STOP_BITS)));
		assertThat("Wait time default", p.getWaitTime(), is(equalTo(0)));
		assertThat("Read timeout default", p.getReadTimeout(),
				is(equalTo(SerialParameters.DEFAULT_READ_TIMEOUT)));
		assertThat("RS-485 mode default", p.getRs485ModeEnabled(), is(nullValue()));
		assertThat("RS-485 RTS high default", p.isRs485RtsHighEnabled(),
				is(SerialParameters.DEFAULT_RS485_RTS_HIGH_ENABLED));
		assertThat("RS-485 termination default", p.isRs485TerminationEnabled(), is(false));
		assertThat("RS-485 echo default", p.isRs485EchoEnabled(), is(false));
		assertThat("RS-485 before send delay default", p.getRs485BeforeSendDelay(),
				is(equalTo(SerialParameters.DEFAULT_RS485_BEFORE_SEND_DELAY)));
		assertThat("RS-485 after send delay default", p.getRs485AfterSendDelay(),
				is(equalTo(SerialParameters.DEFAULT_RS485_AFTER_SEND_DELAY)));
	}

	@Test
	public void getters() {
		// GIVEN
		final int baudRate = 4;
		final int dataBits = 3;
		final SerialParity parity = SerialParity.Odd;
		final SerialStopBits stopBits = SerialStopBits.Two;
		final Set<SerialFlowControl> flowControl = EnumSet.of(SerialFlowControl.RTS,
				SerialFlowControl.CTS);
		final int waitTime = 1;
		final int readTimeout = 2;
		final int rs485BeforeSendDelay = 123;
		final int rs485AfterSendDelay = 234;

		BasicSerialParameters p = new BasicSerialParameters();

		// WHEN
		p.setBaudRate(baudRate);
		p.setDataBits(dataBits);
		p.setParity(parity);
		p.setStopBits(stopBits);
		p.setFlowControl(flowControl);
		p.setWaitTime(waitTime);
		p.setReadTimeout(readTimeout);
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(true);
		p.setRs485TerminationEnabled(true);
		p.setRs485EchoEnabled(true);
		p.setRs485BeforeSendDelay(rs485BeforeSendDelay);
		p.setRs485AfterSendDelay(rs485AfterSendDelay);

		// THEN
		assertThat("Baud rate saved", p.getBaudRate(), is(equalTo(baudRate)));
		assertThat("Data bits saved", p.getDataBits(), is(equalTo(dataBits)));
		assertThat("Parity saved", p.getParity(), is(equalTo(parity)));
		assertThat("Stop bits saved", p.getStopBits(), is(equalTo(stopBits)));
		assertThat("Flow control saved", p.getFlowControl(), is(equalTo(flowControl)));
		assertThat("Wait time saved", p.getWaitTime(), is(equalTo(waitTime)));
		assertThat("Read timeout saved", p.getReadTimeout(), is(equalTo(readTimeout)));
		assertThat("RS-485 mode saved", p.getRs485ModeEnabled(), is(true));
		assertThat("RS-485 RTS high saved", p.isRs485RtsHighEnabled(), is(true));
		assertThat("RS-485 termination saved", p.isRs485TerminationEnabled(), is(true));
		assertThat("RS-485 echo saved", p.isRs485EchoEnabled(), is(true));
		assertThat("RS-485 before send delay saved", p.getRs485BeforeSendDelay(),
				is(equalTo(rs485BeforeSendDelay)));
		assertThat("RS-485 after send delay saved", p.getRs485AfterSendDelay(),
				is(equalTo(rs485AfterSendDelay)));
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

	@Test
	public void stringValue_zeroReadTimeout() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setReadTimeout(0);

		assertThat("String value", p.toString(), matchesRegex(
				format("SerialParameters\\{%d %s\\}", p.getBaudRate(), quote(p.bitsShortcut()))));
	}

	@Test
	public void stringValue_nonDefaultFlowControl() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setFlowControl(EnumSet.of(SerialFlowControl.RTS, SerialFlowControl.CTS));

		assertThat("String value", p.toString(),
				matchesRegex(format("SerialParameters\\{%d %s, flowControl=%s.*\\}", p.getBaudRate(),
						quote(p.bitsShortcut()), quote(p.getFlowControl().toString()))));
	}

	@Test
	public void stringValue_emptyFlowControl() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setFlowControl(Collections.emptySet());
		p.setReadTimeout(0);

		assertThat("String value", p.toString(), matchesRegex(
				format("SerialParameters\\{%d %s\\}", p.getBaudRate(), quote(p.bitsShortcut()))));
	}

	@Test
	public void stringValue_nonDefaultRs485() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(true);
		p.setRs485EchoEnabled(true);
		p.setRs485TerminationEnabled(true);
		p.setRs485BeforeSendDelay(123);
		p.setRs485AfterSendDelay(234);

		assertThat("String value", p.toString(), matchesRegex(format(
				"SerialParameters\\{%d %s, readTimeout=%d, RS-485=\\[RtsHigh,Termination,Echo,BeforeSendDelay=%d,AfterSendDelay=%d\\]\\}",
				p.getBaudRate(), quote(p.bitsShortcut()), p.getReadTimeout(),
				p.getRs485BeforeSendDelay(), p.getRs485AfterSendDelay())));
	}

	@Test
	public void stringValue_nonDefaultRs485_disabled() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(false);
		p.setRs485RtsHighEnabled(true);
		p.setRs485EchoEnabled(true);
		p.setRs485TerminationEnabled(true);
		p.setRs485BeforeSendDelay(123);
		p.setRs485AfterSendDelay(234);

		assertThat("String value", p.toString(),
				matchesRegex(format("SerialParameters\\{%d %s, readTimeout=%d\\}", p.getBaudRate(),
						quote(p.bitsShortcut()), p.getReadTimeout())));
	}

	@Test
	public void stringValue_nonDefaultRs485_noOthersEnabled() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(false);
		p.setRs485BeforeSendDelay(0);
		p.setRs485AfterSendDelay(0);

		assertThat("String value", p.toString(),
				matchesRegex(format("SerialParameters\\{%d %s, readTimeout=%d, RS-485\\}",
						p.getBaudRate(), quote(p.bitsShortcut()), p.getReadTimeout())));
	}

	@Test
	public void stringValue_nonDefaultRs485_rtsHighEnabled() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(true);
		p.setRs485EchoEnabled(false);
		p.setRs485TerminationEnabled(false);
		p.setRs485BeforeSendDelay(0);
		p.setRs485AfterSendDelay(0);

		assertThat("String value", p.toString(),
				matchesRegex(format("SerialParameters\\{%d %s, readTimeout=%d, RS-485=\\[RtsHigh\\]\\}",
						p.getBaudRate(), quote(p.bitsShortcut()), p.getReadTimeout())));
	}

	@Test
	public void stringValue_nonDefaultRs485_echoEnabled() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(false);
		p.setRs485EchoEnabled(true);
		p.setRs485TerminationEnabled(false);
		p.setRs485BeforeSendDelay(0);
		p.setRs485AfterSendDelay(0);

		assertThat("String value", p.toString(),
				matchesRegex(format("SerialParameters\\{%d %s, readTimeout=%d, RS-485=\\[Echo\\]\\}",
						p.getBaudRate(), quote(p.bitsShortcut()), p.getReadTimeout())));
	}

	@Test
	public void stringValue_nonDefaultRs485_terminationEnabled() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(false);
		p.setRs485EchoEnabled(false);
		p.setRs485TerminationEnabled(true);
		p.setRs485BeforeSendDelay(0);
		p.setRs485AfterSendDelay(0);

		assertThat("String value", p.toString(),
				matchesRegex(
						format("SerialParameters\\{%d %s, readTimeout=%d, RS-485=\\[Termination\\]\\}",
								p.getBaudRate(), quote(p.bitsShortcut()), p.getReadTimeout())));
	}

	@Test
	public void stringValue_nonDefaultRs485_beforeSendDelay() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(false);
		p.setRs485EchoEnabled(false);
		p.setRs485TerminationEnabled(false);
		p.setRs485BeforeSendDelay(123);
		p.setRs485AfterSendDelay(0);

		assertThat("String value", p.toString(),
				matchesRegex(format(
						"SerialParameters\\{%d %s, readTimeout=%d, RS-485=\\[BeforeSendDelay=%d\\]\\}",
						p.getBaudRate(), quote(p.bitsShortcut()), p.getReadTimeout(),
						p.getRs485BeforeSendDelay())));
	}

	@Test
	public void stringValue_nonDefaultRs485_afterSendDelay() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();
		p.setRs485ModeEnabled(true);
		p.setRs485RtsHighEnabled(false);
		p.setRs485EchoEnabled(false);
		p.setRs485TerminationEnabled(false);
		p.setRs485BeforeSendDelay(0);
		p.setRs485AfterSendDelay(234);

		assertThat("String value", p.toString(),
				matchesRegex(format(
						"SerialParameters\\{%d %s, readTimeout=%d, RS-485=\\[AfterSendDelay=%d\\]\\}",
						p.getBaudRate(), quote(p.bitsShortcut()), p.getReadTimeout(),
						p.getRs485AfterSendDelay())));
	}

	@Test
	public void populateRs485Flags() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();

		// WHEN
		p.populateRs485Flags("!RtsHigh,Termination,Echo,BeforeSendDelay=1234,AfterSendDelay=2345");

		// THEN
		assertThat("RS-485 RTS high configured OFF", p.isRs485RtsHighEnabled(), is(false));
		assertThat("RS-485 termination configured ON", p.isRs485TerminationEnabled(), is(true));
		assertThat("RS-485 echo configured ON", p.isRs485EchoEnabled(), is(true));
		assertThat("RS-485 before send delay configured with value", p.getRs485BeforeSendDelay(),
				is(equalTo(1234)));
		assertThat("RS-485 after send delay configured with value", p.getRs485AfterSendDelay(),
				is(equalTo(2345)));
	}

	private void assertRs485Defaults(BasicSerialParameters p) {
		assertThat("RS-485 RTS high left as default", p.isRs485RtsHighEnabled(),
				is(SerialParameters.DEFAULT_RS485_RTS_HIGH_ENABLED));
		assertThat("RS-485 termination left as default", p.isRs485TerminationEnabled(), is(false));
		assertThat("RS-485 echo left as default", p.isRs485EchoEnabled(), is(false));
		assertThat("RS-485 before send delay left as default", p.getRs485BeforeSendDelay(),
				is(equalTo(SerialParameters.DEFAULT_RS485_BEFORE_SEND_DELAY)));
		assertThat("RS-485 after send delay left as default", p.getRs485AfterSendDelay(),
				is(equalTo(SerialParameters.DEFAULT_RS485_AFTER_SEND_DELAY)));
	}

	@Test
	public void populateRs485Flags_null() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();

		// WHEN
		p.populateRs485Flags(null);

		// THEN
		assertRs485Defaults(p);
	}

	@Test
	public void populateRs485Flags_empty() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();

		// WHEN
		p.populateRs485Flags("");

		// THEN
		assertRs485Defaults(p);
	}

	@Test
	public void populateRs485Flags_unknownNumValue() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();

		// WHEN
		p.populateRs485Flags("Thing=123");

		// THEN
		assertRs485Defaults(p);
	}

	@Test
	public void populateRs485Flags_unknownValue() {
		// GIVEN
		BasicSerialParameters p = new BasicSerialParameters();

		// WHEN
		p.populateRs485Flags("Thing");

		// THEN
		assertRs485Defaults(p);
	}

}
