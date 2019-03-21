/*
 * Copyright 2005-2018 the original author or authors.
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

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;

import org.apache.commons.httpclient.URIException;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.FreePortScanner;

public class HttpComponentsMessageSenderIntegrationTest
		extends AbstractHttpWebServiceMessageSenderIntegrationTestCase<HttpComponentsMessageSender> {

	@Override
	protected HttpComponentsMessageSender createMessageSender() {
		return new HttpComponentsMessageSender();
	}

	@Test
	public void testMaxConnections() throws URISyntaxException, URIException {
		final String url1 = "https://www.example.com";
		URI uri1 = new URI(url1);
		HttpHost host1 = new HttpHost(uri1.getHost(), uri1.getPort(), uri1.getScheme());
		HttpRoute route1 = new HttpRoute(host1, null, true);
		assertThat(route1.isSecure(), equalTo(true));
		assertThat(route1.getTargetHost().getHostName(), equalTo("www.example.com"));
		assertTrue((route1.getTargetHost().getPort() == -1) || (route1.getTargetHost().getPort() == 443));

		final String url2 = "https://www.example.com:8080";
		URI uri2 = new URI(url2);
		HttpHost host2 = new HttpHost(uri2.getHost(), uri2.getPort(), uri2.getScheme());
		HttpRoute route2 = new HttpRoute(host2);
		assertThat(route2.isSecure(), equalTo(false));
		assertThat(route2.getTargetHost().getHostName(), equalTo("www.example.com"));
		assertThat(route2.getTargetHost().getPort(), equalTo(8080));

		final String url3 = "https://www.springframework.org";
		URI uri3 = new URI(url3);
		HttpHost host3 = new HttpHost(uri3.getHost(), uri3.getPort(), uri3.getScheme());
		HttpRoute route3 = new HttpRoute(host3);
		assertThat(route3.isSecure(), equalTo(false));
		assertThat(route3.getTargetHost().getHostName(), equalTo("www.springframework.org"));
		assertTrue((route3.getTargetHost().getPort() ==  -1) || (route3.getTargetHost().getPort() == 80));

		HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();
		messageSender.setMaxTotalConnections(2);
		Map<String, String> maxConnectionsPerHost = new HashMap<String, String>();
		maxConnectionsPerHost.put(url1, "1");
		maxConnectionsPerHost.put(url2, "7");
		maxConnectionsPerHost.put(url3, "10");
		messageSender.setMaxConnectionsPerHost(maxConnectionsPerHost);

		PoolingClientConnectionManager poolingClientConnectionManager =
				(PoolingClientConnectionManager) messageSender.getHttpClient().getConnectionManager();
		assertThat(poolingClientConnectionManager.getMaxPerRoute(route1), equalTo(1));
		assertThat(poolingClientConnectionManager.getMaxPerRoute(route2), equalTo(7));
		assertThat(poolingClientConnectionManager.getMaxPerRoute(route3), equalTo(10));
	}

	@Test
	public void testContextClose() throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		int port = FreePortScanner.getFreePort();
		Server jettyServer = new Server(port);
		Context jettyContext = new Context(jettyServer, "/");
		jettyContext.addServlet(new ServletHolder(new EchoServlet()), "/");
		jettyServer.start();
		WebServiceConnection connection = null;
		try {

			StaticApplicationContext appContext = new StaticApplicationContext();
			appContext.registerSingleton("messageSender", HttpComponentsMessageSender.class);
			appContext.refresh();

			HttpComponentsMessageSender messageSender = appContext
					.getBean("messageSender", HttpComponentsMessageSender.class);
			connection = messageSender.createConnection(new URI("http://localhost:" + port));

			connection.send(new SaajSoapMessage(messageFactory.createMessage()));
			connection.receive(new SaajSoapMessageFactory(messageFactory));

			appContext.close();
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException ex) {
					// ignore
				}
			}
			if (jettyServer.isRunning()) {
				jettyServer.stop();
			}
		}

	}

	@SuppressWarnings("serial")
	private class EchoServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			response.setContentType("text/xml");
			FileCopyUtils.copy(request.getInputStream(), response.getOutputStream());

		}
	}


}
