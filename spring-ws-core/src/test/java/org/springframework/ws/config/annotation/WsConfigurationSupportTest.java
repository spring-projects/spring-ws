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

package org.springframework.ws.config.annotation;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arjen Poutsma
 */
public class WsConfigurationSupportTest {

	private ApplicationContext applicationContext;

	@BeforeEach
	public void setUp() throws Exception {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(TestConfig.class);
		applicationContext.refresh();

		this.applicationContext = applicationContext;
	}

	@Test
	public void interceptors() {

		PayloadRootAnnotationMethodEndpointMapping endpointMapping = this.applicationContext
			.getBean(PayloadRootAnnotationMethodEndpointMapping.class);

		assertThat(endpointMapping.getOrder()).isEqualTo(0);

		EndpointInterceptor[] interceptors = endpointMapping.getInterceptors();

		assertThat(interceptors).hasSize(1);
		assertThat(interceptors[0]).isInstanceOf(MyInterceptor.class);
	}

	@Test
	public void defaultMethodEndpointAdapter() {

		DefaultMethodEndpointAdapter endpointAdapter = this.applicationContext
			.getBean(DefaultMethodEndpointAdapter.class);

		assertThat(endpointAdapter).isNotNull();
		assertThat(endpointAdapter).isInstanceOf(MyDefaultMethodEndpointAdapter.class);
	}

	@Configuration
	public static class TestConfig extends WsConfigurationSupport {

		@Override
		protected void addInterceptors(List<EndpointInterceptor> interceptors) {
			interceptors.add(new MyInterceptor());
		}

		@Bean
		@Override
		public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
			return new MyDefaultMethodEndpointAdapter();
		}

	}

	public static class MyInterceptor extends EndpointInterceptorAdapter {

	}

	public static class MyDefaultMethodEndpointAdapter extends DefaultMethodEndpointAdapter {

	}

}
