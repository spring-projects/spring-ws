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

package org.springframework.ws.client.core;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.support.FreePortScanner;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.xml.sax.SAXException;

public abstract class AbstractSoap11WebServiceTemplateIntegrationTestCase {

	private static Server jettyServer;

	private static String baseUrl;

	private WebServiceTemplate template;

	private String messagePayload = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";

	@BeforeClass
	public static void startJetty() throws Exception {
		int port = FreePortScanner.getFreePort();
		baseUrl = "http://localhost:" + port;
		jettyServer = new Server(port);
		Context jettyContext = new Context(jettyServer, "/");
		jettyContext.addServlet(new ServletHolder(new EchoSoapServlet()), "/soap/echo");
		jettyContext.addServlet(new ServletHolder(new SoapFaultServlet()), "/soap/fault");
		SoapFaultServlet badRequestFault = new SoapFaultServlet();
		badRequestFault.setSc(400);
		jettyContext.addServlet(new ServletHolder(badRequestFault), "/soap/badRequestFault");
		jettyContext.addServlet(new ServletHolder(new NoResponseSoapServlet()), "/soap/noResponse");
		jettyContext.addServlet(new ServletHolder(new AttachmentsServlet()), "/soap/attachment");
		jettyContext.addServlet(new ServletHolder(new ErrorServlet(404)), "/errors/notfound");
		jettyContext.addServlet(new ServletHolder(new ErrorServlet(500)), "/errors/server");
		jettyServer.start();
	}

	@AfterClass
	public static void stopJetty() throws Exception {
		if (jettyServer.isRunning()) {
			jettyServer.stop();
		}
	}

	@Before
	public void createWebServiceTemplate() throws Exception {
		template = new WebServiceTemplate(createMessageFactory());
		template.setMessageSender(new HttpComponentsMessageSender());
	}

	public abstract SoapMessageFactory createMessageFactory() throws Exception;

	@Test
	public void sendSourceAndReceiveToResult() throws SAXException, IOException {
		StringResult result = new StringResult();
		boolean b = template.sendSourceAndReceiveToResult(baseUrl + "/soap/echo", new StringSource(messagePayload), result);
		Assert.assertTrue("Invalid result", b);
		assertXMLEqual(messagePayload, result.toString());
	}

	@Test
	public void sendSourceAndReceiveToResultNoResponse() {
		boolean b = template.sendSourceAndReceiveToResult(baseUrl + "/soap/noResponse", new StringSource(messagePayload),
				new StringResult());
		Assert.assertFalse("Invalid result", b);
	}

	@Test
	public void marshalSendAndReceiveResponse() throws TransformerConfigurationException {
		final Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		final Object requestObject = new Object();
		Marshaller marshaller = new Marshaller() {

			@Override
			public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
				Assert.assertEquals("Invalid object", graph, requestObject);
				try {
					transformer.transform(new StringSource(messagePayload), result);
				} catch (TransformerException e) {
					Assert.fail(e.getMessage());
				}
			}

			@Override
			public boolean supports(Class<?> clazz) {
				Assert.assertEquals("Invalid class", Object.class, clazz);
				return true;
			}
		};
		final Object responseObject = new Object();
		Unmarshaller unmarshaller = new Unmarshaller() {

			@Override
			public Object unmarshal(Source source) throws XmlMappingException, IOException {
				return responseObject;
			}

			@Override
			public boolean supports(Class<?> clazz) {
				Assert.assertEquals("Invalid class", Object.class, clazz);
				return true;
			}
		};
		template.setMarshaller(marshaller);
		template.setUnmarshaller(unmarshaller);
		Object result = template.marshalSendAndReceive(baseUrl + "/soap/echo", requestObject);
		Assert.assertEquals("Invalid response object", responseObject, result);
	}

	@Test
	public void marshalSendAndReceiveNoResponse() throws TransformerConfigurationException {
		final Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		final Object requestObject = new Object();
		Marshaller marshaller = new Marshaller() {

			@Override
			public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
				Assert.assertEquals("Invalid object", graph, requestObject);
				try {
					transformer.transform(new StringSource(messagePayload), result);
				} catch (TransformerException e) {
					Assert.fail(e.getMessage());
				}
			}

			@Override
			public boolean supports(Class<?> clazz) {
				Assert.assertEquals("Invalid class", Object.class, clazz);
				return true;
			}
		};
		template.setMarshaller(marshaller);
		Object result = template.marshalSendAndReceive(baseUrl + "/soap/noResponse", requestObject);
		Assert.assertNull("Invalid response object", result);
	}

	@Test
	public void notFound() {
		try {
			template.sendSourceAndReceiveToResult(baseUrl + "/errors/notfound", new StringSource(messagePayload),
					new StringResult());
			Assert.fail("WebServiceTransportException expected");
		} catch (WebServiceTransportException ex) {
			// expected
		}
	}

	@Test
	public void fault() {
		Result result = new StringResult();
		try {
			template.sendSourceAndReceiveToResult(baseUrl + "/soap/fault", new StringSource(messagePayload), result);
			Assert.fail("SoapFaultClientException expected");
		} catch (SoapFaultClientException ex) {
			// expected
		}
	}

	@Test
	public void faultNonCompliant() {
		Result result = new StringResult();
		template.setCheckConnectionForFault(false);
		template.setCheckConnectionForError(false);
		try {
			template.sendSourceAndReceiveToResult(baseUrl + "/soap/badRequestFault", new StringSource(messagePayload),
					result);
			Assert.fail("SoapFaultClientException expected");
		} catch (SoapFaultClientException ex) {
			// expected
		}
	}

	@Test
	public void attachment() {
		template.sendSourceAndReceiveToResult(baseUrl + "/soap/attachment", new StringSource(messagePayload),
				new WebServiceMessageCallback() {

					@Override
					public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
						SoapMessage soapMessage = (SoapMessage) message;
						final String attachmentContent = "content";
						soapMessage.addAttachment("attachment-1",
								new DataHandler(new ByteArrayDataSource(attachmentContent, "text/plain")));
					}
				}, new StringResult());
	}

	/** Servlet that returns and error message for a given status code. */
	@SuppressWarnings("serial")
	private static class ErrorServlet extends HttpServlet {

		private int sc;

		private ErrorServlet(int sc) {
			this.sc = sc;
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.sendError(sc);
		}
	}

	/** Abstract SOAP Servlet */
	@SuppressWarnings("serial")
	private abstract static class AbstractSoapServlet extends HttpServlet {

		protected MessageFactory messageFactory = null;

		private int sc = -1;

		public void setSc(int sc) {
			this.sc = sc;
		}

		@Override
		public void init(ServletConfig servletConfig) throws ServletException {
			super.init(servletConfig);
			try {
				messageFactory = MessageFactory.newInstance();
			} catch (SOAPException ex) {
				throw new ServletException("Unable to create message factory" + ex.getMessage());
			}
		}

		@Override
		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			try {
				MimeHeaders headers = getHeaders(req);
				SOAPMessage request = messageFactory.createMessage(headers, req.getInputStream());
				SOAPMessage reply = onMessage(request);
				if (sc != -1) {
					resp.setStatus(sc);
				}
				if (reply != null) {
					reply.saveChanges();
					if (sc == -1) {
						resp.setStatus(!reply.getSOAPBody().hasFault() ? HttpServletResponse.SC_OK
								: HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					}
					putHeaders(reply.getMimeHeaders(), resp);
					reply.writeTo(resp.getOutputStream());
				} else if (sc == -1) {
					resp.setStatus(HttpServletResponse.SC_ACCEPTED);
				}
			} catch (Exception ex) {
				throw new ServletException("SAAJ POST failed " + ex.getMessage(), ex);
			}
		}

		private MimeHeaders getHeaders(HttpServletRequest httpServletRequest) {
			Enumeration<?> enumeration = httpServletRequest.getHeaderNames();
			MimeHeaders headers = new MimeHeaders();
			while (enumeration.hasMoreElements()) {
				String headerName = (String) enumeration.nextElement();
				String headerValue = httpServletRequest.getHeader(headerName);
				StringTokenizer values = new StringTokenizer(headerValue, ",");
				while (values.hasMoreTokens()) {
					headers.addHeader(headerName, values.nextToken().trim());
				}
			}
			return headers;
		}

		private void putHeaders(MimeHeaders headers, HttpServletResponse res) {
			Iterator<?> it = headers.getAllHeaders();
			while (it.hasNext()) {
				MimeHeader header = (MimeHeader) it.next();
				String[] values = headers.getHeader(header.getName());
				res.setHeader(header.getName(), StringUtils.arrayToCommaDelimitedString(values));
			}
		}

		protected abstract SOAPMessage onMessage(SOAPMessage message) throws SOAPException;
	}

	@SuppressWarnings("serial")
	private static class EchoSoapServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
			return message;
		}
	}

	@SuppressWarnings("serial")
	private static class NoResponseSoapServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
			return null;
		}
	}

	@SuppressWarnings("serial")
	private static class SoapFaultServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
			SOAPMessage response = messageFactory.createMessage();
			SOAPBody body = response.getSOAPBody();
			body.addFault(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), "Server fault");
			return response;
		}
	}

	@SuppressWarnings("serial")
	private static class AttachmentsServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
			assertEquals("No attachments found", 1, message.countAttachments());
			return null;
		}
	}

}
