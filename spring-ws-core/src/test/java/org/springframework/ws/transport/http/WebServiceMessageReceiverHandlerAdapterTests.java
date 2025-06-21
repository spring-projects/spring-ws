/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.transport.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class WebServiceMessageReceiverHandlerAdapterTests {

	private static final String REQUEST = """
			 <SOAP-ENV:Envelope
			  xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
			  SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
				  <SOAP-ENV:Body>
					<m:GetLastTradePrice xmlns:m="Some-URI">
						<symbol>DIS</symbol>
					</m:GetLastTradePrice>
				</SOAP-ENV:Body>
			</SOAP-ENV:Envelope>""";

	private WebServiceMessageReceiverHandlerAdapter adapter;

	private MockHttpServletRequest httpRequest;

	private MockHttpServletResponse httpResponse;

	private WebServiceMessageFactory factoryMock;

	private FaultAwareWebServiceMessage responseMock;

	private FaultAwareWebServiceMessage requestMock;

	@BeforeEach
	void setUp() {

		this.adapter = new WebServiceMessageReceiverHandlerAdapter();
		this.httpRequest = new MockHttpServletRequest();
		this.httpResponse = new MockHttpServletResponse();
		this.factoryMock = createMock(WebServiceMessageFactory.class);
		this.adapter.setMessageFactory(this.factoryMock);
		this.requestMock = createMock("request", FaultAwareWebServiceMessage.class);
		this.responseMock = createMock("response", FaultAwareWebServiceMessage.class);
	}

	@Test
	void testHandleNonPost() throws Exception {

		this.httpRequest.setMethod(HttpTransportConstants.METHOD_GET);
		replayMockControls();

		this.adapter.handle(this.httpRequest, this.httpResponse, (WebServiceMessageReceiver) messageContext -> {
		});

		assertThat(this.httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

		verifyMockControls();
	}

	@Test
	void testHandlePostNoResponse() throws Exception {

		this.httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		this.httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		this.httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		this.httpRequest.setCharacterEncoding("UTF-8");
		expect(this.factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(this.responseMock);

		replayMockControls();

		this.adapter.handle(this.httpRequest, this.httpResponse, (WebServiceMessageReceiver) messageContext -> {
		});

		assertThat(this.httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_ACCEPTED);
		assertThat(this.httpResponse.getContentAsString()).hasSize(0);

		verifyMockControls();
	}

	@Test
	void testHandlePostResponse() throws Exception {

		this.httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		this.httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		this.httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		this.httpRequest.setCharacterEncoding("UTF-8");
		expect(this.factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(this.requestMock);
		expect(this.factoryMock.createWebServiceMessage()).andReturn(this.responseMock);
		expect(this.responseMock.getFaultCode()).andReturn(null);
		this.responseMock.writeTo(isA(OutputStream.class));

		replayMockControls();

		this.adapter.handle(this.httpRequest, this.httpResponse,
				(WebServiceMessageReceiver) MessageContext::getResponse);

		assertThat(this.httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

		verifyMockControls();
	}

	@Test
	void testHandlePostFault() throws Exception {

		this.httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		this.httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		this.httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		this.httpRequest.setCharacterEncoding("UTF-8");
		expect(this.factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(this.requestMock);
		expect(this.factoryMock.createWebServiceMessage()).andReturn(this.responseMock);
		expect(this.responseMock.getFaultCode()).andReturn(SoapVersion.SOAP_11.getServerOrReceiverFaultName());
		this.responseMock.writeTo(isA(OutputStream.class));

		replayMockControls();

		this.adapter.handle(this.httpRequest, this.httpResponse,
				(WebServiceMessageReceiver) MessageContext::getResponse);

		assertThat(this.httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		verifyMockControls();
	}

	@Test
	void testHandleNotFound() throws Exception {

		this.httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		this.httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		this.httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		this.httpRequest.setCharacterEncoding("UTF-8");
		expect(this.factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(this.requestMock);

		replayMockControls();

		this.adapter.handle(this.httpRequest, this.httpResponse, (WebServiceMessageReceiver) messageContext -> {
			throw new NoEndpointFoundException(messageContext.getRequest());
		});

		assertThat(this.httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);

		verifyMockControls();

	}

	@Test
	void testHandleInvalidXml() throws Exception {

		this.httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		this.httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		this.httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		this.httpRequest.setCharacterEncoding("UTF-8");
		expect(this.factoryMock.createWebServiceMessage(isA(InputStream.class)))
			.andThrow(new InvalidXmlException(null, null));

		replayMockControls();

		this.adapter.handle(this.httpRequest, this.httpResponse, (WebServiceMessageReceiver) messageContext -> {
		});

		assertThat(this.httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);

		verifyMockControls();
	}

	private void replayMockControls() {
		replay(this.factoryMock, this.requestMock, this.responseMock);
	}

	private void verifyMockControls() {
		verify(this.factoryMock, this.requestMock, this.responseMock);
	}

}
