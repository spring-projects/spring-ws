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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;

/**
 * SAAJ-specific implementation of the <code>SoapMessageContext</code> interface. Created by the
 * <code>SaajSoapMessageContextFactory</code>.
 *
 * @author Arjen Poutsma
 * @see SaajSoapMessageContextFactory
 */
public class SaajSoapMessageContext extends AbstractSoapMessageContext {

    private SaajSoapMessage request;

    private SaajSoapMessage response;

    private final MessageFactory messageFactory;

    /**
     * Creates a new instance based on the given SAAJ request message, and a message factory.
     *
     * @param request        the request message
     * @param messageFactory the message factory used for creating a response
     */
    public SaajSoapMessageContext(SOAPMessage request, MessageFactory messageFactory) {
        Assert.notNull(request);
        Assert.notNull(messageFactory);
        this.request = new SaajSoapMessage(request);
        this.messageFactory = messageFactory;
    }

    /**
     * Returns the request as a SAAJ SOAP message.
     */
    public SOAPMessage getSaajRequest() {
        return request.getSaajMessage();
    }

    /**
     * Sets the request to the given SAAJ SOAP message.
     */
    public void setSaajRequest(SOAPMessage request) {
        Assert.notNull(request);
        this.request = new SaajSoapMessage(request);
    }

    /**
     * Returns the response as a SAAJ SOAP message.
     */
    public SOAPMessage getSaajResponse() {
        return response != null ? response.getSaajMessage() : null;
    }

    /**
     * Sets the response to the given SAAJ SOAP message.
     */
    public void setSaajResponse(SOAPMessage response) {
        Assert.notNull(response);
        this.response = new SaajSoapMessage(response);
    }

    public SoapMessage getSoapResponse() {
        return response;
    }

    public SoapMessage getSoapRequest() {
        return request;
    }

    public SoapMessage createSoapResponseInternal() {
        try {
            SOAPMessage saajMessage = messageFactory.createMessage();
            response = new SaajSoapMessage(saajMessage);
            return response;
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create message: " + ex.toString(), ex);
        }
    }
}
