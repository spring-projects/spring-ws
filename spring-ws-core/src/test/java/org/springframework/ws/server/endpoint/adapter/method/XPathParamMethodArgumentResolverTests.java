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

package org.springframework.ws.server.endpoint.adapter.method;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Namespace;
import org.springframework.ws.server.endpoint.annotation.Namespaces;
import org.springframework.ws.server.endpoint.annotation.XPathParam;

import static org.assertj.core.api.Assertions.assertThat;

@Namespaces(@Namespace(prefix = "tns", uri = "http://springframework.org/spring-ws"))
class XPathParamMethodArgumentResolverTests {

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

	@BeforeEach
	void setUp() throws Exception {

		this.resolver = new XPathParamMethodArgumentResolver();
		Method supportedTypes = getClass().getMethod("supportedTypes", Boolean.TYPE, Double.TYPE, Node.class,
				NodeList.class, String.class);
		this.booleanParameter = new MethodParameter(supportedTypes, 0);
		this.doubleParameter = new MethodParameter(supportedTypes, 1);
		this.nodeParameter = new MethodParameter(supportedTypes, 2);
		this.nodeListParameter = new MethodParameter(supportedTypes, 3);
		this.stringParameter = new MethodParameter(supportedTypes, 4);
		this.convertedParameter = new MethodParameter(getClass().getMethod("convertedType", Integer.TYPE), 0);
		this.unsupportedParameter = new MethodParameter(getClass().getMethod("unsupported", String.class), 0);
		this.namespaceMethodParameter = new MethodParameter(getClass().getMethod("namespacesMethod", String.class), 0);
		this.namespaceClassParameter = new MethodParameter(getClass().getMethod("namespacesClass", String.class), 0);
	}

	@Test
	void supportsParameter() {

		assertThat(this.resolver.supportsParameter(this.booleanParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.doubleParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.nodeParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.nodeListParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.stringParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.convertedParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.unsupportedParameter)).isFalse();
	}

	@Test
	void resolveBoolean() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.booleanParameter);

		assertThat(result).isInstanceOf(Boolean.class);

		Boolean b = (Boolean) result;

		assertThat(b).isTrue();
	}

	@Test
	void resolveDouble() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.doubleParameter);

		assertThat(result).isInstanceOf(Double.class);

		Double d = (Double) result;

		assertThat(d).isEqualTo(42D);
	}

	@Test
	void resolveNode() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.nodeParameter);

		assertThat(result).isInstanceOf(Node.class);

		Node node = (Node) result;

		assertThat(node.getLocalName()).isEqualTo("child");
	}

	@Test
	void resolveNodeList() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.nodeListParameter);

		assertThat(result).isInstanceOf(NodeList.class);

		NodeList nodeList = (NodeList) result;

		assertThat(nodeList.getLength()).isEqualTo(1);
		assertThat(nodeList.item(0).getLocalName()).isEqualTo("child");
	}

	@Test
	void resolveString() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.stringParameter);

		assertThat(result).isInstanceOf(String.class);

		String s = (String) result;

		assertThat(s).isEqualTo("text");
	}

	@Test
	void resolveConvertedType() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.convertedParameter);

		assertThat(result).isInstanceOf(Integer.class);
		assertThat(result).isEqualTo(42);
	}

	@Test
	void resolveNamespacesMethod() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(
				"<root xmlns=\"http://springframework.org/spring-ws\">text</root>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.namespaceMethodParameter);

		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo("text");
	}

	@Test
	void resolveNamespacesClass() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(
				"<root xmlns=\"http://springframework.org/spring-ws\">text</root>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = this.resolver.resolveArgument(messageContext, this.namespaceClassParameter);

		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo("text");
	}

	public void unsupported(String s) {
	}

	public void supportedTypes(@XPathParam("/root/child") boolean param1,
			@XPathParam("/root/child/number") double param2, @XPathParam("/root/child") Node param3,
			@XPathParam("/root/*") NodeList param4, @XPathParam("/root/child/text") String param5) {
	}

	public void convertedType(@XPathParam("/root/child/number") int param) {
	}

	@Namespaces(@Namespace(prefix = "tns", uri = "http://springframework.org/spring-ws"))
	public void namespacesMethod(@XPathParam("/tns:root") String s) {
	}

	public void namespacesClass(@XPathParam("/tns:root") String s) {
	}

}
