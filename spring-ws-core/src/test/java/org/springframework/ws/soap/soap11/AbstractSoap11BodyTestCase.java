/*
 * Copyright 2005-2010 the original author or authors.
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

import java.util.Locale;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.AbstractSoapBodyTestCase;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractSoap11BodyTestCase extends AbstractSoapBodyTestCase {

	@Test
	public void testGetType() {
		assertThat(soapBody).isInstanceOf(Soap11Body.class);
	}

	@Test
	public void testGetName() {
		assertThat(soapBody.getName()).isEqualTo(SoapVersion.SOAP_11.getBodyName());
	}

	@Test
	public void testGetSource() throws Exception {

		StringResult result = new StringResult();
		transformer.transform(soapBody.getSource(), result);

		XmlAssert.assertThat(result.toString()).and("<Body xmlns='http://schemas.xmlsoap.org/soap/envelope/' />")
				.ignoreWhitespace().areSimilar();
	}

	@Test
	public void testAddMustUnderstandFault() throws Exception {

		SoapFault fault = soapBody.addMustUnderstandFault("SOAP Must Understand Error", null);

		assertThat(fault.getFaultCode())
				.isEqualTo(new QName("http://schemas.xmlsoap.org/soap/envelope/", "MustUnderstand"));
		assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" + "<faultcode>"
				+ soapBody.getName().getPrefix() + ":MustUnderstand</faultcode>"
				+ "<faultstring>SOAP Must Understand Error</faultstring></SOAP-ENV:Fault>");
	}

	@Test
	public void testAddClientFault() throws Exception {

		SoapFault fault = soapBody.addClientOrSenderFault("faultString", null);

		assertThat(fault.getFaultCode()).isEqualTo(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"));
		assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" + "<faultcode>"
				+ soapBody.getName().getPrefix() + ":Client</faultcode>" + "<faultstring>faultString</faultstring>"
				+ "</SOAP-ENV:Fault>");
	}

	@Test
	public void testAddServerFault() throws Exception {

		SoapFault fault = soapBody.addServerOrReceiverFault("faultString", null);

		assertThat(fault.getFaultCode()).isEqualTo(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"));
		assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" + "<faultcode>"
				+ soapBody.getName().getPrefix() + ":Server</faultcode>" + "<faultstring>faultString</faultstring>"
				+ "</SOAP-ENV:Fault>");
	}

	@Test
	public void testAddFault() throws Exception {

		QName faultCode = new QName("http://www.springframework.org", "fault", "spring");
		String faultString = "faultString";
		Soap11Fault fault = ((Soap11Body) soapBody).addFault(faultCode, faultString, Locale.ENGLISH);

		assertThat(fault).isNotNull();
		assertThat(soapBody.hasFault()).isTrue();
		assertThat(soapBody.getFault()).isNotNull();
		assertThat(fault.getFaultCode()).isEqualTo(faultCode);
		assertThat(fault.getFaultStringOrReason()).isEqualTo(faultString);
		assertThat(fault.getFaultStringLocale()).isEqualTo(Locale.ENGLISH);

		String actor = "http://www.springframework.org/actor";
		fault.setFaultActorOrRole(actor);

		assertThat(fault.getFaultActorOrRole()).isEqualTo(actor);
		assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' "
				+ "xmlns:spring='http://www.springframework.org'>" + "<faultcode>spring:fault</faultcode>"
				+ "<faultstring xml:lang='en'>" + faultString + "</faultstring>" + "<faultactor>" + actor + "</faultactor>"
				+ "</SOAP-ENV:Fault>");
	}

	@Test
	public void testAddFaultNoPrefix() {

		QName faultCode = new QName("http://www.springframework.org", "fault");
		String faultString = "faultString";
		Soap11Fault fault = ((Soap11Body) soapBody).addFault(faultCode, faultString, Locale.ENGLISH);

		assertThat(fault).isNotNull();
		assertThat(soapBody.hasFault()).isTrue();
		assertThat(soapBody.getFault()).isNotNull();
		assertThat(fault.getFaultCode()).isEqualTo(faultCode);
		assertThat(fault.getFaultStringOrReason()).isEqualTo(faultString);
		assertThat(fault.getFaultStringLocale()).isEqualTo(Locale.ENGLISH);

		String actor = "http://www.springframework.org/actor";
		fault.setFaultActorOrRole(actor);

		assertThat(fault.getFaultActorOrRole()).isEqualTo(actor);
	}

	@Test
	public void testAddFaultWithDetail() throws Exception {

		QName faultCode = new QName("http://www.springframework.org", "fault", "spring");
		String faultString = "faultString";
		SoapFault fault = ((Soap11Body) soapBody).addFault(faultCode, faultString, null);
		SoapFaultDetail detail = fault.addFaultDetail();
		QName detailName = new QName("http://www.springframework.org", "detailEntry", "spring");
		SoapFaultDetailElement detailElement1 = detail.addFaultDetailElement(detailName);
		StringSource detailContents = new StringSource("<detailContents xmlns='namespace'/>");
		transformer.transform(detailContents, detailElement1.getResult());
		SoapFaultDetailElement detailElement2 = detail.addFaultDetailElement(detailName);
		detailContents = new StringSource("<detailContents xmlns='namespace'/>");
		transformer.transform(detailContents, detailElement2.getResult());

		assertPayloadEqual(
				"<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' xmlns:spring='http://www.springframework.org'>"
						+ "<faultcode>spring:fault</faultcode>" + "<faultstring>" + faultString + "</faultstring>" + "<detail>"
						+ "<spring:detailEntry xmlns:spring='http://www.springframework.org'><detailContents xmlns='namespace'/></spring:detailEntry>"
						+ "<spring:detailEntry xmlns:spring='http://www.springframework.org'><detailContents xmlns='namespace'/></spring:detailEntry>"
						+ "</detail>" + "</SOAP-ENV:Fault>");

	}

	@Test
	public void testAddFaultWithDetailResult() throws Exception {

		SoapFault fault = ((Soap11Body) soapBody).addFault(new QName("namespace", "localPart", "prefix"), "Fault", null);
		SoapFaultDetail detail = fault.addFaultDetail();
		transformer.transform(new StringSource("<detailContents xmlns='namespace'/>"), detail.getResult());
		transformer.transform(new StringSource("<detailContents xmlns='namespace'/>"), detail.getResult());

		assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>"
				+ "<faultcode xmlns:prefix='namespace'>prefix:localPart</faultcode>" + "<faultstring>Fault</faultstring>"
				+ "<detail>" + "<detailContents xmlns='namespace'/>" + "<detailContents xmlns='namespace'/>"
				+ "</detail></SOAP-ENV:Fault>");
	}
}
