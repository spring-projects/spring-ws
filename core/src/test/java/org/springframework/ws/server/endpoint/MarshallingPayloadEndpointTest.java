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

package org.springframework.ws.server.endpoint;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.oxm.mime.MimeMarshaller;
import org.springframework.oxm.mime.MimeUnmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

public class MarshallingPayloadEndpointTest {

    private Transformer transformer;

    private MessageContext context;

    private WebServiceMessageFactory factoryMock;

    @Before
    public void setUp() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
        transformer = TransformerFactory.newInstance().newTransformer();
        factoryMock = createMock(WebServiceMessageFactory.class);

        context = new DefaultMessageContext(request, factoryMock);
    }

    @Test
    public void testInvoke() throws Exception {
        Unmarshaller unmarshaller = new SimpleMarshaller() {
            @Override
            public Object unmarshal(Source source) throws XmlMappingException {
                try {
                    StringWriter writer = new StringWriter();
                    transformer.transform(source, new StreamResult(writer));
                    assertXMLEqual("Invalid source", "<request/>", writer.toString());
                    return 42L;
                }
                catch (Exception e) {
                    Assert.fail(e.getMessage());
                    return null;
                }
            }
        };
        Marshaller marshaller = new SimpleMarshaller() {
            @Override
            public void marshal(Object graph, Result result) throws XmlMappingException {
                Assert.assertEquals("Invalid graph", "result", graph);
                try {
                    transformer.transform(new StreamSource(new StringReader("<result/>")), result);
                }
                catch (TransformerException e) {
                    Assert.fail(e.getMessage());
                }
            }
        };
        AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {
            @Override
            protected Object invokeInternal(Object requestObject) throws Exception {
                Assert.assertEquals("Invalid request object", 42L, requestObject);
                return "result";
            }
        };
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(unmarshaller);
        endpoint.afterPropertiesSet();

        expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

        replay(factoryMock);

        endpoint.invoke(context);
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        Assert.assertNotNull("Invalid result", response);
        assertXMLEqual("Invalid response", "<result/>", response.getPayloadAsString());

        verify(factoryMock);
    }

    @Test
    public void testInvokeNullResponse() throws Exception {
        Unmarshaller unmarshaller = new SimpleMarshaller() {
            @Override
            public Object unmarshal(Source source) throws XmlMappingException {
                try {
                    StringWriter writer = new StringWriter();
                    transformer.transform(source, new StreamResult(writer));
                    assertXMLEqual("Invalid source", "<request/>", writer.toString());
                    return (long) 42;
                }
                catch (Exception e) {
                    Assert.fail(e.getMessage());
                    return null;
                }
            }
        };
        Marshaller marshaller = new SimpleMarshaller() {
            @Override
            public void marshal(Object graph, Result result) throws XmlMappingException {
                Assert.fail("marshal not expected");
            }
        };
        AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {
            @Override
            protected Object invokeInternal(Object requestObject) throws Exception {
                Assert.assertEquals("Invalid request object", (long) 42, requestObject);
                return null;
            }
        };
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(unmarshaller);
        endpoint.afterPropertiesSet();
        replay(factoryMock);
        endpoint.invoke(context);
        Assert.assertFalse("Response created", context.hasResponse());
        verify(factoryMock);
    }

    @Test
    public void testInvokeNoRequest() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage((StringBuffer) null);
        context = new DefaultMessageContext(request, factoryMock);
        AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {

            @Override
            protected Object invokeInternal(Object requestObject) throws Exception {
                Assert.assertNull("No request expected", requestObject);
                return null;
            }
        };
        endpoint.setMarshaller(new SimpleMarshaller());
        endpoint.setUnmarshaller(new SimpleMarshaller());
        endpoint.afterPropertiesSet();
        replay(factoryMock);
        endpoint.invoke(context);
        Assert.assertFalse("Response created", context.hasResponse());
        verify(factoryMock);
    }

    @Test
    public void testInvokeMimeMarshaller() throws Exception {
        MimeUnmarshaller unmarshaller = createMock(MimeUnmarshaller.class);
        MimeMarshaller marshaller = createMock(MimeMarshaller.class);
        MimeMessage request = createMock("request", MimeMessage.class);
        MimeMessage response = createMock("response", MimeMessage.class);
        Source requestSource = new StringSource("<request/>");
        expect(request.getPayloadSource()).andReturn(requestSource);
        expect(factoryMock.createWebServiceMessage()).andReturn(response);
        expect(unmarshaller.unmarshal(eq(requestSource), isA(MimeContainer.class))).andReturn(42L);
        Result responseResult = new StringResult();
        expect(response.getPayloadResult()).andReturn(responseResult);
        marshaller.marshal(eq("result"), eq(responseResult), isA(MimeContainer.class));

        replay(factoryMock, unmarshaller, marshaller, request, response);

        AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {
            @Override
            protected Object invokeInternal(Object requestObject) throws Exception {
                Assert.assertEquals("Invalid request object", 42L, requestObject);
                return "result";
            }
        };
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(unmarshaller);
        endpoint.afterPropertiesSet();

        context = new DefaultMessageContext(request, factoryMock);
        endpoint.invoke(context);
        Assert.assertNotNull("Invalid result", response);

        verify(factoryMock, unmarshaller, marshaller, request, response);
    }

    private static class SimpleMarshaller implements Marshaller, Unmarshaller {

        public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
            fail("Not expected");
        }

        public Object unmarshal(Source source) throws XmlMappingException, IOException {
            fail("Not expected");
            return null;
        }

        public boolean supports(Class<?> clazz) {
            return false;
        }
    }

}
