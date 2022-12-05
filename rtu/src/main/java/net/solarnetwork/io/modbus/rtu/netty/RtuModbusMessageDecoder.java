/* ==================================================================
 * RtuModbusMessageDecoder.java - 1/12/2022 4:52:07 pm
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import net.solarnetwork.io.modbus.AddressedModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;
import net.solarnetwork.io.modbus.rtu.netty.RtuModbusMessageDecoder.DecoderState;

/**
 * Decoder for RTU Modbus messages.
 *
 * @author matt
 * @version 1.0
 */
public class RtuModbusMessageDecoder extends ReplayingDecoder<DecoderState> {

	private static final Logger log = LoggerFactory.getLogger(RtuModbusMessageDecoder.class);

	/** The length of the fixed-length header. */
	public static final int FIXED_HEADER_LENGTH = 7;

	/*- RTU frame structure:
	 
	   |0||1|---||--|
	   +-++-+---++--+
	   |a||f|...||cc|
	   +-++-+---++--+
	   
	   a  = address (unit ID)
	   f  = function code + data
	   cc = 16-bit CRC (LE order)
	 */

	/**
	 * States of the decoder.
	 */
	enum DecoderState {
		READ_FIXED_HEADER,
		READ_PAYLOAD,
		BAD_DATA,
	}

	/** True if decoding response messages, false for requests. */
	private final boolean controller;

	private short unitId;

	/**
	 * Constructor.
	 * 
	 * @param controller
	 *        {@literal true} if operating as a controller where decoding is for
	 *        Modbus response message, or {@literal false} if operating as a
	 *        responder where decoding is for Modbus request messages
	 */
	public RtuModbusMessageDecoder(boolean controller) {
		super(DecoderState.READ_FIXED_HEADER);
		this.controller = controller;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		switch (state()) {
			case READ_FIXED_HEADER:
				readFixedHeader(ctx, in);
				break;

			case READ_PAYLOAD:
				readPayload(ctx, in, out);
				break;

			case BAD_DATA:
				// discard input
				in.skipBytes(in.readableBytes());
				checkpoint(DecoderState.READ_FIXED_HEADER);
				break;
		}
	}

	private void readFixedHeader(ChannelHandlerContext ctx, ByteBuf in) {
		unitId = in.readUnsignedByte();
		checkpoint(DecoderState.READ_PAYLOAD);
	}

	private void readPayload(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		ModbusMessage msg = null;
		ModbusMessage req = null;
		AddressedModbusMessage reqAddr = null;
		if ( controller ) {
			// inbound response
			int len = ModbusMessageUtils.discoverResponsePayloadLength(in);
			if ( len < 1 ) {
				checkpoint(DecoderState.BAD_DATA);
				return;
			}
			req = ctx.channel().attr(NettyModbusClient.LAST_ENCODED_MESSAGE).get();
			reqAddr = (req instanceof AddressedModbusMessage ? (AddressedModbusMessage) req : null);
			msg = ModbusMessageUtils.decodeResponsePayload(unitId,
					(reqAddr != null ? reqAddr.getAddress() : 0),
					(reqAddr != null ? reqAddr.getCount() : 0), in);
		} else {
			// inbound request
			msg = ModbusMessageUtils.decodeRequestPayload(unitId, 0, 0, in);
		}
		if ( msg != null ) {
			short crc = in.readShortLE();
			short computedCrc = RtuModbusMessage.computeCrc(unitId, msg);
			if ( crc != computedCrc ) {
				log.warn("CRC mismatch: frame value {} but computed value {} from {}",
						Short.toUnsignedInt(crc), Short.toUnsignedInt(computedCrc), msg);
			}
			if ( req != null ) {
				msg = new SimpleModbusMessageReply(req, msg);
				ctx.channel().attr(NettyModbusClient.LAST_ENCODED_MESSAGE).compareAndSet(req, null);
			} else {
				msg = new RtuModbusMessage(msg, crc);
			}
			out.add(msg);
		}
		checkpoint(DecoderState.READ_FIXED_HEADER);
	}

}
