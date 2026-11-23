/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client.integration;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.ws.test.integration.CustomerCountRequest;
import org.springframework.ws.test.integration.CustomerCountResponse;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

/**
 * Integration test for client-side WebService testing. In different package so we can't
 * use the package-protected classes.
 *
 * @author Arjen Poutsma
 */
@SpringJUnitConfig
class ClientIntegrationTests {

	@Autowired
	private CustomerClient client;

	private MockWebServiceServer mockServer;

	@BeforeEach
	void createServer() {
		this.mockServer = MockWebServiceServer.createServer(this.client);
	}

	@Test
	void basic() {
		Source expectedRequestPayload = new StringSource(
				"<customerCountRequest xmlns='http://springframework.org/spring-ws'>"
						+ "<customerName>John Doe</customerName>" + "</customerCountRequest>");
		Source responsePayload = new StringSource("<customerCountResponse xmlns='http://springframework.org/spring-ws'>"
				+ "<customerCount>10</customerCount>" + "</customerCountResponse>");

		this.mockServer.expect(payload(expectedRequestPayload)).andRespond(withPayload(responsePayload));
		int result = this.client.getCustomerCount();
		assertThat(result).isEqualTo(10);
		this.mockServer.verify();
	}

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		Jaxb2Marshaller jaxb2Marshaller() {
			Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
			jaxb2Marshaller.setClassesToBeBound(CustomerCountRequest.class, CustomerCountResponse.class);
			return jaxb2Marshaller;
		}

		@Bean
		WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshallerUnmarshaller) {
			WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
			webServiceTemplate.setDefaultUri("http://example.com");
			webServiceTemplate.setMarshaller(marshallerUnmarshaller);
			webServiceTemplate.setUnmarshaller(marshallerUnmarshaller);
			return webServiceTemplate;
		}

		@Bean
		CustomerClient customerClient(WebServiceTemplate webServiceTemplate) {
			CustomerClient client = new CustomerClient();
			client.setWebServiceTemplate(webServiceTemplate);
			return client;
		}

	}

}
