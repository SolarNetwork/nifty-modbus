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
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.rtu.jsc.JscSerialPort;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;

/**
 * Test cases for the {@link JscSerialPort} class.
 *
 * @author matt
 * @version 1.0
 */
public class JscSerialPortTests {

	private static final String MISSING_PORT = "/dev/nifty-modbus-does-not-exist";

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

}
