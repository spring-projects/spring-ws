/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.transform.TransformerException;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerObjectSupport;

public class AbstractMethodArgumentResolverTests extends TransformerObjectSupport {

	protected static final String NAMESPACE_URI = "http://springframework.org/ws";

	protected static final String LOCAL_NAME = "request";

	protected static final String XML = "<" + LOCAL_NAME + " xmlns=\"" + NAMESPACE_URI + "\"/>";

	protected MessageContext createSaajMessageContext() throws SOAPException {

		MessageFactory saajFactory = MessageFactory.newInstance();
		SOAPMessage saajMessage = saajFactory.createMessage();
		saajMessage.getSOAPBody().addChildElement(LOCAL_NAME, "", NAMESPACE_URI);

		return new DefaultMessageContext(new SaajSoapMessage(saajMessage), new SaajSoapMessageFactory(saajFactory));
	}

	protected MessageContext createMockMessageContext() throws TransformerException {

		MockWebServiceMessage request = new MockWebServiceMessage(new StringSource(XML));
		return new DefaultMessageContext(request, new MockWebServiceMessageFactory());
	}

	protected MessageContext createCachingAxiomMessageContext() throws Exception {
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory, true, false);
		transform(new StringSource(XML), request.getPayloadResult());
		AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
		soapMessageFactory.afterPropertiesSet();

		return new DefaultMessageContext(request, soapMessageFactory);
	}

	protected MessageContext createNonCachingAxiomMessageContext() throws Exception {
		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage request = new AxiomSoapMessage(axiomFactory, false, false);
		transform(new StringSource(XML), request.getPayloadResult());
		AxiomSoapMessageFactory soapMessageFactory = new AxiomSoapMessageFactory();
		soapMessageFactory.setPayloadCaching(false);
		soapMessageFactory.afterPropertiesSet();

		return new DefaultMessageContext(request, soapMessageFactory);
	}

}
