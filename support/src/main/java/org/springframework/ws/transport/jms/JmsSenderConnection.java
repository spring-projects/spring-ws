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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for client-side JMS access. Exposes a {@link
 * BytesMessage} request and response message.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class JmsSenderConnection extends AbstractSenderConnection implements WebServiceConnection {

    private final ConnectionFactory connectionFactory;

    private final Connection connection;

    private final Session session;

    private final Destination requestDestination;

    private Destination responseDestination;

    private Message requestMessage;

    private Message responseMessage;

    private long receiveTimeout;

    private int deliveryMode;

    private long timeToLive;

    private int priority;

    private String textMessageEncoding;

    private int messageType;

    /** Constructs a new JMS connection with the given parameters. */
    protected JmsSenderConnection(ConnectionFactory connectionFactory,
                                  Connection connection,
                                  Session session,
                                  Destination requestDestination) throws JMSException {
        Assert.notNull(connectionFactory, "'connectionFactory' must not be null");
        Assert.notNull(connection, "'connection' must not be null");
        Assert.notNull(session, "'session' must not be null");
        this.connectionFactory = connectionFactory;
        this.connection = connection;
        this.session = session;
        this.requestDestination = requestDestination;
    }

    /** Returns the request message for this connection. Returns either a {@link BytesMessage} or a {@link TextMessage}. */
    public Message getRequestMessage() {
        return requestMessage;
    }

    /**
     * Returns the response message, if any, for this connection. Returns either a {@link BytesMessage} or a {@link
     * TextMessage}.
     */
    public Message getResponseMessage() {
        return responseMessage;
    }

    /*
     * Package-friendly setters
     */

    void setResponseDestination(Destination responseDestination) {
        this.responseDestination = responseDestination;
    }

    void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    void setDeliveryMode(int deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    void setTextMessageEncoding(String textMessageEncoding) {
        this.textMessageEncoding = textMessageEncoding;
    }

    void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    /*
     * Errors
     */

    public boolean hasError() throws IOException {
        return false;
    }

    public String getErrorMessage() throws IOException {
        return null;
    }

    /*
     * Sending
     */

    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        try {
            if (messageType == JmsTransportConstants.BYTES_MESSAGE_TYPE) {
                requestMessage = session.createBytesMessage();
            }
            else {
                requestMessage = session.createTextMessage();
            }
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
    }

    protected void addRequestHeader(String name, String value) throws IOException {
        try {
            JmsTransportUtils.addHeader(requestMessage, name, value);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not set property", ex);
        }
    }

    protected OutputStream getRequestOutputStream() throws IOException {
        if (requestMessage instanceof BytesMessage) {
            return new BytesMessageOutputStream((BytesMessage) requestMessage);
        }
        else {
            return new ByteArrayOutputStream() {

                public void close() throws IOException {
                    String text = new String(toByteArray(), textMessageEncoding);
                    try {
                        ((TextMessage) requestMessage).setText(text);
                    }
                    catch (JMSException ex) {
                        throw new JmsTransportException(ex);
                    }
                }
            };
        }
    }

    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        MessageProducer messageProducer = null;
        try {
            messageProducer = session.createProducer(requestDestination);
            messageProducer.setDeliveryMode(deliveryMode);
            messageProducer.setTimeToLive(timeToLive);
            messageProducer.setPriority(priority);
            if (responseDestination == null) {
                responseDestination = session.createTemporaryQueue();
            }
            requestMessage.setJMSReplyTo(responseDestination);
            connection.start();
            messageProducer.send(requestMessage);
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
        finally {
            JmsUtils.closeMessageProducer(messageProducer);
        }
    }

    /*
     * Receiving
     */

    protected void onReceiveBeforeRead() throws IOException {
        MessageConsumer messageConsumer = null;
        try {
            messageConsumer = session.createConsumer(responseDestination);
            Message message = receiveTimeout >= 0 ? messageConsumer.receive(receiveTimeout) : messageConsumer.receive();
            if (message instanceof BytesMessage || message instanceof TextMessage) {
                responseMessage = message;
            }
            else {
                throw new IllegalArgumentException(
                        "Wrong message type: [" + message.getClass() + "]. Only BytesMessages can be handled.");
            }
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
        finally {
            JmsUtils.closeMessageConsumer(messageConsumer);
            if (responseDestination instanceof TemporaryQueue) {
                try {
                    ((TemporaryQueue) responseDestination).delete();
                }
                catch (JMSException e) {
                    // ignore
                }
            }
        }
    }

    protected boolean hasResponse() throws IOException {
        return responseMessage != null;
    }

    protected Iterator getResponseHeaderNames() throws IOException {
        try {
            return JmsTransportUtils.getHeaderNames(responseMessage);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property names", ex);
        }
    }

    protected Iterator getResponseHeaders(String name) throws IOException {
        try {
            return JmsTransportUtils.getHeaders(responseMessage, name);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property value", ex);
        }
    }

    protected InputStream getResponseInputStream() throws IOException {
        if (responseMessage instanceof BytesMessage) {
            return new BytesMessageInputStream((BytesMessage) responseMessage);
        }
        else {
            TextMessage textMessage = (TextMessage) responseMessage;
            try {
                String text = textMessage.getText();
                byte[] contents = text != null ? text.getBytes(textMessageEncoding) : new byte[0];
                return new ByteArrayInputStream(contents);
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }
    }

    protected void onClose() throws IOException {
        JmsUtils.closeSession(session);
        ConnectionFactoryUtils.releaseConnection(connection, connectionFactory, true);
    }

}
