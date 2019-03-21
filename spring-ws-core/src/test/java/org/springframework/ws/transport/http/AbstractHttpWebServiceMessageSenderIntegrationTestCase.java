/*
 * Copyright 2005-2018 the original author or authors.
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPOutputStream;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.FreePortScanner;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;

public abstract class AbstractHttpWebServiceMessageSenderIntegrationTestCase<T extends AbstractHttpWebServiceMessageSender> {

	private Server jettyServer;

	private static final String REQUEST_HEADER_NAME = "RequestHeader";

	private static final String REQUEST_HEADER_VALUE = "RequestHeaderValue";

	private static final String RESPONSE_HEADER_NAME = "ResponseHeader";

	private static final String RESPONSE_HEADER_VALUE = "ResponseHeaderValue";

	private static final String REQUEST = "<Request xmlns='http://springframework.org/spring-ws/' />";

	private static final String SOAP_REQUEST =
			"<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'><SOAP-ENV:Header/><SOAP-ENV:Body>" +
					REQUEST + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

	private static final String RESPONSE = "<Response  xmlns='http://springframework.org/spring-ws/' />";

	private static final String SOAP_RESPONSE =
			"<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'><SOAP-ENV:Header/><SOAP-ENV:Body>" +
					RESPONSE + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

	private Context jettyContext;

	private MessageFactory saajMessageFactory;

	private TransformerFactory transformerFactory;

	private WebServiceMessageFactory messageFactory;

	protected T messageSender;

	protected URI connectionUri;

	@Before
	public final void setUp() throws Exception {
		int port = FreePortScanner.getFreePort();
		connectionUri = new URI("http", null, "localhost", port, null, null, null);
		jettyServer = new Server(port);
		jettyContext = new Context(jettyServer, "/");
		messageSender = createMessageSender();
		if (messageSender instanceof InitializingBean) {
			((InitializingBean) messageSender).afterPropertiesSet();
		}
		XMLUnit.setIgnoreWhitespace(true);
		saajMessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		messageFactory = new SaajSoapMessageFactory(saajMessageFactory);
		transformerFactory = TransformerFactoryUtils.newInstance();
	}

	protected abstract T createMessageSender();

	@After
	public final void tearDown() throws Exception {
		if (jettyServer.isRunning()) {
			jettyServer.stop();
		}
	}

	@Test
	public void testSupports() throws URISyntaxException {
		Assert.assertTrue("Message sender does not support HTTP url", messageSender.supports(connectionUri));
	}

	@Test
	public void testSendAndReceiveResponse() throws Exception {
		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveNoResponse() throws Exception {
		validateNonResponse(new MyServlet());
	}

	@Test
	public void testSendAndReceiveNoResponseAccepted() throws Exception {
		MyServlet servlet = new MyServlet();
		servlet.setResponseStatus(HttpServletResponse.SC_ACCEPTED);
		validateNonResponse(servlet);
	}

	@Test
	public void testSendAndReceiveCompressed() throws Exception {
		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		servlet.setGzip(true);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveInvalidContentSize() throws Exception {
		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		servlet.setContentLength(-1);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveCompressedInvalidContentSize() throws Exception {
		MyServlet servlet = new MyServlet();
		servlet.setResponse(true);
		servlet.setGzip(true);
		servlet.setContentLength(-1);
		validateResponse(servlet);
	}

	@Test
	public void testSendAndReceiveFault() throws Exception {
		MyServlet servlet = new MyServlet();
		servlet.setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		servlet.setResponse(true);
		jettyContext.addServlet(new ServletHolder(servlet), "/");
		jettyServer.start();
		FaultAwareWebServiceConnection connection =
				(FaultAwareWebServiceConnection) messageSender.createConnection(connectionUri);
		SOAPMessage request = createRequest();
		try {
			connection.send(new SaajSoapMessage(request));
			connection.receive(messageFactory);
			Assert.assertTrue("Response has no fault", connection.hasFault());
		}
		finally {
			connection.close();
		}
	}

	private void validateResponse(Servlet servlet) throws Exception {
		jettyContext.addServlet(new ServletHolder(servlet), "/");
		jettyServer.start();
		FaultAwareWebServiceConnection connection =
				(FaultAwareWebServiceConnection) messageSender.createConnection(connectionUri);
		SOAPMessage request = createRequest();
		try {
			connection.send(new SaajSoapMessage(request));
			SaajSoapMessage response = (SaajSoapMessage) connection.receive(messageFactory);
			Assert.assertNotNull("No response", response);
			Assert.assertFalse("Response has fault", connection.hasFault());
			SOAPMessage saajResponse = response.getSaajMessage();
			String[] headerValues = saajResponse.getMimeHeaders().getHeader(RESPONSE_HEADER_NAME);
			Assert.assertNotNull("Response has no header", headerValues);
			Assert.assertEquals("Response has invalid header", 1, headerValues.length);
			Assert.assertEquals("Response has invalid header values", RESPONSE_HEADER_VALUE, headerValues[0]);
			StringResult result = new StringResult();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(response.getPayloadSource(), result);
			assertXMLEqual("Invalid response", RESPONSE, result.toString());
		}
		finally {
			connection.close();
		}
	}

	private void validateNonResponse(Servlet servlet) throws Exception {
		jettyContext.addServlet(new ServletHolder(servlet), "/");
		jettyServer.start();

		WebServiceConnection connection = messageSender.createConnection(connectionUri);
		SOAPMessage request = createRequest();
		try {
			connection.send(new SaajSoapMessage(request));
			WebServiceMessage response = connection.receive(messageFactory);
			Assert.assertNull("Response", response);
		}
		finally {
			connection.close();
		}
	}

	private SOAPMessage createRequest() throws TransformerException, SOAPException {
		SOAPMessage request = saajMessageFactory.createMessage();
		MimeHeaders mimeHeaders = request.getMimeHeaders();
		mimeHeaders.addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(new StringSource(REQUEST), new DOMResult(request.getSOAPBody()));
		return request;
	}

	@SuppressWarnings("serial")
	private class MyServlet extends HttpServlet {

		private int responseStatus = HttpServletResponse.SC_OK;

		private Integer contentLength;

		private boolean response;

		private boolean gzip;

		public void setResponseStatus(int responseStatus) {
			this.responseStatus = responseStatus;
		}

		public void setContentLength(int contentLength) {
			this.contentLength = contentLength;
		}

		public void setResponse(boolean response) {
			this.response = response;
		}

		public void setGzip(boolean gzip) {
			this.gzip = gzip;
		}

		@Override
		protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
				throws ServletException, IOException {
			try {
				assertEquals("Invalid header value received on server side", REQUEST_HEADER_VALUE,
						httpServletRequest.getHeader(REQUEST_HEADER_NAME));
				String receivedRequest =
						new String(FileCopyUtils.copyToByteArray(httpServletRequest.getInputStream()), "UTF-8");
				assertXMLEqual("Invalid request received", SOAP_REQUEST, receivedRequest);
				if (gzip) {
					assertEquals("Invalid Accept-Encoding header value received on server side", "gzip",
							httpServletRequest.getHeader("Accept-Encoding"));
				}

				httpServletResponse.setStatus(responseStatus);
				if (response) {
					httpServletResponse.addHeader("content-type", "text/xml");
					if (contentLength != null) {
						httpServletResponse.setContentLength(contentLength);
					}
					if (gzip) {
						httpServletResponse.addHeader("Content-Encoding", "gzip");
					}
					httpServletResponse.setHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE);
					OutputStream os;
					if (gzip) {
						os = new GZIPOutputStream(httpServletResponse.getOutputStream());
					}
					else {
						os = httpServletResponse.getOutputStream();
					}
					FileCopyUtils.copy(SOAP_RESPONSE.getBytes("UTF-8"), os);
				}
			}
			catch (Exception ex) {
				throw new ServletException(ex);
			}
		}
	}

}
