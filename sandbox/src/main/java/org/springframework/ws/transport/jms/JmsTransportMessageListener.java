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

package org.springframework.ws.transport.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.transport.ServerTransportObjectSupport;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * JMS <code>MessageListener</code> that can be used to handle incoming JMS messages. Requires a
 * <code>WebServiceMessageFactory</code> which is used to convert the incoming JMS <code>TextMessage</code> into a
 * <code>WebServiceMessage</code>, and passes that context to the required <code>MessageEndpoint</code>. If a response
 * is created, it is sent using a response JMS message.
 * <p/>
 * This class implements both <code>MessageListener</code>, for
 * <p/>
 * Note that the <code>MessageDispatcher</code> implements the <code>MessageEndpoint</code> interface, enabling this
 * adapter to function as a gateway to further message handling logic.
 *
 * @author Arjen Poutsma
 * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * @see #setMessageEndpoint(org.springframework.ws.endpoint.MessageEndpoint)
 */
public class JmsTransportMessageListener extends ServerTransportObjectSupport
        implements SessionAwareMessageListener, MessageListener, InitializingBean {

    private static final Log logger = LogFactory.getLog(JmsTransportMessageListener.class);

    private MessageEndpoint messageEndpoint;

    /**
     * Returns the <code>MessageEndpoint</code> used by this listener.
     */
    public MessageEndpoint getMessageEndpoint() {
        return messageEndpoint;
    }

    /**
     * Sets the <code>MessageEndpoint</code> used by this listener.
     */
    public void setMessageEndpoint(MessageEndpoint messageEndpoint) {
        this.messageEndpoint = messageEndpoint;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(getMessageFactory(), "messageFactory is required");
        Assert.notNull(getMessageEndpoint(), "messageEndpoint must not be null");
        logger.info("Using message factory [" + getMessageFactory() + "]");
    }

    public void onMessage(Message message) {
        try {
            onMessage(message, null);
        }
        catch (JMSException ex) {
            logger.error("Could not handle message: " + ex.getMessage(), ex);
        }
    }

    public void onMessage(Message message, Session session) throws JMSException {
        if (message instanceof TextMessage) {
            logger.info("Received message [" + message.getJMSMessageID() + "]");
            try {
                TransportInputStream tis = new JmsTransportInputStream((TextMessage) message);
                TransportOutputStream tos;
                if (session == null) {
                    tos = null;
                }
                else {
                    tos = new JmsTransportOutputStream(session, ((TextMessage) message).getJMSCorrelationID());
                }
                handle(tis, tos, getMessageEndpoint());
            }
            catch (Exception ex) {
                logger.error(ex, ex);
            }
        }
        else {
            throw new IllegalArgumentException("JmsTransportMessageListener can only handle TextMessages");
        }
    }

    protected void handleResponse(TransportInputStream tis, TransportOutputStream tos, WebServiceMessage response)
            throws Exception {
        if (tos != null) {
            Message requestMessage = ((JmsTransportInputStream) tis).getTextMessage();
            if (requestMessage.getJMSReplyTo() == null) {
                logger.warn("Incoming message has no ReplyTo set, not sending response");
                return;
            }
            response.writeTo(tos);
            Message responseMessage = ((JmsTransportOutputStream) tos).getTextMessage();
            Session session = ((JmsTransportOutputStream) tos).getSession();
            MessageProducer producer = session.createProducer(requestMessage.getJMSReplyTo());
            try {
                producer.send(responseMessage);
            }
            finally {
                JmsUtils.closeMessageProducer(producer);
            }
        }
        else {
            logger.warn("JMS Session is not available, sending of response is impossible");
        }
    }
}
