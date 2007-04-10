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
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.AbstractReceivingWebServiceConnection;
import org.springframework.ws.transport.support.EnumerationIterator;

/** @author Arjen Poutsma */
public class JmsReceivingWebServiceConnection extends AbstractReceivingWebServiceConnection {

    private final BytesMessage requestMessage;

    private final Session session;

    private BytesMessage responseMessage;

    public JmsReceivingWebServiceConnection(BytesMessage requestMessage, Session session) {
        Assert.notNull(requestMessage, "requestMessage must not be null");
        Assert.notNull(session, "session must not be null");
        this.requestMessage = requestMessage;
        this.session = session;
    }

    public void close() throws IOException {
        try {
            session.close();
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not close session", ex);
        }
    }

    protected void sendResponse() throws IOException {
        if (responseMessage != null) {
            MessageProducer producer = null;
            try {
                if (requestMessage.getJMSReplyTo() != null) {
                    producer = session.createProducer(requestMessage.getJMSReplyTo());
                    producer.send(responseMessage);
                }
                else {
                    logger.warn("Incoming message has no ReplyTo set, not sending response");
                }
            }
            catch (JMSException ex) {
                throw new JmsTransportException("Could not send response", ex);
            }
            finally {
                if (producer != null) {
                    JmsUtils.closeMessageProducer(producer);
                }
            }
        }
    }

    private void createResponseMessage() throws IOException {
        if (responseMessage == null) {
            try {
                responseMessage = session.createBytesMessage();
                String correlationID = requestMessage.getJMSCorrelationID();
                if (StringUtils.hasLength(correlationID)) {
                    responseMessage.setJMSCorrelationID(correlationID);
                }
            }
            catch (JMSException ex) {
                throw new JmsTransportException("Could not create response message", ex);
            }
        }
    }

    protected void addResponseHeader(String name, String value) throws IOException {
        try {
            createResponseMessage();
            responseMessage.setStringProperty(name, value);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not set property", ex);
        }
    }

    protected OutputStream getResponseOutputStream() throws IOException {
        createResponseMessage();
        return new BytesMessageOutputStream(responseMessage);
    }

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
}
