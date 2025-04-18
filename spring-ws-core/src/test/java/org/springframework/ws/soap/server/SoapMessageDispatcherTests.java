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

package org.springframework.ws.soap.server;

import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class SoapMessageDispatcherTests {

	private SoapMessageDispatcher dispatcher;

	private SoapEndpointInterceptor interceptorMock;

	@BeforeEach
	public void setUp() {

		this.interceptorMock = createMock(SoapEndpointInterceptor.class);
		this.dispatcher = new SoapMessageDispatcher();
	}

	@Test
	public void testProcessMustUnderstandHeadersUnderstoodSoap11() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		SOAPHeaderElement header = request.getSOAPHeader()
			.addHeaderElement(new QName("http://www.springframework.org", "Header"));
		header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
		header.setMustUnderstand(true);
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		expect(this.interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
				new SoapEndpointInterceptor[] { this.interceptorMock });

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isTrue();

		verify(this.interceptorMock);
	}

	@Test
	public void testProcessMustUnderstandHeadersUnderstoodSoap12() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		SOAPHeaderElement header = request.getSOAPHeader()
			.addHeaderElement(new QName("http://www.springframework.org", "Header"));
		header.setMustUnderstand(true);
		header.setRole(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		expect(this.interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
				new SoapEndpointInterceptor[] { this.interceptorMock });

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isTrue();

		verify(this.interceptorMock);
	}

	@Test
	public void testProcessMustUnderstandHeadersNotUnderstoodSoap11() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		SOAPHeaderElement header = request.getSOAPHeader()
			.addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
		header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
		header.setMustUnderstand(true);
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		expect(this.interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(false);

		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
				new SoapEndpointInterceptor[] { this.interceptorMock });

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isFalse();
		assertThat(context.hasResponse()).isTrue();

		SoapBody responseBody = ((SoapMessage) context.getResponse()).getSoapBody();

		assertThat(responseBody.hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) responseBody.getFault();

		assertThat(fault.getFaultCode()).isEqualTo(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "MustUnderstand"));
		assertThat(fault.getFaultStringOrReason())
			.isEqualTo(SoapMessageDispatcher.DEFAULT_MUST_UNDERSTAND_FAULT_STRING);
		assertThat(fault.getFaultStringLocale()).isEqualTo(Locale.ENGLISH);

		verify(this.interceptorMock);
	}

	@Test
	public void testProcessMustUnderstandHeadersNotUnderstoodSoap12() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		SOAPHeaderElement header = request.getSOAPHeader()
			.addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
		header.setMustUnderstand(true);
		header.setRole(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		expect(this.interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(false);

		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
				new SoapEndpointInterceptor[] { this.interceptorMock });

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isFalse();
		assertThat(context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) context.getResponse();
		SoapBody responseBody = response.getSoapBody();

		assertThat(responseBody.hasFault()).isTrue();

		Soap12Fault fault = (Soap12Fault) responseBody.getFault();

		assertThat(fault.getFaultCode()).isEqualTo(new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "MustUnderstand"));
		assertThat(fault.getFaultReasonText(Locale.ENGLISH))
			.isEqualTo(SoapMessageDispatcher.DEFAULT_MUST_UNDERSTAND_FAULT_STRING);

		SoapHeader responseHeader = response.getSoapHeader();
		Iterator<SoapHeaderElement> iterator = responseHeader.examineAllHeaderElements();

		assertThat(iterator.hasNext()).isTrue();

		SoapHeaderElement headerElement = iterator.next();

		assertThat(headerElement.getName())
			.isEqualTo(new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "NotUnderstood"));

		verify(this.interceptorMock);
	}

	@Test
	public void testProcessMustUnderstandHeadersForActorSoap11() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		SOAPHeaderElement header = request.getSOAPHeader()
			.addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
		String headerActor = "http://www/springframework.org/role";
		header.setActor(headerActor);
		header.setMustUnderstand(true);
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		expect(this.interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
				new SoapEndpointInterceptor[] { this.interceptorMock }, new String[] { headerActor }, true);

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isTrue();

		verify(this.interceptorMock);
	}

	@Test
	public void testProcessMustUnderstandHeadersForRoleSoap12() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		SOAPHeaderElement header = request.getSOAPHeader()
			.addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
		String headerRole = "http://www/springframework.org/role";
		header.setRole(headerRole);
		header.setMustUnderstand(true);
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		expect(this.interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
				new SoapEndpointInterceptor[] { this.interceptorMock }, new String[] { headerRole }, true);

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isTrue();

		verify(this.interceptorMock);
	}

	@Test
	public void testProcessNoHeader() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		request.getSOAPHeader().detachNode();
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
				new SoapEndpointInterceptor[] { this.interceptorMock }, new String[] { "role" }, true);

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isTrue();

		verify(this.interceptorMock);
	}

	@Test
	public void testProcessMustUnderstandHeadersNoInterceptors() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage request = messageFactory.createMessage();
		SOAPHeaderElement header = request.getSOAPHeader()
			.addHeaderElement(new QName("http://www.springframework.org", "Header"));
		header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
		header.setMustUnderstand(true);
		SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
		MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
		replay(this.interceptorMock);

		SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(), null);

		boolean result = this.dispatcher.handleRequest(chain, context);

		assertThat(result).isFalse();

		verify(this.interceptorMock);
	}

}
