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

package org.springframework.ws.mock.client2;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;

public class SoapHeaderMatcherTest {

    private SoapHeaderMatcher matcher;

    private QName expectedHeaderName;

    @Before
    public void setUp() throws Exception {
        expectedHeaderName = new QName("http://example.com", "header");
        matcher = new SoapHeaderMatcher(expectedHeaderName);
    }

    @Test
    public void match() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        saajMessage.getSOAPHeader().addHeaderElement(expectedHeaderName);
        SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

        matcher.match(null, soapMessage);
    }

    @Test(expected = AssertionError.class)
    public void nonMatch() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

        matcher.match(null, soapMessage);
    }

    @Test(expected = AssertionError.class)
    public void nonSoap() throws Exception {
        WebServiceMessage message = createMock(WebServiceMessage.class);

        matcher.match(null, message);
    }

}