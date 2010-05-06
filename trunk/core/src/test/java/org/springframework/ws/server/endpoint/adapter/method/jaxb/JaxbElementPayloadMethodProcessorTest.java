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

package org.springframework.ws.server.endpoint.adapter.method.jaxb;

import java.io.IOException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @author Arjen Poutsma */
public class JaxbElementPayloadMethodProcessorTest {

    private JaxbElementPayloadMethodProcessor processor;

    private MethodParameter supportedParameter;

    private MethodParameter supportedReturnType;

    @Before
    public void setUp() throws Exception {
        processor = new JaxbElementPayloadMethodProcessor();
        supportedParameter = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), 0);
        supportedReturnType = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), -1);
    }

    @Test
    public void supportsParameter() {
        assertTrue("processor does not support @JAXBElement parameter",
                processor.supportsParameter(supportedParameter));
    }

    @Test
    public void supportsReturnType() {
        assertTrue("processor does not support @JAXBElement return type",
                processor.supportsReturnType(supportedReturnType));
    }

    @Test
    public void resolveArgument() throws JAXBException {
        WebServiceMessage request = new MockWebServiceMessage("<myType><string>Foo</string></myType>");
        MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        JAXBElement<?> result = processor.resolveArgument(messageContext, supportedParameter);
        assertTrue("result not a MyType", result.getValue() instanceof MyType);
        MyType type = (MyType) result.getValue();
        assertEquals("invalid result", "Foo", type.getString());
    }

    @Test
    public void handleReturnValue() throws JAXBException, IOException, SAXException {
        MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

        MyType type = new MyType();
        type.setString("Foo");
        JAXBElement<MyType> element = new JAXBElement<MyType>(new QName("type"), MyType.class, type);
        processor.handleReturnValue(messageContext, supportedReturnType, element);
        assertTrue("context has no response", messageContext.hasResponse());
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
        assertXMLEqual("<type><string>Foo</string></type>", response.getPayloadAsString());
    }

    

    @ResponsePayload
    public JAXBElement<MyType> supported(@RequestPayload JAXBElement<MyType> element) {
        return element;
    }

    @XmlType
    public static class MyType {

        private String string;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }

}
