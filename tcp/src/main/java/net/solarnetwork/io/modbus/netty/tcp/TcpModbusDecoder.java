/* ==================================================================
 * TcpModbusDecoder.java - 27/11/2022 9:41:24 am
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

package net.solarnetwork.io.modbus.netty.tcp;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import net.solarnetwork.io.modbus.handler.ModbusFunctionDecoder;
import net.solarnetwork.io.modbus.netty.tcp.TcpModbusDecoder.DecoderState;

/**
 * Decoder for TCP Modbus messages.
 *
 * @author matt
 * @version 1.0
 */
public class TcpModbusDecoder extends ReplayingDecoder<DecoderState> {

	private static final Logger log = LoggerFactory.getLogger(TcpModbusDecoder.class);

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
		BAD_MESSAGE,
	}

	private final ModbusFunctionDecoder payloadDecoder = new ModbusFunctionDecoder();

	private int transactionId;
	private int protocolId;
	private short unitId;
	private int payloadLength;

	/**
	 * Constructor.
	 */
	public TcpModbusDecoder() {
		super(DecoderState.READ_FIXED_HEADER);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			switch (state()) {
				case READ_FIXED_HEADER:
					readFixedHeader(ctx, in);
					break;

				case READ_PAYLOAD:
					readPayload(ctx, in);
					break;

				case BAD_MESSAGE:
					// Keep discarding until disconnection.
					in.skipBytes(actualReadableBytes());
					break;

				default:
					// Shouldn't reach here.
					throw new Error("Unknown decode state");
			}
		} catch ( Exception e ) {
			log.debug("Exception decoding Modbus message: {}", e, e);
			checkpoint(DecoderState.BAD_MESSAGE);
		}
	}

	private void readFixedHeader(ChannelHandlerContext ctx, ByteBuf in) {
		if ( in.readableBytes() < FIXED_HEADER_LENGTH ) {
			return;
		}
		transactionId = in.readUnsignedShort();
		protocolId = in.readUnsignedShort();
		payloadLength = in.readUnsignedShort() - 1; // minus 1 for unitId below
		unitId = in.readUnsignedByte();
		checkpoint(DecoderState.READ_PAYLOAD);
	}

	private void readPayload(ChannelHandlerContext ctx, ByteBuf in) {
		if ( in.readableBytes() < payloadLength ) {
			return;
		}

	}
}
