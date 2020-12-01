/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractSoapHeaderTestCase extends AbstractSoapElementTestCase {

	protected SoapHeader soapHeader;

	protected static final String NAMESPACE = "http://www.springframework.org";

	protected static final String PREFIX = "spring";

	@Override
	protected final SoapElement createSoapElement() throws Exception {

		soapHeader = createSoapHeader();
		return soapHeader;
	}

	protected abstract SoapHeader createSoapHeader() throws Exception;

	@Test
	public void testAddHeaderElement() throws Exception {

		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);

		assertThat(headerElement).isNotNull();
		assertThat(headerElement.getName()).isEqualTo(qName);

		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();

		assertThat(iterator.hasNext()).isTrue();

		String payload = "<content xmlns='http://www.springframework.org'/>";
		transformer.transform(new StringSource(payload), headerElement.getResult());

		assertHeaderElementEqual(headerElement,
				"<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>");
	}

	@Test
	public void testRemoveHeaderElement() {

		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		soapHeader.removeHeaderElement(qName);
		soapHeader.addHeaderElement(qName);
		soapHeader.removeHeaderElement(qName);
		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testExamineAllHeaderElement() throws Exception {

		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);

		assertThat(headerElement.getName()).isEqualTo(qName);
		assertThat(headerElement).isNotNull();

		String payload = "<content xmlns='http://www.springframework.org'/>";
		transformer.transform(new StringSource(payload), headerElement.getResult());
		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		headerElement = iterator.next();

		assertThat(headerElement.getName()).isEqualTo(qName);

		StringResult result = new StringResult();
		transformer.transform(headerElement.getSource(), result);

		XmlAssert.assertThat(result.toString())
				.and("<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>")
				.ignoreWhitespace().areSimilar();
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testExamineHeaderElementWithName() {

		QName name1 = new QName(NAMESPACE, "name1", PREFIX);
		QName name2 = new QName(NAMESPACE, "name2", PREFIX);
		soapHeader.addHeaderElement(name1);
		soapHeader.addHeaderElement(name2);
		Iterator<SoapHeaderElement> iterator = soapHeader.examineHeaderElements(name1);

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		SoapHeaderElement headerElement = iterator.next();

		assertThat(headerElement.getName()).isEqualTo(name1);
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testExamineMustUnderstandHeaderElements() {

		QName qName1 = new QName(NAMESPACE, "localName1", PREFIX);
		SoapHeaderElement headerElement1 = soapHeader.addHeaderElement(qName1);
		headerElement1.setMustUnderstand(true);
		headerElement1.setActorOrRole("role1");
		QName qName2 = new QName(NAMESPACE, "localName2", PREFIX);
		SoapHeaderElement headerElement2 = soapHeader.addHeaderElement(qName2);
		headerElement2.setMustUnderstand(true);
		headerElement2.setActorOrRole("role2");
		Iterator<SoapHeaderElement> iterator = soapHeader.examineMustUnderstandHeaderElements("role1");

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		SoapHeaderElement headerElement = iterator.next();

		assertThat(headerElement.getName()).isEqualTo(qName1);
		assertThat(headerElement.getMustUnderstand()).isTrue();
		assertThat(headerElement.getActorOrRole()).isEqualTo("role1");
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testGetResult() throws Exception {

		String content = "<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>";
		transformer.transform(new StringSource(content), soapHeader.getResult());
		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();

		assertThat(iterator.hasNext()).isTrue();

		SoapHeaderElement headerElement = iterator.next();

		assertThat(iterator.hasNext()).isFalse();
		assertHeaderElementEqual(headerElement, content);
	}

	protected void assertHeaderElementEqual(SoapHeaderElement headerElement, String expected) throws Exception {

		StringResult result = new StringResult();
		transformer.transform(headerElement.getSource(), result);

		XmlAssert.assertThat(result.toString()).and(expected).ignoreWhitespace().areSimilar();
	}
}
