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

package org.springframework.ws.endpoint;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.easymock.MockControl;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

public class MarshallingPayloadEndpointTest extends XMLTestCase {

    private Transformer transformer;

    private MessageContext context;

    private MockControl factoryControl;

    private WebServiceMessageFactory factoryMock;

    protected void setUp() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
        transformer = TransformerFactory.newInstance().newTransformer();
        factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        factoryMock = (WebServiceMessageFactory) factoryControl.getMock();

        context = new DefaultMessageContext(request, factoryMock);

    }

    public void testInvoke() throws Exception {
        Unmarshaller unmarshaller = new Unmarshaller() {
            public Object unmarshal(Source source) throws XmlMappingException {
                try {
                    StringWriter writer = new StringWriter();
                    transformer.transform(source, new StreamResult(writer));
                    assertXMLEqual("Invalid source", "<request/>", writer.toString());
                    return new Long(42);
                }
                catch (Exception e) {
                    fail(e.getMessage());
                    return null;
                }
            }
        };
        Marshaller marshaller = new Marshaller() {
            public void marshal(Object graph, Result result) throws XmlMappingException {
                assertEquals("Invalid graph", "result", graph);
                try {
                    transformer.transform(new StreamSource(new StringReader("<result/>")), result);
                }
                catch (TransformerException e) {
                    fail(e.getMessage());
                }
            }
        };
        AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {
            protected Object invokeInternal(Object requestObject) throws Exception {
                assertEquals("Invalid request object", new Long(42), requestObject);
                return "result";
            }
        };
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(unmarshaller);
        endpoint.afterPropertiesSet();

        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), new MockWebServiceMessage());
        factoryControl.replay();

        endpoint.invoke(context);
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        assertNotNull("Invalid result", response);
        assertXMLEqual("Invalid response", "<result/>", response.getPayloadAsString());

        factoryControl.verify();
    }

    public void testInvokeNullResponse() throws Exception {
        Unmarshaller unmarshaller = new Unmarshaller() {
            public Object unmarshal(Source source) throws XmlMappingException {
                try {
                    StringWriter writer = new StringWriter();
                    transformer.transform(source, new StreamResult(writer));
                    assertXMLEqual("Invalid source", "<request/>", writer.toString());
                    return new Long(42);
                }
                catch (Exception e) {
                    fail(e.getMessage());
                    return null;
                }
            }
        };
        Marshaller marshaller = new Marshaller() {
            public void marshal(Object graph, Result result) throws XmlMappingException {
                fail("marshal not expected");
            }
        };
        AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {
            protected Object invokeInternal(Object requestObject) throws Exception {
                assertEquals("Invalid request object", new Long(42), requestObject);
                return null;
            }
        };
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(unmarshaller);
        endpoint.afterPropertiesSet();
        factoryControl.replay();
        endpoint.invoke(context);
        assertFalse("Response created", context.hasResponse());
        factoryControl.verify();
    }

}
