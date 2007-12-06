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

package org.springframework.ws.transport.jms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * Implementation of {@link WebServiceConnection} that is used for server-side JMS access. Exposes a {@link
 * BytesMessage} request and response message.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class JmsReceiverConnection extends AbstractReceiverConnection {

    private final BytesMessage requestMessage;

    private final Session session;

    private BytesMessage responseMessage;

    /** Constructs a new JMS connection with the given parameters. */
    protected JmsReceiverConnection(BytesMessage requestMessage, Session session) {
        Assert.notNull(requestMessage, "requestMessage must not be null");
        Assert.notNull(session, "session must not be null");
        this.requestMessage = requestMessage;
        this.session = session;
    }

    /** Returns the request message for this connection. */
    public BytesMessage getRequestMessage() {
        return requestMessage;
    }

    /** Returns the response message, if any, for this connection. */
    public BytesMessage getResponseMessage() {
        return responseMessage;
    }

    public String getErrorMessage() throws IOException {
        return null;
    }

    public boolean hasError() throws IOException {
        return false;
    }

    /*
     * Receiving
     */

    protected Iterator getRequestHeaderNames() throws IOException {
        try {
            return new EnumerationIterator(requestMessage.getPropertyNames());
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property names", ex);
        }
    }

    protected Iterator getRequestHeaders(String name) throws IOException {
        try {
            String value = requestMessage.getStringProperty(name);
            return Collections.singletonList(value).iterator();
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property value", ex);
        }
    }

    protected InputStream getRequestInputStream() throws IOException {
        return new BytesMessageInputStream(requestMessage);
    }

    /*
     * Sending
     */

    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        try {
            responseMessage = session.createBytesMessage();
            responseMessage.setJMSCorrelationID(requestMessage.getJMSMessageID());
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not create response message", ex);
        }
    }

    protected void addResponseHeader(String name, String value) throws IOException {
        try {
            responseMessage.setStringProperty(name, value);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not set property", ex);
        }
    }

    protected OutputStream getResponseOutputStream() throws IOException {
        return new BytesMessageOutputStream(responseMessage);
    }

    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        MessageProducer messageProducer = null;
        try {
            if (requestMessage.getJMSReplyTo() != null) {
                messageProducer = session.createProducer(requestMessage.getJMSReplyTo());
                messageProducer.setDeliveryMode(requestMessage.getJMSDeliveryMode());
                messageProducer.setPriority(requestMessage.getJMSPriority());
                messageProducer.send(responseMessage);
            }
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
        finally {
            JmsUtils.closeMessageProducer(messageProducer);
        }
    }

}
