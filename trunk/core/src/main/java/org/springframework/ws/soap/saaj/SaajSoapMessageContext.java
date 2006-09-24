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
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;
import org.springframework.ws.transport.TransportRequest;
import org.springframework.ws.transport.TransportResponse;

/**
 * SAAJ-specific implementation of the <code>SoapMessageContext</code> interface. Created by the
 * <code>SaajSoapMessageContextFactory</code>.
 *
 * @author Arjen Poutsma
 * @see SaajSoapMessageContextFactory
 */
public class SaajSoapMessageContext extends AbstractSoapMessageContext {

    private final MessageFactory messageFactory;

    /**
     * Creates a new instance based on the given SAAJ request message, and a message factory.
     *
     * @param request          the request message
     * @param transportRequest the transport request
     * @param messageFactory   the message factory used for creating a response
     */
    public SaajSoapMessageContext(SOAPMessage request,
                                  TransportRequest transportRequest,
                                  MessageFactory messageFactory) {
        super(new SaajSoapMessage(request), transportRequest);
        Assert.notNull(messageFactory);
        this.messageFactory = messageFactory;
    }

    /**
     * Returns the request as a SAAJ SOAP message.
     */
    public SOAPMessage getSaajRequest() {
        return ((SaajSoapMessage) getSoapRequest()).getSaajMessage();
    }

    /**
     * Sets the request to the given SAAJ SOAP message.
     */
    public void setSaajRequest(SOAPMessage request) {
        setRequest(new SaajSoapMessage(request));
    }

    /**
     * Returns the response as a SAAJ SOAP message.
     */
    public SOAPMessage getSaajResponse() {
        return ((SaajSoapMessage) getSoapResponse()).getSaajMessage();
    }

    /**
     * Sets the response to the given SAAJ SOAP message.
     */
    public void setSaajResponse(SOAPMessage response) {
        setResponse(new SaajSoapMessage(response));
    }

    public void sendResponse(TransportResponse transportResponse) throws IOException {
        if (hasResponse()) {
            SOAPMessage response = getSaajResponse();
            try {
                if (response.saveRequired()) {
                    response.saveChanges();
                }
                // some SAAJ implementations (Axis 1) do not have a Content-Type header by default
                MimeHeaders headers = response.getMimeHeaders();
                if (ObjectUtils.isEmpty(headers.getHeader("Content-Type"))) {
                    headers.addHeader("Content-Type", getSoapResponse().getVersion().getContentType());
                    if (response.saveRequired()) {
                        response.saveChanges();
                    }
                }
                for (Iterator iterator = headers.getAllHeaders(); iterator.hasNext();) {
                    MimeHeader mimeHeader = (MimeHeader) iterator.next();
                    transportResponse.addHeader(mimeHeader.getName(), mimeHeader.getValue());
                }
                response.writeTo(transportResponse.getOutputStream());
            }
            catch (SOAPException ex) {
                throw new SaajSoapMessageException("Could not write message to TransportResponse: " + ex.getMessage(),
                        ex);
            }

        }
    }

    protected SoapMessage createResponseSoapMessage() {
        try {
            SOAPMessage saajMessage = messageFactory.createMessage();
            return new SaajSoapMessage(saajMessage);
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create message: " + ex.toString(), ex);
        }
    }
}
