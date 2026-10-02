/* ==================================================================
 * LocalIoEventLoopGroupFactory.java - 3/10/2026 9:12:40 am
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

package net.solarnetwork.io.modbus.netty.channel;

import java.util.function.BiFunction;
import io.netty.channel.EventLoopGroup;

/**
 * Factory for single-threaded {@code MultiThreadIoEventLoopGroup} instances
 * that use a {@code LocalIoHandler}.
 * 
 * <p>
 * The event loop groups created by this factory only execute tasks: they
 * perform no IO of their own. That suits a channel that performs its own IO,
 * like {@link net.solarnetwork.io.modbus.netty.serial.SerialPortChannel}, where
 * a dedicated thread for the channel is wanted.
 * </p>
 *
 * @author matt
 * @version 1.0
 * @since 1.6.0
 */
public class LocalIoEventLoopGroupFactory implements BiFunction<Object, Boolean, EventLoopGroup> {

	/**
	 * A default factory instance.
	 */
	public static BiFunction<Object, Boolean, EventLoopGroup> INSTANCE = LocalIoEventLoopGroupFactory::createEventLoopGroup;

	/**
	 * Constructor.
	 */
	public LocalIoEventLoopGroupFactory() {
		super();
	}

	@Override
	public EventLoopGroup apply(Object context, Boolean parent) {
		return createEventLoopGroup(context, parent);
	}

	private static EventLoopGroup createEventLoopGroup(Object context, Boolean parent) {
		return new io.netty.channel.MultiThreadIoEventLoopGroup(1,
				io.netty.channel.local.LocalIoHandler.newFactory());
	}

}
