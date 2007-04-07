/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.transport.support;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

/**
 * Convenience base class for server-side transport objects. Contains a {@link WebServiceMessageFactory}, and has
 * methods for handling incoming {@link WebServiceConnection}s.
 *
 * @author Arjen Poutsma
 * @see #handleConnection
 */
public abstract class WebServiceMessageReceiverObjectSupport implements InitializingBean {

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    private WebServiceMessageFactory messageFactory;

    /** Returns the <code>WebServiceMessageFactory</code>. */
    public WebServiceMessageFactory getMessageFactory() {
        return messageFactory;
    }

    /** Sets the <code>WebServiceMessageFactory</code>. */
    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(messageFactory, "messageFactory is required");
        logger.info("Using message factory [" + messageFactory + "]");
    }

    /**
     * Handles an incoming connection by reading a message from the connection input stream, passing it to the receiver,
     * and writing the response (if any) to the output stream.
     * <p/>
     * Stores the given connection in the transport context.
     *
     * @param connection the incoming connection
     * @param receiver   the handler of the message, typically a {@link org.springframework.ws.server.MessageDispatcher}
     * @see org.springframework.ws.transport.context.TransportContext
     */
    protected final void handleConnection(WebServiceConnection connection, WebServiceMessageReceiver receiver)
            throws Exception {
        TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
        TransportContextHolder.setTransportContext(new DefaultTransportContext(connection));

        try {
            MessageContext messageContext = handleRequest(connection);
            receiver.receive(messageContext);
            if (!messageContext.hasResponse()) {
                handleNoResponse(connection);
            }
            else {
                handleResponse(connection, messageContext.getResponse());
            }
        }
        catch (NoEndpointFoundException ex) {
            handleNoEndpointFound(connection);
        }
        finally {
            try {
                connection.close();
            }
            catch (IOException ex) {
                logger.debug("Could not close connection", ex);
            }
            TransportContextHolder.setTransportContext(previousTransportContext);
        }
    }

    private MessageContext handleRequest(WebServiceConnection connection) throws IOException {
        TransportInputStream tis = connection.getTransportInputStream();
        try {
            WebServiceMessage messageRequest = getMessageFactory().createWebServiceMessage(tis);
            return new DefaultMessageContext(messageRequest, getMessageFactory());
        }
        finally {
            tis.close();
        }
    }

    /**
     * Invoked from {@link #handleConnection} when no response is given. Default implementation does nothing. Can be
     * overriden to set certain transport-specific response headers.
     *
     * @param connection the incoming connection
     */
    protected void handleNoResponse(WebServiceConnection connection) {
    }

    /**
     * Handles the sending of the response. Invoked from {@link #handleConnection}. Default implementation writes the
     * given response to the given <code>TransportOutputStream</code>. Can be overriden to set certain
     * transport-specific headers.
     *
     * @param connection the incoming connection
     * @param response   the response message
     * @see WebServiceMessage#writeTo(java.io.OutputStream)
     */
    protected void handleResponse(WebServiceConnection connection, WebServiceMessage response) throws Exception {
        TransportOutputStream tos = connection.getTransportOutputStream();
        try {
            response.writeTo(tos);
            tos.flush();
        }
        finally {
            tos.close();
        }
    }

    /**
     * Invoked from {@link #handleConnection} when no suitable endpoint is found. Default implementation does nothing.
     * Can be overriden to set certain transport-specific response headers.
     *
     * @param connection the incoming connection
     */
    protected void handleNoEndpointFound(WebServiceConnection connection) {
    }

}
