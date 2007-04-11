/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter;

import javax.xml.transform.Source;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathParamAnnotationEndpointAdapterTest extends TestCase {

    private static final String CONTENTS = "<root><child><text>text</text><number>42.0</number></child></root>";

    private XPathParamAnnotationEndpointAdapter adapter;

    private boolean supportedTypesInvoked = false;

    private boolean supportedSourceInvoked;

    protected void setUp() throws Exception {
        adapter = new XPathParamAnnotationEndpointAdapter();
        adapter.afterPropertiesSet();
    }

    public void testUnsupportedInvalidParam() throws NoSuchMethodException {
        MethodEndpoint endpoint = new MethodEndpoint(this, "unsupportedInvalidParamType", new Class[]{Integer.TYPE});
        assertFalse("Method supported", adapter.supports(endpoint));
    }

    public void testUnsupportedInvalidReturnType() throws NoSuchMethodException {
        MethodEndpoint endpoint = new MethodEndpoint(this, "unsupportedInvalidReturnType", new Class[]{String.class});
        assertFalse("Method supported", adapter.supports(endpoint));
    }

    public void testUnsupportedInvalidParams() throws NoSuchMethodException {
        MethodEndpoint endpoint =
                new MethodEndpoint(this, "unsupportedInvalidParams", new Class[]{String.class, String.class});
        assertFalse("Method supported", adapter.supports(endpoint));
    }

    public void testSupportedTypes() throws NoSuchMethodException {
        MethodEndpoint endpoint = new MethodEndpoint(this, "supportedTypes",
                new Class[]{Boolean.TYPE, Double.TYPE, Node.class, NodeList.class, String.class});
        assertTrue("Not all types supported", adapter.supports(endpoint));
    }

    public void testSupportsStringSource() throws NoSuchMethodException {
        MethodEndpoint endpoint = new MethodEndpoint(this, "supportedStringSource", new Class[]{String.class});
        assertTrue("StringSource method not supported", adapter.supports(endpoint));
    }

    public void testSupportsSource() throws NoSuchMethodException {
        MethodEndpoint endpoint = new MethodEndpoint(this, "supportedSource", new Class[]{String.class});
        assertTrue("Source method not supported", adapter.supports(endpoint));
    }

    public void testSupportsVoid() throws NoSuchMethodException {
        MethodEndpoint endpoint = new MethodEndpoint(this, "supportedVoid", new Class[]{String.class});
        assertTrue("void method not supported", adapter.supports(endpoint));
    }

    public void testInvokeTypes() throws Exception {
        MockControl messageControl = MockControl.createControl(WebServiceMessage.class);
        WebServiceMessage messageMock = (WebServiceMessage) messageControl.getMock();
        messageControl.expectAndReturn(messageMock.getPayloadSource(), new StringSource(CONTENTS));
        messageControl.replay();
        MockControl factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        WebServiceMessageFactory factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        factoryControl.replay();

        MessageContext messageContext = new DefaultMessageContext(messageMock, factoryMock);
        MethodEndpoint endpoint = new MethodEndpoint(this, "supportedTypes",
                new Class[]{Boolean.TYPE, Double.TYPE, Node.class, NodeList.class, String.class});
        adapter.invoke(messageContext, endpoint);
        assertTrue("Method not invoked", supportedTypesInvoked);

        messageControl.verify();
        factoryControl.verify();

    }

    public void testInvokeSource() throws Exception {
        MockControl messageControl = MockControl.createControl(WebServiceMessage.class);
        WebServiceMessage requestMock = (WebServiceMessage) messageControl.getMock();
        WebServiceMessage responseMock = (WebServiceMessage) messageControl.getMock();
        messageControl.expectAndReturn(requestMock.getPayloadSource(), new StringSource(CONTENTS));
        messageControl.expectAndReturn(responseMock.getPayloadResult(), new StringResult());
        messageControl.replay();
        MockControl factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        WebServiceMessageFactory factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), responseMock);
        factoryControl.replay();

        MessageContext messageContext = new DefaultMessageContext(requestMock, factoryMock);
        MethodEndpoint endpoint = new MethodEndpoint(this, "supportedSource", new Class[]{String.class});
        adapter.invoke(messageContext, endpoint);
        assertTrue("Method not invoked", supportedSourceInvoked);

        messageControl.verify();
        factoryControl.verify();
    }

    public void supportedVoid(@XPathParam("/")String param1) {
    }

    public Source supportedSource(@XPathParam("/")String param1) {
        supportedSourceInvoked = true;
        return new StringSource("<response/>");
    }

    public StringSource supportedStringSource(@XPathParam("/")String param1) {
        return null;
    }

    public void supportedTypes(@XPathParam("/root/child")boolean param1,
                               @XPathParam("/root/child/number")double param2,
                               @XPathParam("/root/child")Node param3,
                               @XPathParam("/root/*")NodeList param4,
                               @XPathParam("/root/child/text")String param5) {
        supportedTypesInvoked = true;
        assertTrue("Invalid boolean value", param1);
        assertEquals("Invalid double value", 42D, param2, 0.00001D);
        assertEquals("Invalid Node value", "child", param3.getLocalName());
        assertEquals("Invalid NodeList value", 1, param4.getLength());
        assertEquals("Invalid Node value", "child", param4.item(0).getLocalName());
        assertEquals("Invalid Node value", "text", param5);
    }

    public void unsupportedInvalidParams(@XPathParam("/")String param1, String param2) {

    }

    public String unsupportedInvalidReturnType(@XPathParam("/")String param1) {
        return null;
    }

    public void unsupportedInvalidParamType(@XPathParam("/")int param1) {
    }
}