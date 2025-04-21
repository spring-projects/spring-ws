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

package org.springframework.ws.soap;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoapHeaderTests extends AbstractSoapElementTests {

	protected SoapHeader soapHeader;

	protected static final String NAMESPACE = "http://www.springframework.org";

	protected static final String PREFIX = "spring";

	@Override
	protected final SoapElement createSoapElement() throws Exception {

		this.soapHeader = createSoapHeader();
		return this.soapHeader;
	}

	protected abstract SoapHeader createSoapHeader() throws Exception;

	@Test
	void testAddHeaderElement() throws Exception {

		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		SoapHeaderElement headerElement = this.soapHeader.addHeaderElement(qName);

		assertThat(headerElement).isNotNull();
		assertThat(headerElement.getName()).isEqualTo(qName);

		Iterator<SoapHeaderElement> iterator = this.soapHeader.examineAllHeaderElements();

		assertThat(iterator.hasNext()).isTrue();

		String payload = "<content xmlns='http://www.springframework.org'/>";
		this.transformer.transform(new StringSource(payload), headerElement.getResult());

		assertHeaderElementEqual(headerElement,
				"<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>");
	}

	@Test
	void testRemoveHeaderElement() {

		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		this.soapHeader.removeHeaderElement(qName);
		this.soapHeader.addHeaderElement(qName);
		this.soapHeader.removeHeaderElement(qName);
		Iterator<SoapHeaderElement> iterator = this.soapHeader.examineAllHeaderElements();

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	void testExamineAllHeaderElement() throws Exception {

		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		SoapHeaderElement headerElement = this.soapHeader.addHeaderElement(qName);

		assertThat(headerElement.getName()).isEqualTo(qName);
		assertThat(headerElement).isNotNull();

		String payload = "<content xmlns='http://www.springframework.org'/>";
		this.transformer.transform(new StringSource(payload), headerElement.getResult());
		Iterator<SoapHeaderElement> iterator = this.soapHeader.examineAllHeaderElements();

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		headerElement = iterator.next();

		assertThat(headerElement.getName()).isEqualTo(qName);

		StringResult result = new StringResult();
		this.transformer.transform(headerElement.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>")
			.ignoreWhitespace()
			.areSimilar();
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	void testExamineHeaderElementWithName() {

		QName name1 = new QName(NAMESPACE, "name1", PREFIX);
		QName name2 = new QName(NAMESPACE, "name2", PREFIX);
		this.soapHeader.addHeaderElement(name1);
		this.soapHeader.addHeaderElement(name2);
		Iterator<SoapHeaderElement> iterator = this.soapHeader.examineHeaderElements(name1);

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		SoapHeaderElement headerElement = iterator.next();

		assertThat(headerElement.getName()).isEqualTo(name1);
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	void testExamineMustUnderstandHeaderElements() {

		QName qName1 = new QName(NAMESPACE, "localName1", PREFIX);
		SoapHeaderElement headerElement1 = this.soapHeader.addHeaderElement(qName1);
		headerElement1.setMustUnderstand(true);
		headerElement1.setActorOrRole("role1");
		QName qName2 = new QName(NAMESPACE, "localName2", PREFIX);
		SoapHeaderElement headerElement2 = this.soapHeader.addHeaderElement(qName2);
		headerElement2.setMustUnderstand(true);
		headerElement2.setActorOrRole("role2");
		Iterator<SoapHeaderElement> iterator = this.soapHeader.examineMustUnderstandHeaderElements("role1");

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		SoapHeaderElement headerElement = iterator.next();

		assertThat(headerElement.getName()).isEqualTo(qName1);
		assertThat(headerElement.getMustUnderstand()).isTrue();
		assertThat(headerElement.getActorOrRole()).isEqualTo("role1");
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	protected void testGetResult() throws Exception {

		String content = "<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>";
		this.transformer.transform(new StringSource(content), this.soapHeader.getResult());
		Iterator<SoapHeaderElement> iterator = this.soapHeader.examineAllHeaderElements();

		assertThat(iterator.hasNext()).isTrue();

		SoapHeaderElement headerElement = iterator.next();

		assertThat(iterator.hasNext()).isFalse();
		assertHeaderElementEqual(headerElement, content);
	}

	protected void assertHeaderElementEqual(SoapHeaderElement headerElement, String expected) throws Exception {

		StringResult result = new StringResult();
		this.transformer.transform(headerElement.getSource(), result);

		XmlAssert.assertThat(result.toString()).and(expected).ignoreWhitespace().areSimilar();
	}

}
