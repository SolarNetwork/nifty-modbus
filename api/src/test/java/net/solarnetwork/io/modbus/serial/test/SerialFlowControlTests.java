/* ==================================================================
 * SerialFlowControlTests.java - 27/08/2024 4:16:16 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
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
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.serial.SerialFlowControl;

/**
 * Test cases for the {@link SerialFlowControl} class.
 *
 * @author matt
 * @version 1.0
 */
public class SerialFlowControlTests {

	@Test
	public void getCode() {
		assertThat(SerialFlowControl.None.getCode(), is(equalTo(SerialFlowControl.NO_FLOW_CONTROL)));
		assertThat(SerialFlowControl.RTS.getCode(), is(equalTo(SerialFlowControl.RTS_FLOW_CONTROL)));
		assertThat(SerialFlowControl.CTS.getCode(), is(equalTo(SerialFlowControl.CTS_FLOW_CONTROL)));
		assertThat(SerialFlowControl.DSR.getCode(), is(equalTo(SerialFlowControl.DSR_FLOW_CONTROL)));
		assertThat(SerialFlowControl.DTR.getCode(), is(equalTo(SerialFlowControl.DTR_FLOW_CONTROL)));
		assertThat(SerialFlowControl.XonXoffIn.getCode(),
				is(equalTo(SerialFlowControl.XONXOFF_IN_FLOW_CONTROL)));
		assertThat(SerialFlowControl.XonXoffOut.getCode(),
				is(equalTo(SerialFlowControl.XONXOFF_OUT_FLOW_CONTROL)));
	}

	@Test
	public void forCode() {
		assertThat(SerialFlowControl.forCode(SerialFlowControl.NO_FLOW_CONTROL),
				is(equalTo(SerialFlowControl.None)));
		assertThat(SerialFlowControl.forCode(SerialFlowControl.RTS_FLOW_CONTROL),
				is(equalTo(SerialFlowControl.RTS)));
		assertThat(SerialFlowControl.forCode(SerialFlowControl.CTS_FLOW_CONTROL),
				is(equalTo(SerialFlowControl.CTS)));
		assertThat(SerialFlowControl.forCode(SerialFlowControl.DSR_FLOW_CONTROL),
				is(equalTo(SerialFlowControl.DSR)));
		assertThat(SerialFlowControl.forCode(SerialFlowControl.DTR_FLOW_CONTROL),
				is(equalTo(SerialFlowControl.DTR)));
		assertThat(SerialFlowControl.forCode(SerialFlowControl.XONXOFF_IN_FLOW_CONTROL),
				is(equalTo(SerialFlowControl.XonXoffIn)));
		assertThat(SerialFlowControl.forCode(SerialFlowControl.XONXOFF_OUT_FLOW_CONTROL),
				is(equalTo(SerialFlowControl.XonXoffOut)));
	}

	@Test
	public void forCode_unknown() {
		assertThrows(IllegalArgumentException.class, () -> {
			SerialFlowControl.forCode(-1);
		}, "Unknown flow control code throws exception");
	}

	@Test
	public void forAbbreviation() {
		assertThat(SerialFlowControl.forAbbreviation("no"),
				is(equalTo(EnumSet.of(SerialFlowControl.None))));
		assertThat(SerialFlowControl.forAbbreviation("none"),
				is(equalTo(EnumSet.of(SerialFlowControl.None))));
		assertThat(SerialFlowControl.forAbbreviation("cts"),
				is(equalTo(EnumSet.of(SerialFlowControl.CTS))));
		assertThat(SerialFlowControl.forAbbreviation("rts/cts"),
				is(equalTo(EnumSet.of(SerialFlowControl.RTS, SerialFlowControl.CTS))));
		assertThat(SerialFlowControl.forAbbreviation("dsr"),
				is(equalTo(EnumSet.of(SerialFlowControl.DSR))));
		assertThat(SerialFlowControl.forAbbreviation("dtr/dsr"),
				is(equalTo(EnumSet.of(SerialFlowControl.DTR, SerialFlowControl.DSR))));
		assertThat(SerialFlowControl.forAbbreviation("xon"),
				is(equalTo(EnumSet.of(SerialFlowControl.XonXoffIn))));
		assertThat(SerialFlowControl.forAbbreviation("xoff"),
				is(equalTo(EnumSet.of(SerialFlowControl.XonXoffOut))));
		assertThat(SerialFlowControl.forAbbreviation("xon/xoff"),
				is(equalTo(EnumSet.of(SerialFlowControl.XonXoffIn, SerialFlowControl.XonXoffOut))));
	}

	@Test
	public void forAbbreviation_noneRemoved() {
		assertThat(SerialFlowControl.forAbbreviation("none/rts/cts"),
				is(equalTo(EnumSet.of(SerialFlowControl.RTS, SerialFlowControl.CTS))));
		assertThat(SerialFlowControl.forAbbreviation("none/dsr"),
				is(equalTo(EnumSet.of(SerialFlowControl.DSR))));
	}

	@Test
	public void forAbbreviation_noneOnly() {
		assertThat(SerialFlowControl.forAbbreviation("none"),
				is(equalTo(EnumSet.of(SerialFlowControl.None))));
	}

	@Test
	public void forAbbreviation_null() {
		assertThat(SerialFlowControl.forAbbreviation(null),
				is(equalTo(EnumSet.of(SerialFlowControl.None))));
	}

	@Test
	public void forAbbreviation_empty() {
		assertThat(SerialFlowControl.forAbbreviation(""),
				is(equalTo(EnumSet.of(SerialFlowControl.None))));
	}

	@Test
	public void forAbbreviation_unknown() {
		assertThrows(IllegalArgumentException.class, () -> {
			SerialFlowControl.forAbbreviation("foo");
		}, "Unknown flow control abbreviation throws exception");
	}

}
