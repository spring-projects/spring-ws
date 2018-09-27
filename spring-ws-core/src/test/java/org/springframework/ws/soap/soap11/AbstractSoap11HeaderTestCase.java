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

import java.util.Iterator;
import javax.xml.namespace.QName;

import static org.xmlunit.assertj.XmlAssert.assertThat;
import static org.junit.Assert.*;
import org.junit.Test;

import org.springframework.ws.soap.AbstractSoapHeaderTestCase;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;

public abstract class AbstractSoap11HeaderTestCase extends AbstractSoapHeaderTestCase {

	private static final String PREFIX = "spring";

	@Test
	public void testGetType() {
		assertTrue("Invalid type returned", soapHeader instanceof Soap11Header);
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals("Invalid qualified name", new QName(SoapVersion.SOAP_11.getEnvelopeNamespaceUri(), "Header"),
				soapHeader.getName());
	}

	@Test
	public void testGetSource() throws Exception {
		StringResult result = new StringResult();
		transformer.transform(soapHeader.getSource(), result);
		assertThat(result.toString()).and("<SOAP-ENV:Header xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' />").areSimilar();
	}

	@Test
	public void testExamineHeaderElementsToProcessActors() throws Exception {
		QName qName = new QName(NAMESPACE, "localName1", PREFIX);
		SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole("role1");
		qName = new QName(NAMESPACE, "localName2", PREFIX);
		headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole("role2");
		qName = new QName(NAMESPACE, "localName3", PREFIX);
		headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole(SoapVersion.SOAP_11.getNextActorOrRoleUri());
		Iterator<SoapHeaderElement> iterator = ((Soap11Header) soapHeader).examineHeaderElementsToProcess(new String[]{"role1"});
		assertNotNull("header element iterator is null", iterator);
		assertTrue("header element iterator has no elements", iterator.hasNext());
		checkHeaderElement(iterator.next());
		assertTrue("header element iterator has no elements", iterator.hasNext());
		checkHeaderElement(iterator.next());
		assertFalse("header element iterator has too many elements", iterator.hasNext());
	}

	@Test
	public void testExamineHeaderElementsToProcessNoActors() throws Exception {
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
		assertNotNull("header element iterator is null", iterator);
		assertTrue("header element iterator has no elements", iterator.hasNext());
		checkHeaderElement(iterator.next());
		assertTrue("header element iterator has no elements", iterator.hasNext());
		checkHeaderElement(iterator.next());
		assertFalse("header element iterator has too many elements", iterator.hasNext());
	}

	private void checkHeaderElement(SoapHeaderElement headerElement) {
		QName name = headerElement.getName();
		assertTrue("Invalid name on header element", new QName(NAMESPACE, "localName1", PREFIX).equals(name) ||
				new QName(NAMESPACE, "localName3", PREFIX).equals(name));
	}

}
