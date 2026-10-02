/* ==================================================================
 * JscSerialPortTests.java - 2/10/2026 5:20:14 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package net.solarnetwork.io.modbus.rtu.jsc.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import net.solarnetwork.io.modbus.rtu.jsc.JscSerialPort;
import net.solarnetwork.io.modbus.rtu.jsc.JscSerialPortProvider;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialFlowControl;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Test cases for the {@link JscSerialPort} class.
 * 
 * <p>
 * The tests that need a serial device are only run when the
 * {@link #PORT_PROPERTY} system property is set to the name of a serial device
 * to use.
 * </p>
 *
 * @author matt
 * @version 1.1
 */
public class JscSerialPortTests {

	private static final String MISSING_PORT = "/dev/nifty-modbus-does-not-exist";

	/**
	 * The system property that enables the tests that need a serial device,
	 * whose value is the name of the serial device to open.
	 */
	private static final String PORT_PROPERTY = "nifty.modbus.test.jsc.port";

	/** A device that exists on Unix-like systems, but is not a serial port. */
	private static final String NOT_SERIAL_DEVICE = "/dev/null";

	@Test
	public void construct_null() {
		assertThrows(IllegalArgumentException.class, () -> {
			new JscSerialPort(null);
		}, "Null argument is not allowed");
	}

	@Test
	public void construct_empty() {
		assertThrows(IllegalArgumentException.class, () -> {
			new JscSerialPort("");
		}, "Empty argument is not allowed");
	}

	@Test
	public void name_notOpen() {
		// GIVEN
		JscSerialPort port = new JscSerialPort(MISSING_PORT);

		// THEN
		assertThat("Configured name returned when not open", port.getName(), is(equalTo(MISSING_PORT)));
		assertThat("Starts closed", port.isOpen(), is(equalTo(false)));
	}

	@Test
	public void open_missingPort() throws IOException {
		// GIVEN
		JscSerialPort port = new JscSerialPort(MISSING_PORT);

		// WHEN
		assertThrows(IOException.class, () -> {
			port.open(new BasicSerialParameters());
		}, "Opening a port that does not exist fails");

		// THEN
		assertThat("Port is not open after failing to open", port.isOpen(), is(equalTo(false)));
		assertThat("Configured name returned when not open", port.getName(), is(equalTo(MISSING_PORT)));
		assertThrows(IOException.class, () -> {
			port.getInputStream();
		}, "No input stream available when not open");
		assertThrows(IOException.class, () -> {
			port.getOutputStream();
		}, "No output stream available when not open");

		// closing a port that never opened is allowed
		port.close();
	}

	@Test
	public void provider() {
		// WHEN
		SerialPort port = new JscSerialPortProvider().getSerialPort(MISSING_PORT);

		// THEN
		assertThat("JSC serial port provided", port, is(instanceOf(JscSerialPort.class)));
		assertThat("Serial port is for name given", port.getName(), is(equalTo(MISSING_PORT)));
	}

	@Test
	@EnabledIfSystemProperty(named = PORT_PROPERTY, matches = ".+")
	public void open_notSerialDevice() throws IOException {
		assumeTrue(new File(NOT_SERIAL_DEVICE).exists(), "Requires " + NOT_SERIAL_DEVICE);

		// GIVEN
		JscSerialPort port = new JscSerialPort(NOT_SERIAL_DEVICE);

		// WHEN
		assertThrows(IOException.class, () -> {
			port.open(new BasicSerialParameters());
		}, "Opening a device that is not a serial port fails");

		// THEN
		assertThat("Port is not open after failing to open", port.isOpen(), is(equalTo(false)));
	}

	@Test
	@EnabledIfSystemProperty(named = PORT_PROPERTY, matches = ".+")
	public void open_close() throws IOException {
		final String portName = System.getProperty(PORT_PROPERTY);

		// open with a range of settings, to exercise how they are applied
		final SerialParity[] parities = SerialParity.values();
		final SerialStopBits[] stopBits = SerialStopBits.values();
		for ( int i = 0; i < parities.length; i++ ) {
			// GIVEN
			final BasicSerialParameters params = new BasicSerialParameters();
			params.setBaudRate(9600);
			params.setParity(parities[i]);
			params.setStopBits(stopBits[i % stopBits.length]);
			if ( i % 2 == 0 ) {
				params.setFlowControl(EnumSet.allOf(SerialFlowControl.class));
				params.setRs485ModeEnabled(true);
			}
			final JscSerialPort port = new JscSerialPort(portName);

			// WHEN
			port.open(params);
			try {
				// THEN
				final String msg = "Port with " + parities[i] + " parity";
				assertThat(msg + " is open", port.isOpen(), is(equalTo(true)));
				assertThat(msg + " has system name", port.getName(), is(notNullValue()));
				assertThat(msg + " has input stream", port.getInputStream(), is(notNullValue()));
				assertThat(msg + " has output stream", port.getOutputStream(), is(notNullValue()));

				// opening again does nothing
				port.open(params);
				assertThat(msg + " is still open", port.isOpen(), is(equalTo(true)));
			} finally {
				port.close();
			}
			assertThat("Port is closed", port.isOpen(), is(equalTo(false)));

			// closing again does nothing
			port.close();
		}
	}

}
