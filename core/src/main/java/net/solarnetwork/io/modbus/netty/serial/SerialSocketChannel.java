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
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SerialSocketChannel extends SocketChannel {

	private static final Logger logger = LoggerFactory.getLogger(SerialSocketChannel.class);

	private SerialChannel child = null;

	/**
	 * Initializes a new instance of this class.
	 *
	 * @param provider
	 *        The provider that created this channel
	 */
	protected SerialSocketChannel(SelectorProvider provider) {
		super(provider);
	}

	@Override
	public SocketChannel bind(SocketAddress local) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SocketChannel shutdownInput() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SocketChannel shutdownOutput() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Socket socket() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConnected() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConnectionPending() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean connect(SocketAddress remote) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean finishConnect() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getOption(SocketOption<T> name) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<SocketOption<?>> supportedOptions() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
		logger.debug("Requesting Blocking mode to '{}'", block ? "blocking" : "non blocking");
	}

	public SerialChannel getChild() {
		return child;
	}

	public void setChild(SerialChannel child) {
		this.child = child;
	}
}
