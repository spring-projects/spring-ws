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

package org.springframework.ws.soap.server.endpoint;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageException;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;

public class SoapFaultMappingExceptionResolverTest {

	private SoapFaultMappingExceptionResolver resolver;

	@BeforeEach
	public void setUp() throws Exception {
		resolver = new SoapFaultMappingExceptionResolver();
	}

	@Test
	public void testGetDepth() {

		assertThat(resolver.getDepth("java.lang.Exception", new Exception())).isEqualTo(0);
		assertThat(resolver.getDepth("java.lang.Exception", new IllegalArgumentException())).isEqualTo(2);
		assertThat(resolver.getDepth("IllegalArgumentException", new IllegalStateException())).isEqualTo(-1);
	}

	@Test
	public void testResolveExceptionClientSoap11() throws Exception {

		Properties mappings = new Properties();
		mappings.setProperty(Exception.class.getName(), "SERVER, Server error");
		mappings.setProperty(RuntimeException.class.getName(), "CLIENT, Client error");
		resolver.setExceptionMappings(mappings);

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

		boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo("Client error");
		assertThat(fault.getFaultDetail()).isNull();
	}

	@Test
	public void testResolveExceptionSenderSoap12() throws Exception {

		Properties mappings = new Properties();
		mappings.setProperty(Exception.class.getName(), "RECEIVER, Receiver error, en");
		mappings.setProperty(RuntimeException.class.getName(), "SENDER, Sender error, en");
		resolver.setExceptionMappings(mappings);

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

		boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_12.getClientOrSenderFaultName());
		assertThat(fault.getFaultReasonText(Locale.ENGLISH)).isEqualTo("Sender error");
		assertThat(fault.getFaultDetail()).isNull();
	}

	@Test
	public void testResolveExceptionServerSoap11() throws Exception {

		Properties mappings = new Properties();
		mappings.setProperty(Exception.class.getName(), "CLIENT, Client error");
		mappings.setProperty(RuntimeException.class.getName(), "SERVER, Server error");
		resolver.setExceptionMappings(mappings);

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

		boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getServerOrReceiverFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo("Server error");
		assertThat(fault.getFaultDetail()).isNull();
	}

	@Test
	public void testResolveExceptionReceiverSoap12() throws Exception {

		Properties mappings = new Properties();
		mappings.setProperty(Exception.class.getName(), "SENDER, Sender error");
		mappings.setProperty(RuntimeException.class.getName(), "RECEIVER, Receiver error");
		resolver.setExceptionMappings(mappings);

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

		boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_12.getServerOrReceiverFaultName());
		assertThat(fault.getFaultReasonText(Locale.ENGLISH)).isEqualTo("Receiver error");
		assertThat(fault.getFaultDetail()).isNull();
	}

	@Test
	public void testResolveExceptionDefault() throws Exception {

		Properties mappings = new Properties();
		mappings.setProperty(SoapMessageException.class.getName(), "SERVER,Server error");
		resolver.setExceptionMappings(mappings);
		SoapFaultDefinition defaultFault = new SoapFaultDefinition();
		defaultFault.setFaultCode(SoapFaultDefinition.CLIENT);
		resolver.setDefaultFault(defaultFault);
		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

		boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo("bla");
		assertThat(fault.getFaultDetail()).isNull();

		// SWS-226
		result = resolver.resolveException(context, null, new IllegalArgumentException());

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo("java.lang.IllegalArgumentException");
		assertThat(fault.getFaultDetail()).isNull();
	}

	@Test
	public void testResolveNoMessageException() throws Exception {

		Properties mappings = new Properties();
		mappings.setProperty(IOException.class.getName(), "SERVER");
		resolver.setExceptionMappings(mappings);

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

		boolean result = resolver.resolveException(context, null, new IOException());

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getServerOrReceiverFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo("java.io.IOException");
		assertThat(fault.getFaultDetail()).isNull();
	}
}
