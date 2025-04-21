/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

@Deprecated
class XPathParamAnnotationMethodEndpointAdapterTests {

	private static final String CONTENTS = "<root><child><text>text</text><number>42.0</number></child></root>";

	private XPathParamAnnotationMethodEndpointAdapter adapter;

	private boolean supportedTypesInvoked = false;

	private boolean supportedSourceInvoked;

	private boolean namespacesInvoked;

	@BeforeEach
	void setUp() throws Exception {

		this.adapter = new XPathParamAnnotationMethodEndpointAdapter();
		this.adapter.afterPropertiesSet();
	}

	@Test
	void testUnsupportedInvalidParam() throws NoSuchMethodException {

		MethodEndpoint endpoint = new MethodEndpoint(this, "unsupportedInvalidParamType", Integer.TYPE);

		assertThat(this.adapter.supports(endpoint)).isFalse();
	}

	@Test
	void testUnsupportedInvalidReturnType() throws NoSuchMethodException {

		MethodEndpoint endpoint = new MethodEndpoint(this, "unsupportedInvalidReturnType", String.class);
		assertThat(this.adapter.supports(endpoint)).isFalse();
	}

	@Test
	void testUnsupportedInvalidParams() throws NoSuchMethodException {

		MethodEndpoint endpoint = new MethodEndpoint(this, "unsupportedInvalidParams", String.class, String.class);
		assertThat(this.adapter.supports(endpoint)).isFalse();
	}

	@Test
	void testSupportedTypes() throws NoSuchMethodException {

		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedTypes", Boolean.TYPE, Double.TYPE, Node.class,
				NodeList.class, String.class);
		assertThat(this.adapter.supports(endpoint)).isTrue();
	}

	@Test
	void testSupportsStringSource() throws NoSuchMethodException {

		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedStringSource", String.class);
		assertThat(this.adapter.supports(endpoint)).isTrue();
	}

	@Test
	void testSupportsSource() throws NoSuchMethodException {

		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedSource", String.class);
		assertThat(this.adapter.supports(endpoint)).isTrue();
	}

	@Test
	void testSupportsVoid() throws NoSuchMethodException {

		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedVoid", String.class);
		assertThat(this.adapter.supports(endpoint)).isTrue();
	}

	@Test
	void testInvokeTypes() throws Exception {

		WebServiceMessage messageMock = createMock(WebServiceMessage.class);
		expect(messageMock.getPayloadSource()).andReturn(new StringSource(CONTENTS));
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		replay(messageMock, factoryMock);

		MessageContext messageContext = new DefaultMessageContext(messageMock, factoryMock);
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedTypes", Boolean.TYPE, Double.TYPE, Node.class,
				NodeList.class, String.class);
		this.adapter.invoke(messageContext, endpoint);

		assertThat(this.supportedTypesInvoked).isTrue();

		verify(messageMock, factoryMock);
	}

	@Test
	void testInvokeSource() throws Exception {

		WebServiceMessage requestMock = createMock(WebServiceMessage.class);
		WebServiceMessage responseMock = createMock(WebServiceMessage.class);
		expect(requestMock.getPayloadSource()).andReturn(new StringSource(CONTENTS));
		expect(responseMock.getPayloadResult()).andReturn(new StringResult());
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
		replay(requestMock, responseMock, factoryMock);

		MessageContext messageContext = new DefaultMessageContext(requestMock, factoryMock);
		MethodEndpoint endpoint = new MethodEndpoint(this, "supportedSource", String.class);
		this.adapter.invoke(messageContext, endpoint);

		assertThat(this.supportedSourceInvoked).isTrue();

		verify(requestMock, responseMock, factoryMock);
	}

	@Test
	void testInvokeVoidDom() throws Exception {

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

		Map<String, String> namespaces = new HashMap<>();
		namespaces.put("root", rootNamespace);
		namespaces.put("child", childNamespace);
		this.adapter.setNamespaces(namespaces);

		MessageContext messageContext = new DefaultMessageContext(requestMock, factoryMock);
		MethodEndpoint endpoint = new MethodEndpoint(this, "namespaces", Node.class);
		this.adapter.invoke(messageContext, endpoint);

		assertThat(this.namespacesInvoked).isTrue();
	}

	public void supportedVoid(@XPathParam("/") String param1) {
	}

	public Source supportedSource(@XPathParam("/") String param1) {

		this.supportedSourceInvoked = true;
		return new StringSource("<response/>");
	}

	public StringSource supportedStringSource(@XPathParam("/") String param1) {
		return null;
	}

	public void supportedTypes(@XPathParam("/root/child") boolean param1,
			@XPathParam("/root/child/number") double param2, @XPathParam("/root/child") Node param3,
			@XPathParam("/root/*") NodeList param4, @XPathParam("/root/child/text") String param5) {

		this.supportedTypesInvoked = true;

		assertThat(param1).isTrue();
		assertThat(param2).isEqualTo(42D);
		assertThat(param3.getLocalName()).isEqualTo("child");
		assertThat(param4.getLength()).isEqualTo(1);
		assertThat(param4.item(0).getLocalName()).isEqualTo("child");
		assertThat(param5).isEqualTo("text");
	}

	public void unsupportedInvalidParams(@XPathParam("/") String param1, String param2) {

	}

	public String unsupportedInvalidReturnType(@XPathParam("/") String param1) {
		return null;
	}

	public void unsupportedInvalidParamType(@XPathParam("/") int param1) {
	}

	public void namespaces(@XPathParam(".") Node param) {

		this.namespacesInvoked = true;
		assertThat(param.getLocalName()).isEqualTo("child");
	}

}
