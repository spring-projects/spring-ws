/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.xml.xsd.commons;

import javax.xml.transform.dom.DOMSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.xsd.AbstractXsdSchemaTests;
import org.springframework.xml.xsd.XsdSchema;

import static org.assertj.core.api.Assertions.assertThat;

class CommonsXsdSchemaTests extends AbstractXsdSchemaTests {

	@Override
	protected XsdSchema createSchema(Resource resource) throws Exception {

		XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
		XmlSchema schema = schemaCollection.read(SaxUtils.createInputSource(resource));

		return new CommonsXsdSchema(schema);
	}

	@Test
	void testXmime() throws Exception {

		Resource resource = new ClassPathResource("xmime.xsd", AbstractXsdSchemaTests.class);
		XsdSchema schema = createSchema(resource);
		String namespace = "urn:test";

		assertThat(schema.getTargetNamespace()).isEqualTo(namespace);

		Document result = (Document) ((DOMSource) schema.getSource()).getNode();
		Element schemaElement = result.getDocumentElement();
		Element elementElement = (Element) schemaElement.getFirstChild();

		assertThat(elementElement.getAttributeNS("http://www.w3.org/2005/05/xmlmime", "expectedContentTypes"))
			.isNotNull();
	}

}
