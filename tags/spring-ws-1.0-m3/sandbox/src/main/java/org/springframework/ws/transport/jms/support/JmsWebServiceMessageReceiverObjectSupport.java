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

package org.springframework.ws.transport.jms.support;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.jms.JmsTransportInputStream;
import org.springframework.ws.transport.jms.JmsTransportOutputStream;
import org.springframework.ws.transport.support.WebServiceMessageReceiverObjectSupport;

/**
 * Convenience base class for JMS server-side transport objects. Contains a {@link WebServiceMessageReceiver}, and has
 * methods for handling incoming JMS <code>Message</code> requests.
 * <p/>
 * This class can be used as a base for a EJB MessageDrivenBean, or using Spring-2.0's MessageDriven POJO's.
 *
 * @author Arjen Poutsma
 * @see #handle(javax.jms.Message,javax.jms.Session)
 */
public abstract class JmsWebServiceMessageReceiverObjectSupport extends WebServiceMessageReceiverObjectSupport
        implements InitializingBean {

    private WebServiceMessageReceiver messageReceiver;

    /**
     * Returns the <code>WebServiceMessageReceiver</code> used by this listener.
     */
    public WebServiceMessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    /**
     * Sets the <code>WebServiceMessageReceiver</code> used by this listener.
     */
    public void setMessageReceiver(WebServiceMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getMessageFactory(), "messageFactory is required");
        Assert.notNull(getMessageReceiver(), "messageReceiver must not be null");
        logger.info("Using message factory [" + getMessageFactory() + "]");
    }

    /**
     * Handles an incoming <code>Message</code>s. Uses the given <code>Session</code> to create a response request.
     *
     * @param request the incoming message
     * @param session the JMS session used to create a response
     * @throws IllegalArgumentException when request is not a <code>BytesMessage</code>
     */
    protected final void handle(Message request, Session session) throws Exception {
        if (request instanceof BytesMessage) {
            TransportInputStream tis = new JmsTransportInputStream((BytesMessage) request);
            TransportOutputStream tos = new JmsTransportOutputStream(session, request.getJMSCorrelationID());
            handle(tis, tos, getMessageReceiver());
        }
        else {
            throw new IllegalArgumentException(
                    "Wrong message type: [" + request.getClass() + "]. Only BytesMessages can be handled");
        }

    }

    protected final void handleResponse(TransportInputStream tis, TransportOutputStream tos, WebServiceMessage response)
            throws Exception {
        Message requestMessage = ((JmsTransportInputStream) tis).getMessage();
        if (requestMessage.getJMSReplyTo() == null) {
            logger.warn("Incoming message has no ReplyTo set, not sending response");
            return;
        }
        response.writeTo(tos);
        Session session = ((JmsTransportOutputStream) tos).getSession();
        MessageProducer producer = session.createProducer(requestMessage.getJMSReplyTo());
        Message responseMessage = ((JmsTransportOutputStream) tos).getMessage();
        try {
            producer.send(responseMessage);
        }
        finally {
            JmsUtils.closeMessageProducer(producer);
        }
    }
}
