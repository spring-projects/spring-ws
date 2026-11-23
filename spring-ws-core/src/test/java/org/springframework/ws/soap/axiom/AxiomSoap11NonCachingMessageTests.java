/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMSourcedElement;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageTests;

import static org.assertj.core.api.Assertions.assertThat;

public class AxiomSoap11NonCachingMessageTests extends AbstractSoap11MessageTests {

	@Override
	protected String getNS() {
		return "soapenv";
	}

	@Override
	protected SoapMessage createSoapMessage() {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.setSoapVersion(SoapVersion.SOAP_11);

		return messageFactory.createWebServiceMessage();
	}

	@Override
	public void testWriteToTransportOutputStream() throws Exception {

		super.testWriteToTransportOutputStream();

		SoapBody body = this.soapMessage.getSoapBody();
		OMSourcedElement axiomPayloadEle = (OMSourcedElement) ((AxiomSoapBody) body).getAxiomElement()
			.getFirstElement();

		assertThat(axiomPayloadEle.isExpanded()).isFalse();

		axiomPayloadEle.getFirstElement();

		assertThat(axiomPayloadEle.isExpanded()).isTrue();
		assertThat(axiomPayloadEle.getLocalName()).isEqualTo("payload");
	}

}
