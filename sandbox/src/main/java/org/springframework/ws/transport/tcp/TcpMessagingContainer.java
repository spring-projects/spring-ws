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

import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.AbstractMessagingContainer;

/** @author Arjen Poutsma */
public class TcpMessagingContainer extends AbstractMessagingContainer {

    private ServerSocket serverSocket;

    private InetAddress bindAddress;

    private int backlog = -1;

    private int port = -1;

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
     * @throws java.net.UnknownHostException when the given address is not known
     * @see java.net.ServerSocket#ServerSocket(int,int,java.net.InetAddress)
     */
    public void setBindAddress(String bindAddress) throws UnknownHostException {
        this.bindAddress = InetAddress.getByName(bindAddress);
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (port == -1) {
            throw new IllegalArgumentException("port is required");
        }
        openServerSocket();
    }

    protected void onActivate() throws IOException {
        openServerSocket();
    }

    protected void onStart() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting tcp server [" + serverSocket.getLocalSocketAddress() + "]");
        }
        getTaskExecutor().execute(new SocketAcceptingRunnable());
    }

    protected void onStop() {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping tcp server [" + serverSocket.getLocalSocketAddress() + "]");
        }
    }

    protected void onShutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("Shutting down tcp server [" + serverSocket.getLocalSocketAddress() + "]");
        }
        closeServerSocket();
    }

    /**
     * Establish a shared <code>ServerSocket</code> for this server.
     * <p/>
     * The default implementation delegates to <code>refreshSharedConnection</code>, which does one immediate attempt
     * and throws an exception if it fails. Can be overridden to have a recovery proces in place, retrying until a
     * ServerSocket can be successfully established.
     *
     * @see #refreshServerSocket()
     */
    protected void openServerSocket() throws IOException {
        refreshServerSocket();
    }

    /**
     * Refresh the shared <code>ServerSocket</code> that this server holds.
     * <p/>
     * Called on startup and also after an infrastructure exception that occured during listener setup and/or
     * execution.
     */
    protected final void refreshServerSocket() throws IOException {
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

    private class SocketAcceptingRunnable implements Runnable {

        public void run() {
            while (isRunning()) {
                try {
                    Socket socket = serverSocket.accept();
                    TcpRequestHandler handler = new TcpRequestHandler(socket);
                    getTaskExecutor().execute(handler);
                }
                catch (InterruptedIOException ex) {
                    logger.warn(ex);
                }
                catch (IOException ex) {
                    logger.warn("Could not accept incoming connection: " + ex.getMessage());
                }
            }
        }
    }

    private class TcpRequestHandler implements Runnable {

        private final Socket socket;

        public TcpRequestHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            WebServiceConnection connection = new TcpReceivingWebServiceConnection(socket);
            try {
                handleConnection(connection);
            }
            catch (Exception ex) {
                logger.warn("Could not handle request", ex);
            }
        }
    }

}
