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

import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;

public class SoapFaultAnnotationExceptionResolverTest {

	private SoapFaultAnnotationExceptionResolver resolver;

	@BeforeEach
	public void setUp() throws Exception {
		resolver = new SoapFaultAnnotationExceptionResolver();
	}

	@Test
	public void testResolveExceptionClientSoap11() throws Exception {

		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(factory);

		boolean result = resolver.resolveException(context, null, new MyClientException());

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

		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(factory);

		boolean result = resolver.resolveException(context, null, new MySenderException());

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

		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(factory);

		boolean result = resolver.resolveException(context, null, new MyServerException());

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

		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage message = saajFactory.createMessage();
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

		boolean result = resolver.resolveException(context, null, new MyReceiverException());

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

		SoapFaultDefinition defaultFault = new SoapFaultDefinition();
		defaultFault.setFaultCode(SoapFaultDefinition.CLIENT);
		defaultFault.setFaultStringOrReason("faultstring");
		resolver.setDefaultFault(defaultFault);
		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(factory);

		boolean result = resolver.resolveException(context, null, new NonAnnotatedException());

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo("faultstring");
		assertThat(fault.getFaultDetail()).isNull();
	}

	@Test
	public void testResolveExceptionCustom() throws Exception {

		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(factory);

		boolean result = resolver.resolveException(context, null, new MyCustomException());

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(new QName("http://springframework.org/spring-ws", "Fault"));
		assertThat(fault.getFaultStringOrReason()).isEqualTo("MyCustomException thrown");
		assertThat(fault.getFaultStringLocale()).isEqualTo(new Locale("nl"));
	}

	@Test
	public void testResolveExceptionInheritance() throws Exception {

		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(factory);

		boolean result = resolver.resolveException(context, null, new MySubClientException());

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
	public void testResolveExceptionExceptionMessage() throws Exception {

		MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
		MessageContext context = new DefaultMessageContext(factory);

		boolean result = resolver.resolveException(context, null, new NoStringOrReasonException("Exception message"));

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo("Exception message");
		assertThat(fault.getFaultDetail()).isNull();
	}

	@SoapFault(faultCode = FaultCode.CLIENT, faultStringOrReason = "Client error")
	@SuppressWarnings("serial")
	public class MyClientException extends Exception {

	}

	@SuppressWarnings("serial")
	public class MySubClientException extends MyClientException {

	}

	@SoapFault(faultCode = FaultCode.CLIENT)
	@SuppressWarnings("serial")
	public class NoStringOrReasonException extends Exception {

		public NoStringOrReasonException(String message) {
			super(message);
		}
	}

	@SoapFault(faultCode = FaultCode.SENDER, faultStringOrReason = "Sender error")
	@SuppressWarnings("serial")
	public class MySenderException extends Exception {

	}

	@SoapFault(faultCode = FaultCode.SERVER, faultStringOrReason = "Server error")
	@SuppressWarnings("serial")
	public class MyServerException extends Exception {

	}

	@SoapFault(faultCode = FaultCode.RECEIVER, faultStringOrReason = "Receiver error")
	@SuppressWarnings("serial")
	public class MyReceiverException extends Exception {

	}

	@SoapFault(faultCode = FaultCode.CUSTOM, customFaultCode = "{http://springframework.org/spring-ws}Fault",
			faultStringOrReason = "MyCustomException thrown", locale = "nl")
	@SuppressWarnings("serial")
	public class MyCustomException extends Exception {

	}

	@SuppressWarnings("serial")
	public class NonAnnotatedException extends Exception {

	}

}
