/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.adapter.method;

import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Namespace;
import org.springframework.ws.server.endpoint.annotation.Namespaces;
import org.springframework.ws.server.endpoint.annotation.XPathParam;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

@Namespaces(@Namespace(prefix = "tns", uri = "http://springframework.org/spring-ws"))
public class XPathParamMethodArgumentResolverTest {

	private static final String CONTENTS = "<root><child><text>text</text><number>42</number></child></root>";

	private XPathParamMethodArgumentResolver resolver;

	private MethodParameter booleanParameter;

	private MethodParameter doubleParameter;

	private MethodParameter nodeParameter;

	private MethodParameter nodeListParameter;

	private MethodParameter stringParameter;

	private MethodParameter convertedParameter;

	private MethodParameter unsupportedParameter;

	private MethodParameter namespaceMethodParameter;

	private MethodParameter namespaceClassParameter;

	@Before
	public void setUp() throws Exception {
		resolver = new XPathParamMethodArgumentResolver();
		Method supportedTypes = getClass()
				.getMethod("supportedTypes", Boolean.TYPE, Double.TYPE, Node.class, NodeList.class, String.class);
		booleanParameter = new MethodParameter(supportedTypes, 0);
		doubleParameter = new MethodParameter(supportedTypes, 1);
		nodeParameter = new MethodParameter(supportedTypes, 2);
		nodeListParameter = new MethodParameter(supportedTypes, 3);
		stringParameter = new MethodParameter(supportedTypes, 4);
		convertedParameter = new MethodParameter(getClass().getMethod("convertedType", Integer.TYPE), 0);
		unsupportedParameter = new MethodParameter(getClass().getMethod("unsupported", String.class), 0);
		namespaceMethodParameter = new MethodParameter(getClass().getMethod("namespacesMethod", String.class), 0);
		namespaceClassParameter = new MethodParameter(getClass().getMethod("namespacesClass", String.class), 0);
	}

	@Test
	public void supportsParameter() {
		assertTrue("resolver does not support boolean parameter", resolver.supportsParameter(booleanParameter));
		assertTrue("resolver does not support double parameter", resolver.supportsParameter(doubleParameter));
		assertTrue("resolver does not support Node parameter", resolver.supportsParameter(nodeParameter));
		assertTrue("resolver does not support NodeList parameter", resolver.supportsParameter(nodeListParameter));
		assertTrue("resolver does not support String parameter", resolver.supportsParameter(stringParameter));
		assertTrue("resolver does not support String parameter", resolver.supportsParameter(convertedParameter));
		assertFalse("resolver supports parameter without @XPathParam", resolver.supportsParameter(unsupportedParameter));
	}

	@Test
	public void resolveBoolean() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, booleanParameter);

		assertTrue("resolver does not return boolean", result instanceof Boolean);
		Boolean b = (Boolean) result;
		assertTrue("Invalid boolean value", b);
	}
	@Test
	public void resolveDouble() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, doubleParameter);

		assertTrue("resolver does not return double", result instanceof Double);
		Double d = (Double) result;
		assertEquals("Invalid double value", 42D, d, 0D);
	}

	@Test
	public void resolveNode() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, nodeParameter);

		assertTrue("resolver does not return Node", result instanceof Node);
		Node node  = (Node) result;
		assertEquals("Invalid node value", "child", node.getLocalName());
	}

	@Test
	public void resolveNodeList() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, nodeListParameter);

		assertTrue("resolver does not return NodeList", result instanceof NodeList);
		NodeList nodeList  = (NodeList) result;
		assertEquals("Invalid NodeList value", 1, nodeList.getLength());
		assertEquals("Invalid Node value", "child", nodeList.item(0).getLocalName());
	}

	@Test
	public void resolveString() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, stringParameter);

		assertTrue("resolver does not return String", result instanceof String);
		String s  = (String) result;
		assertEquals("Invalid string value", "text", s);
	}
	
	@Test
	public void resolveConvertedType() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, convertedParameter);

		assertTrue("resolver does not return String", result instanceof Integer);
		Integer i  = (Integer) result;
		assertEquals("Invalid integer value", new Integer(42), i);
	}

	@Test
	public void resolveNamespacesMethod() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(
				"<root xmlns=\"http://springframework.org/spring-ws\">text</root>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, namespaceMethodParameter);

		assertTrue("resolver does not return String", result instanceof String);
		String s  = (String) result;
		assertEquals("Invalid string value", "text", s);
	}
	
	@Test
	public void resolveNamespacesClass() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage(
				"<root xmlns=\"http://springframework.org/spring-ws\">text</root>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, namespaceClassParameter);

		assertTrue("resolver does not return String", result instanceof String);
		String s  = (String) result;
		assertEquals("Invalid string value", "text", s);
	}

	public void unsupported(String s) {
	}

	public void supportedTypes(@XPathParam("/root/child")boolean param1,
							   @XPathParam("/root/child/number")double param2,
							   @XPathParam("/root/child") Node param3,
							   @XPathParam("/root/*") NodeList param4,
							   @XPathParam("/root/child/text")String param5) {
	}

	public void convertedType(@XPathParam("/root/child/number")int param) {
	}

	@Namespaces(@Namespace(prefix = "tns", uri = "http://springframework.org/spring-ws"))
	public void namespacesMethod(@XPathParam("/tns:root")String s) {
	}

	public void namespacesClass(@XPathParam("/tns:root")String s) {
	}

}
