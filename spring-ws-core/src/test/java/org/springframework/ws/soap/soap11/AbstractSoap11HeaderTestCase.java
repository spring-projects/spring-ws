/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.soap11;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.AbstractSoapHeaderTestCase;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractSoap11HeaderTestCase extends AbstractSoapHeaderTestCase {

	private static final String PREFIX = "spring";

	@Test
	public void testGetType() {
		assertThat(soapHeader).isInstanceOf(Soap11Header.class);
	}

	@Test
	public void testGetName() {
		assertThat(soapHeader.getName()).isEqualTo(new QName(SoapVersion.SOAP_11.getEnvelopeNamespaceUri(), "Header"));
	}

	@Test
	public void testGetSource() throws Exception {

		StringResult result = new StringResult();
		transformer.transform(soapHeader.getSource(), result);

		XmlAssert.assertThat(result.toString())
				.and("<SOAP-ENV:Header xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' />").ignoreWhitespace()
				.areSimilar();
	}

	@Test
	public void testExamineHeaderElementsToProcessActors() {

		QName qName = new QName(NAMESPACE, "localName1", PREFIX);
		SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole("role1");
		qName = new QName(NAMESPACE, "localName2", PREFIX);
		headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole("role2");
		qName = new QName(NAMESPACE, "localName3", PREFIX);
		headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole(SoapVersion.SOAP_11.getNextActorOrRoleUri());
		Iterator<SoapHeaderElement> iterator = ((Soap11Header) soapHeader)
				.examineHeaderElementsToProcess(new String[] { "role1" });

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		checkHeaderElement(iterator.next());

		assertThat(iterator.hasNext()).isTrue();

		checkHeaderElement(iterator.next());

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testExamineHeaderElementsToProcessNoActors() {

		QName qName = new QName(NAMESPACE, "localName1", PREFIX);
		SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole("");
		qName = new QName(NAMESPACE, "localName2", PREFIX);
		headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole("role1");
		qName = new QName(NAMESPACE, "localName3", PREFIX);
		headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole(SoapVersion.SOAP_11.getNextActorOrRoleUri());
		Iterator<SoapHeaderElement> iterator = ((Soap11Header) soapHeader).examineHeaderElementsToProcess(new String[0]);

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		checkHeaderElement(iterator.next());

		assertThat(iterator.hasNext()).isTrue();

		checkHeaderElement(iterator.next());

		assertThat(iterator.hasNext()).isFalse();
	}

	private void checkHeaderElement(SoapHeaderElement headerElement) {

		QName name = headerElement.getName();

		assertThat(name).is(new Condition<>(value -> value.equals(new QName(NAMESPACE, "localName1", PREFIX))
				|| value.equals(new QName(NAMESPACE, "localName3", PREFIX)), ""));
	}

}
