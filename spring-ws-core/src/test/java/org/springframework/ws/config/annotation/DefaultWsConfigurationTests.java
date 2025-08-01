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

package org.springframework.ws.config.annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.addressing.server.AnnotationActionEndpointMapping;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;
import org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arjen Poutsma
 */
class DefaultWsConfigurationTests {

	private ApplicationContext applicationContext;

	@BeforeEach
	void setUp() {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(TestConfig.class);
		applicationContext.refresh();

		this.applicationContext = applicationContext;
	}

	@Test
	void payloadRootAnnotationMethodEndpointMapping() {

		PayloadRootAnnotationMethodEndpointMapping endpointMapping = this.applicationContext
			.getBean(PayloadRootAnnotationMethodEndpointMapping.class);

		assertThat(endpointMapping.getOrder()).isEqualTo(0);
	}

	@Test
	void soapActionAnnotationMethodEndpointMapping() {

		SoapActionAnnotationMethodEndpointMapping endpointMapping = this.applicationContext
			.getBean(SoapActionAnnotationMethodEndpointMapping.class);

		assertThat(endpointMapping.getOrder()).isEqualTo(1);
	}

	@Test
	void annotationActionEndpointMapping() {

		AnnotationActionEndpointMapping endpointMapping = this.applicationContext
			.getBean(AnnotationActionEndpointMapping.class);

		assertThat(endpointMapping.getOrder()).isEqualTo(2);
	}

	@Test
	void defaultMethodEndpointAdapter() {

		DefaultMethodEndpointAdapter adapter = this.applicationContext.getBean(DefaultMethodEndpointAdapter.class);

		assertThat(adapter.getMethodArgumentResolvers()).isNotEmpty();
		assertThat(adapter.getMethodReturnValueHandlers()).isNotEmpty();
	}

	@EnableWs
	@Configuration
	public static class TestConfig {

		@Bean(name = "testEndpoint")
		public TestEndpoint testEndpoint() {
			return new TestEndpoint();
		}

	}

	@Endpoint
	private static final class TestEndpoint {

		@SoapAction("handle")
		public void handle() {
		}

	}

}
