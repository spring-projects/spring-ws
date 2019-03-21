/*
 * Copyright 2005-2014 the original author or authors.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.junit.Test;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap11.AbstractSoap11BodyTestCase;
import org.springframework.xml.transform.StringSource;

public class AxiomSoap11BodyTest extends AbstractSoap11BodyTestCase {

	@Override
	protected SoapBody createSoapBody() throws Exception {
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);
		return axiomSoapMessage.getSoapBody();
	}

	@Test
	public void testPayloadNoCaching() throws Exception {
		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.setSoapVersion(SoapVersion.SOAP_11);

		AxiomSoapMessage axiomSoapMessage = messageFactory.createWebServiceMessage();
		soapBody = axiomSoapMessage.getSoapBody();

		String payload = "<payload xmlns='http://www.springframework.org' />";
		transformer.transform(new StringSource(payload), soapBody.getPayloadResult());
		assertPayloadEqual(payload);
	}

}
