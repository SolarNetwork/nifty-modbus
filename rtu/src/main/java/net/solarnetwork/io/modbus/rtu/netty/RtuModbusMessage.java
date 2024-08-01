/* ==================================================================
 * RtuModbusMessage.java - 1/12/2022 3:04:29 pm
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

package net.solarnetwork.io.modbus.rtu.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.solarnetwork.io.modbus.ModbusByteUtils;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusValidationException;
import net.solarnetwork.io.modbus.netty.msg.ModbusPayloadEncoder;

/**
 * A RTU-encapsulated Modbus message.
 *
 * @author matt
 * @version 1.0
 */
public class RtuModbusMessage
		implements net.solarnetwork.io.modbus.rtu.RtuModbusMessage, ModbusPayloadEncoder {

	private final long timestamp;
	private final short crc;
	private final ModbusMessage body;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The current system time will be used for the timestamp value, and the CRC
	 * will be calculated from the {@code unitId} and {@code body} values.
	 * </p>
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param body
	 *        the message body, must implement {@link ModbusPayloadEncoder}.
	 * 
	 * @throws ClassCastException
	 *         if {@code body} does not implement {@link ModbusPayloadEncoder}
	 */
	public RtuModbusMessage(int unitId, ModbusMessage body) {
		this(System.currentTimeMillis(), body, computeCrc(unitId, body));
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The CRC will be calculated from the {@code unitId} and {@code body}
	 * values.
	 * </p>
	 * 
	 * @param timestamp
	 *        the timestamp
	 * @param unitId
	 *        the unit ID
	 * @param body
	 *        the message body, must implement {@link ModbusPayloadEncoder}.
	 * 
	 * @throws ClassCastException
	 *         if {@code body} does not implement {@link ModbusPayloadEncoder}
	 */
	public RtuModbusMessage(long timestamp, int unitId, ModbusMessage body) {
		this(timestamp, body, computeCrc(unitId, body));
	}

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The current system time will be used for the timestamp value.
	 * </p>
	 * 
	 * @param body
	 *        the message body, must implement {@link ModbusPayloadEncoder}.
	 * @param crc
	 *        the provided cyclic redundancy check value
	 * 
	 * @throws IllegalArgumentException
	 *         if {@code body} does not implement {@link ModbusPayloadEncoder}
	 */
	public RtuModbusMessage(ModbusMessage body, short crc) {
		this(System.currentTimeMillis(), body, crc);
	}

	/**
	 * Constructor.
	 * 
	 * @param timestamp
	 *        the timestamp
	 * @param body
	 *        the message body, must implement {@link ModbusPayloadEncoder}.
	 * @param crc
	 *        the provided cyclic redundancy check value
	 * @throws IllegalArgumentException
	 *         if {@code body} does not implement {@link ModbusPayloadEncoder}
	 */
	public RtuModbusMessage(long timestamp, ModbusMessage body, short crc) {
		super();
		this.timestamp = timestamp;
		this.crc = crc;
		if ( body == null ) {
			throw new IllegalArgumentException("The body argument must not be null.");
		} else if ( !(body instanceof ModbusPayloadEncoder) ) {
			throw new IllegalArgumentException("The body argument must implement ModbusPayloadEncoder.");
		}
		this.body = body;
	}

	@Override
	public RtuModbusMessage validate() throws ModbusValidationException {
		net.solarnetwork.io.modbus.rtu.RtuModbusMessage.super.validate();
		body.validate();
		return this;
	}

	@Override
	public boolean isSameAs(ModbusMessage obj) {
		if ( obj == this ) {
			return true;
		}
		if ( !(obj instanceof RtuModbusMessage) ) {
			return false;
		}
		RtuModbusMessage other = (RtuModbusMessage) obj;
		if ( crc != other.crc ) {
			return false;
		}
		return body.isSameAs(other.body);
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
		builder.append("RtuModbusMessage{timestamp=");
		builder.append(timestamp);
		builder.append(", crc=");
		builder.append(Short.toUnsignedInt(crc));
		builder.append(", body=");
		builder.append(body);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the wrapped message.
	 * 
	 * @return the wrapped message
	 */
	public ModbusMessage getBody() {
		return body;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public short getCrc() {
		return crc;
	}

	@Override
	public short computeCrc() {
		return computeCrc(getUnitId(), body);
	}

	/**
	 * Compute the cyclic redundancy check of a message and given unit ID.
	 * 
	 * @param unitId
	 *        the unit ID
	 * @param body
	 *        the message
	 * @return the computed CRC
	 */
	public static short computeCrc(int unitId, ModbusMessage body) {
		if ( !(body instanceof ModbusPayloadEncoder) ) {
			return (short) 0;
		}
		ModbusPayloadEncoder enc = (ModbusPayloadEncoder) body;
		int len = enc.payloadLength() + 1;
		ByteBuf buf = Unpooled.buffer(len);
		buf.writeByte(unitId);
		enc.encodeModbusPayload(buf);
		return ModbusByteUtils.computeCrc(buf.array(), 0, len);
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
	public ModbusError getError() {
		return body.getError();
	}

	/*- RTU frame structure:
	 
	   |0||1|---||--|
	   +-++-+---++--+
	   |a||f|...||cc|
	   +-++-+---++--+
	   
	   a  = address (unit ID)
	   f  = function code + data
	   cc = 16-bit CRC (LE order)
	 */

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		int s = out.writerIndex();
		out.writeByte(getUnitId());
		((ModbusPayloadEncoder) body).encodeModbusPayload(out);

		int len = out.writerIndex() - s;
		byte[] payload = new byte[len];
		out.slice(s, payload.length).readBytes(payload);
		short crc = ModbusByteUtils.computeCrc(payload, 0, len);
		out.writeShortLE(crc);
	}

	@Override
	public int payloadLength() {
		return 3 + ((ModbusPayloadEncoder) body).payloadLength();
	}

}
