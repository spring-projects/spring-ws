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

package org.springframework.ws.transport.http;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.assertj.core.api.ThrowingConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.transport.TransportConstants;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("httpserver-applicationContext.xml")
public class WebServiceHttpHandlerIntegrationTests {

	@Autowired
	private int port;

	@Test
	public void testInvalidMethod() {
		HttpGet httpRequest = new HttpGet(serviceUrl());
		execute(httpRequest, response -> {
			assertThat(response.getCode()).isEqualTo(HttpTransportConstants.STATUS_METHOD_NOT_ALLOWED);
			assertThat(response.containsHeader(HttpHeaders.CONTENT_LENGTH)).isTrue();
			assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH).getValue()).isEqualTo("0");
		});
	}

	@Test
	public void testNoResponse() throws IOException {
		HttpPost httpRequest = new HttpPost(serviceUrl());
		httpRequest.addHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/NoResponse");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTests.class);
		httpRequest.setEntity(new InputStreamEntity(soapRequest.getInputStream(), ContentType.TEXT_XML));
		execute(httpRequest, response -> {
			assertThat(response.getCode()).isEqualTo(HttpTransportConstants.STATUS_ACCEPTED);
			assertThat(response.containsHeader(HttpHeaders.CONTENT_LENGTH)).isTrue();
			assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH).getValue()).isEqualTo("0");
		});
	}

	@Test
	public void testResponse() throws IOException {
		HttpPost httpRequest = new HttpPost(serviceUrl());
		httpRequest.addHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/Response");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTests.class);
		httpRequest.setEntity(new InputStreamEntity(soapRequest.getInputStream(), ContentType.TEXT_XML));
		execute(httpRequest, response -> {
			assertThat(response.getCode()).isEqualTo(HttpTransportConstants.STATUS_OK);
			assertThat(response.containsHeader(HttpHeaders.CONTENT_LENGTH)).isTrue();
			assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH).getValue()).asInt().isGreaterThan(0);
		});
	}

	@Test
	public void testNoEndpoint() throws IOException {
		HttpPost httpRequest = new HttpPost(serviceUrl());
		httpRequest.addHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/NoEndpoint");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTests.class);
		httpRequest.setEntity(new InputStreamEntity(soapRequest.getInputStream(), ContentType.TEXT_XML));
		execute(httpRequest, response -> {
			assertThat(response.getCode()).isEqualTo(HttpTransportConstants.STATUS_NOT_FOUND);
			assertThat(response.containsHeader(HttpHeaders.CONTENT_LENGTH)).isTrue();
			assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH).getValue()).isEqualTo("0");
		});
	}

	@Test
	public void testFault() throws IOException {
		HttpPost httpRequest = new HttpPost(serviceUrl());
		httpRequest.addHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/Fault");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTests.class);
		httpRequest.setEntity(new InputStreamEntity(soapRequest.getInputStream(), ContentType.TEXT_XML));
		execute(httpRequest, response -> assertThat(response.getCode())
			.isEqualTo(HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR));
	}

	private String serviceUrl() {
		return "http://localhost:%s/service".formatted(this.port);
	}

	private void execute(ClassicHttpRequest request, ThrowingConsumer<ClassicHttpResponse> responseHandler) {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpClientResponseHandler<Object> rh = httpResponse -> {
				responseHandler.accept(httpResponse);
				return null;
			};
			httpclient.execute(request, rh);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
