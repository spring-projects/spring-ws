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

package org.springframework.ws.soap.saaj;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.transport.TransportInputStream;

/**
 * SAAJ-specific implementation of the {@link org.springframework.ws.WebServiceMessageFactory WebServiceMessageFactory}.
 * This factory will use SAAJ 1.3 when found, or fall back to SAAJ 1.2.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.saaj.SaajSoapMessage
 */
public class SaajSoapMessageFactory implements WebServiceMessageFactory, InitializingBean {

    private static final Log logger = LogFactory.getLog(SaajSoapMessageFactory.class);

    private MessageFactory messageFactory;

    private String messageFactoryProtocol;

    /**
     * Default, empty constructor.
     */
    public SaajSoapMessageFactory() {
    }

    /**
     * Constructor that takes a message factory as an argument.
     */
    public SaajSoapMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Sets the SAAJ <code>MessageFactory</code>.
     */
    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Returns the SAAJ <code>MessageFactory</code> used.
     */
    public MessageFactory getSaajMessageFactory() {
        return messageFactory;
    }

    public void afterPropertiesSet() throws Exception {
        if (messageFactory != null) {
            return;
        }
        try {
            if (SaajUtils.getSaajVersion() >= SaajUtils.SAAJ_13) {
                if (!StringUtils.hasLength(messageFactoryProtocol)) {
                    messageFactoryProtocol = SOAPConstants.DEFAULT_SOAP_PROTOCOL;
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Creating SAAJ 1.3 MessageFactory with " + messageFactoryProtocol);
                }
                messageFactory = MessageFactory.newInstance(messageFactoryProtocol);
            }
            else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_12) {
                if (logger.isInfoEnabled()) {
                    logger.info("Creating SAAJ 1.2 MessageFactory");
                }
                messageFactory = MessageFactory.newInstance();
            }
            else {
                throw new IllegalStateException(
                        "SaajSoapMessageFactory requires SAAJ 1.2, which was not" + "found on the classpath");
            }
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create MessageFactory: " + ex.getMessage(), ex);
        }
    }

    public WebServiceMessage createWebServiceMessage() {
        try {
            return new SaajSoapMessage(messageFactory.createMessage());
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create empty message: " + ex.getMessage(), ex);
        }
    }

    public WebServiceMessage createWebServiceMessage(InputStream inputStream) throws IOException {
        MimeHeaders mimeHeaders = new MimeHeaders();
        if (inputStream instanceof TransportInputStream) {
            TransportInputStream transportInputStream = (TransportInputStream) inputStream;
            for (Iterator headerNames = transportInputStream.getHeaderNames(); headerNames.hasNext();) {
                String headerName = (String) headerNames.next();
                for (Iterator headerValues = transportInputStream.getHeaders(headerName); headerValues.hasNext();) {
                    String headerValue = (String) headerValues.next();
                    StringTokenizer tokenizer = new StringTokenizer(headerValue, ",");
                    while (tokenizer.hasMoreTokens()) {
                        mimeHeaders.addHeader(headerName, tokenizer.nextToken().trim());
                    }
                }
            }
        }
        try {
            return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, inputStream));
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create message from InputStream: " + ex.getMessage(), ex);
        }
    }

    /**
     * Sets the protocol for the <code>MessageFactory</code>. Only used for SAAJ 1.3+, defaults to
     * <code>SOAPConstants.DEFAULT_SOAP_PROTOCOL</code> (i.e. SOAP 1.1).
     *
     * @see MessageFactory#newInstance(String)
     * @see javax.xml.soap.SOAPConstants#DEFAULT_SOAP_PROTOCOL
     * @see javax.xml.soap.SOAPConstants#SOAP_1_1_PROTOCOL
     * @see javax.xml.soap.SOAPConstants#SOAP_1_2_PROTOCOL
     * @see javax.xml.soap.SOAPConstants#DYNAMIC_SOAP_PROTOCOL
     */
    public void setSoapProtocol(String messageFactoryProtocol) {
        this.messageFactoryProtocol = messageFactoryProtocol;
    }
}
