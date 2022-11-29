/* ==================================================================
 * ModbusMessageEncoder.java - 29/11/2022 7:26:32 am
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

package net.solarnetwork.io.modbus.netty.handler;

import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusPayloadEncoder;

/**
 * Encoder of {@link ModbusMessage} to {@link ByteBuf}.
 * 
 * <p>
 * The message must implement {@link ModbusPayloadEncoder} to be encoded.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageEncoder extends MessageToMessageEncoder<ModbusMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ModbusMessage msg, List<Object> out)
			throws Exception {
		ModbusPayloadEncoder enc = (msg instanceof ModbusPayloadEncoder ? (ModbusPayloadEncoder) msg
				: null);
		if ( enc != null ) {
			int len = enc.payloadLength();
			ByteBuf buf = ctx.alloc().buffer(len);
			enc.encodeModbusPayload(buf);
			out.add(buf);
			ctx.channel().attr(NettyModbusClient.LAST_ENCODED_MESSAGE).set(msg);
		}
	}

}
