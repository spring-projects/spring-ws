/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap12.AbstractSoap12BodyTests;
import org.springframework.xml.transform.StringSource;

class AxiomSoap12BodyTests extends AbstractSoap12BodyTests {

	@Override
	protected SoapBody createSoapBody() {

		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP12Factory();
		AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);
		return axiomSoapMessage.getSoapBody();
	}

	@Test
	void testPayloadNoCaching() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.setSoapVersion(SoapVersion.SOAP_12);

		AxiomSoapMessage axiomSoapMessage = messageFactory.createWebServiceMessage();
		this.soapBody = axiomSoapMessage.getSoapBody();

		String payload = "<payload xmlns='http://www.springframework.org' />";
		this.transformer.transform(new StringSource(payload), this.soapBody.getPayloadResult());

		assertPayloadEqual(payload);
	}

}
