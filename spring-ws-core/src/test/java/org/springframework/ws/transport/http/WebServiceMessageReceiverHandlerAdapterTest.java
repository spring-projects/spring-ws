/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.transport.http;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

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

public class WebServiceMessageReceiverHandlerAdapterTest {

	private static final String REQUEST = " <SOAP-ENV:Envelope\n"
			+ "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
			+ "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "	  <SOAP-ENV:Body>\n"
			+ "		<m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + "			<symbol>DIS</symbol>\n"
			+ "		</m:GetLastTradePrice>\n" + "	</SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

	private WebServiceMessageReceiverHandlerAdapter adapter;

	private MockHttpServletRequest httpRequest;

	private MockHttpServletResponse httpResponse;

	private WebServiceMessageFactory factoryMock;

	private FaultAwareWebServiceMessage responseMock;

	private FaultAwareWebServiceMessage requestMock;

	@BeforeEach
	public void setUp() throws Exception {

		adapter = new WebServiceMessageReceiverHandlerAdapter();
		httpRequest = new MockHttpServletRequest();
		httpResponse = new MockHttpServletResponse();
		factoryMock = createMock(WebServiceMessageFactory.class);
		adapter.setMessageFactory(factoryMock);
		requestMock = createMock("request", FaultAwareWebServiceMessage.class);
		responseMock = createMock("response", FaultAwareWebServiceMessage.class);
	}

	@Test
	public void testHandleNonPost() throws Exception {

		httpRequest.setMethod(HttpTransportConstants.METHOD_GET);
		replayMockControls();

		adapter.handle(httpRequest, httpResponse, (WebServiceMessageReceiver) messageContext -> {});

		assertThat(httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

		verifyMockControls();
	}

	@Test
	public void testHandlePostNoResponse() throws Exception {

		httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		httpRequest.setCharacterEncoding("UTF-8");
		expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(responseMock);

		replayMockControls();

		adapter.handle(httpRequest, httpResponse, (WebServiceMessageReceiver) messageContext -> {});

		assertThat(httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_ACCEPTED);
		assertThat(httpResponse.getContentAsString()).hasSize(0);

		verifyMockControls();
	}

	@Test
	public void testHandlePostResponse() throws Exception {

		httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		httpRequest.setCharacterEncoding("UTF-8");
		expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(requestMock);
		expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
		expect(responseMock.getFaultCode()).andReturn(null);
		responseMock.writeTo(isA(OutputStream.class));

		replayMockControls();

		adapter.handle(httpRequest, httpResponse, (WebServiceMessageReceiver) MessageContext::getResponse);

		assertThat(httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

		verifyMockControls();
	}

	@Test
	public void testHandlePostFault() throws Exception {

		httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		httpRequest.setCharacterEncoding("UTF-8");
		expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(requestMock);
		expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
		expect(responseMock.getFaultCode()).andReturn(SoapVersion.SOAP_11.getServerOrReceiverFaultName());
		responseMock.writeTo(isA(OutputStream.class));

		replayMockControls();

		adapter.handle(httpRequest, httpResponse, (WebServiceMessageReceiver) MessageContext::getResponse);

		assertThat(httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		verifyMockControls();
	}

	@Test
	public void testHandleNotFound() throws Exception {

		httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		httpRequest.setCharacterEncoding("UTF-8");
		expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(requestMock);

		replayMockControls();

		adapter.handle(httpRequest, httpResponse, (WebServiceMessageReceiver) messageContext -> {
			throw new NoEndpointFoundException(messageContext.getRequest());
		});

		assertThat(httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);

		verifyMockControls();

	}

	@Test
	public void testHandleInvalidXml() throws Exception {

		httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
		httpRequest.setContent(REQUEST.getBytes(StandardCharsets.UTF_8));
		httpRequest.setContentType("text/xml; charset=\"utf-8\"");
		httpRequest.setCharacterEncoding("UTF-8");
		expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andThrow(new InvalidXmlException(null, null));

		replayMockControls();

		adapter.handle(httpRequest, httpResponse, (WebServiceMessageReceiver) messageContext -> {});

		assertThat(httpResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);

		verifyMockControls();
	}

	private void replayMockControls() {
		replay(factoryMock, requestMock, responseMock);
	}

	private void verifyMockControls() {
		verify(factoryMock, requestMock, responseMock);
	}

}
