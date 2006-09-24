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
import org.easymock.classextension.MockClassControl;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.ws.mock.soap.MockSoapMessage;
import org.springframework.ws.mock.soap.MockSoapMessageContext;

public class MarshallingPayloadEndpointTest extends XMLTestCase {

    private MockControl endpointControl;

    private AbstractMarshallingPayloadEndpoint endpointMock;

    protected void setUp() throws Exception {
        endpointControl = MockClassControl.createControl(AbstractMarshallingPayloadEndpoint.class);
        endpointMock = (AbstractMarshallingPayloadEndpoint) endpointControl.getMock();

        endpointControl.reset();
    }

    public void testInvoke() throws Exception {
        MockSoapMessage request = new MockSoapMessage();
        request.setPayload("<request/>");
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();

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
        endpointMock.setMarshaller(marshaller);
        endpointMock.setUnmarshaller(unmarshaller);
        endpointMock.afterPropertiesSet();
        endpointControl.reset();

        endpointControl.expectAndReturn(endpointMock.invokeInternal(new Long(42)), "result");
        endpointControl.replay();
        MockSoapMessageContext context = new MockSoapMessageContext(request);
        endpointMock.invoke(context);
        MockSoapMessage response = (MockSoapMessage) context.getResponse();
        assertNotNull("Invalid result", response);
        assertXMLEqual("Invalid response", "<result/>", response.getPayloadAsString());
        endpointControl.verify();
    }

    public void testInvokeNullResponse() throws Exception {
        MockSoapMessage request = new MockSoapMessage();
        request.setPayload("<request/>");
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();

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
        endpointMock.setMarshaller(marshaller);
        endpointMock.setUnmarshaller(unmarshaller);
        endpointMock.afterPropertiesSet();
        endpointControl.reset();
        endpointControl.expectAndReturn(endpointMock.invokeInternal(new Long(42)), null);
        endpointControl.replay();
        MockSoapMessageContext context = new MockSoapMessageContext(request);
        endpointMock.invoke(context);
        MockSoapMessage response = (MockSoapMessage) context.getResponse();
        assertNull("Invalid result", response);
        endpointControl.verify();
    }

}
