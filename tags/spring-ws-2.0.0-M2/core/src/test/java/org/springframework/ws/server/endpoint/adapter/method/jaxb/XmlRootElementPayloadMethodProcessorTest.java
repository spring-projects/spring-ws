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
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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

public class XmlRootElementPayloadMethodProcessorTest {

    private XmlRootElementPayloadMethodProcessor processor;

    private MethodParameter rootElementParameter;

    private MethodParameter typeParameter;

    private MethodParameter rootElementReturnType;

    @Before
    public void setUp() throws Exception {
        processor = new XmlRootElementPayloadMethodProcessor();
        rootElementParameter = new MethodParameter(getClass().getMethod("rootElement", MyRootElement.class), 0);
        typeParameter = new MethodParameter(getClass().getMethod("type", MyType.class), 0);
        rootElementReturnType = new MethodParameter(getClass().getMethod("rootElement", MyRootElement.class), -1);
    }

    @Test
    public void supportsParameter() {
        assertTrue("processor does not support @XmlRootElement parameter",
                processor.supportsParameter(rootElementParameter));
        assertTrue("processor does not support @XmlType parameter", processor.supportsParameter(typeParameter));
    }

    @Test
    public void supportsReturnType() {
        assertTrue("processor does not support @XmlRootElement return type",
                processor.supportsReturnType(rootElementReturnType));
    }

    @Test
    public void resolveArgumentRootElement() throws JAXBException {
        WebServiceMessage request = new MockWebServiceMessage("<myRootElement><string>Foo</string></myRootElement>");
        MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        Object result = processor.resolveArgument(messageContext, rootElementParameter);
        assertTrue("result not a MyRootElement", result instanceof MyRootElement);
        MyRootElement rootElement = (MyRootElement) result;
        assertEquals("invalid result", "Foo", rootElement.getString());
    }

    @Test
    public void resolveArgumentType() throws JAXBException {
        WebServiceMessage request = new MockWebServiceMessage("<myType><string>Foo</string></myType>");
        MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        Object result = processor.resolveArgument(messageContext, typeParameter);
        assertTrue("result not a MyType", result instanceof MyType);
        MyType type = (MyType) result;
        assertEquals("invalid result", "Foo", type.getString());
    }

    @Test
    public void handleReturnValue() throws JAXBException, IOException, SAXException {
        MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

        MyRootElement rootElement = new MyRootElement();
        rootElement.setString("Foo");
        processor.handleReturnValue(messageContext, rootElementReturnType, rootElement);
        assertTrue("context has no response", messageContext.hasResponse());
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
        assertXMLEqual("<myRootElement><string>Foo</string></myRootElement>", response.getPayloadAsString());
    }

    @ResponsePayload
    public MyRootElement rootElement(@RequestPayload MyRootElement rootElement) {
        return rootElement;
    }

    public void type(@RequestPayload MyType type) {
    }

    @XmlRootElement
    public static class MyRootElement {

        private String string;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
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
