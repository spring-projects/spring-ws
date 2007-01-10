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
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;

import org.springframework.util.Assert;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * JMS specific implementation of the <code>TransportInputStream</code> interface. Exposes a JMS
 * <code>BytesMessage</code>.
 *
 * @author Arjen Poutsma
 * @see #getMessage()
 */
public class JmsTransportInputStream extends TransportInputStream {

    private final BytesMessage message;

    /**
     * Constructs a new instance of the <code>JmsTransportInputStream</code> using the provided JMS
     * <code>BytesMessage</code>.
     *
     * @param message the JMS message
     */
    public JmsTransportInputStream(BytesMessage message) {
        Assert.notNull(message, "message must not be null");
        this.message = message;
    }

    /**
     * Returns the wrapped JMS message.
     */
    public BytesMessage getMessage() {
        return message;
    }

    protected InputStream createInputStream() throws IOException {
        return new BytesMessageInputStream();
    }

    public Iterator getHeaderNames() throws IOException {
        try {
            return new EnumerationIterator(message.getPropertyNames());
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property names", ex);
        }
    }

    public Iterator getHeaders(String name) throws IOException {
        try {
            String value = message.getStringProperty(name);
            return Collections.singletonList(value).iterator();
        }
        catch (JMSException ex) {
            throw new JmsTransportException("Could not get property value", ex);
        }
    }

    /**
     * InputStream that wraps the JMS <code>BytesMessage</code>.
     */
    private class BytesMessageInputStream extends InputStream {

        public int read(byte b[]) throws IOException {
            try {
                return message.readBytes(b);
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }

        public int read(byte b[], int off, int len) throws IOException {
            if (off == 0) {
                try {
                    return message.readBytes(b, len);
                }
                catch (JMSException ex) {
                    throw new JmsTransportException(ex);
                }
            }
            else {
                return super.read(b, off, len);
            }
        }

        public int read() throws IOException {
            try {
                return message.readByte();
            }
            catch (MessageEOFException ex) {
                return -1;
            }
            catch (JMSException ex) {
                throw new JmsTransportException(ex);
            }
        }
    }
}
