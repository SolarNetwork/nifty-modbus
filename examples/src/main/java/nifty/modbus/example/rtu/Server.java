/* ==================================================================
 * Server.java - 12/01/2026 3:29:24 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

package nifty.modbus.example.rtu;

import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readInputsResponse;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.rtu.jsc.JscSerialPortProvider;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusServer;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParameters;

/**
 * Modbus RTU server example.
 *
 * @author matt
 * @version 1.0
 */
public class Server {

	private Server() {
		// not available
	}

	/**
	 * Run a read-only RTU server.
	 *
	 * <p>
	 * Pass the serial device name and baud as arguments, e.g.
	 * {@code /dev/ttyUSB0 9600}.
	 * </p>
	 *
	 * @param args
	 *        the serial device and desired baud
	 */
	public static void main(String[] args) {
		if ( args.length < 2 ) {
			System.err.println("Pass serial device name followed by desired baud.");
			System.exit(1);
		}
		BasicSerialParameters params = new BasicSerialParameters();
		try {
			params.setBaudRate(Integer.parseInt(args[1]));
		} catch ( NumberFormatException e ) {
			System.err.println("The baud (second argument) must be an integer, e.g. 9600.");
			System.exit(1);
		}
		try {
			serveReadOnlyInputs(args[0], params);
		} catch ( Exception e ) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	/**
	 * Start a Modbus RTU server that can handle read input register requests,
	 * returning fake register data in each response.
	 *
	 * @param device
	 *        the serial device to listen on, e.g. {@code /dev/ttyAMA0}
	 * @param serialParameters
	 *        the serial parameters
	 * @throws Exception
	 *         if an error occurs
	 */
	public static void serveReadOnlyInputs(final String device, final SerialParameters serialParameters)
			throws Exception {
		NettyRtuModbusServer server = new NettyRtuModbusServer(device, serialParameters,
				new JscSerialPortProvider());
		server.setMessageHandler((msg, sender) -> {
			// this handler only supports read input registers requests
			RegistersModbusMessage req = msg.unwrap(RegistersModbusMessage.class);
			if ( req != null && req.getFunction().blockType() == ModbusBlockType.Input ) {

				// generate some fake data that matches the request register count
				short[] resultData = new short[req.getCount()];
				for ( int i = 0; i < resultData.length; i++ ) {
					resultData[i] = (short) i;
				}

				// respond with the fake data
				sender.accept(readInputsResponse(req.getUnitId(), req.getAddress(), resultData));
			} else {
				// send back error that we don't handle that request
				sender.accept(new BaseModbusMessage(msg.getUnitId(), msg.getFunction(),
						ModbusErrorCode.IllegalFunction));
			}
		});

		try {
			server.start();

			// a real application would have another way to keep the server alive
			while ( true ) {
				Thread.sleep(60_000);
			}
		} finally {
			server.stop();
		}
	}

}
