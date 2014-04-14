/*
 * Copyright 2005-2014 the original author or authors.
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

import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.EndpointAwareWebServiceConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
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
 * @since 1.0.0
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

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(messageFactory, "messageFactory is required");
    }

    /**
     * Handles an incoming connection by {@link WebServiceConnection#receive(WebServiceMessageFactory) receving} a
     * message from it, passing it to the {@link WebServiceMessageReceiver#receive(MessageContext) receiver}, and {@link
     * WebServiceConnection#send(WebServiceMessage) sending} the response (if any).
     * <p/>
     * Stores the given connection in the {@link TransportContext}.
     *
     * @param connection the incoming connection
     * @param receiver   the handler of the message, typically a {@link org.springframework.ws.server.MessageDispatcher}
     */
    protected final void handleConnection(WebServiceConnection connection, WebServiceMessageReceiver receiver)
            throws Exception {
        logUri(connection);
        TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
        TransportContextHolder.setTransportContext(new DefaultTransportContext(connection));

        try {
            WebServiceMessage request = connection.receive(getMessageFactory());
            MessageContext messageContext = new DefaultMessageContext(request, getMessageFactory());
            receiver.receive(messageContext);
            if (messageContext.hasResponse()) {
                WebServiceMessage response = messageContext.getResponse();
                if (response instanceof FaultAwareWebServiceMessage &&
                        connection instanceof FaultAwareWebServiceConnection) {
                    FaultAwareWebServiceMessage faultResponse = (FaultAwareWebServiceMessage) response;
                    FaultAwareWebServiceConnection faultConnection = (FaultAwareWebServiceConnection) connection;
                    faultConnection.setFault(faultResponse.hasFault());
                }
                connection.send(messageContext.getResponse());
            }
        }
        catch (NoEndpointFoundException ex) {
	        handleNoEndpointFoundException(ex, connection, receiver);
        }
        finally {
            TransportUtils.closeConnection(connection);
            TransportContextHolder.setTransportContext(previousTransportContext);
        }
    }

	/**
	 * Template method for handling {@code NoEndpointFoundException}s.
	 * <p/>
	 * Default implementation calls
	 * {@link EndpointAwareWebServiceConnection#endpointNotFound()} on the given
	 * connection, if possible.
	 *
	 * @param ex the {@code NoEndpointFoundException}
	 * @param connection the current {@code WebServiceConnection}
	 * @param receiver the {@code WebServiceMessageReceiver}
	 * @throws Exception in case of errors
	 */
	protected void handleNoEndpointFoundException(NoEndpointFoundException ex,
			WebServiceConnection connection,
			WebServiceMessageReceiver receiver) throws Exception {
		if (connection instanceof EndpointAwareWebServiceConnection) {
			((EndpointAwareWebServiceConnection) connection).endpointNotFound();
		}
	}

	private void logUri(WebServiceConnection connection) {
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Accepting incoming [" + connection + "] at [" + connection.getUri() + "]");
            }
            catch (URISyntaxException e) {
                // ignore
            }
        }
    }

}
