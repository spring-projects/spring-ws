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

package org.springframework.xml.xsd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.validation.XmlValidator;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractXsdSchemaTests {

	@Test
	void testSingle() throws Exception {
		Resource xsdResource = new ClassPathResource("single.xsd", AbstractXsdSchemaTests.class);
		XsdSchema xsdSchema = createSchema(xsdResource);
		assertThat(xsdSchema.getTargetNamespace()).isEqualTo("http://www.springframework.org/spring-ws/single/schema");
		Document actual = createDocument(xsdSchema);
		Document expected = createDocument(xsdResource);
		XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void testIncludes() throws Exception {
		Resource xsdResource = new ClassPathResource("including.xsd", AbstractXsdSchemaTests.class);
		XsdSchema xsdSchema = createSchema(xsdResource);
		assertThat(xsdSchema.getTargetNamespace()).isEqualTo("http://www.springframework.org/spring-ws/include/schema");
		Document expected = createDocument(xsdResource);
		Document actual = createDocument(xsdSchema);
		XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void testImports() throws Exception {
		Resource xsdResource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTests.class);
		XsdSchema xsdSchema = createSchema(xsdResource);
		assertThat(xsdSchema.getTargetNamespace())
			.isEqualTo("http://www.springframework.org/spring-ws/importing/schema");
		Document expected = createDocument(xsdResource);
		Document actual = createDocument(xsdSchema);
		XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void testXmlNamespace() throws Exception {
		Resource xsdResource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTests.class);
		XsdSchema xsdSchema = createSchema(xsdResource);
		assertThat(xsdSchema.getTargetNamespace()).isEqualTo("http://www.springframework.org/spring-ws/xmlNamespace");
		Document expected = createDocument(xsdResource);
		Document actual = createDocument(xsdSchema);
		XmlAssert.assertThat(actual).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void testCreateValidator() throws Exception {
		Resource xsdResource = new ClassPathResource("single.xsd", AbstractXsdSchemaTests.class);
		XsdSchema xsdSchema = createSchema(xsdResource);
		XmlValidator validator = xsdSchema.createValidator();
		assertThat(validator).isNotNull();
	}

	@Test
	void testLoadXsdSchemaConcurrently() throws Exception {
		ClassPathResource xsdResource = new ClassPathResource("single.xsd", AbstractXsdSchemaTests.class);
		XsdSchema xsdSchema = createSchema(xsdResource);
		int numberOfThreads = 4;
		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch readyToStart = new CountDownLatch(numberOfThreads);
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<Document>> documents = new ArrayList<>();
		try {
			for (int i = 0; i < numberOfThreads; i++) {
				documents.add(executorService.submit(() -> {
					readyToStart.countDown();
					startSignal.await();
					return createDocument(xsdSchema);
				}));
			}
			readyToStart.await();
			startSignal.countDown();
			Document expected = createDocument(xsdResource);
			assertThat(documents).hasSize(numberOfThreads)
				.extracting(Future::get)
				.allSatisfy((actual) -> XmlAssert.assertThat(actual).and(expected).ignoreWhitespace());
		}
		finally {
			executorService.shutdownNow();
		}
	}

	protected abstract XsdSchema createSchema(Resource resource) throws Exception;

	private Document createDocument(Resource resource) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		return documentBuilderFactory.newDocumentBuilder().parse(SaxUtils.createInputSource(resource));
	}

	private Document createDocument(XsdSchema schema) throws Exception {
		DOMResult domResult = new DOMResult();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformerFactory.newTransformer().transform(schema.getSource(), domResult);
		return (Document) domResult.getNode();
	}

}
