/* ==================================================================
 * TcpTestUtils.java - 4/12/2022 7:56:38 am
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

package net.solarnetwork.io.modbus.tcp.netty.test.support;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * TCP test utilities.
 *
 * @author matt
 * @version 1.0
 */
public class TcpTestUtils {

	/**
	 * Get a free IP port.
	 * 
	 * @return the free port
	 */
	public static final int freePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		} finally {
			try {
				if ( socket != null ) {
					socket.close();
				}
			} catch ( IOException e ) {
				// ignore
			}
		}
	}

}
