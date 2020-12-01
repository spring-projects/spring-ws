/*
 * Copyright 2005-2010 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.transport.TransportConstants;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("httpserver-applicationContext.xml")
public class WebServiceHttpHandlerIntegrationTest {

	private HttpClient client;

	@Autowired private int port;

	private String url;

	@BeforeEach
	public void createHttpClient() {

		client = new HttpClient();
		url = "http://localhost:" + port + "/service";
	}

	@Test
	public void testInvalidMethod() throws IOException {

		GetMethod getMethod = new GetMethod(url);
		client.executeMethod(getMethod);

		assertThat(getMethod.getStatusCode()).isEqualTo(HttpTransportConstants.STATUS_METHOD_NOT_ALLOWED);
		assertThat(getMethod.getResponseContentLength()).isEqualTo(0);
	}

	@Test
	public void testNoResponse() throws IOException {

		PostMethod postMethod = new PostMethod(url);
		postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
		postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
				"http://springframework.org/spring-ws/NoResponse");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
		postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));

		client.executeMethod(postMethod);

		assertThat(postMethod.getStatusCode()).isEqualTo(HttpTransportConstants.STATUS_ACCEPTED);
		assertThat(postMethod.getResponseContentLength()).isEqualTo(0);
	}

	@Test
	public void testResponse() throws IOException {

		PostMethod postMethod = new PostMethod(url);
		postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
		postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/Response");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
		postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
		client.executeMethod(postMethod);

		assertThat(postMethod.getStatusCode()).isEqualTo(HttpTransportConstants.STATUS_OK);
		assertThat(postMethod.getResponseContentLength()).isGreaterThan(0);
	}

	@Test
	public void testNoEndpoint() throws IOException {

		PostMethod postMethod = new PostMethod(url);
		postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
		postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
				"http://springframework.org/spring-ws/NoEndpoint");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
		postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));

		client.executeMethod(postMethod);

		assertThat(postMethod.getStatusCode()).isEqualTo(HttpTransportConstants.STATUS_NOT_FOUND);
		assertThat(postMethod.getResponseContentLength()).isEqualTo(0);
	}

	@Test
	public void testFault() throws IOException {

		PostMethod postMethod = new PostMethod(url);
		postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
		postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/Fault");
		Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
		postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));

		client.executeMethod(postMethod);

		assertThat(postMethod.getStatusCode()).isEqualTo(HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR);
	}
}
