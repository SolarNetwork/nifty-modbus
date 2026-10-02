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
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;
import net.solarnetwork.io.modbus.AddressedModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusUnsupportedFunctionException;
import net.solarnetwork.io.modbus.netty.msg.ModbusMessageUtils;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;
import net.solarnetwork.io.modbus.tcp.TcpModbusUnsupportedFunctionException;
import net.solarnetwork.io.modbus.tcp.netty.TcpModbusMessageDecoder.DecoderState;

/**
 * Decoder for TCP Modbus messages.
 * 
 * <p>
 * Frames are delimited using the length field of the frame header, so a frame
 * that cannot be decoded, for example because it uses an unsupported function,
 * does not prevent the frames that follow it from being decoded. When a frame
 * cannot be decoded a {@link DecoderException} is fired on the channel
 * pipeline, with the reason as its cause, and decoding continues with the
 * next frame.
 * </p>
 * 
 * <p>
 * A frame header with a length outside the range allowed by Modbus cannot be
 * the start of a frame. When that happens all buffered input is discarded and
 * a {@link CorruptedFrameException} is thrown.
 * </p>
 *
 * @author matt
 * @version 1.1
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

	/** The smallest valid frame length field value: a unit ID and function code. */
	private static final int MIN_FRAME_LENGTH = 2;

	/** The largest valid frame length field value: a unit ID and 253 byte PDU. */
	private static final int MAX_FRAME_LENGTH = 254;

	private int transactionId;
	private short unitId;
	private int payloadLength;

	/**
	 * Constructor.
	 * 
	 * @param controller
	 *        {@code true} if operating as a controller where decoding is for
	 *        Modbus response message, or {@code false} if operating as a
	 *        responder where decoding is for Modbus request messages
	 * @param pendingMessages
	 *        a mapping of transaction IDs to associated messages, to handle
	 *        request and response pairing
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
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
				readFixedHeader(in);
				break;

			case READ_PAYLOAD:
				readPayload(ctx, in, out);
				break;
		}
	}

	private void readFixedHeader(ByteBuf in) {
		transactionId = in.readUnsignedShort();
		in.skipBytes(2); // just assuming is 0 for TCP
		final int length = in.readUnsignedShort();
		unitId = in.readUnsignedByte();
		if ( length < MIN_FRAME_LENGTH || length > MAX_FRAME_LENGTH ) {
			// not the start of a frame, so where the next frame starts is unknown
			in.skipBytes(actualReadableBytes());
			checkpoint(DecoderState.READ_FIXED_HEADER);
			throw new CorruptedFrameException("Invalid Modbus TCP frame length " + length + ".");
		}
		payloadLength = length - 1; // length includes unit ID
		checkpoint(DecoderState.READ_PAYLOAD);
	}

	private void readPayload(ChannelHandlerContext ctx, ByteBuf frame, List<Object> out) {
		// take the complete payload, so the next frame can be found regardless of how decoding goes
		final ByteBuf in = frame.readSlice(payloadLength);
		checkpoint(DecoderState.READ_FIXED_HEADER);

		ModbusMessage msg = null;
		try {
			if ( controller ) {
				// inbound response
				TcpModbusMessage req = pendingMessages.get(transactionId);
				AddressedModbusMessage addr = (req != null ? req.unwrap(AddressedModbusMessage.class)
						: null);
				ModbusMessage payload = ModbusMessageUtils.decodeResponsePayload(unitId,
						(addr != null ? addr.getAddress() : 0), (addr != null ? addr.getCount() : 0),
						in);
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
					TcpModbusMessage req = new TcpModbusMessage(System.currentTimeMillis(),
							transactionId, payload);
					pendingMessages.put(transactionId, req);
					msg = req;
				}
			}
		} catch ( ModbusUnsupportedFunctionException ufe ) {
			ctx.fireExceptionCaught(new DecoderException(new TcpModbusUnsupportedFunctionException(
					ufe.getCode(), ufe.getUnitId(), transactionId)));
			return;
		} catch ( RuntimeException e ) {
			ctx.fireExceptionCaught(e instanceof DecoderException ? e : new DecoderException(e));
			return;
		}
		if ( msg != null ) {
			out.add(msg);
		}
	}
}
