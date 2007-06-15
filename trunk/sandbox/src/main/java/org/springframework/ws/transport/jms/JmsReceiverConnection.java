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
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/** @author Arjen Poutsma */
public class JmsReceiverConnection extends AbstractReceiverConnection
        implements JmsTransportConstants, FaultAwareWebServiceConnection {

    private final Log logger;

    private final BytesMessage requestMessage;

    private final Session session;

    private BytesMessage responseMessage;

    protected JmsReceiverConnection(BytesMessage requestMessage, Session session, Log logger) {
        Assert.notNull(requestMessage, "requestMessage must not be null");
        Assert.notNull(session, "session must not be null");
        Assert.notNull(logger, "'logger' must not be null");
        this.requestMessage = requestMessage;
        this.session = session;
        this.logger = logger;
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
            Enumeration headers = requestMessage.getPropertyNames();
            List results = new ArrayList();
            while (headers.hasMoreElements()) {
                String header = (String) headers.nextElement();
                if (header.startsWith(JmsTransportConstants.PROPERTY_PREFIX)) {
                    results.add(header);
                }
            }
            return results.iterator();
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
            responseMessage.setStringProperty(PROPERTY_BINDING_VERSION, "1.0");
            if (message instanceof FaultAwareWebServiceMessage) {
                FaultAwareWebServiceMessage faultMessage = (FaultAwareWebServiceMessage) message;
                responseMessage.setBooleanProperty(PROPERTY_IS_FAULT, faultMessage.hasFault());
            }
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not create response message", ex);
        }
    }

    protected void addResponseHeader(String name, String value) throws IOException {
        try {
            String property = JmsTransportUtils.headerToJmsProperty(name);
            responseMessage.setStringProperty(property, value);
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
            else {
                logger.warn("Incoming message has no ReplyTo set, not sending response");
            }
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
        finally {
            JmsUtils.closeMessageProducer(messageProducer);
        }
    }

    public void close() throws IOException {
    }

    /*
     * Faults
     */

    public boolean hasFault() throws IOException {
        try {
            return requestMessage.getBooleanProperty(JmsTransportConstants.PROPERTY_IS_FAULT);
        }
        catch (JMSException ex) {
            throw new JmsTransportException(ex);
        }
    }

    public void setFault(boolean fault) throws IOException {
        if (responseMessage != null) {
            try {
                responseMessage.setBooleanProperty(JmsTransportConstants.PROPERTY_IS_FAULT, true);
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }
    }


}
