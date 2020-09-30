/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadRootSmartSoapEndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.SoapActionSmartEndpointInterceptor;

public class InterceptorsBeanDefinitionParserTest {

	@Test
	public void namespace() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"interceptorsBeanDefinitionParserTest.xml", getClass());
		Map<String, ?> result = applicationContext.getBeansOfType(DelegatingSmartEndpointInterceptor.class);
		assertEquals("no smart interceptors found", 8, result.size());

		result = applicationContext.getBeansOfType(PayloadRootSmartSoapEndpointInterceptor.class);
		assertEquals("no interceptors found", 3, result.size());

		result = applicationContext.getBeansOfType(SoapActionSmartEndpointInterceptor.class);
		assertEquals("no interceptors found", 3, result.size());
	}

	@Test
	public void ordering() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"interceptorsBeanDefinitionParserOrderTest.xml", getClass());

		List<DelegatingSmartEndpointInterceptor> interceptors = new ArrayList<DelegatingSmartEndpointInterceptor>(
				applicationContext.getBeansOfType(DelegatingSmartEndpointInterceptor.class).values());
		assertEquals("not enough smart interceptors found", 6, interceptors.size());

		for (int i = 0; i < interceptors.size(); i++) {
			DelegatingSmartEndpointInterceptor delegatingInterceptor = interceptors.get(i);
			MyInterceptor interceptor = (MyInterceptor) delegatingInterceptor.getDelegate();
			assertEquals("Invalid ordering found for [" + delegatingInterceptor + "]", i, interceptor.getOrder());
		}
	}

	@Test
	public void injection() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"interceptorsBeanDefinitionParserInjectionTest.xml", getClass());

		List<DelegatingSmartEndpointInterceptor> interceptors = new ArrayList<DelegatingSmartEndpointInterceptor>(
				applicationContext.getBeansOfType(DelegatingSmartEndpointInterceptor.class).values());
		assertEquals("not enough smart interceptors found", 1, interceptors.size());

		DummyInterceptor interceptor = (DummyInterceptor) interceptors.get(0).getDelegate();
		assertNotNull(interceptor.getPropertyDependency());
		assertNotNull(interceptor.getAutowiredDependency());
	}

}
