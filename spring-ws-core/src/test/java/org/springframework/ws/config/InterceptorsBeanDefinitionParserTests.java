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

package org.springframework.ws.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadRootSmartSoapEndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.SoapActionSmartEndpointInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptorsBeanDefinitionParserTests {

	@Test
	void namespace() {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"interceptorsBeanDefinitionParserTest.xml", getClass());
		Map<String, ?> result = applicationContext.getBeansOfType(DelegatingSmartEndpointInterceptor.class);

		assertThat(result).hasSize(8);

		result = applicationContext.getBeansOfType(PayloadRootSmartSoapEndpointInterceptor.class);

		assertThat(result).hasSize(3);

		result = applicationContext.getBeansOfType(SoapActionSmartEndpointInterceptor.class);

		assertThat(result).hasSize(3);
	}

	@Test
	void ordering() {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"interceptorsBeanDefinitionParserOrderTest.xml", getClass());

		List<DelegatingSmartEndpointInterceptor> interceptors = new ArrayList<>(
				applicationContext.getBeansOfType(DelegatingSmartEndpointInterceptor.class).values());

		assertThat(interceptors).hasSize(6);

		for (int i = 0; i < interceptors.size(); i++) {

			DelegatingSmartEndpointInterceptor delegatingInterceptor = interceptors.get(i);
			MyInterceptor interceptor = (MyInterceptor) delegatingInterceptor.getDelegate();

			assertThat(interceptor.getOrder()).isEqualTo(i);
		}
	}

	@Test
	void injection() {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"interceptorsBeanDefinitionParserInjectionTest.xml", getClass());

		List<DelegatingSmartEndpointInterceptor> interceptors = new ArrayList<>(
				applicationContext.getBeansOfType(DelegatingSmartEndpointInterceptor.class).values());

		assertThat(interceptors).hasSize(1);

		DummyInterceptor interceptor = (DummyInterceptor) interceptors.get(0).getDelegate();

		assertThat(interceptor.getPropertyDependency()).isNotNull();
		assertThat(interceptor.getAutowiredDependency()).isNotNull();
	}

}
