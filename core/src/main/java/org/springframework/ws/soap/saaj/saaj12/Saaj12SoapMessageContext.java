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

package org.springframework.ws.soap.saaj.saaj12;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageContext;
import org.springframework.ws.transport.TransportRequest;

/**
 * SAAJ 1.2 specific implementation of the <code>SoapMessageContext</code> interface. Created by the
 * <code>SaajSoapMessageContextFactory</code>.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.saaj.SaajSoapMessageContextFactory
 */
public class Saaj12SoapMessageContext extends SaajSoapMessageContext {

    /**
     * Creates a new instance based on the given SAAJ request message, and a message factory.
     *
     * @param request          the request message
     * @param transportRequest the transport request
     * @param messageFactory   the message factory used for creating a response
     */
    public Saaj12SoapMessageContext(SOAPMessage request,
                                    TransportRequest transportRequest,
                                    MessageFactory messageFactory) {
        super(new Saaj12SoapMessage(request), transportRequest, messageFactory);
    }

    protected SaajSoapMessage createSaajSoapMessage(SOAPMessage saajMessage) {
        return new Saaj12SoapMessage(saajMessage);
    }

    public void setSaajRequest(SOAPMessage request) {
        setRequest(new Saaj12SoapMessage(request));
    }

    public void setSaajResponse(SOAPMessage response) {
        setResponse(new Saaj12SoapMessage(response));
    }
}
