/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

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
		assertNotNull("No SoapHeaderElement returned", headerElement);
		assertEquals("Invalid qName for element", qName, headerElement.getName());
		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();
		assertTrue("SoapHeader has no elements", iterator.hasNext());
		String payload = "<content xmlns='http://www.springframework.org'/>";
		transformer.transform(new StringSource(payload), headerElement.getResult());
		assertHeaderElementEqual(headerElement,
				"<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>");
	}

	@Test
	public void testRemoveHeaderElement() throws Exception {
		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		soapHeader.removeHeaderElement(qName);
		soapHeader.addHeaderElement(qName);
		soapHeader.removeHeaderElement(qName);
		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();
		assertFalse("SoapHeader has elements", iterator.hasNext());
	}

	@Test
	public void testExamineAllHeaderElement() throws Exception {
		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
		assertEquals("Invalid qName for element", qName, headerElement.getName());
		assertNotNull("No SoapHeaderElement returned", headerElement);
		String payload = "<content xmlns='http://www.springframework.org'/>";
		transformer.transform(new StringSource(payload), headerElement.getResult());
		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();
		assertNotNull("header element iterator is null", iterator);
		assertTrue("header element iterator has no elements", iterator.hasNext());
		headerElement = iterator.next();
		assertEquals("Invalid qName for element", qName, headerElement.getName());
		StringResult result = new StringResult();
		transformer.transform(headerElement.getSource(), result);
		assertXMLEqual("Invalid contents of header element",
				"<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>",
				result.toString());
		assertFalse("header element iterator has too many elements", iterator.hasNext());
	}

	@Test
	public void testExamineHeaderElementWithName() throws Exception {
		QName name1 = new QName(NAMESPACE, "name1", PREFIX);
		QName name2 = new QName(NAMESPACE, "name2", PREFIX);
		soapHeader.addHeaderElement(name1);
		soapHeader.addHeaderElement(name2);
		Iterator<SoapHeaderElement> iterator = soapHeader.examineHeaderElements(name1);
		assertNotNull("header element iterator is null", iterator);
		assertTrue("header element iterator has no elements", iterator.hasNext());
		SoapHeaderElement headerElement = iterator.next();
		assertEquals("Invalid qName for element", name1, headerElement.getName());
		assertFalse("header element iterator has too many elements", iterator.hasNext());
	}

	@Test
	public void testExamineMustUnderstandHeaderElements() throws Exception {
		QName qName1 = new QName(NAMESPACE, "localName1", PREFIX);
		SoapHeaderElement headerElement1 = soapHeader.addHeaderElement(qName1);
		headerElement1.setMustUnderstand(true);
		headerElement1.setActorOrRole("role1");
		QName qName2 = new QName(NAMESPACE, "localName2", PREFIX);
		SoapHeaderElement headerElement2 = soapHeader.addHeaderElement(qName2);
		headerElement2.setMustUnderstand(true);
		headerElement2.setActorOrRole("role2");
		Iterator<SoapHeaderElement> iterator = soapHeader.examineMustUnderstandHeaderElements("role1");
		assertNotNull("header element iterator is null", iterator);
		assertTrue("header element iterator has no elements", iterator.hasNext());
		SoapHeaderElement headerElement = iterator.next();
		assertEquals("Invalid name on header element", qName1, headerElement.getName());
		assertTrue("MustUnderstand not set on header element", headerElement.getMustUnderstand());
		assertEquals("Invalid role on header element", "role1", headerElement.getActorOrRole());
		assertFalse("header element iterator has too many elements", iterator.hasNext());
	}

	@Test
	public void testGetResult() throws Exception {
		String content = "<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>";
		transformer.transform(new StringSource(content), soapHeader.getResult());
		Iterator<SoapHeaderElement> iterator = soapHeader.examineAllHeaderElements();
		assertTrue("Header has no children", iterator.hasNext());
		SoapHeaderElement headerElement = iterator.next();
		assertFalse("Header has too many children", iterator.hasNext());
		assertHeaderElementEqual(headerElement, content);
	}

	protected void assertHeaderElementEqual(SoapHeaderElement headerElement, String expected) throws Exception {
		StringResult result = new StringResult();
		transformer.transform(headerElement.getSource(), result);
		assertXMLEqual("Invalid contents of header element", expected, result.toString());
	}

}
