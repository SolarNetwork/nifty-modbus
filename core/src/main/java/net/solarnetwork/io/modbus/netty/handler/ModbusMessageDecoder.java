/* ==================================================================
 * ModbusMessageDecoder.java - 27/11/2022 9:41:24 am
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
import io.netty.handler.codec.ByteToMessageDecoder;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;

/**
 * Decoder for Modbus messages.
 *
 * @author matt
 * @version 1.0
 */
public class ModbusMessageDecoder extends ByteToMessageDecoder {

	/*- Frame structure:
	 
	   |1|2..|
	   +-|---+
	   |f|ddd|
	   +-----+
	   
	   f  = function code
	   d  = data (variable length)
	 */

	/** True if decoding response messages, false for requests. */
	private final boolean controller;

	/**
	 * Constructor.
	 * 
	 * @param controller
	 *        {@literal true} if operating as a controller where decoding is for
	 *        Modbus response message, or {@literal false} if operating as a
	 *        responder where decoding is for Modbus request messages
	 */
	public ModbusMessageDecoder(boolean controller) {
		super();
		this.controller = controller;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		final int expectedLength = (controller ? ModbusMessageUtils.discoverResponsePayloadLength(in)
				: ModbusMessageUtils.discoverRequestPayloadLength(in));
		if ( expectedLength < 1 ) {
			return;
		}
		if ( in.readableBytes() < expectedLength ) {
			return;
		}
		try {
			ModbusMessage msg = null;
			if ( controller ) {
				// inbound response
				msg = ModbusMessageUtils.decodeResponsePayload(in);
			} else {
				// inbound request
				msg = ModbusMessageUtils.decodeRequestPayload(in);
			}
			if ( msg != null ) {
				out.add(msg);
			}
		} catch ( IllegalArgumentException | UnsupportedOperationException e ) {
			in.skipBytes(actualReadableBytes());
		}
	}

}
