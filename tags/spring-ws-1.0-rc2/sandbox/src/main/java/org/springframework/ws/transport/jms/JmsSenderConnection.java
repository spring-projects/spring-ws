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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/** @author Arjen Poutsma */
public class JmsSenderConnection extends AbstractSenderConnection
        implements FaultAwareWebServiceConnection, JmsTransportConstants {

    private final ConnectionFactory connectionFactory;

    private final Connection connection;

    private final Session session;

    private final Destination requestDestination;

    private Destination responseDestination;

    private final JmsUri uri;

    private BytesMessage requestMessage;

    private BytesMessage responseMessage;

    private long receiveTimeout;

    protected JmsSenderConnection(JmsUri uri, ConnectionFactory connectionFactory, long receiveTimeout)
            throws JMSException {
        Assert.notNull(uri, "'uri' must not be null");
        Assert.notNull(connectionFactory, "'connectionFactory' must not be null");
        this.connectionFactory = connectionFactory;
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (uri.isPubSubDomain()) {
            requestDestination = session.createTopic(uri.getDestination());
        }
        else {
            requestDestination = session.createQueue(uri.getDestination());
        }
        this.uri = uri;
        this.receiveTimeout = receiveTimeout;
    }

    public BytesMessage getRequestMessage() {
        return requestMessage;
    }

    public BytesMessage getResponseMessage() {
        return responseMessage;
    }

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
            requestMessage = session.createBytesMessage();
            requestMessage.setStringProperty(PROPERTY_BINDING_VERSION, "1.0");
            if (message instanceof FaultAwareWebServiceMessage) {
                FaultAwareWebServiceMessage faultMessage = (FaultAwareWebServiceMessage) message;
                requestMessage.setBooleanProperty(PROPERTY_IS_FAULT, faultMessage.hasFault());
            }
            requestMessage.setStringProperty(PROPERTY_REQUEST_IRI, uri.toString());
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
    }

    protected void addRequestHeader(String name, String value) throws IOException {
        try {
            String property = JmsTransportUtils.headerToJmsProperty(name);
            requestMessage.setStringProperty(property, value);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not set property", ex);
        }
    }

    protected OutputStream getRequestOutputStream() throws IOException {
        return new BytesMessageOutputStream(requestMessage);
    }

    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        MessageProducer messageProducer = null;
        try {
            messageProducer = session.createProducer(requestDestination);
            messageProducer.setDeliveryMode(uri.getDeliveryMode());
            messageProducer.setTimeToLive(uri.getTimeToLive());
            messageProducer.setPriority(uri.getPriority());
            if (uri.hasReplyTo()) {
                if (uri.isPubSubDomain()) {
                    responseDestination = session.createTopic(uri.getReplyTo());
                }
                else {
                    responseDestination = session.createQueue(uri.getReplyTo());
                }

            }
            else {
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
            responseMessage = (BytesMessage) (receiveTimeout >= 0 ? messageConsumer.receive(receiveTimeout) :
                    messageConsumer.receive());
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
            List headerNames = new ArrayList();
            Enumeration propertyNames = responseMessage.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                headerNames.add(JmsTransportUtils.jmsPropertyToHeader(propertyName));
            }
            return headerNames.iterator();
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property names", ex);
        }
    }

    protected Iterator getResponseHeaders(String name) throws IOException {
        try {
            String propertyName = JmsTransportUtils.headerToJmsProperty(name);
            String value = responseMessage.getStringProperty(propertyName);
            if (value != null) {
                return Collections.singletonList(value).iterator();
            }
            else {
                return Collections.EMPTY_LIST.iterator();
            }
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property value", ex);
        }
    }

    protected InputStream getResponseInputStream() throws IOException {
        return new BytesMessageInputStream(responseMessage);
    }

    public void close() throws IOException {
        JmsUtils.closeSession(session);
        ConnectionFactoryUtils.releaseConnection(connection, connectionFactory, true);
    }

    /*
     * Faults
     */

    public boolean hasFault() throws IOException {
        if (responseMessage != null) {
            try {
                return responseMessage.getBooleanProperty(JmsTransportConstants.PROPERTY_IS_FAULT);
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }
        else {
            return false;
        }
    }

    public void setFault(boolean fault) throws IOException {
        try {
            requestMessage.setBooleanProperty(JmsTransportConstants.PROPERTY_IS_FAULT, true);
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
    }

}
