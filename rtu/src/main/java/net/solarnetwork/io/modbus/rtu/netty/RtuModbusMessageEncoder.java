/* ==================================================================
 * RtuModbusMessageEncoder.java - 1/12/2022 4:52:20 pm
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

import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;

/**
 * Encoder of {@link ModbusMessage} to RTU encapsulated {@link ByteBuf} Modbus
 * frame.
 *
 * @author matt
 * @version 1.0
 */
public class RtuModbusMessageEncoder extends MessageToMessageEncoder<ModbusMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ModbusMessage msg, List<Object> out)
			throws Exception {
		RtuModbusMessage rtu = null;
		if ( msg instanceof RtuModbusMessage ) {
			rtu = (RtuModbusMessage) msg;
		} else {
			// outbound response
			rtu = new RtuModbusMessage(msg.getUnitId(), msg);
		}
		if ( rtu != null ) {
			int len = rtu.payloadLength();
			ByteBuf buf = ctx.alloc().buffer(len);
			rtu.encodeModbusPayload(buf);
			out.add(buf);
			ctx.channel().attr(NettyModbusClient.LAST_ENCODED_MESSAGE).set(msg);
		}
	}

}
