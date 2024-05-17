/* ==================================================================
 * SerialParityTests.java - 6/12/2022 3:25:42 pm
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.serial.SerialParity;

/**
 * Test cases for the {@link SerialParityTests} class.
 *
 * @author matt
 * @version 1.0
 */
public class SerialParityTests {

	@Test
	public void getCode() {
		assertThat(SerialParity.None.getCode(), is(equalTo(SerialParity.NO_PARITY)));
		assertThat(SerialParity.Even.getCode(), is(equalTo(SerialParity.EVEN_PARITY)));
		assertThat(SerialParity.Odd.getCode(), is(equalTo(SerialParity.ODD_PARITY)));
		assertThat(SerialParity.Mark.getCode(), is(equalTo(SerialParity.MARK_PARITY)));
		assertThat(SerialParity.Space.getCode(), is(equalTo(SerialParity.SPACE_PARITY)));
	}

	@Test
	public void forCode() {
		assertThat(SerialParity.forCode(SerialParity.NO_PARITY), is(equalTo(SerialParity.None)));
		assertThat(SerialParity.forCode(SerialParity.EVEN_PARITY), is(equalTo(SerialParity.Even)));
		assertThat(SerialParity.forCode(SerialParity.ODD_PARITY), is(equalTo(SerialParity.Odd)));
		assertThat(SerialParity.forCode(SerialParity.MARK_PARITY), is(equalTo(SerialParity.Mark)));
		assertThat(SerialParity.forCode(SerialParity.SPACE_PARITY), is(equalTo(SerialParity.Space)));
	}

	@Test
	public void forCode_unknown() {
		assertThrows(IllegalArgumentException.class, () -> {
			SerialParity.forCode(-1);
		}, "Unknown parity code throws exception");
	}

	@Test
	public void getAbbreviation() {
		assertThat(SerialParity.None.getAbbreviation(),
				is(equalTo(SerialParity.NO_PARITY_ABBREVIATION)));
		assertThat(SerialParity.Even.getAbbreviation(),
				is(equalTo(SerialParity.EVEN_PARITY_ABBREVIATION)));
		assertThat(SerialParity.Odd.getAbbreviation(),
				is(equalTo(SerialParity.ODD_PARITY_ABBREVIATION)));
		assertThat(SerialParity.Mark.getAbbreviation(),
				is(equalTo(SerialParity.MARK_PARITY_ABBREVIATION)));
		assertThat(SerialParity.Space.getAbbreviation(),
				is(equalTo(SerialParity.SPACE_PARITY_ABBREVIATION)));
	}

	@Test
	public void forAbbreviation() {
		assertThat(SerialParity.forAbbreviation(SerialParity.NO_PARITY_ABBREVIATION),
				is(equalTo(SerialParity.None)));
		assertThat(SerialParity.forAbbreviation(SerialParity.EVEN_PARITY_ABBREVIATION),
				is(equalTo(SerialParity.Even)));
		assertThat(SerialParity.forAbbreviation(SerialParity.ODD_PARITY_ABBREVIATION),
				is(equalTo(SerialParity.Odd)));
		assertThat(SerialParity.forAbbreviation(SerialParity.MARK_PARITY_ABBREVIATION),
				is(equalTo(SerialParity.Mark)));
		assertThat(SerialParity.forAbbreviation(SerialParity.SPACE_PARITY_ABBREVIATION),
				is(equalTo(SerialParity.Space)));
	}

	@Test
	public void forAbbreviation_lower() {
		assertThat(SerialParity.forAbbreviation(SerialParity.NO_PARITY_ABBREVIATION.toLowerCase()),
				is(equalTo(SerialParity.None)));
		assertThat(SerialParity.forAbbreviation(SerialParity.EVEN_PARITY_ABBREVIATION.toLowerCase()),
				is(equalTo(SerialParity.Even)));
		assertThat(SerialParity.forAbbreviation(SerialParity.ODD_PARITY_ABBREVIATION.toLowerCase()),
				is(equalTo(SerialParity.Odd)));
		assertThat(SerialParity.forAbbreviation(SerialParity.MARK_PARITY_ABBREVIATION.toLowerCase()),
				is(equalTo(SerialParity.Mark)));
		assertThat(SerialParity.forAbbreviation(SerialParity.SPACE_PARITY_ABBREVIATION.toLowerCase()),
				is(equalTo(SerialParity.Space)));
	}

	@Test
	public void forAbbreviation_null() {
		assertThat("Null input returns null", SerialParity.forAbbreviation(null), is(nullValue()));
	}

	@Test
	public void forAbbreviation_unknown() {
		assertThrows(IllegalArgumentException.class, () -> {
			SerialParity.forAbbreviation("?");
		}, "Unknown parity abbreviation throws exception");
	}

}
