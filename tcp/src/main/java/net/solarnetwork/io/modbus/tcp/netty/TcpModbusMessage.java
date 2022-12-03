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
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusPayloadEncoder;

/**
 * A TCP-encapsulated Modbus message.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusMessage
		implements net.solarnetwork.io.modbus.tcp.TcpModbusMessage, ModbusPayloadEncoder {

	/** The TCP protocol ID. */
	public static final int TCP_PROTOCOL_ID = 0;

	private final long timestamp;
	private final int transactionId;
	private final ModbusMessage body;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The current system time will be used for the timestamp value.
	 * </p>
	 * 
	 * @param transactionId
	 *        the transaction ID
	 * @param body
	 *        the message body, must implement {@link ModbusPayloadEncoder}.
	 * @throws IllegalArgumentException
	 *         if {@code body} does not implement {@link ModbusPayloadEncoder}
	 */
	public TcpModbusMessage(int transactionId, ModbusMessage body) {
		this(System.currentTimeMillis(), transactionId, body);
	}

	/**
	 * Constructor.
	 * 
	 * @param timestamp
	 *        the timestamp
	 * @param transactionId
	 *        the transaction ID
	 * @param body
	 *        the message body, must implement {@link ModbusPayloadEncoder}.
	 * @throws IllegalArgumentException
	 *         if {@code body} does not implement {@link ModbusPayloadEncoder}
	 */
	public TcpModbusMessage(long timestamp, int transactionId, ModbusMessage body) {
		super();
		this.timestamp = timestamp;
		this.transactionId = transactionId;
		if ( body == null ) {
			throw new IllegalArgumentException("The body argument must not be null.");
		} else if ( !(body instanceof ModbusPayloadEncoder) ) {
			throw new IllegalArgumentException("The body argument must implement ModbusPayloadEncoder.");
		}
		this.body = body;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
		if ( msgType.isAssignableFrom(body.getClass()) ) {
			return (T) body;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TcpModbusMessage{timestamp=");
		builder.append(timestamp);
		builder.append(", txId=");
		builder.append(transactionId);
		builder.append(", ");
		if ( body != null ) {
			builder.append("body=");
			builder.append(body);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public long getTimestamp() {
		return timestamp;
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
	public ModbusFunction getFunction() {
		return body.getFunction();
	}

	@Override
	public ModbusErrorCode getError() {
		return body.getError();
	}

	/*- TCP frame structure:
	 
	   |0-|2-|4-|6||7|8..|
	   +--+--+--+-||-+---+
	   |tt|pp|ll|u||f|...|
	   +--+--+--+-++-+---+
	   
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
		out.writeBytes(header);
		((ModbusPayloadEncoder) body).encodeModbusPayload(out);
	}

	@Override
	public int payloadLength() {
		return 7 + ((ModbusPayloadEncoder) body).payloadLength();
	}

}
