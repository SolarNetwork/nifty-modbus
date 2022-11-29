/* ==================================================================
 * SimpleTransactionIdSupplier.java - 29/11/2022 9:15:14 am
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

package net.solarnetwork.io.modbus.tcp;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessage;

/**
 * Simple in-memory supplier for Modbus TCP transaction IDs.
 * 
 * <p>
 * Note the IDs supplied by this class are global across all instances.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class SimpleTransactionIdSupplier implements IntSupplier {

	/** A global default instance. */
	public static final SimpleTransactionIdSupplier INSTANCE = new SimpleTransactionIdSupplier();

	private static final AtomicInteger COUNTER = new AtomicInteger(1);

	@Override
	public int getAsInt() {
		int result;
		int next;
		do {
			result = COUNTER.get();
			next = (result < TcpModbusMessage.MAX_TRANSACTION_ID ? result + 1 : 1);
		} while ( !COUNTER.compareAndSet(result, next) );
		return result;
	}

	/**
	 * Get the transaction ID that would be returned next from
	 * {@link #getAsInt()}.
	 * 
	 * @return the next ID
	 */
	public int nextId() {
		return COUNTER.get();
	}

	/**
	 * Reset the transaction counter.
	 * 
	 * <p>
	 * The next call to {@link #getAsInt()} will return {@literal 1}.
	 * </p>
	 */
	public void reset() {
		COUNTER.set(1);
	}

	/**
	 * Force the transaction counter to a specific value.
	 * 
	 * @param value
	 *        the "next" value to set; the next call to {@link #getAsInt()} will
	 *        return this value
	 */
	public void set(int value) {
		COUNTER.set(value);
	}

}
