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

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.w3c.dom.Document;

import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.support.FreePortScanner;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;

import static org.custommonkey.xmlunit.XMLAssert.*;

public class DomPoxWebServiceTemplateIntegrationTest {

	private static Server jettyServer;

	private static String baseUrl;

	@BeforeClass
	public static void startJetty() throws Exception {
		int port = FreePortScanner.getFreePort();
		baseUrl = "http://localhost:" + port;
		jettyServer = new Server(port);
		Context jettyContext = new Context(jettyServer, "/");
		jettyContext.addServlet(new ServletHolder(new PoxServlet()), "/pox");
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

	@Test
	public void domPox() throws Exception {
		WebServiceTemplate template = new WebServiceTemplate(new DomPoxMessageFactory());
		template.setMessageSender(new HttpComponentsMessageSender());
		String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
		StringResult result = new StringResult();
		template.sendSourceAndReceiveToResult(baseUrl + "/pox", new StringSource(content),
				result);
		assertXMLEqual(content, result.toString());
		try {
			template.sendSourceAndReceiveToResult(baseUrl + "/errors/notfound",
					new StringSource(content), new StringResult());
			Assert.fail("WebServiceTransportException expected");
		}
		catch (WebServiceTransportException ex) {
			//expected
		}
		try {
			template.sendSourceAndReceiveToResult(baseUrl + "/errors/server",
					new StringSource(content), result);
			Assert.fail("WebServiceTransportException expected");
		}
		catch (WebServiceTransportException ex) {
			//expected
		}
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

	/** Simple POX Servlet. */
	@SuppressWarnings("serial")
	private static class PoxServlet extends HttpServlet {

		private DocumentBuilderFactory documentBuilderFactory;

		private TransformerFactory transformerFactory;

		@Override
		public void init(ServletConfig servletConfig) throws ServletException {
			super.init(servletConfig);
			documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			transformerFactory = TransformerFactoryUtils.newInstance();
		}

		@Override
		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			try {
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document message = documentBuilder.parse(req.getInputStream());
				Transformer transformer = transformerFactory.newTransformer();
				transformer.transform(new DOMSource(message), new StreamResult(resp.getOutputStream()));
			}
			catch (Exception ex) {
				throw new ServletException("POX POST failed" + ex.getMessage());
			}
		}
	}

}
