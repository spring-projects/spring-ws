/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.soap.soap12;

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

public abstract class AbstractSoap12HeaderTestCase extends AbstractSoapHeaderTestCase {

	@Test
	public void testGetType() {
		assertThat(soapHeader).isInstanceOf(Soap12Header.class);
	}

	@Test
	public void testGetName() {
		assertThat(soapHeader.getName()).isEqualTo(new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "Header"));
	}

	@Test
	public void testGetSource() throws Exception {

		StringResult result = new StringResult();
		transformer.transform(soapHeader.getSource(), result);

		XmlAssert.assertThat(result.toString()).and("<Header xmlns='http://www.w3.org/2003/05/soap-envelope' />")
				.ignoreWhitespace().areSimilar();
	}

	@Test
	public void testAddNotUnderstood() throws Exception {

		Soap12Header soap12Header = (Soap12Header) soapHeader;
		QName headerName = new QName("http://www.springframework.org", "NotUnderstood", "spring-ws");
		soap12Header.addNotUnderstoodHeaderElement(headerName);
		StringResult result = new StringResult();
		transformer.transform(soapHeader.getSource(), result);

		XmlAssert.assertThat(result.toString())
				.and("<Header xmlns='http://www.w3.org/2003/05/soap-envelope' >"
						+ "<NotUnderstood qname='spring-ws:NotUnderstood' xmlns:spring-ws='http://www.springframework.org' />"
						+ "</Header>")
				.ignoreWhitespace().areSimilar();
	}

	@Test
	public void testAddUpgrade() throws Exception {

		String[] supportedUris = new String[] { "http://schemas.xmlsoap.org/soap/envelope/",
				"http://www.w3.org/2003/05/soap-envelope" };
		Soap12Header soap12Header = (Soap12Header) soapHeader;
		SoapHeaderElement header = soap12Header.addUpgradeHeaderElement(supportedUris);
		StringResult result = new StringResult();
		transformer.transform(soapHeader.getSource(), result);

		assertThat(header.getName()).isEqualTo(new QName("http://www.w3.org/2003/05/soap-envelope", "Upgrade"));
		// XMLUnit can't test this:
		/*
				assertXMLEqual("Invalid contents of header", "<Header xmlns='http://www.w3.org/2003/05/soap-envelope' >" +
						"<Upgrade>" +
						"<SupportedEnvelope xmlns:ns0='http://schemas.xmlsoap.org/soap/envelope/' qname='ns0:Envelope'/>" +
						"<SupportedEnvelope xmlns:ns1='http://www.w3.org/2003/05/soap-envelope' qname='ns1:Envelope'/>" +
						"</Upgrade>" +
						"</Header>", result.toString());
		*/
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
		headerElement.setActorOrRole(SoapVersion.SOAP_12.getNextActorOrRoleUri());
		Iterator<SoapHeaderElement> iterator = ((Soap12Header) soapHeader)
				.examineHeaderElementsToProcess(new String[] { "role1" }, false);

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
		headerElement.setActorOrRole(SoapVersion.SOAP_12.getNextActorOrRoleUri());
		Iterator<SoapHeaderElement> iterator = ((Soap12Header) soapHeader).examineHeaderElementsToProcess(new String[0],
				false);

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		checkHeaderElement(iterator.next());

		assertThat(iterator.hasNext()).isTrue();

		checkHeaderElement(iterator.next());

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testExamineHeaderElementsToProcessUltimateDestination() {

		QName qName = new QName(NAMESPACE, "localName", PREFIX);
		SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
		headerElement.setActorOrRole(SoapVersion.SOAP_12.getUltimateReceiverRoleUri());
		Iterator<SoapHeaderElement> iterator = ((Soap12Header) soapHeader)
				.examineHeaderElementsToProcess(new String[] { "role" }, true);

		assertThat(iterator).isNotNull();

		headerElement = iterator.next();

		assertThat(headerElement.getName()).isEqualTo(new QName(NAMESPACE, "localName", PREFIX));
		assertThat(iterator.hasNext()).isFalse();
	}

	private void checkHeaderElement(SoapHeaderElement headerElement) {

		QName name = headerElement.getName();

		assertThat(name).is(new Condition<>(value -> new QName(NAMESPACE, "localName1", PREFIX).equals(value)
				|| new QName(NAMESPACE, "localName3", PREFIX).equals(value), ""));
	}
}
