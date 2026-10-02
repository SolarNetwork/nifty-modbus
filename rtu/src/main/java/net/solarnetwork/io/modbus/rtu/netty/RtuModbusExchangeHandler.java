/* ==================================================================
 * RtuModbusExchangeHandler.java - 2/10/2026 6:05:12 pm
 *
 * Copyright 2026 SolarNetwork.net Dev Team
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

import static java.lang.String.format;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.ModbusMessageReply;
import net.solarnetwork.io.modbus.ModbusTimeoutException;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient;
import net.solarnetwork.io.modbus.netty.handler.NettyModbusClient.PendingMessage;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;

/**
 * Handler to manage Modbus RTU request/response exchanges for a client
 * (controller).
 *
 * <p>
 * Modbus RTU is a half-duplex protocol without any way to match a response to
 * its request other than by the order they occur in. Only one request can be
 * outstanding at any time. This handler enforces that by queuing outbound
 * request messages and writing them one at a time. The next request is not
 * written until a response to the previous one is received, or the reply
 * timeout expires.
 * </p>
 *
 * <p>
 * This handler must be placed after a {@link RtuModbusMessageEncoder} and
 * {@link RtuModbusMessageDecoder} in the channel pipeline. Inbound messages are
 * handled like this:
 * </p>
 *
 * <ul>
 * <li>a message that arrives when no request is outstanding is discarded</li>
 * <li>a message with a valid CRC whose unit ID or function code differs from
 * the outstanding request is discarded, and the request remains
 * outstanding</li>
 * <li>a message that cannot be decoded completes the outstanding request with
 * an exception</li>
 * <li>otherwise the message is passed on as the response to the outstanding
 * request; this includes a message with an invalid CRC, which the receiver is
 * expected to detect by calling {@link ModbusMessage#validate()}</li>
 * </ul>
 *
 * <p>
 * Any buffered input in the {@link RtuModbusMessageDecoder} is discarded before
 * each request is written, so data left over from a previous exchange, such as
 * a partial response, does not corrupt the next one.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 1.6.0
 */
public class RtuModbusExchangeHandler extends ChannelDuplexHandler {

	private static final Logger log = LoggerFactory.getLogger(RtuModbusExchangeHandler.class);

	private final ConcurrentMap<ModbusMessage, PendingMessage> pending;
	private final LongSupplier replyTimeout;

	// the following are only accessed from the event loop

	private final Queue<Exchange> queue = new ArrayDeque<>(8);
	private @Nullable Exchange current;
	private @Nullable Future<?> currentTimeout;

	/**
	 * Constructor.
	 *
	 * @param pending
	 *        the client's map of request messages pending responses
	 * @param replyTimeout
	 *        supplier of the maximum time to wait for a response after a
	 *        request is written, in milliseconds; anything less than
	 *        {@literal 1} disables the timeout
	 * @throws IllegalArgumentException
	 *         if any argument is {@code null}
	 */
	public RtuModbusExchangeHandler(ConcurrentMap<ModbusMessage, PendingMessage> pending,
			LongSupplier replyTimeout) {
		super();
		if ( pending == null ) {
			throw new IllegalArgumentException("The pending argument must not be null.");
		}
		this.pending = pending;
		if ( replyTimeout == null ) {
			throw new IllegalArgumentException("The replyTimeout argument must not be null.");
		}
		this.replyTimeout = replyTimeout;
	}

	/**
	 * A queued or outstanding request.
	 */
	private static final class Exchange {

		private final ModbusMessage request;
		private final ChannelPromise promise;

		private Exchange(ModbusMessage request, ChannelPromise promise) {
			super();
			this.request = request;
			this.promise = promise;
		}

	}

	@SuppressWarnings("FutureReturnValueIgnored")
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if ( !(msg instanceof ModbusMessage) ) {
			ctx.write(msg, promise);
			return;
		}
		queue.add(new Exchange((ModbusMessage) msg, promise));
		writeNext(ctx);
	}

	/**
	 * Write the next queued request, if no request is outstanding.
	 */
	@SuppressWarnings("FutureReturnValueIgnored")
	private void writeNext(ChannelHandlerContext ctx) {
		while ( current == null ) {
			final Exchange next = queue.poll();
			if ( next == null ) {
				return;
			}
			final PendingMessage p = pending.get(next.request);
			if ( next.promise.isDone() || (p != null && p.getFuture().isDone()) ) {
				// nobody is waiting for the response any more, e.g. caller timed out
				log.debug("Not sending abandoned request {}", next.request);
				if ( p != null ) {
					pending.remove(next.request, p);
				}
				next.promise.tryFailure(new IOException("Request abandoned before being sent."));
				continue;
			}

			current = next;
			resetDecoder(ctx);
			final ChannelFuture f = ctx.writeAndFlush(next.request, next.promise);
			if ( f.isDone() ) {
				if ( !f.isSuccess() ) {
					current = null;
					continue;
				}
			} else {
				f.addListener((ChannelFutureListener) future -> {
					if ( !future.isSuccess() && current == next ) {
						finish(ctx, next);
						writeNext(ctx);
					}
				});
			}

			final long timeout = replyTimeout.getAsLong();
			if ( timeout > 0 ) {
				currentTimeout = ctx.executor().schedule(() -> {
					if ( current != next ) {
						return;
					}
					finish(ctx, next);
					fail(next, new ModbusTimeoutException(
							format("Timeout waiting for response to %s.", next.request)));
					writeNext(ctx);
				}, timeout, TimeUnit.MILLISECONDS);
			}
		}
	}

	@SuppressWarnings("ReferenceEquality")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if ( !(msg instanceof ModbusMessage) ) {
			ctx.fireChannelRead(msg);
			return;
		}
		final ModbusMessage message = (ModbusMessage) msg;
		final Exchange exchange = this.current;
		final ModbusMessageReply reply = message.unwrap(ModbusMessageReply.class);
		if ( exchange == null || reply == null || reply.getRequest() != exchange.request ) {
			log.debug("Discarding unexpected message {}", message);
			ReferenceCountUtil.release(msg);
			return;
		}

		final ModbusMessage frame = (reply instanceof SimpleModbusMessageReply
				? ((SimpleModbusMessageReply) reply).getReply()
				: message);
		if ( frame instanceof net.solarnetwork.io.modbus.rtu.RtuModbusMessage
				&& !((net.solarnetwork.io.modbus.rtu.RtuModbusMessage) frame).isCrcValid() ) {
			// the framing can't be trusted, so discard whatever followed the frame; the
			// message is still passed on, for the caller to detect with validate()
			resetDecoder(ctx);
		} else if ( message.getUnitId() != exchange.request.getUnitId()
				|| message.getFunction().getCode() != exchange.request.getFunction().getCode() ) {
			log.debug("Discarding message {} that is not a response to {}", message, exchange.request);
			// restore request association cleared by decoder, to keep waiting for the response
			ctx.channel().attr(NettyModbusClient.LAST_ENCODED_MESSAGE).set(exchange.request);
			ReferenceCountUtil.release(msg);
			return;
		}

		finish(ctx, exchange);
		ctx.fireChannelRead(msg);
		writeNext(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		final Throwable t = (cause instanceof DecoderException && cause.getCause() != null
				? cause.getCause()
				: cause);
		final Exchange exchange = this.current;
		if ( exchange == null ) {
			log.warn("Exception on Modbus RTU connection {}: {}", ctx.channel().remoteAddress(),
					t.toString());
			return;
		}
		log.debug("Exception on Modbus RTU connection {} waiting for response to {}: {}",
				ctx.channel().remoteAddress(), exchange.request, t.toString());
		finish(ctx, exchange);
		resetDecoder(ctx);
		fail(exchange, t);
		writeNext(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		failAll(ctx, new IOException(format("Connection to %s closed.", ctx.channel().remoteAddress())));
		ctx.fireChannelInactive();
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		failAll(ctx, new IOException("Modbus RTU exchange handler removed."));
	}

	private void failAll(ChannelHandlerContext ctx, Throwable cause) {
		final Exchange exchange = this.current;
		if ( exchange != null ) {
			finish(ctx, exchange);
			fail(exchange, cause);
		}
		for ( Exchange queued = queue.poll(); queued != null; queued = queue.poll() ) {
			fail(queued, cause);
		}
	}

	/**
	 * Mark the outstanding request as no longer outstanding.
	 */
	private void finish(ChannelHandlerContext ctx, Exchange exchange) {
		if ( current != exchange ) {
			return;
		}
		current = null;
		final Future<?> timeout = this.currentTimeout;
		if ( timeout != null ) {
			timeout.cancel(false);
			currentTimeout = null;
		}
		ctx.channel().attr(NettyModbusClient.LAST_ENCODED_MESSAGE).compareAndSet(exchange.request, null);
	}

	/**
	 * Complete a request with an exception.
	 */
	private void fail(Exchange exchange, Throwable cause) {
		exchange.promise.tryFailure(cause);
		final PendingMessage p = pending.remove(exchange.request);
		if ( p != null ) {
			p.getFuture().completeExceptionally(cause);
		}
	}

	private static void resetDecoder(ChannelHandlerContext ctx) {
		final RtuModbusMessageDecoder decoder = ctx.pipeline().get(RtuModbusMessageDecoder.class);
		if ( decoder != null ) {
			decoder.reset();
		}
	}

}
