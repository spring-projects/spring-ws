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

package org.springframework.ws.soap.soap12;

import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.soap.AbstractSoapBodyTests;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoap12BodyTests extends AbstractSoapBodyTests {

	@Test
	void testGetType() {
		assertThat(this.soapBody).isInstanceOf(Soap12Body.class);
	}

	@Test
	void testGetName() {
		assertThat(this.soapBody.getName()).isEqualTo(new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "Body"));
	}

	@Test
	void testGetSource() throws Exception {

		StringResult result = new StringResult();
		this.transformer.transform(this.soapBody.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<Body xmlns='http://www.w3.org/2003/05/soap-envelope' />")
			.ignoreWhitespace()
			.areSimilar();
	}

	@Test
	void testAddMustUnderstandFault() throws Exception {

		SoapFault fault = this.soapBody.addMustUnderstandFault("SOAP Must Understand Error", Locale.ENGLISH);

		assertThat(fault.getFaultCode())
			.isEqualTo(new QName("http://www.w3.org/2003/05/soap-envelope", "MustUnderstand"));

		StringResult result = new StringResult();
		this.transformer.transform(fault.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>"
					+ "<soapenv:Code><soapenv:Value>" + this.soapBody.getName().getPrefix()
					+ ":MustUnderstand</soapenv:Value></soapenv:Code>"
					+ "<soapenv:Reason><soapenv:Text xml:lang='en'>SOAP Must Understand Error</soapenv:Text>"
					+ "</soapenv:Reason></soapenv:Fault>")
			.ignoreWhitespace()
			.areSimilar();
	}

	@Test
	void testAddSenderFault() throws Exception {

		SoapFault fault = this.soapBody.addClientOrSenderFault("faultString", Locale.ENGLISH);

		assertThat(fault.getFaultCode()).isEqualTo(new QName("http://www.w3.org/2003/05/soap-envelope", "Sender"));

		StringResult result = new StringResult();
		this.transformer.transform(fault.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>"
					+ "<soapenv:Code><soapenv:Value>" + this.soapBody.getName().getPrefix()
					+ ":Sender</soapenv:Value></soapenv:Code>"
					+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
					+ "</soapenv:Fault>")
			.ignoreWhitespace()
			.areSimilar();
	}

	@Test
	void testAddReceiverFault() throws Exception {

		SoapFault fault = this.soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);

		assertThat(fault.getFaultCode()).isEqualTo(new QName("http://www.w3.org/2003/05/soap-envelope", "Receiver"));

		StringResult result = new StringResult();
		this.transformer.transform(fault.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>"
					+ "<soapenv:Code><soapenv:Value>" + this.soapBody.getName().getPrefix()
					+ ":Receiver</soapenv:Value></soapenv:Code>"
					+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
					+ "</soapenv:Fault>")
			.ignoreWhitespace()
			.areSimilar();
	}

	@Test
	void testAddFaultWithDetail() throws Exception {

		SoapFault fault = this.soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);
		SoapFaultDetail detail = fault.addFaultDetail();
		SoapFaultDetailElement detailElement = detail
			.addFaultDetailElement(new QName("namespace", "localPart", "prefix"));
		StringSource detailContents = new StringSource("<detailContents xmlns='namespace'/>");
		this.transformer.transform(detailContents, detailElement.getResult());
		StringResult result = new StringResult();
		this.transformer.transform(fault.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>"
					+ "<soapenv:Code><soapenv:Value>" + this.soapBody.getName().getPrefix()
					+ ":Receiver</soapenv:Value>" + "</soapenv:Code>"
					+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
					+ "<soapenv:Detail><prefix:localPart xmlns:prefix='namespace'><detailContents xmlns='namespace'/>"
					+ "</prefix:localPart></soapenv:Detail></soapenv:Fault>")
			.ignoreWhitespace()
			.areSimilar();
	}

	@Test
	void testAddFaultWithDetailResult() throws Exception {

		SoapFault fault = this.soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);
		SoapFaultDetail detail = fault.addFaultDetail();
		this.transformer.transform(new StringSource("<detailContents xmlns='namespace'/>"), detail.getResult());
		this.transformer.transform(new StringSource("<detailContents xmlns='namespace'/>"), detail.getResult());
		StringResult result = new StringResult();
		this.transformer.transform(fault.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>"
					+ "<soapenv:Code><soapenv:Value>" + this.soapBody.getName().getPrefix()
					+ ":Receiver</soapenv:Value>" + "</soapenv:Code>"
					+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
					+ "<soapenv:Detail>" + "<detailContents xmlns='namespace'/>" + "<detailContents xmlns='namespace'/>"
					+ "</soapenv:Detail></soapenv:Fault>")
			.ignoreWhitespace()
			.areSimilar();
	}

	@Test
	void testAddFaultWithSubcode() throws Exception {

		Soap12Fault fault = (Soap12Fault) this.soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);
		QName subcode1 = new QName("http://www.springframework.org", "Subcode1", "spring-ws");
		fault.addFaultSubcode(subcode1);
		QName subcode2 = new QName("http://www.springframework.org", "Subcode2", "spring-ws");
		fault.addFaultSubcode(subcode2);
		Iterator<QName> iterator = fault.getFaultSubcodes();

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(subcode1);
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo(subcode2);
		assertThat(iterator.hasNext()).isFalse();

		StringResult result = new StringResult();
		this.transformer.transform(fault.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>"
					+ "<soapenv:Code><soapenv:Value>" + this.soapBody.getName().getPrefix()
					+ ":Receiver</soapenv:Value>"
					+ "<soapenv:Subcode><soapenv:Value xmlns:spring-ws='http://www.springframework.org'>spring-ws:Subcode1</soapenv:Value>"
					+ "<soapenv:Subcode><soapenv:Value xmlns:spring-ws='http://www.springframework.org'>spring-ws:Subcode2</soapenv:Value>"
					+ "</soapenv:Subcode></soapenv:Subcode></soapenv:Code>"
					+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
					+ "</soapenv:Fault>")
			.ignoreWhitespace()
			.areSimilar();
	}

}
