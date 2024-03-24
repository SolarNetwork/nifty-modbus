/* ==================================================================
 * PendingMessageTests.java - 25/03/2024 10:58:09 am
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

package net.solarnetwork.io.modbus.netty.handler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;

/**
 * Test cases for the {@link PendingMessage} class.
 *
 * @author matt
 * @version 1.0
 */
public class PendingMessageTests {

	@Test
	public void construct_nullMessage() {
		// GIVEN
		CompletableFuture<ModbusMessage> future = new CompletableFuture<>();

		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			new PendingMessage(null, future);
		}, "Null message not allowed");

	}

	@Test
	public void construct_nullFuture() {
		// GIVEN
		ModbusMessage msg = RegistersModbusMessage.readRegistersRequest(ModbusBlockType.Input, 1, 0, 1);

		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			new PendingMessage(msg, null);
		}, "Null future not allowed");

	}

	@Test
	public void accessors() {
		// GIVEN
		ModbusMessage msg = RegistersModbusMessage.readRegistersRequest(ModbusBlockType.Input, 1, 0, 1);
		CompletableFuture<ModbusMessage> future = new CompletableFuture<>();

		// WHEN
		long now = System.currentTimeMillis();
		PendingMessage pending = new PendingMessage(msg, future);

		// THEN
		assertThat("Request provded", pending.getRequest(), is(sameInstance(msg)));
		assertThat("Future provided", pending.getFuture(), is(sameInstance(future)));
		assertThat("Created roughly now", pending.getCreated() - now, is(lessThanOrEqualTo(100L)));
	}

	@Test
	public void toStringValue() {
		// GIVEN
		ModbusMessage msg = RegistersModbusMessage.readRegistersRequest(ModbusBlockType.Input, 1, 0, 1);
		CompletableFuture<ModbusMessage> future = new CompletableFuture<>();

		// WHEN
		PendingMessage pending = new PendingMessage(msg, future);

		// THEN
		Pattern toStringPat = Pattern.compile(
				"PendingMessage\\{created=\\d+, request=" + Pattern.quote(msg.toString()) + "\\}");
		assertThat("toString() generated", pending.toString(), matchesPattern(toStringPat));
	}

}
