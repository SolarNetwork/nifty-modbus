/* ==================================================================
 * NioEventLoopGroupFactory.java - 12/03/2026 12:02:10 pm
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
 * Factory for {@code NioEventLoopGroup} instances.
 *
 * @author matt
 * @version 1.0
 * @since 1.5
 */
public class NioEventLoopGroupFactory implements BiFunction<Object, Boolean, EventLoopGroup> {

	/**
	 * A default factory instance.
	 */
	public static BiFunction<Object, Boolean, EventLoopGroup> INSTANCE = NioEventLoopGroupFactory::createEventLoopGroup;

	/**
	 * Constructor.
	 */
	public NioEventLoopGroupFactory() {
		super();
	}

	@Override
	public EventLoopGroup apply(Object context, Boolean parent) {
		return createEventLoopGroup(context, parent);
	}

	@SuppressWarnings("deprecation")
	private static EventLoopGroup createEventLoopGroup(Object context, Boolean parent) {
		return new io.netty.channel.nio.NioEventLoopGroup();
	}

}
