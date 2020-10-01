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

package org.springframework.ws.server.endpoint.adapter.method;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Namespace;
import org.springframework.ws.server.endpoint.annotation.Namespaces;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	@BeforeEach
	public void setUp() throws Exception {

		resolver = new XPathParamMethodArgumentResolver();
		Method supportedTypes = getClass().getMethod("supportedTypes", Boolean.TYPE, Double.TYPE, Node.class,
				NodeList.class, String.class);
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

		assertThat(resolver.supportsParameter(booleanParameter)).isTrue();
		assertThat(resolver.supportsParameter(doubleParameter)).isTrue();
		assertThat(resolver.supportsParameter(nodeParameter)).isTrue();
		assertThat(resolver.supportsParameter(nodeListParameter)).isTrue();
		assertThat(resolver.supportsParameter(stringParameter)).isTrue();
		assertThat(resolver.supportsParameter(convertedParameter)).isTrue();
		assertThat(resolver.supportsParameter(unsupportedParameter)).isFalse();
	}

	@Test
	public void resolveBoolean() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, booleanParameter);

		assertThat(result).isInstanceOf(Boolean.class);

		Boolean b = (Boolean) result;

		assertThat(b).isTrue();
	}

	@Test
	public void resolveDouble() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, doubleParameter);

		assertThat(result).isInstanceOf(Double.class);

		Double d = (Double) result;

		assertThat(d).isEqualTo(42D);
	}

	@Test
	public void resolveNode() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, nodeParameter);

		assertThat(result).isInstanceOf(Node.class);

		Node node = (Node) result;

		assertThat(node.getLocalName()).isEqualTo("child");
	}

	@Test
	public void resolveNodeList() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, nodeListParameter);

		assertThat(result).isInstanceOf(NodeList.class);

		NodeList nodeList = (NodeList) result;

		assertThat(nodeList.getLength()).isEqualTo(1);
		assertThat(nodeList.item(0).getLocalName()).isEqualTo("child");
	}

	@Test
	public void resolveString() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, stringParameter);

		assertThat(result).isInstanceOf(String.class);

		String s = (String) result;

		assertThat(s).isEqualTo("text");
	}

	@Test
	public void resolveConvertedType() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(CONTENTS);
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, convertedParameter);

		assertThat(result).isInstanceOf(Integer.class);
		assertThat(result).isEqualTo(42);
	}

	@Test
	public void resolveNamespacesMethod() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(
				"<root xmlns=\"http://springframework.org/spring-ws\">text</root>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, namespaceMethodParameter);

		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo("text");
	}

	@Test
	public void resolveNamespacesClass() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(
				"<root xmlns=\"http://springframework.org/spring-ws\">text</root>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = resolver.resolveArgument(messageContext, namespaceClassParameter);

		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo("text");
	}

	public void unsupported(String s) {}

	public void supportedTypes( //
			@XPathParam("/root/child") boolean param1, //
			@XPathParam("/root/child/number") double param2, //
			@XPathParam("/root/child") Node param3, //
			@XPathParam("/root/*") NodeList param4, //
			@XPathParam("/root/child/text") String param5) {}

	public void convertedType(@XPathParam("/root/child/number") int param) {}

	@Namespaces(@Namespace(prefix = "tns", uri = "http://springframework.org/spring-ws"))
	public void namespacesMethod(@XPathParam("/tns:root") String s) {}

	public void namespacesClass(@XPathParam("/tns:root") String s) {}

}
