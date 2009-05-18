/*
 * Copyright 2002-2009 the original author or authors.
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

import java.io.ByteArrayInputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageFactoryTestCase;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.xml.transform.StringResult;

public class AxiomSoap11MessageFactoryTest extends AbstractSoap11MessageFactoryTestCase {

    protected WebServiceMessageFactory createMessageFactory() throws Exception {
        AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
        factory.afterPropertiesSet();
        return factory;
    }

    public void testRepetitiveReadCaching() throws Exception {
        AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
        messageFactory.setPayloadCaching(true);
        messageFactory.afterPropertiesSet();

        String xml = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Body>" +
                "<root xmlns='http://springframework.org/spring-ws'><child /></root>" +
                "</soapenv:Body></soapenv:Envelope>";
        TransportInputStream tis = new MockTransportInputStream(new ByteArrayInputStream(xml.getBytes()));
        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(message.getPayloadSource(), result);
        transformer.transform(message.getPayloadSource(), result);
    }

    public void testRepetitiveReadNoCaching() throws Exception {
        AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
        messageFactory.setPayloadCaching(false);
        messageFactory.afterPropertiesSet();

        String xml = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Body>" +
                "<root xmlns='http://springframework.org/spring-ws'><child /></root>" +
                "</soapenv:Body></soapenv:Envelope>";
        TransportInputStream tis = new MockTransportInputStream(new ByteArrayInputStream(xml.getBytes()));
        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(message.getPayloadSource(), result);
        try {
            transformer.transform(message.getPayloadSource(), result);
            fail("TransformerException expected");
        }
        catch (TransformerException expected) {
            // ignore
        }
    }


}