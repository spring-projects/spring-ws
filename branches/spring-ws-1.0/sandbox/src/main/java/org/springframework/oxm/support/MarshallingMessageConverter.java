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

package org.springframework.oxm.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

/**
 * Spring JMS {@link MessageConverter} that uses a {@link Marshaller} and {@link Unmarshaller}. Marshals an object to a
 * {@link BytesMessage}, or to a {@link TextMessage} if the {@link #setMarshalToTextMessage(boolean)
 * marshalToTextMessage} is <code>true</code>. Unmarshals from a {@link TextMessage} or {@link BytesMessage} to an
 * object.
 *
 * @author Arjen Poutsma
 */
public class MarshallingMessageConverter implements MessageConverter, InitializingBean {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private boolean marshalToTextMessage = false;

    /**
     * Constructs a new <code>MarshallingMessageConverter</code> with no {@link Marshaller} set. The marshaller must be
     * set after construction by invoking {@link #setMarshaller(Marshaller)}.
     */
    public MarshallingMessageConverter() {
    }

    /**
     * Constructs a new <code>MarshallingMessageConverter</code> with the given {@link Marshaller} set.  If the given
     * {@link Marshaller} also implements the {@link Unmarshaller} interface, it is used for both marshalling and
     * unmarshalling. Otherwise, an exception is thrown.
     * <p/>
     * Note that all {@link Marshaller} implementations in Spring-WS also implement the {@link Unmarshaller} interface,
     * so that you can safely use this constructor.
     *
     * @param marshaller object used as marshaller and unmarshaller
     * @throws IllegalArgumentException when <code>marshaller</code> does not implement the {@link Unmarshaller}
     *                                  interface
     */
    public MarshallingMessageConverter(Marshaller marshaller) {
        Assert.notNull(marshaller, "marshaller must not be null");
        if (!(marshaller instanceof Unmarshaller)) {
            throw new IllegalArgumentException("Marshaller [" + marshaller + "] does not implement the Unmarshaller " +
                    "interface. Please set an Unmarshaller explicitely by using the " +
                    "AbstractMarshallingPayloadEndpoint(Marshaller, Unmarshaller) constructor.");
        }
        else {
            this.marshaller = marshaller;
            this.unmarshaller = (Unmarshaller) marshaller;
        }
    }

    /**
     * Creates a new <code>MarshallingMessageConverter</code> with the given marshaller and unmarshaller.
     *
     * @param marshaller   the marshaller to use
     * @param unmarshaller the unmarshaller to use
     */
    public MarshallingMessageConverter(Marshaller marshaller, Unmarshaller unmarshaller) {
        Assert.notNull(marshaller, "marshaller must not be null");
        Assert.notNull(unmarshaller, "unmarshaller must not be null");
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    /**
     * Indicates whether {@link #toMessage(Object,Session)} should marshal to a {@link TextMessage} or a {@link
     * BytesMessage}. The default is <code>false</code>, i.e. this converter marshals to a {@link BytesMessage}.
     */
    public void setMarshalToTextMessage(boolean marshalToTextMessage) {
        this.marshalToTextMessage = marshalToTextMessage;
    }

    /** Sets the {@link Marshaller} to be used by this message converter. */
    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /** Sets the {@link Marshaller} to be used by this message converter. */
    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(marshaller, "Property 'marshaller' is required");
        Assert.notNull(unmarshaller, "Property 'unmarshaller' is required");
    }

    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        Result result;
        Message message;
        if (marshalToTextMessage) {
            message = session.createTextMessage();
            result = new StringResult();
        }
        else {
            message = session.createBytesMessage();
            result = new StreamResult(new BytesMessageOutputStream((BytesMessage) message));
        }
        try {
            marshaller.marshal(object, result);
            if (marshalToTextMessage) {
                ((TextMessage) message).setText(result.toString());
            }
            return message;
        }
        catch (MessageConversionException ex) {
            handleMessageConversionException(ex);
            throw ex;
        }
        catch (IOException ex) {
            throw new MessageConversionException("Could not marshal message [" + message + "]", ex);
        }
    }

    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        Source source;
        if (message instanceof TextMessage) {
            source = new StringSource(((TextMessage) message).getText());
        }
        else if (message instanceof BytesMessage) {
            source = new StreamSource(new BytesMessageInputStream((BytesMessage) message));
        }
        else {
            throw new MessageConversionException(
                    "MarshallingMessageConverter only supports TextMessages and BytesMessages");
        }
        try {
            return unmarshaller.unmarshal(source);
        }
        catch (MessageConversionException ex) {
            handleMessageConversionException(ex);
            throw ex;
        }
        catch (IOException ex) {
            throw new MessageConversionException("Could not unmarshal message [" + message + "]", ex);
        }
    }

    private void handleMessageConversionException(MessageConversionException ex) throws JMSException {
        if (ex.getCause() instanceof JMSException) {
            throw (JMSException) ex.getCause();
        }
        else {
            throw ex;
        }
    }

    /** Input stream that wraps a {@link BytesMessage}. */
    private static class BytesMessageInputStream extends InputStream {

        private BytesMessage message;

        BytesMessageInputStream(BytesMessage message) {
            this.message = message;
        }

        public int read(byte b[]) throws IOException {
            try {
                return message.readBytes(b);
            }
            catch (JMSException ex) {
                throw new MessageConversionException("Could not read byte array", ex);
            }
        }

        public int read(byte b[], int off, int len) throws IOException {
            if (off == 0) {
                try {
                    return message.readBytes(b, len);
                }
                catch (JMSException ex) {
                    throw new MessageConversionException("Could not read byte array", ex);
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
                throw new MessageConversionException("Could not read byte", ex);
            }
        }
    }

    /** Output stream that wraps a {@link BytesMessage}. */
    private static class BytesMessageOutputStream extends OutputStream {

        private BytesMessage message;

        BytesMessageOutputStream(BytesMessage message) {
            this.message = message;
        }

        public void write(byte b[]) throws IOException {
            try {
                message.writeBytes(b);
            }
            catch (JMSException ex) {
                throw new MessageConversionException("Could not write byte array", ex);
            }
        }

        public void write(byte b[], int off, int len) throws IOException {
            try {
                message.writeBytes(b, off, len);
            }
            catch (JMSException ex) {
                throw new MessageConversionException("Could not write byte array", ex);
            }
        }

        public void write(int b) throws IOException {
            try {
                message.writeByte((byte) b);
            }
            catch (JMSException ex) {
                throw new MessageConversionException("Could not write byte", ex);
            }
        }
    }
}

