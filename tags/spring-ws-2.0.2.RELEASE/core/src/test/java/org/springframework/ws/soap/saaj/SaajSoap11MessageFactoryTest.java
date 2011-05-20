/*
 * Copyright 2002-2010 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageFactoryTestCase;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SaajSoap11MessageFactoryTest extends AbstractSoap11MessageFactoryTestCase {

    @Override
    protected WebServiceMessageFactory createMessageFactory() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        return new SaajSoapMessageFactory(messageFactory);
    }

    @Test
    public void properties() throws IOException {
        Map<String, ?> properties = Collections.singletonMap(SOAPMessage.WRITE_XML_DECLARATION, "true");
        ((SaajSoapMessageFactory)messageFactory).setMessageProperties(properties);
        SoapMessage soapMessage = (SoapMessage) messageFactory.createWebServiceMessage();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        soapMessage.writeTo(os);
        String result = os.toString("UTF-8");
        assertTrue("XML declaration not written", result.startsWith("<?xml version=\"1.0\""));
    }


}