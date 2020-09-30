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

package org.springframework.ws.soap.soap12;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.springframework.ws.soap.AbstractSoapBodyTestCase;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSoap12BodyTestCase extends AbstractSoapBodyTestCase {

	@Test
	public void testGetType() {
		assertTrue("Invalid type returned", soapBody instanceof Soap12Body);
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals("Invalid qualified name", new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "Body"),
				soapBody.getName());
	}

	@Test
	public void testGetSource() throws Exception {
		StringResult result = new StringResult();
		transformer.transform(soapBody.getSource(), result);
		assertXMLEqual("Invalid contents of body", "<Body xmlns='http://www.w3.org/2003/05/soap-envelope' />",
				result.toString());
	}

	@Test
	public void testAddMustUnderstandFault() throws Exception {
		SoapFault fault = soapBody.addMustUnderstandFault("SOAP Must Understand Error", Locale.ENGLISH);
		assertEquals("Invalid fault code", new QName("http://www.w3.org/2003/05/soap-envelope", "MustUnderstand"),
				fault.getFaultCode());
		StringResult result = new StringResult();
		transformer.transform(fault.getSource(), result);
		assertXMLEqual("Invalid contents of body",
				"<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" + "<soapenv:Code><soapenv:Value>"
						+ soapBody.getName().getPrefix() + ":MustUnderstand</soapenv:Value></soapenv:Code>"
						+ "<soapenv:Reason><soapenv:Text xml:lang='en'>SOAP Must Understand Error</soapenv:Text>"
						+ "</soapenv:Reason></soapenv:Fault>",
				result.toString());
	}

	@Test
	public void testAddSenderFault() throws Exception {
		SoapFault fault = soapBody.addClientOrSenderFault("faultString", Locale.ENGLISH);
		assertEquals("Invalid fault code", new QName("http://www.w3.org/2003/05/soap-envelope", "Sender"),
				fault.getFaultCode());
		StringResult result = new StringResult();
		transformer.transform(fault.getSource(), result);
		assertXMLEqual("Invalid contents of body",
				"<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" + "<soapenv:Code><soapenv:Value>"
						+ soapBody.getName().getPrefix() + ":Sender</soapenv:Value></soapenv:Code>"
						+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
						+ "</soapenv:Fault>",
				result.toString());
	}

	@Test
	public void testAddReceiverFault() throws Exception {
		SoapFault fault = soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);
		assertEquals("Invalid fault code", new QName("http://www.w3.org/2003/05/soap-envelope", "Receiver"),
				fault.getFaultCode());
		StringResult result = new StringResult();
		transformer.transform(fault.getSource(), result);
		assertXMLEqual("Invalid contents of body",
				"<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" + "<soapenv:Code><soapenv:Value>"
						+ soapBody.getName().getPrefix() + ":Receiver</soapenv:Value></soapenv:Code>"
						+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
						+ "</soapenv:Fault>",
				result.toString());
	}

	@Test
	public void testAddFaultWithDetail() throws Exception {
		SoapFault fault = soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);
		SoapFaultDetail detail = fault.addFaultDetail();
		SoapFaultDetailElement detailElement = detail.addFaultDetailElement(new QName("namespace", "localPart", "prefix"));
		StringSource detailContents = new StringSource("<detailContents xmlns='namespace'/>");
		transformer.transform(detailContents, detailElement.getResult());
		StringResult result = new StringResult();
		transformer.transform(fault.getSource(), result);
		assertXMLEqual("Invalid source for body",
				"<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" + "<soapenv:Code><soapenv:Value>"
						+ soapBody.getName().getPrefix() + ":Receiver</soapenv:Value>" + "</soapenv:Code>"
						+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
						+ "<soapenv:Detail><prefix:localPart xmlns:prefix='namespace'><detailContents xmlns='namespace'/>"
						+ "</prefix:localPart></soapenv:Detail></soapenv:Fault>",
				result.toString());
	}

	@Test
	public void testAddFaultWithDetailResult() throws Exception {
		SoapFault fault = soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);
		SoapFaultDetail detail = fault.addFaultDetail();
		transformer.transform(new StringSource("<detailContents xmlns='namespace'/>"), detail.getResult());
		transformer.transform(new StringSource("<detailContents xmlns='namespace'/>"), detail.getResult());
		StringResult result = new StringResult();
		transformer.transform(fault.getSource(), result);
		assertXMLEqual("Invalid source for body",
				"<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" + "<soapenv:Code><soapenv:Value>"
						+ soapBody.getName().getPrefix() + ":Receiver</soapenv:Value>" + "</soapenv:Code>"
						+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
						+ "<soapenv:Detail>" + "<detailContents xmlns='namespace'/>" + "<detailContents xmlns='namespace'/>"
						+ "</soapenv:Detail></soapenv:Fault>",
				result.toString());
	}

	@Test
	public void testAddFaultWithSubcode() throws Exception {
		Soap12Fault fault = (Soap12Fault) soapBody.addServerOrReceiverFault("faultString", Locale.ENGLISH);
		QName subcode1 = new QName("http://www.springframework.org", "Subcode1", "spring-ws");
		fault.addFaultSubcode(subcode1);
		QName subcode2 = new QName("http://www.springframework.org", "Subcode2", "spring-ws");
		fault.addFaultSubcode(subcode2);
		Iterator<QName> iterator = fault.getFaultSubcodes();
		assertTrue("No subcode found", iterator.hasNext());
		assertEquals("Invalid subcode", subcode1, iterator.next());
		assertTrue("No subcode found", iterator.hasNext());
		assertEquals("Invalid subcode", subcode2, iterator.next());
		assertFalse("Subcode found", iterator.hasNext());
		StringResult result = new StringResult();
		transformer.transform(fault.getSource(), result);
		assertXMLEqual("Invalid source for body", "<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>"
				+ "<soapenv:Code><soapenv:Value>" + soapBody.getName().getPrefix() + ":Receiver</soapenv:Value>"
				+ "<soapenv:Subcode><soapenv:Value xmlns:spring-ws='http://www.springframework.org'>spring-ws:Subcode1</soapenv:Value>"
				+ "<soapenv:Subcode><soapenv:Value xmlns:spring-ws='http://www.springframework.org'>spring-ws:Subcode2</soapenv:Value>"
				+ "</soapenv:Subcode></soapenv:Subcode></soapenv:Code>"
				+ "<soapenv:Reason><soapenv:Text xml:lang='en'>faultString</soapenv:Text></soapenv:Reason>"
				+ "</soapenv:Fault>", result.toString());
	}

}
