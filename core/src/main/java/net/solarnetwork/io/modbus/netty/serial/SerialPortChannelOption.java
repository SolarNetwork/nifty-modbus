/* ==================================================================
 * SerialPortChannelOption.java - 2/12/2022 7:05:23 am
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

package net.solarnetwork.io.modbus.netty.serial;

import java.util.Set;
import io.netty.channel.ChannelOption;
import net.solarnetwork.io.modbus.serial.SerialFlowControl;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Option for configuring a serial port connection.
 *
 * @author matt
 * @version 1.0
 */
public class SerialPortChannelOption<T> extends ChannelOption<T> {

	/** The baud rate. */
	public static final ChannelOption<Integer> BAUD_RATE = valueOf("BAUD_RATE");

	/** The stop bits. */
	public static final ChannelOption<SerialStopBits> STOP_BITS = valueOf("STOP_BITS");

	/** The data bits. */
	public static final ChannelOption<Integer> DATA_BITS = valueOf("DATA_BITS");

	/** The parity bit. */
	public static final ChannelOption<SerialParity> PARITY = valueOf("PARITY");

	/** The parity bit. */
	public static final ChannelOption<Set<SerialFlowControl>> FLOW_CONTROL = valueOf("FLOW_CONTROL");

	/** The RS-485 mode. */
	public static final ChannelOption<Boolean> RS485 = valueOf("RS485");

	/** The RS-485 RTS high mode. */
	public static final ChannelOption<Boolean> RS485_RTS_HIGH = valueOf("RS485_RTS_HIGH");

	/** The RS-485 termination mode. */
	public static final ChannelOption<Boolean> RS485_TERMINATION = valueOf("RS485_TERMINATION");

	/** The RS-485 echo mode. */
	public static final ChannelOption<Boolean> RS485_ECHO = valueOf("RS485_ECHO");

	/** The RS-485 before send delay, in microseconds. */
	public static final ChannelOption<Integer> RS485_BEFORE_SEND_DELAY = valueOf(
			"RS485_BEFORE_SEND_DELAY");

	/** The RS-485 after send delay, in microseconds. */
	public static final ChannelOption<Integer> RS485_AFTER_SEND_DELAY = valueOf(
			"RS485_AFTER_SEND_DELAY");

	/** The wait time, in milliseconds. */
	public static final ChannelOption<Integer> WAIT_TIME = valueOf("WAIT_TIME");

	/** The read timeout, in milliseconds. */
	public static final ChannelOption<Integer> READ_TIMEOUT = valueOf("READ_TIMEOUT");

	@SuppressWarnings("deprecation")
	protected SerialPortChannelOption() {
		super(null);
	}

}
