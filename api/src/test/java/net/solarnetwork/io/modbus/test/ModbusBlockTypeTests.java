/* ==================================================================
 * ModbusBlockTypeTests.java - 5/12/2022 5:28:30 pm
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

package net.solarnetwork.io.modbus.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusBlockType;

/**
 * Test cases for the {@link ModbusBlockType} class.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusBlockTypeTests {

	// @formatter:off
	
	@Test
	public void getCode() {
		assertThat(ModbusBlockType.Coil.getCode(), is(equalTo(0)));
		assertThat(ModbusBlockType.Discrete.getCode(), is(equalTo(1)));
		assertThat(ModbusBlockType.Holding.getCode(), is(equalTo(3)));
		assertThat(ModbusBlockType.Input.getCode(), is(equalTo(4)));
		assertThat(ModbusBlockType.Diagnostic.getCode(), is(equalTo(-1)));
	}

	@Test
	public void bitType() {
		assertThat(ModbusBlockType.Coil.isBitType(), is(equalTo(true)));
		assertThat(ModbusBlockType.Discrete.isBitType(), is(equalTo(true)));
		assertThat(ModbusBlockType.Holding.isBitType(), is(equalTo(false)));
		assertThat(ModbusBlockType.Input.isBitType(), is(equalTo(false)));
		assertThat(ModbusBlockType.Diagnostic.isBitType(), is(equalTo(false)));
	}

	@Test
	public void bitCount() {
		assertThat(ModbusBlockType.Coil.getBitCount(), is(equalTo(1)));
		assertThat(ModbusBlockType.Discrete.getBitCount(), is(equalTo(1)));
		assertThat(ModbusBlockType.Holding.getBitCount(), is(equalTo(16)));
		assertThat(ModbusBlockType.Input.getBitCount(), is(equalTo(16)));
		assertThat(ModbusBlockType.Diagnostic.getBitCount(), is(equalTo(0)));
	}

	@Test
	public void readOnly() {
		assertThat(ModbusBlockType.Coil.isReadOnly(), is(equalTo(false)));
		assertThat(ModbusBlockType.Discrete.isReadOnly(), is(equalTo(true)));
		assertThat(ModbusBlockType.Holding.isReadOnly(), is(equalTo(false)));
		assertThat(ModbusBlockType.Input.isReadOnly(), is(equalTo(true)));
		assertThat(ModbusBlockType.Diagnostic.isReadOnly(), is(equalTo(true)));
	}
	
	@Test
	public void valueOf() {
		assertThat(ModbusBlockType.valueOf(0), is(equalTo(ModbusBlockType.Coil)));
		assertThat(ModbusBlockType.valueOf(1), is(equalTo(ModbusBlockType.Discrete)));
		assertThat(ModbusBlockType.valueOf(3), is(equalTo(ModbusBlockType.Holding)));
		assertThat(ModbusBlockType.valueOf(4), is(equalTo(ModbusBlockType.Input)));
		assertThat(ModbusBlockType.valueOf(-1), is(equalTo(ModbusBlockType.Diagnostic)));
	}
	
	@Test
	public void valueOf_user() {
		assertThat(ModbusBlockType.valueOf(0x65), is(nullValue()));
	}
	
	@Test
	public void forCode() {
		assertThat(ModbusBlockType.forCode(0), is(equalTo(ModbusBlockType.Coil)));
		assertThat(ModbusBlockType.forCode(1), is(equalTo(ModbusBlockType.Discrete)));
		assertThat(ModbusBlockType.forCode(3), is(equalTo(ModbusBlockType.Holding)));
		assertThat(ModbusBlockType.forCode(4), is(equalTo(ModbusBlockType.Input)));
		assertThat(ModbusBlockType.forCode(-1), is(equalTo(ModbusBlockType.Diagnostic)));
	}
	
	@Test
	public void forCode_user() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ModbusBlockType.forCode(0x65);
		}, "forCode throws IllegalArgumentException for user-defined block type codes");
	}
	
}
