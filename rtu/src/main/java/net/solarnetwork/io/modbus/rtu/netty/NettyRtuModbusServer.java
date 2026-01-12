/* ==================================================================
 * NettyRtuModbusServer.java - 12/01/2026 11:59:43 am
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

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.logging.LoggingHandler;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.SimpleModbusMessageReply;
import net.solarnetwork.io.modbus.netty.serial.SerialAddress;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannel;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;

/**
 * A basic asynchronous Modbus RTU server.
 * 
 * <p>
 * This server listens for Modbus requests, decodes them into
 * {@link ModbusMessage} instances, and then passes those to the handler
 * configured via {@link #setMessageHandler(BiConsumer)}. The handler must
 * provide a response {@link ModbusMessage}, which this server will then encode
 * and send back to the connected client.
 * </p>
 *
 * @author matt
 * @version 1.0
 */
public class NettyRtuModbusServer implements ChannelFactory<SerialPortChannel> {

	private static final Logger log = LoggerFactory.getLogger(NettyRtuModbusServer.class);

	private final String device;
	private final SerialParameters serialParameters;
	private final SerialPortProvider serialPortProvider;
	private final boolean privateEventLoopGroup;

	private BiConsumer<ModbusMessage, Consumer<ModbusMessage>> messageHandler;
	private BiFunction<String, Boolean, Boolean> clientConnectionListener;
	private boolean wireLogging;

	private EventLoopGroup eventLoopGroup;
	private Channel channel;

	/**
	 * Constructor.
	 * 
	 * @param device
	 *        the serial device to listen on, e.g. {@code /dev/ttyAMA0}
	 * @param serialParameters
	 *        the serial parameters
	 * @param serialPortProvider
	 *        the serial port provider
	 * @param eventLoopGroup
	 *        the event loop group, or {@literal null} to create an internal one
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public NettyRtuModbusServer(String device, SerialParameters serialParameters,
			SerialPortProvider serialPortProvider) {
		this(device, serialParameters, serialPortProvider, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param device
	 *        the serial device to listen on, e.g. {@code /dev/ttyAMA0}
	 * @param serialParameters
	 *        the serial parameters
	 * @param serialPortProvider
	 *        the serial port provider
	 * @param eventLoopGroup
	 *        the event loop group, or {@literal null} to create an internal one
	 * @throws IllegalArgumentException
	 *         if any argument except {@code eventLoopGroup} is {@literal null}
	 */
	public NettyRtuModbusServer(String device, SerialParameters serialParameters,
			SerialPortProvider serialPortProvider, EventLoopGroup eventLoopGroup) {
		super();
		if ( device == null ) {
			throw new IllegalArgumentException("The device argument must not be null.");
		}
		this.device = device;
		if ( serialParameters == null ) {
			throw new IllegalArgumentException("The serialParameters argument must not be null.");
		}
		this.serialParameters = serialParameters;
		if ( serialPortProvider == null ) {
			throw new IllegalArgumentException("The serialPortProvider argument must not be null.");
		}
		this.serialPortProvider = serialPortProvider;
		if ( eventLoopGroup == null ) {
			eventLoopGroup = defaultEventLoopGroup();
			this.privateEventLoopGroup = true;
		} else {
			this.privateEventLoopGroup = false;
		}
		this.eventLoopGroup = eventLoopGroup;
	}

	@SuppressWarnings("deprecation")
	private static EventLoopGroup defaultEventLoopGroup() {
		// TODO: need a non-deprecated replacement
		return new io.netty.channel.oio.OioEventLoopGroup();
	}

	@Override
	public SerialPortChannel newChannel() {
		SerialPortChannel channel = new SerialPortChannel(serialPortProvider);
		channel.config().setSerialParameters(serialParameters);
		return channel;
	}

	/**
	 * Start the server.
	 * 
	 * <p>
	 * Upon return the server will be bound and ready to accept connections on
	 * the configured port.
	 * </p>
	 */
	public synchronized void start() throws IOException {
		if ( this.channel != null ) {
			return;
		}
		try {
			if ( eventLoopGroup.isShuttingDown() ) {
				if ( privateEventLoopGroup ) {
					eventLoopGroup = defaultEventLoopGroup();
				} else {
					throw new IOException("External EventLoopGroup is stopped.");
				}
			}
			// @formatter:off
			Bootstrap bootstrap = new Bootstrap()
					.group(eventLoopGroup)
					.channelFactory(this)
					.remoteAddress(new SerialAddress(device))
					.handler(new HandlerInitializer());
			// @formatter:on

			Channel channel = bootstrap.connect().sync().channel();
			channel.closeFuture().addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if ( privateEventLoopGroup ) {
						eventLoopGroup.shutdownGracefully();
					}
				}
			});
			this.channel = channel;
		} catch ( Exception e ) {
			String msg = String.format("Error starting Modbus server on port %s", device);
			if ( e instanceof IOException ) {
				log.warn("{}: {}", msg, e.getMessage());
				throw (IOException) e;
			} else {
				log.error(msg, e);
			}
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Initialize the channel.
	 * 
	 * <p>
	 * This should be called by extending classes via a
	 * {@code ClientInitializer} implementation configured on the
	 * {@link Bootstrap}. It is primarily exposed here to help with unit tests.
	 * </p>
	 * 
	 * @param channel
	 *        the channel to initialize
	 */
	protected void initChannel(Channel ch) {
		ChannelPipeline pipeline = ch.pipeline();
		if ( wireLogging ) {
			pipeline.addLast(new LoggingHandler("net.solarnetwork.io.modbus.server." + device));
		}
		pipeline.addLast(new RtuModbusMessageEncoder(), new RtuModbusMessageDecoder(false),
				new Handler());
	}

	/**
	 * Stop the server.
	 */
	public synchronized void stop() {
		if ( privateEventLoopGroup && eventLoopGroup != null ) {
			eventLoopGroup.shutdownGracefully();
			eventLoopGroup = null;
		}
		if ( channel != null ) {
			channel.close().awaitUninterruptibly();
			channel = null;
		}
	}

	/**
	 * Initializer for client connections.
	 */
	private final class HandlerInitializer extends ChannelInitializer<SerialPortChannel> {

		@Override
		protected void initChannel(SerialPortChannel ch) throws Exception {
			NettyRtuModbusServer.this.initChannel(ch);
		}

	}

	/**
	 * Handler for client connections.
	 */
	private final class Handler extends SimpleChannelInboundHandler<ModbusMessage> {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.info("Client connected: {}", ctx.channel());
			final BiFunction<String, Boolean, Boolean> listener = getClientConnectionListener();
			if ( listener != null ) {
				Boolean result = listener.apply(((SerialAddress) ctx.channel().remoteAddress()).name(),
						true);
				if ( result != null && !result ) {
					// close the connection
					ctx.close();
				}
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			log.info("Client disconnected: {}", ctx.channel());
			final BiFunction<String, Boolean, Boolean> listener = getClientConnectionListener();
			if ( listener != null ) {
				// note the return value is not used here
				listener.apply(((SerialAddress) ctx.channel().remoteAddress()).name(), false);
			}
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ModbusMessage msg) throws Exception {
			log.debug("Request: {}", msg);
			final BiConsumer<ModbusMessage, Consumer<ModbusMessage>> h = getMessageHandler();
			if ( h == null ) {
				return;
			}
			h.accept(msg, (r) -> {
				ctx.channel().writeAndFlush(new SimpleModbusMessageReply(msg, r));
			});
		}

	}

	/**
	 * Get the serial device.
	 * 
	 * @return the device
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * Get the serial parameters.
	 * 
	 * @return the serialParameters
	 */
	public SerialParameters getSerialParameters() {
		return serialParameters;
	}

	/**
	 * Get the serial port provider.
	 * 
	 * @return the serialPortProvider
	 */
	public SerialPortProvider getSerialPortProvider() {
		return serialPortProvider;
	}

	/**
	 * Get the message handler.
	 * 
	 * @return the handler
	 */
	public BiConsumer<ModbusMessage, Consumer<ModbusMessage>> getMessageHandler() {
		return messageHandler;
	}

	/**
	 * Set the message handler.
	 * 
	 * <p>
	 * This handler will be passed an inbound message along with another
	 * {@code Consumer} for the reply message.
	 * </p>
	 * 
	 * @param messageHandler
	 *        the handler to set
	 */
	public void setMessageHandler(BiConsumer<ModbusMessage, Consumer<ModbusMessage>> messageHandler) {
		this.messageHandler = messageHandler;
	}

	/**
	 * Get an optional listener for client connection events.
	 * 
	 * @return a client connection listener, or {@code null}
	 * @see #setClientConnectionListener(BiFunction)
	 */
	public BiFunction<String, Boolean, Boolean> getClientConnectionListener() {
		return clientConnectionListener;
	}

	/**
	 * Set an optional listener for client connection events.
	 * 
	 * <p>
	 * The serial device name is passed to the consumer as the first argument.
	 * When a client connects, {@code true} will be passed as the second
	 * argument; when a client disconnects, {@code false} will be passed.
	 * </p>
	 * 
	 * <p>
	 * The return argument is inspected only after a connection event. If
	 * {@code false} is returned, the client connection will be closed. This
	 * provides a way to deny a client connection.
	 * </p>
	 * 
	 * @param clientConnectionListener
	 *        the client connection listener, or {@code null}
	 */
	public void setClientConnectionListener(
			BiFunction<String, Boolean, Boolean> clientConnectionListener) {
		this.clientConnectionListener = clientConnectionListener;
	}

	/**
	 * Get the "wire logging" setting.
	 * 
	 * @return {@literal true} to enable wire-level logging of all messages
	 */
	public boolean isWireLogging() {
		return wireLogging;
	}

	/**
	 * Set the "wire logging" setting.
	 * 
	 * @param wireLogging
	 *        {@literal true} to enable wire-level logging of all messages
	 */
	public void setWireLogging(boolean wireLogging) {
		this.wireLogging = wireLogging;
	}

}
