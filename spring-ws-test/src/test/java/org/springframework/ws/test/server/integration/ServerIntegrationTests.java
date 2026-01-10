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

package org.springframework.ws.test.server.integration;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.server.endpoint.mapping.jaxb.XmlRootElementEndpointMapping;
import org.springframework.ws.test.integration.CustomerCountRequest;
import org.springframework.ws.test.integration.CustomerCountResponse;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.payload;

/**
 * @author Arjen Poutsma
 */
@SpringJUnitConfig
class ServerIntegrationTests {

	@Autowired
	private ApplicationContext applicationContext;

	private MockWebServiceClient mockClient;

	@BeforeEach
	void createClient() {
		this.mockClient = MockWebServiceClient.createClient(this.applicationContext);
	}

	@Test
	void basic() {
		Source requestPayload = new StringSource("<customerCountRequest xmlns='http://springframework.org/spring-ws'>"
				+ "<customerName>John Doe</customerName>" + "</customerCountRequest>");
		Source expectedResponsePayload = new StringSource(
				"<customerCountResponse xmlns='http://springframework.org/spring-ws'>"
						+ "<customerCount>42</customerCount>" + "</customerCountResponse>");
		this.mockClient.sendRequest(withPayload(requestPayload)).andExpect(payload(expectedResponsePayload));
	}

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		XmlRootElementEndpointMapping endpointMapping() {
			return new XmlRootElementEndpointMapping();
		}

		@Bean
		CustomerEndpoint customerEndpoint() {
			return new CustomerEndpoint();
		}

		@Bean
		Jaxb2Marshaller jaxb2Marshaller() {
			Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
			jaxb2Marshaller.setClassesToBeBound(CustomerCountRequest.class, CustomerCountResponse.class);
			return jaxb2Marshaller;
		}

	}

}
