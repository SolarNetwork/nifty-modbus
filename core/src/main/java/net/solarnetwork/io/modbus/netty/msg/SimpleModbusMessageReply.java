/* ==================================================================
 * SimpleModbusMessageReply.java - 29/11/2022 8:06:37 am
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

package net.solarnetwork.io.modbus.netty.msg;

import io.netty.buffer.ByteBuf;
import net.solarnetwork.io.modbus.ModbusError;
import net.solarnetwork.io.modbus.ModbusFunction;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessageReply;

/**
 * Simple implementation of {@link ModbusMessageReply}.
 * 
 * <p>
 * This implementation wraps request and reply messages into a
 * {@link ModbusMessageReply}, delegating all {@link ModbusMessage} methods to
 * the reply instance.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class SimpleModbusMessageReply implements ModbusMessageReply, ModbusPayloadEncoder {

	private final ModbusMessage request;
	private final ModbusMessage reply;

	/**
	 * Constructor.
	 * 
	 * @param request
	 *        the original request message
	 * @param reply
	 *        the reply message
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SimpleModbusMessageReply(ModbusMessage request, ModbusMessage reply) {
		super();
		if ( request == null ) {
			throw new IllegalArgumentException("The request argument must not be null.");
		}
		this.request = request;
		if ( reply == null ) {
			throw new IllegalArgumentException("The reply argument must not be null.");
		} else if ( !(reply instanceof ModbusPayloadEncoder) ) {
			throw new IllegalArgumentException(
					"The reply argument must implement ModbusPayloadEncoder.");
		}
		this.reply = reply;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ModbusMessageReply{request=");
		builder.append(request);
		builder.append(", reply=");
		builder.append(reply);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public ModbusMessage getRequest() {
		return request;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ModbusMessage> T unwrap(Class<T> msgType) {
		if ( msgType.isAssignableFrom(reply.getClass()) ) {
			return (T) reply;
		} else if ( msgType.isAssignableFrom(ModbusMessageReply.class) ) {
			return (T) this;
		}
		return null;
	}

	@Override
	public int getUnitId() {
		return reply.getUnitId();
	}

	@Override
	public ModbusFunction getFunction() {
		return reply.getFunction();
	}

	@Override
	public ModbusError getError() {
		return reply.getError();
	}

	@Override
	public boolean isException() {
		return reply.isException();
	}

	@Override
	public void encodeModbusPayload(ByteBuf out) {
		((ModbusPayloadEncoder) reply).encodeModbusPayload(out);
	}

	@Override
	public int payloadLength() {
		return ((ModbusPayloadEncoder) reply).payloadLength();
	}

	/**
	 * Get the original reply message.
	 * 
	 * @return the reply
	 */
	public ModbusMessage getReply() {
		return reply;
	}

}
