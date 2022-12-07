/* ==================================================================
 * TcpServerExample.java - 7/12/2022 2:11:47 pm
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

import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readHoldingsResponse;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusServer;

/**
 * Example on using the TCP server to respond to Modbus requests.
 *
 * @author matt
 * @version 1.0
 */
public class TcpServerExample {

	/**
	 * Example TCP client read some registers.
	 * 
	 * @param args
	 *        the arguments
	 */
	public static void main(String... args) throws Exception {
		if ( args == null || args.length < 2 ) {
			System.err.println("Must provide the the --port [port] arguments.");
			System.exit(1);
		}

		//
		// Determine the port to listen on
		//

		int bindPort = 5502;
		for ( int i = 0, len = args.length; i < len; i++ ) {
			try {
				switch (args[i]) {
					case "-p":
					case "--port":
						bindPort = Integer.parseInt(args[++i]);
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

		//
		// Create the TCP server on the provided port.
		// 

		NettyTcpModbusServer server = new NettyTcpModbusServer(bindPort);
		server.setMessageHandler((msg, sender) -> {
			// this handler only supports read holding registers requests
			RegistersModbusMessage req = msg.unwrap(RegistersModbusMessage.class);
			if ( req != null && req.getFunction().blockType() == ModbusBlockType.Holding ) {

				// generate some fake data that matches the request register count
				short[] resultData = new short[req.getCount()];
				for ( int i = 0; i < resultData.length; i++ ) {
					resultData[i] = (short) i;
				}

				// respond with the fake data
				sender.accept(readHoldingsResponse(req.getUnitId(), req.getAddress(), resultData));
			} else {
				// send back error that we don't handle that request
				sender.accept(new BaseModbusMessage(msg.getUnitId(), msg.getFunction(),
						ModbusErrorCode.IllegalFunction));
			}
		});

		// 
		// Start the server
		// 

		try {
			server.start();
			System.out.println("Modbus server listening on port " + bindPort);
			while ( true ) {
				Thread.sleep(60_000);
			}
		} finally {
			server.stop();
		}
	}

}
