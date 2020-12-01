/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.axiom;

import static org.assertj.core.api.Assertions.*;

import java.io.StringReader;

import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

@SuppressWarnings("Since15")
public class AxiomSoapFaultDetailTest {

	private static final String FAILING_FAULT = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n	 " + "<soapenv:Body>\n	" + "<soapenv:Fault>\n	"
			+ "<faultcode>Client</faultcode>\n  " + "<faultstring>Client Error</faultstring>\n	" + "<detail>\n "
			+ "<ns1:dispositionReport xmlns:ns1=\"urn:uddi-org:api_v3\">\n  " + "<ns1:result errno=\"10210\"/>\n  "
			+ "</ns1:dispositionReport>" + "</detail>" + "</soapenv:Fault>" + "</soapenv:Body>" + "</soapenv:Envelope>";

	private static final String SUCCEEDING_FAULT = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n	 " + "<soapenv:Body>\n	" + "<soapenv:Fault>\n	"
			+ "<faultcode>Client</faultcode>\n  " + "<faultstring>Client Error</faultstring>\n	" + "<detail>"
			+ "<ns1:dispositionReport xmlns:ns1=\"urn:uddi-org:api_v3\">\n  " + "<ns1:result errno=\"10210\"/>\n  "
			+ "</ns1:dispositionReport>" + "</detail>" + "</soapenv:Fault>" + "</soapenv:Body>" + "</soapenv:Envelope>";

	private AxiomSoapMessage failingMessage;

	private AxiomSoapMessage succeedingMessage;

	@BeforeEach
	public void setUp() throws Exception {

		SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(new StringReader(FAILING_FAULT));
		SOAPMessage soapMessage = builder.getSOAPMessage();

		failingMessage = new AxiomSoapMessage(soapMessage, null, false, true);

		builder = OMXMLBuilderFactory.createSOAPModelBuilder(new StringReader(SUCCEEDING_FAULT));
		soapMessage = builder.getSOAPMessage();

		succeedingMessage = new AxiomSoapMessage(soapMessage, null, false, true);

	}

	@Test
	public void testGetDetailEntriesWorksWithWhitespaceNodes() {

		SoapFault fault = failingMessage.getSoapBody().getFault();

		assertThat(fault).isNotNull();
		assertThat(fault.getFaultDetail()).isNotNull();

		SoapFaultDetail detail = fault.getFaultDetail();

		assertThat(detail.getDetailEntries().hasNext()).isTrue();

		detail.getDetailEntries().next();
	}

	@Test
	public void testGetDetailEntriesWorksWithoutWhitespaceNodes() {

		SoapFault fault = succeedingMessage.getSoapBody().getFault();

		assertThat(fault).isNotNull();
		assertThat(fault.getFaultDetail()).isNotNull();

		SoapFaultDetail detail = fault.getFaultDetail();

		assertThat(detail.getDetailEntries().hasNext()).isTrue();

		detail.getDetailEntries().next();
	}
}
