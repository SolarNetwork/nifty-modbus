/* ==================================================================
 * TcpModbusMessageTests.java - 4/12/2022 7:13:43 am
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

package net.solarnetwork.io.modbus.tcp.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusFunctionCodes;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.tcp.TcpModbusMessage;

/**
 * Test cases for the {@link TcpModbusMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusMessageTests {

	private TcpModbusMessage msg() {
		class TestTcpModbusMessage extends BaseModbusMessage implements TcpModbusMessage {

			private TestTcpModbusMessage() {
				super(1, ModbusFunctionCodes.READ_COILS);
			}

			@Override
			public long getTimestamp() {
				return 0;
			}

			@Override
			public int getTransactionId() {
				return 0;
			}

		}
		;
		return new TestTcpModbusMessage();
	}

	@Test
	public void defaultProtocolId() {
		// GIVEN
		TcpModbusMessage msg = msg();

		// THEN
		assertThat("Default protocol ID provided", msg.getProtocolId(), is(equalTo(0)));
	}

}
