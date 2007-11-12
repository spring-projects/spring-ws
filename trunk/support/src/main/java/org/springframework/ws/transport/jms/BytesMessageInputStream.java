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
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;

import org.springframework.util.Assert;

/**
 * Input stream that wraps a {@link BytesMessage}.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
class BytesMessageInputStream extends InputStream {

    private final BytesMessage message;

    BytesMessageInputStream(BytesMessage message) {
        Assert.notNull(message, "'message' must not be null");
        this.message = message;
    }

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
