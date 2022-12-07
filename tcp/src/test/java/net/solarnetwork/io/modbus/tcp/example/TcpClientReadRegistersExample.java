/* ==================================================================
 * TcpClientReadRegistersExample.java - 7/12/2022 12:31:08 pm
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

package net.solarnetwork.io.modbus.tcp.example;

import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readHoldingsRequest;
import net.solarnetwork.io.modbus.ModbusClient;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusClientConfig;
import net.solarnetwork.io.modbus.tcp.netty.TcpNettyModbusClient;

/**
 * Example on using the TCP client to read some Modbus registers.
 *
 * @author matt
 * @version 1.0
 */
public class TcpClientReadRegistersExample {

	/**
	 * Example TCP client read some registers.
	 * 
	 * @param args
	 *        the arguments
	 */
	public static void main(String... args) throws Exception {
		if ( args == null || args.length < 2 ) {
			System.err.println("Must provide the the --host [host] arguments.");
			System.exit(1);
		}

		//
		// Determine the host + port to connect to, along with the unit ID,
		// starting register address, and count of registers to read.
		//

		String hostName = null;
		int hostPort = 502;
		int unitId = -1;
		int addr = -1;
		int count = 1;
		for ( int i = 0, len = args.length; i < len; i++ ) {
			try {
				switch (args[i]) {
					case "-h":
					case "--host":
						hostName = args[++i];
						break;

					case "-p":
					case "--port":
						hostPort = Integer.parseInt(args[++i]);
						break;

					case "-a":
					case "--unit":
						unitId = Integer.parseInt(args[++i]);
						break;

					case "-r":
					case "--register":
						addr = Integer.parseInt(args[++i]);
						break;

					case "-c":
					case "--count":
						count = Integer.parseInt(args[++i]);
						break;
				}
			} catch ( ArrayIndexOutOfBoundsException e ) {
				System.err.printf("Missing value for %s argument.\n", args[i - 1]);
				System.exit(1);
			} catch ( NumberFormatException e ) {
				System.err.printf("Expected a number value for %s argument.\n", args[i - 1]);
				System.exit(1);
			} catch ( IllegalArgumentException e ) {
				System.err.printf("Invalid value for %s argument: %s\n", args[i - 1], e.getMessage());
				System.exit(1);
			}
		}

		if ( addr < 0 ) {
			System.err.println(
					"Must provide the the starting register to read with the --register [reg] arguments.");
			System.exit(1);
		}

		//
		// Create the TCP client for the provided host + port.
		// 

		NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig(hostName, hostPort);
		config.setAutoReconnect(false);
		ModbusClient client = new TcpNettyModbusClient(config);

		//
		// Read the given registers and print them out.
		//

		try {
			client.start().get();

			ModbusMessage req = readHoldingsRequest(unitId, addr, count);
			RegistersModbusMessage res = client.send(req).unwrap(RegistersModbusMessage.class);

			short[] data = res.dataDecode();
			for ( int i = 0, len = data.length; i < len; i++ ) {
				System.out.printf("%d: 0x%04X\n", addr + i, data[i]);
			}
		} finally {
			client.stop();
		}
	}

}
