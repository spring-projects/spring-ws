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

import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.ResourceSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractXPathTemplateTestCase {

	XPathOperations template;

	private Source namespaces;

	private Source nonamespaces;

	@Before
	public final void setUp() throws Exception {
		template = createTemplate();
		namespaces = new ResourceSource(new ClassPathResource("namespaces.xml", AbstractXPathTemplateTestCase.class));
		nonamespaces =
				new ResourceSource(new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTestCase.class));
	}

	protected abstract XPathOperations createTemplate() throws Exception;

	@Test
	public void testEvaluateAsBoolean() {
		boolean result = template.evaluateAsBoolean("/root/child/boolean", nonamespaces);
		Assert.assertTrue("Invalid result", result);
	}

	@Test
	public void testEvaluateAsBooleanNamespaces() {
		boolean result = template.evaluateAsBoolean("/prefix1:root/prefix2:child/prefix2:boolean", namespaces);
		Assert.assertTrue("Invalid result", result);
	}

	@Test
	public void testEvaluateAsDouble() {
		double result = template.evaluateAsDouble("/root/child/number", nonamespaces);
		Assert.assertEquals("Invalid result", 42D, result, 0D);
	}

	@Test
	public void testEvaluateAsDoubleNamespaces() {
		double result = template.evaluateAsDouble("/prefix1:root/prefix2:child/prefix2:number", namespaces);
		Assert.assertEquals("Invalid result", 42D, result, 0D);
	}

	@Test
	public void testEvaluateAsNode() {
		Node result = template.evaluateAsNode("/root/child", nonamespaces);
		Assert.assertNotNull("Invalid result", result);
		Assert.assertEquals("Invalid localname", "child", result.getLocalName());
	}

	@Test
	public void testEvaluateAsNodeNamespaces() {
		Node result = template.evaluateAsNode("/prefix1:root/prefix2:child", namespaces);
		Assert.assertNotNull("Invalid result", result);
		Assert.assertEquals("Invalid localname", "child", result.getLocalName());
	}

	@Test
	public void testEvaluateAsNodes() {
		List<Node> results = template.evaluateAsNodeList("/root/child/*", nonamespaces);
		Assert.assertNotNull("Invalid result", results);
		Assert.assertEquals("Invalid amount of results", 3, results.size());
	}

	@Test
	public void testEvaluateAsNodesNamespaces() {
		List<Node> results = template.evaluateAsNodeList("/prefix1:root/prefix2:child/*", namespaces);
		Assert.assertNotNull("Invalid result", results);
		Assert.assertEquals("Invalid amount of results", 3, results.size());
	}

	@Test
	public void testEvaluateAsStringNamespaces() throws IOException, SAXException {
		String result = template.evaluateAsString("/prefix1:root/prefix2:child/prefix2:text", namespaces);
		Assert.assertEquals("Invalid result", "text", result);
	}

	@Test
	public void testEvaluateAsString() throws IOException, SAXException {
		String result = template.evaluateAsString("/root/child/text", nonamespaces);
		Assert.assertEquals("Invalid result", "text", result);
	}

	@Test
	public void testEvaluateDomSource() throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(SaxUtils.createInputSource(
				new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTestCase.class)));

		String result = template.evaluateAsString("/root/child/text", new DOMSource(document));
		Assert.assertEquals("Invalid result", "text", result);
	}

	@Test
	public void testEvaluateSAXSource() throws Exception {
		InputStream in = AbstractXPathTemplateTestCase.class.getResourceAsStream("nonamespaces.xml");
		SAXSource source = new SAXSource(new InputSource(in));
		String result = template.evaluateAsString("/root/child/text", source);
		Assert.assertEquals("Invalid result", "text", result);
	}

	@Test
	public void testEvaluateStaxSource() throws Exception {
		InputStream in = AbstractXPathTemplateTestCase.class.getResourceAsStream("nonamespaces.xml");
		XMLStreamReader streamReader = XMLInputFactory.newFactory().createXMLStreamReader(in);
		StAXSource source = new StAXSource(streamReader);
		String result = template.evaluateAsString("/root/child/text", source);
		Assert.assertEquals("Invalid result", "text", result);
	}

	@Test
	public void testEvaluateStreamSourceInputStream() throws IOException, SAXException, ParserConfigurationException {
		InputStream in = AbstractXPathTemplateTestCase.class.getResourceAsStream("nonamespaces.xml");
		StreamSource source = new StreamSource(in);
		String result = template.evaluateAsString("/root/child/text", source);
		Assert.assertEquals("Invalid result", "text", result);
	}

	@Test
	public void testEvaluateStreamSourceSystemId() throws IOException, SAXException, ParserConfigurationException {
		URL url = AbstractXPathTemplateTestCase.class.getResource("nonamespaces.xml");
		String result = template.evaluateAsString("/root/child/text", new StreamSource(url.toString()));
		Assert.assertEquals("Invalid result", "text", result);
	}

	@Test
	public void testInvalidExpression() {
		try {
			template.evaluateAsBoolean("\\", namespaces);
			Assert.fail("No XPathException thrown");
		}
		catch (XPathException ex) {
			// Expected behaviour
		}
	}

	@Test
	public void testEvaluateAsObject() throws Exception {
		String result = template.evaluateAsObject("/root/child", nonamespaces, new NodeMapper<String>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});
		Assert.assertNotNull("Invalid result", result);
		Assert.assertEquals("Invalid localname", "child", result);
	}

	@Test
	public void testEvaluate() throws Exception {
		List<String> results = template.evaluate("/root/child/*", nonamespaces, new NodeMapper<String>() {
			public String mapNode(Node node, int nodeNum) throws DOMException {
				return node.getLocalName();
			}
		});
		Assert.assertNotNull("Invalid result", results);
		Assert.assertEquals("Invalid amount of results", 3, results.size());
		Assert.assertEquals("Invalid first result", "text", results.get(0));
		Assert.assertEquals("Invalid first result", "number", results.get(1));
		Assert.assertEquals("Invalid first result", "boolean", results.get(2));
	}
}
