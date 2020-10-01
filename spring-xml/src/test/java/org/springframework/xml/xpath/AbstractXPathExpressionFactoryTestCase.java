/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.xml.xpath;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathProcessorException;

public abstract class AbstractXPathExpressionFactoryTestCase {

	private Document noNamespacesDocument;

	private Document namespacesDocument;

	private Map<String, String> namespaces = new HashMap<String, String>();

	@BeforeEach
	public void setUp() throws Exception {

		namespaces.put("prefix1", "namespace1");
		namespaces.put("prefix2", "namespace2");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		InputStream inputStream = getClass().getResourceAsStream("nonamespaces.xml");

		try {
			noNamespacesDocument = documentBuilder.parse(inputStream);
		} finally {
			inputStream.close();
		}
		inputStream = getClass().getResourceAsStream("namespaces.xml");

		try {
			namespacesDocument = documentBuilder.parse(inputStream);
		} finally {
			inputStream.close();
		}
	}

	@Test
	public void testThatToStringReturnsOriginalXpathExpression() {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);

		assertThat("/prefix1:root/prefix2:otherchild").isEqualTo(expression.toString());
	}

	@Test
	public void testEvaluateAsBooleanInvalidNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
		boolean result = expression.evaluateAsBoolean(namespacesDocument);

		assertThat(result).isFalse();
	}

	@Test
	public void testEvaluateAsBooleanInvalidNoNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/root/otherchild");
		boolean result = expression.evaluateAsBoolean(noNamespacesDocument);

		assertThat(result).isFalse();
	}

	@Test
	public void testEvaluateAsBooleanNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:child/prefix2:boolean/text()",
				namespaces);
		boolean result = expression.evaluateAsBoolean(namespacesDocument);

		assertThat(result).isTrue();
	}

	@Test
	public void testEvaluateAsBooleanNoNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/root/child/boolean/text()");
		boolean result = expression.evaluateAsBoolean(noNamespacesDocument);

		assertThat(result).isTrue();
	}

	@Test
	public void testEvaluateAsDoubleInvalidNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
		double result = expression.evaluateAsNumber(noNamespacesDocument);

		assertThat(result).isNaN();
	}

	@Test
	public void testEvaluateAsDoubleInvalidNoNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/root/otherchild");
		double result = expression.evaluateAsNumber(noNamespacesDocument);

		assertThat(result).isNaN();
	}

	@Test
	public void testEvaluateAsDoubleNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:child/prefix2:number/text()", namespaces);
		double result = expression.evaluateAsNumber(namespacesDocument);

		assertThat(result).isEqualTo(42.0D);
	}

	@Test
	public void testEvaluateAsDoubleNoNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/root/child/number/text()");
		double result = expression.evaluateAsNumber(noNamespacesDocument);

		assertThat(result).isEqualTo(42.0D);
	}

	@Test
	public void testEvaluateAsNodeInvalidNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
		Node result = expression.evaluateAsNode(namespacesDocument);

		assertThat(result).isNull();
	}

	@Test
	public void testEvaluateAsNodeInvalidNoNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/root/otherchild");
		Node result = expression.evaluateAsNode(noNamespacesDocument);

		assertThat(result).isNull();
	}

	@Test
	public void testEvaluateAsNodeNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:child", namespaces);
		Node result = expression.evaluateAsNode(namespacesDocument);

		assertThat(result).isNotNull();
		assertThat(result.getLocalName()).isEqualTo("child");
	}

	@Test
	public void testEvaluateAsNodeNoNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/root/child");
		Node result = expression.evaluateAsNode(noNamespacesDocument);

		assertThat(result).isNotNull();
		assertThat(result.getLocalName()).isEqualTo("child");
	}

	@Test
	public void testEvaluateAsNodeListNamespaces() throws IOException, SAXException {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:child/*", namespaces);
		List<Node> results = expression.evaluateAsNodeList(namespacesDocument);

		assertThat(results).isNotNull();
		assertThat(results).hasSize(3);
	}

	@Test
	public void testEvaluateAsNodeListNoNamespaces() {

		XPathExpression expression = createXPathExpression("/root/child/*");
		List<Node> results = expression.evaluateAsNodeList(noNamespacesDocument);

		assertThat(results).isNotNull();
		assertThat(results).hasSize(3);
	}

	@Test
	public void testEvaluateAsStringInvalidNamespaces() {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
		String result = expression.evaluateAsString(namespacesDocument);

		assertThat(result).isEmpty();
	}

	@Test
	public void testEvaluateAsStringInvalidNoNamespaces() {

		XPathExpression expression = createXPathExpression("/root/otherchild");
		String result = expression.evaluateAsString(noNamespacesDocument);

		assertThat(result).isEmpty();
	}

	@Test
	public void testEvaluateAsStringNamespaces() {

		XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:child/prefix2:text/text()", namespaces);
		String result = expression.evaluateAsString(namespacesDocument);

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateAsStringNoNamespaces() {

		XPathExpression expression = createXPathExpression("/root/child/text/text()");
		String result = expression.evaluateAsString(noNamespacesDocument);

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateAsObject() {

		XPathExpression expression = createXPathExpression("/root/child");
		String result = expression.evaluateAsObject(noNamespacesDocument, new NodeMapper<String>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("child");
	}

	@Test
	public void testEvaluate() throws Exception {

		XPathExpression expression = createXPathExpression("/root/child/*");
		List<String> results = expression.evaluate(noNamespacesDocument, new NodeMapper<String>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});

		assertThat(results).isNotNull();
		assertThat(results).containsExactly("text", "number", "boolean");
	}

	@Test
	public void testInvalidExpression() {
		assertThatExceptionOfType(XPathParseException.class).isThrownBy(() -> createXPathExpression("\\"));
	}

	protected abstract XPathExpression createXPathExpression(String expression);

	protected abstract XPathExpression createXPathExpression(String expression, Map<String, String> namespaces);
}
