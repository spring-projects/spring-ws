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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * JMS specific implementation of the <code>TransportOutputStream</code> interface. Exposes a JMS
 * <code>TextMessage</code>, constructed lazily using a <code>Session</code>.
 *
 * @author Arjen Poutsma
 * @see #getTextMessage()
 */
public class JmsTransportOutputStream extends TransportOutputStream {

    private TextMessage textMessage;

    private final Session session;

    private String correlationId;

    /**
     * Constructs a new instance of the <code>JmsTransportOutputStream</code> with the given session.
     *
     * @param session the JMS session
     * @see javax.jms.Message#setJMSCorrelationID(String)
     */
    public JmsTransportOutputStream(Session session) {
        Assert.notNull(session, "session must not be null");
        this.session = session;
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
        Assert.hasLength(correlationId, "correlationId must not be null");
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
     * Returns the wrapped JMS <code>TextMessage</code>. Created lazily.
     */
    public TextMessage getTextMessage() throws IOException {
        if (textMessage == null) {
            try {
                textMessage = session.createTextMessage();
                if (StringUtils.hasLength(correlationId)) {
                    textMessage.setJMSCorrelationID(correlationId);
                }
            }
            catch (JMSException ex) {
                throw new IOException("Could not create text message: " + ex.getMessage());
            }
        }
        return textMessage;
    }

    protected OutputStream getOutputStream() throws IOException {
        return new TextMessageOutputStream();
    }

    public void addHeader(String name, String value) throws IOException {
        try {
            getTextMessage().setStringProperty(name, value);
        }
        catch (JMSException ex) {
            throw new IOException("Could not set property " + ex.getMessage());
        }
    }

    private class TextMessageOutputStream extends ByteArrayOutputStream {

        public void close() throws IOException {
            try {
                getTextMessage().setText(new String(toString("UTF-8")));
            }
            catch (JMSException ex) {
                throw new IOException("Could not set message text: " + ex.getMessage());
            }
        }
    }
}
