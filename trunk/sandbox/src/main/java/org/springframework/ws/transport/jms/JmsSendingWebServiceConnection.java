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
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TemporaryQueue;

import org.springframework.util.Assert;
import org.springframework.ws.transport.AbstractSendingWebServiceConnection;
import org.springframework.ws.transport.support.EnumerationIterator;

/** @author Arjen Poutsma */
public class JmsSendingWebServiceConnection extends AbstractSendingWebServiceConnection {

    private final BytesMessage requestMessage;

    private BytesMessage responseMessage;

    private final QueueSession session;

    private TemporaryQueue responseQueue = null;

    private QueueConnection connection;

    private long receiveTimeout;

    private Queue queue;

    public JmsSendingWebServiceConnection(QueueConnection connection,
                                          QueueSession session,
                                          Queue queue,
                                          long receiveTimeout) throws JMSException {
        Assert.notNull(connection, "connection must not be null");
        Assert.notNull(session, "session must not be null");
        Assert.notNull(queue, "queue must not be null");
        this.connection = connection;
        this.session = session;
        this.queue = queue;
        this.receiveTimeout = receiveTimeout;
        requestMessage = session.createBytesMessage();
    }

    public void close() throws IOException {
        try {
            session.close();
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not close session", ex);
        }
        try {
            connection.close();
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not close connection", ex);
        }
    }

    protected void addRequestHeader(String name, String value) throws IOException {
        try {
            requestMessage.setStringProperty(name, value);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not set property", ex);
        }
    }

    protected OutputStream getRequestOutputStream() throws IOException {
        return new BytesMessageOutputStream(requestMessage);
    }

    protected void sendRequest() throws IOException {
        QueueSender sender = null;
        try {
            sender = session.createSender(queue);
            responseQueue = session.createTemporaryQueue();
            requestMessage.setJMSReplyTo(responseQueue);
            connection.start();
            sender.send(requestMessage);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not send request message", ex);
        }
        finally {
            try {
                if (sender != null) {
                    sender.close();
                }
            }
            catch (JMSException ex) {
                throw new JmsTransportException("Could not close QueueSender", ex);
            }
        }
    }

    protected boolean hasResponse() throws IOException {
        if (responseMessage != null) {
            return true;
        }
        else if (responseQueue != null) {
            QueueReceiver receiver = null;
            try {
                receiver = session.createReceiver(responseQueue);
                responseMessage = (BytesMessage) receiver.receive(receiveTimeout);
                return responseMessage != null;
            }
            catch (JMSException ex) {
                throw new JmsTransportException("Could not receive message", ex);
            }
            finally {
                try {
                    if (receiver != null) {
                        receiver.close();
                    }
                }
                catch (JMSException ex) {
                    throw new JmsTransportException("Could not close QueueReceiver", ex);
                }
                try {
                    responseQueue.delete();
                    responseQueue = null;
                }
                catch (JMSException ex) {
                    throw new JmsTransportException("Could not delete temporary response queue", ex);
                }
            }
        }
        else {
            return false;
        }
    }

    protected Iterator getResponseHeaderNames() throws IOException {
        try {
            return new EnumerationIterator(responseMessage.getPropertyNames());
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property names", ex);
        }
    }

    protected Iterator getResponseHeaders(String name) throws IOException {
        try {
            String value = responseMessage.getStringProperty(name);
            return Collections.singletonList(value).iterator();
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property value", ex);
        }
    }

    protected InputStream getResponseInputStream() throws IOException {
        return new BytesMessageInputStream(responseMessage);
    }
}
