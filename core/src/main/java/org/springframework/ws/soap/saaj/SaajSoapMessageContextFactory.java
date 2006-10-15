/*
 * Copyright 2005 the original author or authors.
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
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.soap.saaj.saaj12.Saaj12SoapMessageContext;
import org.springframework.ws.soap.saaj.saaj13.Saaj13SoapMessageContext;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportRequest;

/**
 * SAAJ-specific implementation of the <code>MessageContextFactory</code> interface. Creates a
 * <code>SaajSoapMessageContext</code>. This factory will use SAAJ 1.3 when found, or fall back to SAAJ 1.2.
 *
 * @author Arjen Poutsma
 * @see SaajSoapMessageContext
 */
public class SaajSoapMessageContextFactory implements MessageContextFactory, InitializingBean {

    private static final Log logger = LogFactory.getLog(SaajSoapMessageContextFactory.class);

    private MessageFactory messageFactory;

    private String messageFactoryProtocol;

    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Sets the protocol for the <code>MessageFactory</code>. Only used for SAAJ 1.3+, defaults to
     * <code>SOAPConstants.DEFAULT_SOAP_PROTOCOL</code> (i.e. SOAP 1.1).
     *
     * @see MessageFactory#newInstance(String)
     * @see SOAPConstants#DEFAULT_SOAP_PROTOCOL
     * @see SOAPConstants#SOAP_1_1_PROTOCOL
     * @see SOAPConstants#SOAP_1_2_PROTOCOL
     * @see SOAPConstants#DYNAMIC_SOAP_PROTOCOL
     */
    public void setSoapProtocol(String messageFactoryProtocol) {
        this.messageFactoryProtocol = messageFactoryProtocol;
    }

    public void afterPropertiesSet() throws Exception {
        if (messageFactory == null) {
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
                    throw new IllegalStateException("SaajSoapMessageContextFactory requires SAAJ 1.2, which was not" +
                            "found on the classpath");
                }
            }
            catch (SOAPException ex) {
                throw new SoapMessageCreationException("Could not create MessageFactory: " + ex.getMessage(), ex);
            }
        }
    }

    public MessageContext createContext(TransportContext transportContext) throws IOException {
        TransportRequest transportRequest = transportContext.getTransportRequest();
        MimeHeaders mimeHeaders = new MimeHeaders();
        for (Iterator headerNames = transportRequest.getHeaderNames(); headerNames.hasNext();) {
            String headerName = (String) headerNames.next();
            for (Iterator headerValues = transportRequest.getHeaders(headerName); headerValues.hasNext();) {
                String headerValue = (String) headerValues.next();
                StringTokenizer tokenizer = new StringTokenizer(headerValue, ",");
                while (tokenizer.hasMoreTokens()) {
                    mimeHeaders.addHeader(headerName, tokenizer.nextToken().trim());
                }
            }
        }
        try {
            SOAPMessage requestMessage = messageFactory.createMessage(mimeHeaders, transportRequest.getInputStream());
            if (SaajUtils.getSaajVersion() >= SaajUtils.SAAJ_13) {
                return new Saaj13SoapMessageContext(requestMessage, transportRequest, messageFactory);
            }
            else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_12) {
                return new Saaj12SoapMessageContext(requestMessage, transportRequest, messageFactory);
            }
            else {
                throw new IllegalStateException(
                        "SaajSoapMessageContextFactory requires SAAJ 1.2, which was not" + "found on the classpath");
            }
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create message from TransportRequest: " + ex.getMessage(),
                    ex);
        }
    }

}
