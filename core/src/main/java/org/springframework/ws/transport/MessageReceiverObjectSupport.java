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

package org.springframework.ws.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.MessageEndpoint;

/**
 * Convenience base class for server-side transport objects. Contains a {@link WebServiceMessageFactory}, and has
 * methods for handling incoming <code>WebServiceMessage</code> requests.
 *
 * @author Arjen Poutsma
 * @see #handle(TransportInputStream,TransportOutputStream,org.springframework.ws.endpoint.MessageEndpoint)
 */
public abstract class MessageReceiverObjectSupport implements InitializingBean {

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private WebServiceMessageFactory messageFactory;

    /**
     * Returns the <code>WebServiceMessageFactory</code>.
     */
    public WebServiceMessageFactory getMessageFactory() {
        return messageFactory;
    }

    /**
     * Sets the <code>WebServiceMessageFactory</code>.
     */
    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(messageFactory, "messageFactory is required");
        logger.info("Using message factory [" + messageFactory + "]");
    }

    protected final void handle(TransportInputStream tis, TransportOutputStream tos, MessageEndpoint endpoint)
            throws Exception {
        TransportContext previousTransportContext = TransportContextHolder.getTransportContext();
        TransportContextHolder.setTransportContext(new DefaultTransportContext(tis, tos));

        try {
            WebServiceMessage messageRequest = getMessageFactory().createWebServiceMessage(tis);
            MessageContext messageContext = new DefaultMessageContext(messageRequest, getMessageFactory());
            endpoint.invoke(messageContext);
            if (!messageContext.hasResponse()) {
                handleNoResponse(tis, tos);
            }
            else {
                handleResponse(tis, tos, messageContext.getResponse());
            }
        }
        catch (NoEndpointFoundException ex) {
            handleNoEndpointFound(tis, tos);
        }
        finally {
            TransportContextHolder.setTransportContext(previousTransportContext);
        }
    }

    /**
     * Invoked from {@link #handle(TransportInputStream,TransportOutputStream,org.springframework.ws.endpoint.MessageEndpoint)}
     * when no response is given. Default implementation does nothing. Can be overriden to set certain
     * transport-specific response headers.
     *
     * @param tis the transport input stream
     * @param tos the transport output stream
     */
    protected void handleNoResponse(TransportInputStream tis, TransportOutputStream tos) {
    }

    /**
     * Handles the sending of the response. Invoked from {@link #handle(TransportInputStream,TransportOutputStream,org.springframework.ws.endpoint.MessageEndpoint)}.
     * Default implementation writes the given response to the given <code>TransportOutputStream</code>. Can be
     * overriden to set certain transport-specific headers.
     *
     * @param tis      the transport input stream
     * @param tos      the transport output stream
     * @param response the response message
     * @see WebServiceMessage#writeTo(java.io.OutputStream)
     */
    protected void handleResponse(TransportInputStream tis, TransportOutputStream tos, WebServiceMessage response)
            throws Exception {
        response.writeTo(tos);
    }

    /**
     * Invoked from {@link #handle(TransportInputStream,TransportOutputStream,org.springframework.ws.endpoint.MessageEndpoint)}
     * when no suitable endpoint is found. Default implementation does nothing. Can be overriden to set certain
     * transport-specific response headers.
     *
     * @param tis the transport input stream
     * @param tos the transport output stream
     */
    protected void handleNoEndpointFound(TransportInputStream tis, TransportOutputStream tos) {
    }

}
