/* ==================================================================
 * SimpleTransactionIdSupplierTests.java - 29/11/2022 2:14:19 pm
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

import static net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier.INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier;
import net.solarnetwork.io.modbus.tcp.TcpModbusMessage;

/**
 * Test cases for the {@link SimpleTransactionIdSupplier} class.
 * 
 * <p>
 * This class is annotated with {@code @ResourceLock} to work with the static
 * counter within {@link SimpleTransactionIdSupplier}.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
@ResourceLock(value = "net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier")
public class SimpleTransactionIdSupplierTests {

	private static final Logger log = LoggerFactory.getLogger(SimpleTransactionIdSupplierTests.class);

	@BeforeEach
	public void setup() {
		INSTANCE.reset();
	}

	@Test
	public void lastIdAfterReset() {
		assertThat("After reset next ID is 1", INSTANCE.nextId(), is(equalTo(1)));
	}

	@Test
	public void firstIdAfterReset() {
		assertThat("After reset first ID is 1", INSTANCE.getAsInt(), is(equalTo(1)));
	}

	@Test
	public void idsIncrement() {
		int next = INSTANCE.nextId();
		for ( int i = 0; i < 10; i++ ) {
			assertThat(String.format("ID %d incremented", i), INSTANCE.getAsInt(), is(equalTo(next++)));
		}
	}

	@Test
	public void idsWrap() {
		INSTANCE.set(TcpModbusMessage.MAX_TRANSACTION_ID);
		assertThat("Get largest possible ID", INSTANCE.getAsInt(),
				is(equalTo(TcpModbusMessage.MAX_TRANSACTION_ID)));
		for ( int i = 1; i <= 10; i++ ) {
			assertThat(String.format("ID %d wrap incremented", i), INSTANCE.getAsInt(), is(equalTo(i)));
		}
	}

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private static final class IdTaker implements Runnable {

		private final int iterations;
		private final int taskId = COUNTER.incrementAndGet();
		private final List<Integer> ids = new ArrayList<>(50);

		private IdTaker(int iterations) {
			super();
			this.iterations = iterations;
		}

		@Override
		public void run() {
			log.info("Task {} starting with {} iterations", taskId, iterations);
			for ( int i = 0; i < iterations; i++ ) {
				ids.add(INSTANCE.getAsInt());
			}
			log.info("Task {} finished with {} iterations", taskId, iterations);
		}

	}

	@Test
	public void threaded_wrapped_noDuplicates() throws Exception {
		final int iterations = 1000;
		final int taskCount = 4;

		// make sure we wrap
		INSTANCE.set(TcpModbusMessage.MAX_TRANSACTION_ID - 500);

		ExecutorService s = Executors.newFixedThreadPool(taskCount);
		List<IdTaker> tasks = new ArrayList<>(taskCount);
		try {
			for ( int i = 0; i < taskCount; i++ ) {
				IdTaker task = new IdTaker(iterations);
				tasks.add(task);
				s.submit(task);
			}
		} finally {
			s.shutdown();
			s.awaitTermination(1, TimeUnit.MINUTES);
		}

		// merge all collected IDds into a single set; there should be taskCount * iterations values
		Set<Integer> allIds = new TreeSet<>();
		for ( IdTaker task : tasks ) {
			assertThat(String.format("Task %d collected all ids", task.taskId), task.ids,
					hasSize(iterations));
			allIds.addAll(task.ids);
		}
		log.info("Collected {} total IDs", allIds.size());
		assertThat("No duplicate ids collected", allIds, hasSize(taskCount * iterations));
	}

}
