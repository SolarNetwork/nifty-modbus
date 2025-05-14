/* ==================================================================
 * SerialPort.java - 2/12/2022 6:28:23 am
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

package net.solarnetwork.io.modbus.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SelectionKey;

/**
 * API for a serial port.
 *
 * @author matt
 * @version 1.1
 */
public interface SerialPort {

	/**
	 * Get the name of the serial port, such as {@literal /dev/ttyUSB0} or
	 * {@literal COM1}.
	 *
	 * @return the serial port name
	 */
	String getName();

	/**
	 * Open the stream.
	 *
	 * @param parameters
	 *        the desired serial parameters
	 * @throws IOException
	 *         if any communication error occurs
	 */
	void open(SerialParameters parameters) throws IOException;

	/**
	 * Close the stream.
	 *
	 * @throws IOException
	 *         if any communication error occurs
	 */
	void close() throws IOException;

	/**
	 * Test if the stream is open.
	 *
	 * @return {@literal true} if the stream is open
	 */
	boolean isOpen();

	/**
	 * Get the serial input stream.
	 *
	 * <p>
	 * The stream must have been opened via {@link #open(SerialParameters)}
	 * before calling this.
	 * </p>
	 *
	 * @return the input stream
	 * @throws IOException
	 *         if any communication error occurs
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Get the serial output stream.
	 *
	 * <p>
	 * The stream must have been opened via {@link #open(SerialParameters)}
	 * before calling this.
	 * </p>
	 *
	 * @return the output stream
	 * @throws IOException
	 *         if any communication error occurs
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Add a listener for data events.
	 *
	 * @param listener
	 *        the listener to add
	 * @return {@code true} if the listener was registered successfully
	 * @throws IOException
	 *         if any communication error occurs
	 * @since 1.1
	 */
	//boolean addDataEventListener(EventListener listener) throws IOException;

	/**
	 * Get the number of bytes available to be read from the serial port.
	 *
	 * @return the number of bytes available
	 * @throws IOException
	 *         if any communication error occurs
	 * @since 1.1
	 */
	int bytesAvailable() throws IOException;

	/**
	 * Read bytes from the serial port.
	 *
	 * @param buffer
	 *        the buffer to copy the bytes to
	 * @param offset
	 *        the starting offset within {@code buffer} to copy the bytes to
	 * @param len
	 *        the maximum number of bytes to read
	 * @return the number of bytes read
	 * @throws IOException
	 *         if any communication error occurs
	 * @since 1.1
	 */
	int read(byte[] buffer, int offset, int len) throws IOException;

	/**
	 * Write bytes to the serial port.
	 *
	 * @param buffer
	 *        the source of bytes to write
	 * @param offset
	 *        the starting offset within {@code buffer} to copy the bytes from
	 * @param len
	 *        the maximum number of bytes to write
	 * @return the number of bytes written
	 * @throws IOException
	 *         if any communication error occurs
	 * @since 1.1
	 */
	int write(byte buffer[], int offset, int len) throws IOException;

	/**
	 * Register a NIO selection key.
	 *
	 * @param selectionKey
	 *        the key to register
	 * @throws IOException
	 *         if any communication error occurs
	 * @since 1.1
	 */
	void registerSelectionKey(SelectionKey selectionKey) throws IOException;

}
