/*
 * Copyright 2005-2022 the original author or authors.
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.ResourceSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractXPathTemplateTestCase {

	XPathOperations template;

	private Source namespaces;

	private Source nonamespaces;

	@BeforeEach
	public final void setUp() throws Exception {
		template = createTemplate();
		namespaces = new ResourceSource(new ClassPathResource("namespaces.xml", AbstractXPathTemplateTestCase.class));
		nonamespaces = new ResourceSource(new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTestCase.class));
	}

	protected abstract XPathOperations createTemplate() throws Exception;

	@Test
	public void testEvaluateAsBoolean() {

		boolean result = template.evaluateAsBoolean("/root/child/boolean", nonamespaces);

		assertThat(result).isTrue();
	}

	@Test
	public void testEvaluateAsBooleanNamespaces() {

		boolean result = template.evaluateAsBoolean("/prefix1:root/prefix2:child/prefix2:boolean", namespaces);

		assertThat(result).isTrue();
	}

	@Test
	public void testEvaluateAsDouble() {

		double result = template.evaluateAsDouble("/root/child/number", nonamespaces);

		assertThat(result).isEqualTo(42D);
	}

	@Test
	public void testEvaluateAsDoubleNamespaces() {

		double result = template.evaluateAsDouble("/prefix1:root/prefix2:child/prefix2:number", namespaces);

		assertThat(result).isEqualTo(42D);
	}

	@Test
	public void testEvaluateAsNode() {

		Node result = template.evaluateAsNode("/root/child", nonamespaces);

		assertThat(result).isNotNull();
		assertThat(result.getLocalName()).isEqualTo("child");
	}

	@Test
	public void testEvaluateAsNodeNamespaces() {

		Node result = template.evaluateAsNode("/prefix1:root/prefix2:child", namespaces);

		assertThat(result).isNotNull();
		assertThat(result.getLocalName()).isEqualTo("child");
	}

	@Test
	public void testEvaluateAsNodes() {

		List<Node> results = template.evaluateAsNodeList("/root/child/*", nonamespaces);

		assertThat(results).isNotNull();
		assertThat(results).hasSize(3);
	}

	@Test
	public void testEvaluateAsNodesNamespaces() {

		List<Node> results = template.evaluateAsNodeList("/prefix1:root/prefix2:child/*", namespaces);

		assertThat(results).isNotNull();
		assertThat(results).hasSize(3);
	}

	@Test
	public void testEvaluateAsStringNamespaces() throws IOException, SAXException {

		String result = template.evaluateAsString("/prefix1:root/prefix2:child/prefix2:text", namespaces);

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateAsString() throws IOException, SAXException {

		String result = template.evaluateAsString("/root/child/text", nonamespaces);

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateDomSource() throws IOException, SAXException, ParserConfigurationException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(
				SaxUtils.createInputSource(new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTestCase.class)));

		String result = template.evaluateAsString("/root/child/text", new DOMSource(document));

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateSAXSource() throws Exception {

		InputStream in = AbstractXPathTemplateTestCase.class.getResourceAsStream("nonamespaces.xml");
		SAXSource source = new SAXSource(new InputSource(in));
		String result = template.evaluateAsString("/root/child/text", source);

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateStaxSource() throws Exception {

		InputStream in = AbstractXPathTemplateTestCase.class.getResourceAsStream("nonamespaces.xml");
		XMLStreamReader streamReader = XMLInputFactory.newFactory().createXMLStreamReader(in);
		StAXSource source = new StAXSource(streamReader);
		String result = template.evaluateAsString("/root/child/text", source);

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateStreamSourceInputStream() throws IOException, SAXException, ParserConfigurationException {

		InputStream in = AbstractXPathTemplateTestCase.class.getResourceAsStream("nonamespaces.xml");
		StreamSource source = new StreamSource(in);
		String result = template.evaluateAsString("/root/child/text", source);

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testEvaluateStreamSourceSystemId() throws IOException, SAXException, ParserConfigurationException {

		URL url = AbstractXPathTemplateTestCase.class.getResource("nonamespaces.xml");
		String result = template.evaluateAsString("/root/child/text", new StreamSource(url.toString()));

		assertThat(result).isEqualTo("text");
	}

	@Test
	public void testInvalidExpression() {
		assertThatExceptionOfType(XPathException.class).isThrownBy(() -> template.evaluateAsBoolean("\\", namespaces));
	}

	@Test
	public void testEvaluateAsObject() throws Exception {

		String result = template.evaluateAsObject("/root/child", nonamespaces, new NodeMapper<String>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("child");
	}

	@Test
	public void testEvaluate() throws Exception {

		List<String> results = template.evaluate("/root/child/*", nonamespaces, new NodeMapper<String>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});

		assertThat(results).isNotNull();
		assertThat(results).containsExactly("text", "number", "boolean");
	}
}
