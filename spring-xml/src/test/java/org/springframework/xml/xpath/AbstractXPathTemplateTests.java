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

package org.springframework.xml.xpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.ResourceSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class AbstractXPathTemplateTests {

	XPathOperations template;

	private Source namespaces;

	private Source nonamespaces;

	@BeforeEach
	public final void setUp() throws Exception {
		this.template = createTemplate();
		this.namespaces = new ResourceSource(new ClassPathResource("namespaces.xml", AbstractXPathTemplateTests.class));
		this.nonamespaces = new ResourceSource(
				new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTests.class));
	}

	protected abstract XPathOperations createTemplate() throws Exception;

	@Test
	void testEvaluateAsBoolean() {

		boolean result = this.template.evaluateAsBoolean("/root/child/boolean", this.nonamespaces);

		assertThat(result).isTrue();
	}

	@Test
	void testEvaluateAsBooleanNamespaces() {

		boolean result = this.template.evaluateAsBoolean("/prefix1:root/prefix2:child/prefix2:boolean",
				this.namespaces);

		assertThat(result).isTrue();
	}

	@Test
	void testEvaluateAsDouble() {

		double result = this.template.evaluateAsDouble("/root/child/number", this.nonamespaces);

		assertThat(result).isEqualTo(42D);
	}

	@Test
	void testEvaluateAsDoubleNamespaces() {

		double result = this.template.evaluateAsDouble("/prefix1:root/prefix2:child/prefix2:number", this.namespaces);

		assertThat(result).isEqualTo(42D);
	}

	@Test
	void testEvaluateAsNode() {

		Node result = this.template.evaluateAsNode("/root/child", this.nonamespaces);

		assertThat(result).isNotNull();
		assertThat(result.getLocalName()).isEqualTo("child");
	}

	@Test
	void testEvaluateAsNodeNamespaces() {

		Node result = this.template.evaluateAsNode("/prefix1:root/prefix2:child", this.namespaces);

		assertThat(result).isNotNull();
		assertThat(result.getLocalName()).isEqualTo("child");
	}

	@Test
	void testEvaluateAsNodes() {

		List<Node> results = this.template.evaluateAsNodeList("/root/child/*", this.nonamespaces);

		assertThat(results).isNotNull();
		assertThat(results).hasSize(3);
	}

	@Test
	void testEvaluateAsNodesNamespaces() {

		List<Node> results = this.template.evaluateAsNodeList("/prefix1:root/prefix2:child/*", this.namespaces);

		assertThat(results).isNotNull();
		assertThat(results).hasSize(3);
	}

	@Test
	void testEvaluateAsStringNamespaces() throws IOException, SAXException {

		String result = this.template.evaluateAsString("/prefix1:root/prefix2:child/prefix2:text", this.namespaces);

		assertThat(result).isEqualTo("text");
	}

	@Test
	void testEvaluateAsString() throws IOException, SAXException {

		String result = this.template.evaluateAsString("/root/child/text", this.nonamespaces);

		assertThat(result).isEqualTo("text");
	}

	@Test
	void testEvaluateDomSource() throws IOException, SAXException, ParserConfigurationException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(SaxUtils
			.createInputSource(new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTests.class)));

		String result = this.template.evaluateAsString("/root/child/text", new DOMSource(document));

		assertThat(result).isEqualTo("text");
	}

	@Test
	void testEvaluateSAXSource() throws Exception {

		InputStream in = AbstractXPathTemplateTests.class.getResourceAsStream("nonamespaces.xml");
		SAXSource source = new SAXSource(new InputSource(in));
		String result = this.template.evaluateAsString("/root/child/text", source);

		assertThat(result).isEqualTo("text");
	}

	@Test
	void testEvaluateStaxSource() throws Exception {

		InputStream in = AbstractXPathTemplateTests.class.getResourceAsStream("nonamespaces.xml");
		XMLStreamReader streamReader = XMLInputFactory.newFactory().createXMLStreamReader(in);
		StAXSource source = new StAXSource(streamReader);
		String result = this.template.evaluateAsString("/root/child/text", source);

		assertThat(result).isEqualTo("text");
	}

	@Test
	void testEvaluateStreamSourceInputStream() throws IOException, SAXException, ParserConfigurationException {

		InputStream in = AbstractXPathTemplateTests.class.getResourceAsStream("nonamespaces.xml");
		StreamSource source = new StreamSource(in);
		String result = this.template.evaluateAsString("/root/child/text", source);

		assertThat(result).isEqualTo("text");
	}

	@Test
	void testEvaluateStreamSourceSystemId() throws IOException, SAXException, ParserConfigurationException {

		URL url = AbstractXPathTemplateTests.class.getResource("nonamespaces.xml");
		String result = this.template.evaluateAsString("/root/child/text", new StreamSource(url.toString()));

		assertThat(result).isEqualTo("text");
	}

	@Test
	void testInvalidExpression() {
		assertThatExceptionOfType(XPathException.class)
			.isThrownBy(() -> this.template.evaluateAsBoolean("\\", this.namespaces));
	}

	@Test
	void testEvaluateAsObject() throws Exception {

		String result = this.template.evaluateAsObject("/root/child", this.nonamespaces, new NodeMapper<>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("child");
	}

	@Test
	void testEvaluate() throws Exception {

		List<String> results = this.template.evaluate("/root/child/*", this.nonamespaces, new NodeMapper<>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});

		assertThat(results).isNotNull();
		assertThat(results).containsExactly("text", "number", "boolean");
	}

}
