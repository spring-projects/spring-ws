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

import org.junit.jupiter.api.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.WsConfigurationSupportTests.CustomArgumentResolverConfig.MyArgumentResolver;
import org.springframework.ws.config.annotation.WsConfigurationSupportTests.CustomDefaultMethodEndpointAdapterConfig.MyDefaultMethodEndpointAdapter;
import org.springframework.ws.config.annotation.WsConfigurationSupportTests.CustomInterceptorConfig.MyInterceptor;
import org.springframework.ws.config.annotation.WsConfigurationSupportTests.CustomReturnValueHandlerConfig.MyReturnValueHandler;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.server.endpoint.adapter.method.SourcePayloadMethodProcessor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WsConfigurationSupport}.
 *
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 */
class WsConfigurationSupportTests {

	@Test
	void interceptors() {
		try (ConfigurableApplicationContext applicationContext = load(CustomInterceptorConfig.class)) {
			PayloadRootAnnotationMethodEndpointMapping endpointMapping = applicationContext
				.getBean(PayloadRootAnnotationMethodEndpointMapping.class);
			assertThat(endpointMapping.getOrder()).isEqualTo(0);
			EndpointInterceptor[] interceptors = endpointMapping.getInterceptors();
			assertThat(interceptors).singleElement().isInstanceOf(MyInterceptor.class);
		}
	}

	@Test
	void argumentResolvers() {
		try (ConfigurableApplicationContext applicationContext = load(CustomArgumentResolverConfig.class)) {
			DefaultMethodEndpointAdapter bean = applicationContext.getBean(DefaultMethodEndpointAdapter.class);
			List<MethodArgumentResolver> methodArgumentResolvers = bean.getMethodArgumentResolvers();
			assertThat(methodArgumentResolvers).hasSizeGreaterThan(1).element(0).isInstanceOf(MyArgumentResolver.class);
		}
	}

	@Test
	void returnValueHandlers() {
		try (ConfigurableApplicationContext applicationContext = load(CustomReturnValueHandlerConfig.class)) {
			DefaultMethodEndpointAdapter bean = applicationContext.getBean(DefaultMethodEndpointAdapter.class);
			List<MethodReturnValueHandler> methodReturnValueHandlers = bean.getMethodReturnValueHandlers();
			assertThat(methodReturnValueHandlers).hasSizeGreaterThan(1)
				.element(0)
				.isInstanceOf(MyReturnValueHandler.class);
		}
	}

	@Test
	void defaultMethodEndpointAdapter() {
		try (ConfigurableApplicationContext applicationContext = load(CustomDefaultMethodEndpointAdapterConfig.class)) {
			assertThat(applicationContext.getBean(DefaultMethodEndpointAdapter.class))
				.isInstanceOf(MyDefaultMethodEndpointAdapter.class);
		}
	}

	private ConfigurableApplicationContext load(Class<?>... componentClasses) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(componentClasses);
		applicationContext.refresh();
		return applicationContext;
	}

	@Configuration(proxyBeanMethods = false)
	static class CustomInterceptorConfig extends WsConfigurationSupport {

		@Override
		protected void addInterceptors(List<EndpointInterceptor> interceptors) {
			interceptors.add(new MyInterceptor());
		}

		static class MyInterceptor extends EndpointInterceptorAdapter {

		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomArgumentResolverConfig extends WsConfigurationSupport {

		@Override
		protected void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {
			assertThat(argumentResolvers).isNotEmpty();
			argumentResolvers.add(0, new MyArgumentResolver());
		}

		static class MyArgumentResolver extends SourcePayloadMethodProcessor {

		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomReturnValueHandlerConfig extends WsConfigurationSupport {

		@Override
		protected void addReturnValueHandlers(List<MethodReturnValueHandler> returnValueHandlers) {
			assertThat(returnValueHandlers).isNotEmpty();
			returnValueHandlers.add(0, new MyReturnValueHandler());
		}

		static class MyReturnValueHandler extends SourcePayloadMethodProcessor {

		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomDefaultMethodEndpointAdapterConfig extends WsConfigurationSupport {

		@Bean
		@Override
		public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
			return new MyDefaultMethodEndpointAdapter();
		}

		static class MyDefaultMethodEndpointAdapter extends DefaultMethodEndpointAdapter {

		}

	}

}
