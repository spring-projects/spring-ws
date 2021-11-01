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

import static org.assertj.core.api.Assertions.*;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.support.FreePortScanner;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractSoap11WebServiceTemplateIntegrationTestCase {

	private static Server jettyServer;

	private static String baseUrl;

	private WebServiceTemplate template;

	private String messagePayload = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";

	private Logger logger = LogManager.getLogger();

	@BeforeAll
	public static void startJetty() throws Exception {

		int port = FreePortScanner.getFreePort();
		baseUrl = "http://localhost:" + port;

		jettyServer = new Server(port);
		Connector connector = new ServerConnector(jettyServer);
		jettyServer.addConnector(connector);

		ServletContextHandler jettyContext = new ServletContextHandler();
		jettyContext.setContextPath("/");

		jettyContext.addServlet(EchoSoapServlet.class, "/soap/echo");
		jettyContext.addServlet(SoapFaultServlet.class, "/soap/fault");

		ServletHolder badRequestFault = jettyContext.addServlet(SoapFaultServlet.class, "/soap/badRequestFault");
		badRequestFault.setInitParameter("sc", "400");

		jettyContext.addServlet(NoResponseSoapServlet.class, "/soap/noResponse");
		jettyContext.addServlet(AttachmentsServlet.class, "/soap/attachment");

		ServletHolder notfound = jettyContext.addServlet(ErrorServlet.class, "/errors/notfound");
		notfound.setInitParameter("sc", "404");

		ServletHolder errors = jettyContext.addServlet(ErrorServlet.class, "/errors/server");
		errors.setInitParameter("sc", "500");

		jettyServer.setHandler(jettyContext);
		jettyServer.start();
	}

	@AfterAll
	public static void stopJetty() throws Exception {

		if (jettyServer.isRunning()) {
			jettyServer.stop();
		}
	}

	@BeforeEach
	public void createWebServiceTemplate() throws Exception {

		template = new WebServiceTemplate(createMessageFactory());
		template.setMessageSender(new HttpComponentsMessageSender());
	}

	public abstract SoapMessageFactory createMessageFactory() throws Exception;

	@Test
	public void sendSourceAndReceiveToResult() {

		logger.info(">>> Test case");

		StringResult result = new StringResult();
		boolean b = template.sendSourceAndReceiveToResult(baseUrl + "/soap/echo", new StringSource(messagePayload), result);

		assertThat(b).isTrue();
		XmlAssert.assertThat(result.toString()).and(messagePayload).ignoreWhitespace().areIdentical();
	}

	@Test
	public void sendSourceAndReceiveToResultNoResponse() {

		boolean b = template.sendSourceAndReceiveToResult(baseUrl + "/soap/noResponse", new StringSource(messagePayload),
				new StringResult());

		assertThat(b).isFalse();
	}

	@Test
	public void marshalSendAndReceiveResponse() throws TransformerConfigurationException {

		final Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		final Object requestObject = new Object();

		Marshaller marshaller = new Marshaller() {

			@Override
			public void marshal(Object graph, Result result) throws XmlMappingException {

				assertThat(requestObject).isEqualTo(graph);
				try {
					transformer.transform(new StringSource(messagePayload), result);
				} catch (TransformerException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean supports(Class<?> clazz) {

				assertThat(clazz).isEqualTo(Object.class);
				return true;
			}
		};

		final Object responseObject = new Object();

		Unmarshaller unmarshaller = new Unmarshaller() {

			@Override
			public Object unmarshal(Source source) throws XmlMappingException {
				return responseObject;
			}

			@Override
			public boolean supports(Class<?> clazz) {

				assertThat(clazz).isEqualTo(Object.class);
				return true;
			}
		};

		template.setMarshaller(marshaller);
		template.setUnmarshaller(unmarshaller);
		Object result = template.marshalSendAndReceive(baseUrl + "/soap/echo", requestObject);

		assertThat(result).isEqualTo(responseObject);
	}

	@Test
	public void marshalSendAndReceiveNoResponse() throws TransformerConfigurationException {

		final Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		final Object requestObject = new Object();

		Marshaller marshaller = new Marshaller() {

			@Override
			public void marshal(Object graph, Result result) throws XmlMappingException {

				assertThat(requestObject).isEqualTo(graph);
				try {
					transformer.transform(new StringSource(messagePayload), result);
				} catch (TransformerException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean supports(Class<?> clazz) {

				assertThat(clazz).isEqualTo(Object.class);
				return true;
			}
		};

		template.setMarshaller(marshaller);
		Object result = template.marshalSendAndReceive(baseUrl + "/soap/noResponse", requestObject);

		assertThat(result).isNull();
	}

	@Test
	public void notFound() {

		assertThatExceptionOfType(WebServiceTransportException.class)
				.isThrownBy(() -> template.sendSourceAndReceiveToResult(baseUrl + "/errors/notfound",
						new StringSource(messagePayload), new StringResult()));
	}

	@Test
	public void fault() {

		Result result = new StringResult();

		assertThatExceptionOfType(SoapFaultClientException.class).isThrownBy(
				() -> template.sendSourceAndReceiveToResult(baseUrl + "/soap/fault", new StringSource(messagePayload), result));
	}

	@Test
	public void faultNonCompliant() {

		Result result = new StringResult();
		template.setCheckConnectionForFault(false);
		template.setCheckConnectionForError(false);

		assertThatExceptionOfType(SoapFaultClientException.class).isThrownBy(() -> template
				.sendSourceAndReceiveToResult(baseUrl + "/soap/badRequestFault", new StringSource(messagePayload), result));
	}

	@Test
	public void attachment() {

		template.sendSourceAndReceiveToResult(baseUrl + "/soap/attachment", new StringSource(messagePayload), message -> {

			SoapMessage soapMessage = (SoapMessage) message;
			final String attachmentContent = "content";
			soapMessage.addAttachment("attachment-1",
					new DataHandler(new ByteArrayDataSource(attachmentContent, "text/plain")));
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
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

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
	public static class EchoSoapServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) {
			return message;
		}
	}

	@SuppressWarnings("serial")
	public static class NoResponseSoapServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) {
			return null;
		}
	}

	@SuppressWarnings("serial")
	public static class SoapFaultServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {

			SOAPMessage response = messageFactory.createMessage();
			SOAPBody body = response.getSOAPBody();
			body.addFault(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), "Server fault");
			return response;
		}
	}

	@SuppressWarnings("serial")
	public static class AttachmentsServlet extends AbstractSoapServlet {

		@Override
		protected SOAPMessage onMessage(SOAPMessage message) {

			assertThat(message.countAttachments()).isEqualTo(1);
			return null;
		}
	}

}
