/* ==================================================================
 * WriteHoldingRegisters.java - 17/04/2023 9:11:58 am
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

import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.writeHoldingRequest;
import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.writeHoldingsRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.netty.TcpNettyModbusClient;

/**
 * Examples on how to write a block of Modbus holding registers using the TCP
 * client.
 * 
 * @author matt
 * @version 1.0
 */
public final class WriteHoldingRegisters {

	private WriteHoldingRegisters() {
		// not available
	}

	/**
	 * Synchronously write a single holding register.
	 * 
	 * @param hostName
	 *        the host name or IP address of the Modbus server to connect to
	 * @param hostPort
	 *        the port number to connect on
	 * @param unitId
	 *        the Modbus unit ID to read from
	 * @param addr
	 *        the Modbus register address to start reading from
	 * @param data
	 *        the register data to write
	 * @throws Exception
	 *         if any error occurs
	 */
	public static void writeSync(String hostName, int hostPort, int unitId, int addr, short data)
			throws Exception {
		// create the TCP client
		NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig(hostName, hostPort);
		config.setAutoReconnect(false);
		ModbusClient client = new TcpNettyModbusClient(config);
		try {
			// connect the client
			client.start().get();

			// request holding registers
			ModbusMessage req = writeHoldingRequest(unitId, addr, data);
			ModbusMessage res = client.send(req);

			// print out the results
			if ( !res.isException() ) {
				System.out.println("Wrote holding register data.");
			} else {
				System.err.println("Error writing holding register data: " + res);
			}
		} finally {
			client.stop();
		}
	}

	/**
	 * Synchronously write a block of holding registers.
	 * 
	 * @param hostName
	 *        the host name or IP address of the Modbus server to connect to
	 * @param hostPort
	 *        the port number to connect on
	 * @param unitId
	 *        the Modbus unit ID to read from
	 * @param addr
	 *        the Modbus register address to start reading from
	 * @param data
	 *        the register data to write
	 * @throws Exception
	 *         if any error occurs
	 */
	public static void writeMultipleSync(String hostName, int hostPort, int unitId, int addr,
			short[] data) throws Exception {
		// create the TCP client
		NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig(hostName, hostPort);
		config.setAutoReconnect(false);
		ModbusClient client = new TcpNettyModbusClient(config);
		try {
			// connect the client
			client.start().get();

			// request holding registers
			ModbusMessage req = writeHoldingsRequest(unitId, addr, data);
			ModbusMessage res = client.send(req);

			if ( !res.isException() ) {
				System.out.println("Wrote holding register data.");
			} else {
				System.err.println("Error writing holding register data: " + res);
			}
		} finally {
			client.stop();
		}
	}

	/**
	 * Asynchronously write a block of holding registers.
	 * 
	 * @param hostName
	 *        the host name or IP address of the Modbus server to connect to
	 * @param hostPort
	 *        the port number to connect on
	 * @param unitId
	 *        the Modbus unit ID to read from
	 * @param addr
	 *        the Modbus register address to start reading from
	 * @param data
	 *        the register data to write
	 * @throws Exception
	 *         if any error occurs
	 */
	public static void writeMultipleAsync(String hostName, int hostPort, int unitId, int addr,
			short[] data) throws Exception {
		// create the TCP client
		NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig(hostName, hostPort);
		config.setAutoReconnect(false);
		ModbusClient client = new TcpNettyModbusClient(config);
		try {
			// connect the client
			client.start().get();

			// request holding registers
			ModbusMessage req = writeHoldingsRequest(unitId, addr, data);
			CompletableFuture<ModbusMessage> f = client.sendAsync(req);

			f.thenAccept(res -> {
				if ( !res.isException() ) {
					System.out.println("Wrote holding register data.");
				} else {
					System.err.println("Error writing holding register data: " + res);
				}
			})
					// wait for response, so we do not shut down client before it comes
					.get(10, TimeUnit.SECONDS);
		} finally {
			client.stop();
		}
	}

}
