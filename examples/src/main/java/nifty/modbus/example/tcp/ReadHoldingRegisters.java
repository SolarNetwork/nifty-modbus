/* ==================================================================
 * ReadHoldingRegisters.java - 17/04/2023 8:00:02 am
 * 
 * Copyright 2023 SolarNetwork.net Dev Team
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

package nifty.modbus.example.tcp;

import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readHoldingsRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.netty.TcpNettyModbusClient;

/**
 * Examples on how to read a block of Modbus holding registers using the TCP
 * client.
 * 
 * @author matt
 * @version 1.0
 */
public final class ReadHoldingRegisters {

	private ReadHoldingRegisters() {
		// not available
	}

	/**
	 * Synchronously read a block of holding registers and print out the results
	 * as signed 16-bit numbers.
	 * 
	 * @param hostName
	 *        the host name or IP address of the Modbus server to connect to
	 * @param hostPort
	 *        the port number to connect on
	 * @param unitId
	 *        the Modbus unit ID to read from
	 * @param addr
	 *        the Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus registers to read
	 * @throws Exception
	 *         if any error occurs
	 */
	public static void readSync(String hostName, int hostPort, int unitId, int addr, int count)
			throws Exception {
		// create the TCP client
		NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig(hostName, hostPort);
		config.setAutoReconnect(false);
		ModbusClient client = new TcpNettyModbusClient(config);
		try {
			// connect the client
			client.start().get();

			// request holding registers
			ModbusMessage req = readHoldingsRequest(unitId, addr, count);
			RegistersModbusMessage res = client.send(req).unwrap(RegistersModbusMessage.class);

			// print out the results
			short[] data = res.dataDecode();
			for ( int i = 0, len = data.length; i < len; i++ ) {
				System.out.printf("%d: 0x%04X\n", addr + i, data[i]);
			}
		} finally {
			client.stop();
		}
	}

	/**
	 * Asynchronously read a block of holding registers and print out the
	 * results as unsigned 16-bit numbers.
	 * 
	 * @param hostName
	 *        the host name or IP address of the Modbus server to connect to
	 * @param hostPort
	 *        the port number to connect on
	 * @param unitId
	 *        the Modbus unit ID to read from
	 * @param addr
	 *        the Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus registers to read
	 * @throws Exception
	 *         if any error occurs
	 */
	public static void readAsync(String hostName, int hostPort, int unitId, int addr, int count)
			throws Exception {
		// create the TCP client
		NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig(hostName, hostPort);
		config.setAutoReconnect(false);
		ModbusClient client = new TcpNettyModbusClient(config);
		try {
			// connect the client
			client.start().get();

			// request holding registers
			ModbusMessage req = readHoldingsRequest(unitId, addr, count);
			CompletableFuture<ModbusMessage> f = client.sendAsync(req);

			f.thenAccept(msg -> {
				// print out the results
				RegistersModbusMessage res = msg.unwrap(RegistersModbusMessage.class);
				int[] data = res.dataDecodeUnsigned();
				for ( int i = 0, len = data.length; i < len; i++ ) {
					System.out.printf("%d: 0x%04X\n", addr + i, data[i]);
				}
			})
					// wait for response, so we do not shut down client before it comes
					.get(10, TimeUnit.SECONDS);
		} finally {
			client.stop();
		}
	}

}
