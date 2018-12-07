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

package org.springframework.ws.server.endpoint.adapter;

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.DocumentBuilderFactoryUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import static org.easymock.EasyMock.*;

public class XPathParamAnnotationMethodEndpointAdapterTest {

	private static final String CONTENTS = "<root><child><text>text</text><number>42.0</number></child></root>";

	private XPathParamAnnotationMethodEndpointAdapter adapter;

	private boolean supportedTypesInvoked = false;

	private boolean supportedSourceInvoked;

	private boolean namespacesInvoked;

	@Before
	public void setUp() throws Exception {
		adapter = new XPathParamAnnotationMethodEndpointAdapter();
		adapter.afterPropertiesSet();
	}

	@Test
	public void testUnsupportedInvalidParam() throws NoSuchMethodException {
		MethodEndpoint endpoint = new MethodEndpoint(this, "unsupportedInvalidParamType", new Class[]{Integer.TYPE});
		Assert.assertFalse("Method supported", adapter.supports(endpoint));
	}

	@Test
	public void testUnsupportedInvalidReturnType() throws NoSuchMethodException {
		MethodEndpoint endpoint = new MethodEndpoint(this, "unsupportedInvalidReturnType", new Class[]{String.class});
		Assert.assertFalse("Method supported", adapter.supports(endpoint));
	}

	@Test
	public void testUnsupportedInvalidParams() throws NoSuchMethodException {
		MethodEndpoint endpoint =
				new MethodEndpoint(this, "unsupportedInvalidParams", new Class[]{String.class, String.class});
		Assert.assertFalse("Method supported", adapter.supports(endpoint));
	}

	@Test
	public void testSupportedTypes() throws NoSuchMethodException {
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedTypes",
				new Class[]{Boolean.TYPE, Double.TYPE, Node.class, NodeList.class, String.class});
		Assert.assertTrue("Not all types supported", adapter.supports(endpoint));
	}

	@Test
	public void testSupportsStringSource() throws NoSuchMethodException {
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedStringSource", new Class[]{String.class});
		Assert.assertTrue("StringSource method not supported", adapter.supports(endpoint));
	}

	@Test
	public void testSupportsSource() throws NoSuchMethodException {
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedSource", new Class[]{String.class});
		Assert.assertTrue("Source method not supported", adapter.supports(endpoint));
	}

	@Test
	public void testSupportsVoid() throws NoSuchMethodException {
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedVoid", new Class[]{String.class});
		Assert.assertTrue("void method not supported", adapter.supports(endpoint));
	}

	@Test
	public void testInvokeTypes() throws Exception {
		WebServiceMessage messageMock = createMock(WebServiceMessage.class);
		expect(messageMock.getPayloadSource()).andReturn(new StringSource(CONTENTS));
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		replay(messageMock, factoryMock);

		MessageContext messageContext = new DefaultMessageContext(messageMock, factoryMock);
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedTypes",
				new Class[]{Boolean.TYPE, Double.TYPE, Node.class, NodeList.class, String.class});
		adapter.invoke(messageContext, endpoint);
		Assert.assertTrue("Method not invoked", supportedTypesInvoked);

		verify(messageMock, factoryMock);
	}

	@Test
	public void testInvokeSource() throws Exception {
		WebServiceMessage requestMock = createMock(WebServiceMessage.class);
		WebServiceMessage responseMock = createMock(WebServiceMessage.class);
		expect(requestMock.getPayloadSource()).andReturn(new StringSource(CONTENTS));
		expect(responseMock.getPayloadResult()).andReturn(new StringResult());
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
		replay(requestMock, responseMock, factoryMock);

		MessageContext messageContext = new DefaultMessageContext(requestMock, factoryMock);
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedSource", new Class[]{String.class});
		adapter.invoke(messageContext, endpoint);
		Assert.assertTrue("Method not invoked", supportedSourceInvoked);

		verify(requestMock, responseMock, factoryMock);
	}

	@Test
	public void testInvokeVoidDom() throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		String rootNamespace = "http://rootnamespace";
		Element rootElement = document.createElementNS(rootNamespace, "root");
		document.appendChild(rootElement);
		String childNamespace = "http://childnamespace";
		Element first = document.createElementNS(childNamespace, "child");
		rootElement.appendChild(first);
		Text text = document.createTextNode("value");
		first.appendChild(text);
		Element second = document.createElementNS(rootNamespace, "other-child");
		rootElement.appendChild(second);
		text = document.createTextNode("other-value");
		second.appendChild(text);

		WebServiceMessage requestMock = createMock(WebServiceMessage.class);
		expect(requestMock.getPayloadSource()).andReturn(new DOMSource(first));
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);

		replay(requestMock, factoryMock);

		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("root", rootNamespace);
		namespaces.put("child", childNamespace);
		adapter.setNamespaces(namespaces);

		MessageContext messageContext = new DefaultMessageContext(requestMock, factoryMock);
		MethodEndpoint endpoint = new MethodEndpoint(this, "namespaces", new Class[]{Node.class});
		adapter.invoke(messageContext, endpoint);
		Assert.assertTrue("Method not invoked", namespacesInvoked);
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
		Assert.assertTrue("Invalid boolean value", param1);
		Assert.assertEquals("Invalid double value", 42D, param2, 0.00001D);
		Assert.assertEquals("Invalid Node value", "child", param3.getLocalName());
		Assert.assertEquals("Invalid NodeList value", 1, param4.getLength());
		Assert.assertEquals("Invalid Node value", "child", param4.item(0).getLocalName());
		Assert.assertEquals("Invalid Node value", "text", param5);
	}

	public void unsupportedInvalidParams(@XPathParam("/")String param1, String param2) {

	}

	public String unsupportedInvalidReturnType(@XPathParam("/")String param1) {
		return null;
	}

	public void unsupportedInvalidParamType(@XPathParam("/")int param1) {
	}

	public void namespaces(@XPathParam(".")Node param) {
		namespacesInvoked = true;
		Assert.assertEquals("Invalid parameter", "child", param.getLocalName());
	}
}