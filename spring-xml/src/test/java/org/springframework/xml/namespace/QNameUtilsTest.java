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

package org.springframework.xml.namespace;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.util.StringUtils;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class QNameUtilsTest {

	@Test
	public void testValidQNames() {
		Assert.assertTrue("Namespace QName not validated", QNameUtils.validateQName("{namespace}local"));
		Assert.assertTrue("No Namespace QName not validated", QNameUtils.validateQName("local"));
	}

	@Test
	public void testInvalidQNames() {
		Assert.assertFalse("Null QName validated", QNameUtils.validateQName(null));
		Assert.assertFalse("Empty QName validated", QNameUtils.validateQName(""));
		Assert.assertFalse("Invalid QName validated", QNameUtils.validateQName("{namespace}"));
		Assert.assertFalse("Invalid QName validated", QNameUtils.validateQName("{namespace"));
	}

	@Test
	public void testGetQNameForNodeNoNamespace() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element element = document.createElement("localname");
		QName qName = QNameUtils.getQNameForNode(element);
		Assert.assertNotNull("getQNameForNode returns null", qName);
		Assert.assertEquals("QName has invalid localname", "localname", qName.getLocalPart());
		Assert.assertFalse("Qname has invalid namespace", StringUtils.hasLength(qName.getNamespaceURI()));
		Assert.assertFalse("Qname has invalid prefix", StringUtils.hasLength(qName.getPrefix()));

	}

	@Test
	public void testGetQNameForNodeNoPrefix() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element element = document.createElementNS("namespace", "localname");
		QName qName = QNameUtils.getQNameForNode(element);
		Assert.assertNotNull("getQNameForNode returns null", qName);
		Assert.assertEquals("QName has invalid localname", "localname", qName.getLocalPart());
		Assert.assertEquals("Qname has invalid namespace", "namespace", qName.getNamespaceURI());
		Assert.assertFalse("Qname has invalid prefix", StringUtils.hasLength(qName.getPrefix()));
	}

	@Test
	public void testGetQNameForNode() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element element = document.createElementNS("namespace", "prefix:localname");
		QName qName = QNameUtils.getQNameForNode(element);
		Assert.assertNotNull("getQNameForNode returns null", qName);
		Assert.assertEquals("QName has invalid localname", "localname", qName.getLocalPart());
		Assert.assertEquals("Qname has invalid namespace", "namespace", qName.getNamespaceURI());
		Assert.assertEquals("Qname has invalid prefix", "prefix", qName.getPrefix());
	}

	@Test
	public void testToQualifiedNamePrefix() throws Exception {
		QName qName = new QName("namespace", "localName", "prefix");
		String result = QNameUtils.toQualifiedName(qName);
		Assert.assertEquals("Invalid result", "prefix:localName", result);
	}

	@Test
	public void testToQualifiedNameNoPrefix() throws Exception {
		QName qName = new QName("localName");
		String result = QNameUtils.toQualifiedName(qName);
		Assert.assertEquals("Invalid result", "localName", result);
	}

	@Test
	public void testToQNamePrefix() throws Exception {
		QName result = QNameUtils.toQName("namespace", "prefix:localName");
		Assert.assertEquals("invalid namespace", "namespace", result.getNamespaceURI());
		Assert.assertEquals("invalid prefix", "prefix", result.getPrefix());
		Assert.assertEquals("invalid localname", "localName", result.getLocalPart());
	}

	@Test
	public void testToQNameNoPrefix() throws Exception {
		QName result = QNameUtils.toQName("namespace", "localName");
		Assert.assertEquals("invalid namespace", "namespace", result.getNamespaceURI());
		Assert.assertEquals("invalid prefix", "", result.getPrefix());
		Assert.assertEquals("invalid localname", "localName", result.getLocalPart());
	}


}
