/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.solarnetwork.io.modbus.netty.serial;

import java.io.IOException;
import java.net.SocketAddress;
import io.netty.buffer.ByteBuf;
import net.solarnetwork.io.modbus.serial.SerialPort;

/**
 * This is a wrapper mostly for testing
 * {@link SerialChannel}, @{@link SerialPollingSelector},
 * {@link SerialSelectionKey}, @{@link SerialSelectorProvider}
 * and @{@link SerialSocketChannel}.
 */
public final class SerialChannelHandler_ {

	protected final SerialPort serialPort;
	protected final SocketAddress address;
	private final SerialPortChannelConfig config;

	public SerialChannelHandler_(SerialPort serialPort, SocketAddress address,
			SerialPortChannelConfig config) {
		super();
		this.serialPort = serialPort;
		this.address = address;
		this.config = config;
	}

	public boolean open() {
		try {
			serialPort.open(config);
			return true;
		} catch ( Exception e ) {
			return false;
		}
	}

	public String getIdentifier() {
		return serialPort.getName();
	}

	/**
	 * This method registers the Callback to the SelectionKey /
	 * {@link java.nio.channels.Selector} which is necessary to notify the
	 * {@link java.nio.channels.Selector} about available data.
	 */
	public void registerSelectionKey(SerialSelectionKey selectionKey) throws IOException {
		serialPort.registerSelectionKey(selectionKey);
	}

	public void close() {
		try {
			this.serialPort.close();
		} catch ( IOException e ) {
			// ignore
		}
	}

	public int read(ByteBuf buf) {
		try {
			int bytesToRead = serialPort.bytesAvailable();
			assert bytesToRead > 0;
			byte[] buffer = new byte[bytesToRead];
			int count = serialPort.read(buffer, 0, bytesToRead);
			if ( count > 0 ) {
				buf.writeBytes(buffer, 0, count);
			}
			return count;
		} catch ( IOException e ) {
			return -1;
		}
	}

	public int write(ByteBuf buf) {
		int expectedToWrite = buf.readableBytes();
		byte[] bytes = new byte[expectedToWrite];
		buf.readBytes(bytes);
		try {
			return serialPort.write(bytes, 0, expectedToWrite);
		} catch ( IOException e ) {
			return -1;
		}
	}

}
