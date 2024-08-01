/* ==================================================================
 * TcpModbusMessageEncoder.java - 29/11/2022 7:26:32 am
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
import java.util.function.IntSupplier;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessageReply;
import net.solarnetwork.io.modbus.tcp.SimpleTransactionIdSupplier;

/**
 * Encoder of {@link ModbusMessage} to TCP encapsulated {@link ByteBuf} Modbus
 * frame.
 *
 * @author matt
 * @version 1.1
 */
public class TcpModbusMessageEncoder extends MessageToMessageEncoder<ModbusMessage> {

	/** A mapping of transaction pendingMessages to pair requests/responses. */
	private final ConcurrentMap<Integer, TcpModbusMessage> pendingMessages;

	/** A provider of transaction IDs. */
	private final IntSupplier transactionIdSupplier;

	/**
	 * Constructor.
	 * 
	 * @param pendingMessages
	 *        a mapping of transaction IDs to associated pendingMessages, to
	 *        handle request and response pairing
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public TcpModbusMessageEncoder(ConcurrentMap<Integer, TcpModbusMessage> pendingMessages) {
		this(pendingMessages, SimpleTransactionIdSupplier.INSTANCE);
	}

	/**
	 * Constructor.
	 * 
	 * @param pendingMessages
	 *        a mapping of transaction IDs to associated pendingMessages, to
	 *        handle request and response pairing
	 * @param transactionIdSupplier
	 *        a TCP Modbus transaction ID supplier; only values from 1-65535
	 *        should be supplied
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public TcpModbusMessageEncoder(ConcurrentMap<Integer, TcpModbusMessage> pendingMessages,
			IntSupplier transactionIdSupplier) {
		super();
		if ( pendingMessages == null ) {
			throw new IllegalArgumentException("The pendingMessages argument must not be null.");
		}
		this.pendingMessages = pendingMessages;
		if ( transactionIdSupplier == null ) {
			throw new IllegalArgumentException("The transactionIdSupplier argument must not be null.");
		}
		this.transactionIdSupplier = transactionIdSupplier;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ModbusMessage msg, List<Object> out)
			throws Exception {
		TcpModbusMessage tcp;
		if ( msg instanceof TcpModbusMessage ) {
			tcp = (TcpModbusMessage) msg;
		} else if ( msg instanceof ModbusMessageReply ) {
			// outbound response
			ModbusMessageReply reply = (ModbusMessageReply) msg;
			ModbusMessage req = reply.getRequest();
			net.solarnetwork.io.modbus.tcp.TcpModbusMessage tcpReq = req
					.unwrap(net.solarnetwork.io.modbus.tcp.TcpModbusMessage.class);
			tcp = new TcpModbusMessage(tcpReq.getTransactionId(), reply);
			pendingMessages.remove(tcp.getTransactionId(), req);
		} else {
			// outbound request
			int transactionId = transactionIdSupplier.getAsInt();
			tcp = new TcpModbusMessage(transactionId, msg);
			pendingMessages.put(transactionId, tcp);
		}
		int len = tcp.payloadLength();
		ByteBuf buf = ctx.alloc().buffer(len);
		tcp.encodeModbusPayload(buf);
		out.add(buf);
	}

}
