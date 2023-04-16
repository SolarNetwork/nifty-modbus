/* ==================================================================
 * Server.java - 17/04/2023 9:29:35 am
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

import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readHoldingsResponse;
import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.readInputsResponse;
import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.writeHoldingResponse;
import static net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage.writeHoldingsResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.RegistersModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.BaseModbusMessage;
import net.solarnetwork.io.modbus.tcp.netty.NettyTcpModbusServer;

/**
 * Modbus TCP server example.
 * 
 * @author matt
 * @version 1.0
 */
public final class Server {

	private Server() {
		// not available
	}

	/**
	 * Start a Modbus TCP server that can handle read input register requests,
	 * returning fake register data in each response.
	 * 
	 * @param bindPort
	 *        the port to listen on
	 * @throws Exception
	 *         if an error occurs
	 */
	public void serveReadOnlyInputs(int bindPort) throws Exception {
		NettyTcpModbusServer server = new NettyTcpModbusServer(bindPort);
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

	/**
	 * Start a Modbus TCP server that can handle read/write holding register
	 * requests.
	 * 
	 * <p>
	 * This example uses a simple {@code Map} as the holding registers
	 * repository.
	 * </p>
	 * 
	 * @param bindPort
	 *        the port to listen on
	 * @throws Exception
	 *         if an error occurs
	 */
	public void serveReadWriteHoldings(int bindPort) throws Exception {
		final ConcurrentMap<Integer, Short> holdingRegisters = new ConcurrentHashMap<>(32, 0.9f, 2);

		NettyTcpModbusServer server = new NettyTcpModbusServer(bindPort);
		server.setMessageHandler((msg, sender) -> {
			// this handler only supports read/write holding register requests
			RegistersModbusMessage req = msg.unwrap(RegistersModbusMessage.class);
			ModbusFunctionCode fn = (req != null ? req.getFunction().functionCode() : null);
			if ( fn == ModbusFunctionCode.ReadHoldingRegisters ) {

				// return the current holding register data; up to a maximum of 64 registers;
				// 64 is arbitrary, just to show a limit
				short[] resultData = new short[Math.min(req.getCount(), 64)];
				for ( int i = req.getAddress(), max = req.getAddress() + resultData.length,
						idx = 0; i < max; i++, idx++ ) {
					Short r = holdingRegisters.get(i);
					if ( r != null ) {
						resultData[idx] = r.shortValue();
					}
				}
				sender.accept(readHoldingsResponse(req.getUnitId(), req.getAddress(), resultData));

			} else if ( fn == ModbusFunctionCode.WriteHoldingRegister ) {

				// write single holding register
				short data = req.dataDecode()[0];
				holdingRegisters.put(req.getAddress(), data);
				sender.accept(writeHoldingResponse(req.getUnitId(), req.getAddress(), data));

			} else if ( fn == ModbusFunctionCode.WriteHoldingRegisters ) {

				// write multiple holding registers
				short[] data = req.dataDecode();
				for ( int i = req.getAddress(), max = req.getAddress() + data.length,
						idx = 0; i < max; i++, idx++ ) {
					holdingRegisters.put(i, data[idx]);
				}
				sender.accept(writeHoldingsResponse(req.getUnitId(), req.getAddress(), data.length));

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
