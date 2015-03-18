/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.tcp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.AbstractAsyncStandaloneMessageReceiver;

/** @author Arjen Poutsma */
public class TcpMessageReceiver extends AbstractAsyncStandaloneMessageReceiver {

	public static final int DEFAULT_PORT = 8081;

	private ServerSocket serverSocket;

	private InetAddress bindAddress;

	private int backlog = -1;

	private int port = DEFAULT_PORT;

	/** Sets the port the server will bind to. */
	public void setPort(int port) {
		this.port = port;
	}

	/** Sets the server back log. */
	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	/**
	 * Sets the local internet address the server will bind to. By default, it will accept connections on any/all local
	 * addresses.
	 *
	 * @throws UnknownHostException when the given address is not known
	 * @see ServerSocket#ServerSocket(int,int,java.net.InetAddress)
	 */
	public void setBindAddress(String bindAddress) throws UnknownHostException {
		this.bindAddress = InetAddress.getByName(bindAddress);
	}

	protected void onActivate() throws IOException {
		openServerSocket();
	}

	protected void onStart() {
		if (logger.isInfoEnabled()) {
			logger.info("Starting tcp receiver [" + serverSocket.getLocalSocketAddress() + "]");
		}
		execute(new SocketAcceptingRunnable());
	}

	protected void onStop() {
		if (logger.isInfoEnabled()) {
			logger.info("Stopping tcp receiver [" + serverSocket.getLocalSocketAddress() + "]");
		}
	}

	protected void onShutdown() {
		if (logger.isInfoEnabled()) {
			logger.info("Shutting down tcp receiver [" + serverSocket.getLocalSocketAddress() + "]");
		}
		closeServerSocket();
	}

	/** Establish a <code>ServerSocket</code> for this receiver. */
	protected void openServerSocket() throws IOException {
		closeServerSocket();
		serverSocket = new ServerSocket(port, backlog, bindAddress);
	}

	protected void closeServerSocket() {
		if (serverSocket == null) {
			return;
		}
		try {
			serverSocket.close();
		}
		catch (IOException ex) {
			logger.debug("Could not close ServerSocket", ex);
		}
	}

	private class SocketAcceptingRunnable implements SchedulingAwareRunnable {

		public void run() {
			while (isRunning()) {
				try {
					Socket socket = serverSocket.accept();
					TcpRequestHandler handler = new TcpRequestHandler(socket);
					execute(handler);
				}
				catch (InterruptedIOException ex) {
					logger.warn(ex);
				}
				catch (IOException ex) {
					logger.warn("Could not accept incoming connection: " + ex.getMessage());
				}
			}
		}

		public boolean isLongLived() {
			return true;
		}
	}

	private class TcpRequestHandler implements SchedulingAwareRunnable {

		private final Socket socket;

		public TcpRequestHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			WebServiceConnection connection = new TcpReceiverConnection(socket);
			try {
				handleConnection(connection);
			}
			catch (Exception ex) {
				logger.warn("Could not handle request", ex);
			}
		}

		public boolean isLongLived() {
			return false;
		}
	}

}
