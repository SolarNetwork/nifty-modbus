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
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

class SerialSelectorProvider extends SelectorProvider {

	@Override
	public DatagramChannel openDatagramChannel() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Pipe openPipe() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public AbstractSelector openSelector() throws IOException {
		return new SerialPollingSelector(this);
	}

	@Override
	public ServerSocketChannel openServerSocketChannel() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SocketChannel openSocketChannel() throws IOException {
		return new SerialSocketChannel(this);
	}

}
