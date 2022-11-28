/* ==================================================================
 * TcpModbusMessage.java - 25/11/2022 6:32:39 pm
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

package net.solarnetwork.io.modbus.tcp.netty;

import static net.solarnetwork.io.modbus.ModbusByteUtils.encode16;
import io.netty.buffer.ByteBuf;
import net.solarnetwork.io.modbus.ModbusErrorCode;
import net.solarnetwork.io.modbus.ModbusFunctionCode;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusPayloadEncoder;

/**
 * A TCP Modbus message.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusMessage
		implements net.solarnetwork.io.modbus.tcp.TcpModbusMessage, ModbusPayloadEncoder {

	/** The TCP protocol ID. */
	public static final int TCP_PROTOCOL_ID = 0;

	private final int transactionId;
	private final ModbusMessage body;

	/**
	 * Constructor.
	 * 
	 * @param transactionId
	 *        the trasaction ID
	 * @param body
	 *        the message body, must implement {@link ModbusPayloadEncoder}.
	 * @throws IllegalArgumentException
	 *         if {@code body} does not implement {@link ModbusPayloadEncoder}
	 */
	public TcpModbusMessage(int transactionId, ModbusMessage body) {
		super();
		this.transactionId = transactionId;
		if ( body == null ) {
			throw new IllegalArgumentException("The body argument must not be null.");
		} else if ( !(body instanceof ModbusPayloadEncoder) ) {
			throw new IllegalArgumentException("The body argument must implement ModbusPayloadEncoder.");
		}
		this.body = body;
	}

	@Override
	public int getTransactionId() {
		return transactionId;
	}

	@Override
	public int getUnitId() {
		return body.getUnitId();
	}

	@Override
	public ModbusFunctionCode getFunction() {
		return body.getFunction();
	}

	@Override
	public ModbusErrorCode getError() {
		return body.getError();
	}

	/*- TCP frame structure:
	 
	   |0-|2-|4-|6||7|8..|
	   +----------||-----+
	   |tt|pp|ll|u||f|...|
	   +-----------------+
	   
	   tt = 16-bit transaction ID
	   pp = 16-bit protocol ID (0 for TCP)
	   ll = remaining byte length
	   u  = unit ID
	   f  = function code + data
	 */

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		byte[] header = new byte[7];
		encode16(header, 0, transactionId);
		encode16(header, 4, 1 + ((ModbusPayloadEncoder) body).payloadLength());
		header[6] = (byte) body.getUnitId();
		((ModbusPayloadEncoder) body).encodeModbusPayload(out);
	}

	@Override
	public int payloadLength() {
		return 7 + ((ModbusPayloadEncoder) body).payloadLength();
	}

}
