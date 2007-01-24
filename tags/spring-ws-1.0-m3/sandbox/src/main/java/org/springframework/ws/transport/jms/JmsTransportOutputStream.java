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

import java.io.IOException;
import java.io.OutputStream;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * JMS specific implementation of the <code>TransportOutputStream</code> interface. Exposes a JMS
 * <code>BytesMessage</code>, constructed lazily using a <code>Session</code>.
 *
 * @author Arjen Poutsma
 * @see #getMessage()
 */
public class JmsTransportOutputStream extends TransportOutputStream {

    private BytesMessage message;

    private final Session session;

    private String correlationId;

    /**
     * Constructs a new instance of the <code>JmsTransportOutputStream</code> with the given session.
     *
     * @param session the JMS session
     * @see javax.jms.Message#setJMSCorrelationID(String)
     */
    public JmsTransportOutputStream(Session session) {
        this(session, null);
    }

    /**
     * Constructs a new instance of the <code>JmsTransportOutputStream</code> with the given session and correlation ID.
     * The correlation ID is used for creating a response to a request JMS message.
     *
     * @param session       the JMS session
     * @param correlationId the correlation id
     * @see javax.jms.Message#setJMSCorrelationID(String)
     */
    public JmsTransportOutputStream(Session session, String correlationId) {
        Assert.notNull(session, "session must not be null");
        this.session = session;
        this.correlationId = correlationId;
    }

    /**
     * Returns the wrapped JMS <code>Session</code>.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns the wrapped JMS <code>BytesMessage</code>. Created lazily.
     */
    public BytesMessage getMessage() throws IOException {
        if (message == null) {
            try {
                message = session.createBytesMessage();
                if (StringUtils.hasLength(correlationId)) {
                    message.setJMSCorrelationID(correlationId);
                }
            }
            catch (JMSException ex) {
                throw new JmsTransportException("Could not create message", ex);
            }
        }
        return message;
    }

    protected OutputStream getOutputStream() throws IOException {
        return new BytesMessageOutputStream();
    }

    public void addHeader(String name, String value) throws IOException {
        try {
            getMessage().setStringProperty(name, value);
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not set property", ex);
        }
    }

    /**
     * OutputStream that wraps the JMS <code>BytesMessage</code>.
     */
    private class BytesMessageOutputStream extends OutputStream {

        public void write(byte b[]) throws IOException {
            try {
                getMessage().writeBytes(b);
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }

        public void write(byte b[], int off, int len) throws IOException {
            try {
                getMessage().writeBytes(b, off, len);
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }

        public void write(int b) throws IOException {
            try {
                getMessage().writeByte((byte) b);
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }
    }

}
