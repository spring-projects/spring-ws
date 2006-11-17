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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.springframework.util.Assert;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * JMS specific implementation of the <code>TransportInputStream</code> interface. Exposes a JMS
 * <code>TextMessage</code>.
 *
 * @author Arjen Poutsma
 * @see #getTextMessage()
 */
public class JmsTransportInputStream extends TransportInputStream {

    private final TextMessage textMessage;

    /**
     * Constructs a new instance of the <code>JmsTransportInputStream</code> using the provided JMS
     * <code>TextMessage</code>.
     *
     * @param textMessage the JMS message
     */
    public JmsTransportInputStream(TextMessage textMessage) {
        Assert.notNull(textMessage, "textMessage must not be null");
        this.textMessage = textMessage;
    }

    /**
     * Returns the wrapped JMS <code>TextMessage</code>.
     */
    public TextMessage getTextMessage() {
        return textMessage;
    }

    protected InputStream createInputStream() throws IOException {
        try {
            return new ByteArrayInputStream(textMessage.getText().getBytes("UTF-8"));
        }
        catch (JMSException ex) {
            throw new IOException("Could not get text of message: " + ex.getMessage());
        }
    }

    public Iterator getHeaderNames() throws IOException {
        try {
            return new EnumerationIterator(textMessage.getPropertyNames());
        }
        catch (JMSException ex) {
            throw new IOException("Could not get property names: " + ex.getMessage());
        }
    }

    public Iterator getHeaders(String name) throws IOException {
        try {
            String value = textMessage.getStringProperty(name);
            return Collections.singletonList(value).iterator();
        }
        catch (JMSException ex) {
            throw new IOException("Could not get property value: " + ex.getMessage());
        }
    }
}
