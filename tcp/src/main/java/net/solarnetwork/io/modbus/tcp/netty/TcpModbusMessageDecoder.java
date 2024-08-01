/* ==================================================================
 * TcpModbusMessageDecoder.java - 27/11/2022 9:41:24 am
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

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import net.solarnetwork.io.modbus.AddressedModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessageDecoder.DecoderState;

/**
 * Decoder for TCP Modbus messages.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusMessageDecoder extends ReplayingDecoder<DecoderState> {

	/** The length of the fixed-length header. */
	public static final int FIXED_HEADER_LENGTH = 7;

	/*- TCP frame structure:
	 
	   |0-|2-|4-|6||7|8..|
	   +----------||-----+
	   |tt|pp|ll|u||f|...|
	   +-----------------+
	   
	   tt = 16-bit transaction ID
	   pp = 16-bit protocol ID (0 for TCP)
	   ll = remaining byte length
	   u  = unit ID
	   f  = function code
	 */

	/**
	 * States of the decoder.
	 */
	enum DecoderState {
		READ_FIXED_HEADER,
		READ_PAYLOAD,
	}

	/** True if decoding response messages, false for requests. */
	private final boolean controller;

	/** A mapping of transaction messages to pair requests/responses. */
	private final ConcurrentMap<Integer, TcpModbusMessage> pendingMessages;

	private int transactionId;
	private short unitId;

	/**
	 * Constructor.
	 * 
	 * @param controller
	 *        {@literal true} if operating as a controller where decoding is for
	 *        Modbus response message, or {@literal false} if operating as a
	 *        responder where decoding is for Modbus request messages
	 * @param pendingMessages
	 *        a mapping of transaction IDs to associated messages, to handle
	 *        request and response pairing
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public TcpModbusMessageDecoder(boolean controller,
			ConcurrentMap<Integer, TcpModbusMessage> pendingMessages) {
		super(DecoderState.READ_FIXED_HEADER);
		this.controller = controller;
		if ( pendingMessages == null ) {
			throw new IllegalArgumentException("The pendingMessages argument must not be null.");
		}
		this.pendingMessages = pendingMessages;
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
		}
	}

	private void readFixedHeader(ChannelHandlerContext ctx, ByteBuf in) {
		transactionId = in.readUnsignedShort();
		in.skipBytes(4); // just assuming is 0 for TCP, and we don't mind about payload length bytes
		unitId = in.readUnsignedByte();
		checkpoint(DecoderState.READ_PAYLOAD);
	}

	private void readPayload(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		ModbusMessage msg = null;
		if ( controller ) {
			// inbound response
			TcpModbusMessage req = pendingMessages.get(transactionId);
			AddressedModbusMessage addr = (req != null ? req.unwrap(AddressedModbusMessage.class)
					: null);
			ModbusMessage payload = ModbusMessageUtils.decodeResponsePayload(unitId,
					(addr != null ? addr.getAddress() : 0), (addr != null ? addr.getCount() : 0), in);
			if ( payload != null ) {
				if ( req != null ) {
					pendingMessages.remove(transactionId, req);
					msg = new SimpleModbusMessageReply(req.unwrap(ModbusMessage.class),
							new TcpModbusMessage(transactionId, payload));
				} else {
					msg = new TcpModbusMessage(transactionId, payload);
				}
			}
		} else {
			// inbound request
			ModbusMessage payload = ModbusMessageUtils.decodeRequestPayload(unitId, 0, 0, in);
			if ( payload != null ) {
				TcpModbusMessage req = new TcpModbusMessage(System.currentTimeMillis(), transactionId,
						payload);
				pendingMessages.put(transactionId, req);
				msg = req;
			}
		}
		if ( msg != null ) {
			out.add(msg);
		}
		checkpoint(DecoderState.READ_FIXED_HEADER);
	}
}
