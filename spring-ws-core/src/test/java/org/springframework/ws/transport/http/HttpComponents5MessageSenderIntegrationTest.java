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

package org.springframework.ws.transport.http;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.ws.transport.http.HttpComponents5ClientFactory.*;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MessageFactory;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.FreePortScanner;

class HttpComponents5MessageSenderIntegrationTest
		extends AbstractHttpWebServiceMessageSenderIntegrationTestCase<HttpComponents5MessageSender> {

	@Override
	protected HttpComponents5MessageSender createMessageSender() {
		return new HttpComponents5MessageSender();
	}

	@Test // GH-1164
	void testMaxConnections() throws Exception {

		final String url1 = "https://www.example.com";
		URI uri1 = new URI(url1);
		HttpHost host1 = new HttpHost(uri1.getScheme(), uri1.getHost(), getPort(uri1));
		HttpRoute route1 = new HttpRoute(host1, null, true);

		assertThat(route1.isSecure()).isTrue();
		assertThat(route1.getTargetHost().getHostName()).isEqualTo("www.example.com");
		assertThat(route1.getTargetHost().getPort()).isEqualTo(443);

		final String url2 = "http://www.example.com:8080";
		URI uri2 = new URI(url2);
		HttpHost host2 = new HttpHost(uri2.getScheme(), uri2.getHost(), getPort(uri2));
		HttpRoute route2 = new HttpRoute(host2);

		assertThat(route2.isSecure()).isFalse();
		assertThat(route2.getTargetHost().getHostName()).isEqualTo("www.example.com");
		assertThat(route2.getTargetHost().getPort()).isEqualTo(8080);

		final String url3 = "http://www.springframework.org";
		URI uri3 = new URI(url3);
		HttpHost host3 = new HttpHost(uri3.getScheme(), uri3.getHost(), getPort(uri3));
		HttpRoute route3 = new HttpRoute(host3);

		assertThat(route3.isSecure()).isFalse();
		assertThat(route3.getTargetHost().getHostName()).isEqualTo("www.springframework.org");
		assertThat(route3.getTargetHost().getPort()).isEqualTo(80);

		HttpComponents5ClientFactory clientFactory = new HttpComponents5ClientFactory();

		Map<String, String> maxConnectionsPerHost = new HashMap<>();
		maxConnectionsPerHost.put(url1, "1");
		maxConnectionsPerHost.put(url2, "7");
		maxConnectionsPerHost.put(url3, "10");

		clientFactory.setMaxTotalConnections(2);
		clientFactory.setMaxConnectionsPerHost(maxConnectionsPerHost);

		CloseableHttpClient client = clientFactory.getObject();
		assertThat(client).isNotNull();

		// It is no longer possible way to get connection manager from client
		PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = clientFactory.getConnectionManager();

		assertThat(poolingHttpClientConnectionManager.getMaxPerRoute(route1)).isEqualTo(1);
		assertThat(poolingHttpClientConnectionManager.getMaxPerRoute(route2)).isEqualTo(7);
		assertThat(poolingHttpClientConnectionManager.getMaxPerRoute(route3)).isEqualTo(10);
	}

	@Test // GH-1164
	void testContextClose() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		int port = FreePortScanner.getFreePort();

		Server jettyServer = new Server(port);
		Connector connector = new ServerConnector(jettyServer);
		jettyServer.addConnector(connector);

		ServletContextHandler jettyContext = new ServletContextHandler();
		jettyContext.setContextPath("/");

		jettyContext.addServlet(EchoServlet.class, "/");

		jettyServer.setHandler(jettyContext);
		jettyServer.start();

		WebServiceConnection connection = null;

		try {

			StaticApplicationContext appContext = new StaticApplicationContext();
			appContext.registerSingleton("messageSender", HttpComponents5MessageSender.class);
			appContext.refresh();

			HttpComponents5MessageSender messageSender = appContext.getBean("messageSender",
					HttpComponents5MessageSender.class);
			connection = messageSender.createConnection(new URI("http://localhost:" + port));

			connection.send(new SaajSoapMessage(messageFactory.createMessage()));
			connection.receive(new SaajSoapMessageFactory(messageFactory));

			appContext.close();
		} finally {

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
	public static class EchoServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

			response.setContentType("text/xml");
			FileCopyUtils.copy(request.getInputStream(), response.getOutputStream());

		}
	}

}
