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

package org.springframework.ws.soap.axiom;

import org.apache.axiom.soap.SOAPFactory;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;

/**
 * AXIOM-specific implementation of the <code>SoapMessageContext</code> interface. Created by the
 * <code>AxiomSoapMessageContextFactory</code>.
 *
 * @author Arjen Poutsma
 * @see AxiomSoapMessageContextFactory
 */
public class AxiomSoapMessageContext extends AbstractSoapMessageContext {

    private final SOAPFactory soapFactory;

    private final AxiomSoapMessage request;

    private AxiomSoapMessage response;

    public AxiomSoapMessageContext(AxiomSoapMessage request, SOAPFactory soapFactory) {
        this.soapFactory = soapFactory;
        this.request = request;
    }

    public SoapMessage getSoapRequest() {
        return request;
    }

    public SoapMessage createSoapResponseInternal() {
        response = new AxiomSoapMessage(soapFactory);
        return response;
    }

    public SoapMessage getSoapResponse() {
        return response;
    }
}
