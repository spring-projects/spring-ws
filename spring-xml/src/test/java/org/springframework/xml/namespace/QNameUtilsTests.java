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

package org.springframework.xml.namespace;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.xml.DocumentBuilderFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

class QNameUtilsTests {

	@Test
	void testValidQNames() {

		assertThat(QNameUtils.validateQName("{namespace}local")).isTrue();
		assertThat(QNameUtils.validateQName("local")).isTrue();
	}

	@Test
	void testInvalidQNames() {

		assertThat(QNameUtils.validateQName(null)).isFalse();
		assertThat(QNameUtils.validateQName("")).isFalse();
		assertThat(QNameUtils.validateQName("{namespace}")).isFalse();
		assertThat(QNameUtils.validateQName("{namespace")).isFalse();
	}

	@Test
	void testGetQNameForNodeNoNamespace() throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactoryUtils.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element element = document.createElement("localname");
		QName qName = QNameUtils.getQNameForNode(element);

		assertThat(qName).isNotNull();
		assertThat(qName.getLocalPart()).isEqualTo("localname");
		assertThat(qName.getNamespaceURI()).isEmpty();
		assertThat(qName.getPrefix()).isEmpty();
	}

	@Test
	void testGetQNameForNodeNoPrefix() throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactoryUtils.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element element = document.createElementNS("namespace", "localname");
		QName qName = QNameUtils.getQNameForNode(element);

		assertThat(qName).isNotNull();
		assertThat(qName.getLocalPart()).isEqualTo("localname");
		assertThat(qName.getNamespaceURI()).isEqualTo("namespace");
		assertThat(qName.getPrefix()).isEmpty();
	}

	@Test
	void testGetQNameForNode() throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactoryUtils.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element element = document.createElementNS("namespace", "prefix:localname");
		QName qName = QNameUtils.getQNameForNode(element);

		assertThat(qName).isNotNull();
		assertThat(qName.getLocalPart()).isEqualTo("localname");
		assertThat(qName.getNamespaceURI()).isEqualTo("namespace");
		assertThat(qName.getPrefix()).isEqualTo("prefix");
	}

	@Test
	void testToQualifiedNamePrefix() {

		QName qName = new QName("namespace", "localName", "prefix");
		String result = QNameUtils.toQualifiedName(qName);

		assertThat(result).isEqualTo("prefix:localName");
	}

	@Test
	void testToQualifiedNameNoPrefix() {

		QName qName = new QName("localName");
		String result = QNameUtils.toQualifiedName(qName);

		assertThat(result).isEqualTo("localName");
	}

	@Test
	void testToQNamePrefix() {

		QName result = QNameUtils.toQName("namespace", "prefix:localName");

		assertThat(result.getNamespaceURI()).isEqualTo("namespace");
		assertThat(result.getPrefix()).isEqualTo("prefix");
		assertThat(result.getLocalPart()).isEqualTo("localName");
	}

	@Test
	void testToQNameNoPrefix() {

		QName result = QNameUtils.toQName("namespace", "localName");

		assertThat(result.getNamespaceURI()).isEqualTo("namespace");
		assertThat(result.getPrefix()).isEqualTo("");
		assertThat(result.getLocalPart()).isEqualTo("localName");
	}

}
