/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.transform.TransformerException;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerObjectSupport;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;

/** @author Arjen Poutsma */
public class AbstractMethodArgumentResolverTest extends TransformerObjectSupport {

    protected static final String NAMESPACE_URI = "http://springframework.org/ws";

    protected static final String LOCAL_NAME = "request";

    protected static final String XML = "<" + LOCAL_NAME + " xmlns=\"" + NAMESPACE_URI + "\"/>";

    protected MessageContext createSaajMessageContext() throws javax.xml.soap.SOAPException {
        javax.xml.soap.MessageFactory saajFactory = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPMessage saajMessage = saajFactory.createMessage();
        saajMessage.getSOAPBody().addChildElement(LOCAL_NAME, "", NAMESPACE_URI);
        return new DefaultMessageContext(new SaajSoapMessage(saajMessage), new SaajSoapMessageFactory(saajFactory));
    }

    protected MessageContext createMockMessageContext() throws TransformerException {
        MockWebServiceMessage request = new MockWebServiceMessage(new StringSource(XML));
        return new DefaultMessageContext(request, new MockWebServiceMessageFactory());
    }

    protected MessageContext createCachingAxiomMessageContext() throws Exception {
        SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
        AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory, true, false);
        transform(new StringSource(XML), request.getPayloadResult());
        AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
        soapMessageFactory.afterPropertiesSet();
        return new DefaultMessageContext(request, soapMessageFactory);
    }

    protected MessageContext createNonCachingAxiomMessageContext() throws Exception {
        SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
        AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory, false, false);
        transform(new StringSource(XML), request.getPayloadResult());
        AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
        soapMessageFactory.setPayloadCaching(false);
        soapMessageFactory.afterPropertiesSet();
        return new DefaultMessageContext(request, soapMessageFactory);
    }
}
