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

    private MessageFactory messageFactory;

    /**
     * Creates a new instance based on the given SAAJ request message, and a message factory.
     *
     * @param request        the request message
     * @param messageFactory the message factory used for creating a response
     */
    public SaajSoapMessageContext(SOAPMessage request, MessageFactory messageFactory) {
        this.request = new SaajSoapMessage(request);
        this.messageFactory = messageFactory;
    }

    public SoapMessage getSoapRequest() {
        return request;
    }

    public SoapMessage createSoapResponse() {
        if (response != null) {
            throw new IllegalStateException("Response already created");
        }
        try {
            SOAPMessage saajMessage = messageFactory.createMessage();
            response = new SaajSoapMessage(saajMessage);
            return response;
        }
        catch (SOAPException ex) {
            throw new SoapMessageCreationException("Could not create message: " + ex.toString(), ex);
        }
    }

    public SoapMessage getSoapResponse() {
        return response;
    }

    /**
     * Sets the response to the given SAAJ SOAP message.
     *
     * @param response the response
     */
    public void setSaajResponse(SOAPMessage response) {
        if (this.response != null) {
            throw new IllegalStateException("Response already created");
        }
        this.response = new SaajSoapMessage(response);
    }
}
