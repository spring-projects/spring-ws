/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint.mapping;

import static org.easymock.EasyMock.*;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;
import org.springframework.ws.soap.server.endpoint.annotation.SoapActions;

public class SoapActionAnnotationMethodEndpointMappingTest {

	private SoapActionAnnotationMethodEndpointMapping mapping;

	private StaticApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception {
		applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("mapping", SoapActionAnnotationMethodEndpointMapping.class);
		applicationContext.registerSingleton("endpoint", MyEndpoint.class);
		applicationContext.refresh();
		mapping = (SoapActionAnnotationMethodEndpointMapping) applicationContext.getBean("mapping");
	}

	@Test
	public void registrationSingle() throws Exception {
		SoapMessage requestMock = createMock(SoapMessage.class);
		expect(requestMock.getSoapAction()).andReturn("http://springframework.org/spring-ws/SoapAction");
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		replay(requestMock, factoryMock);

		MessageContext context = new DefaultMessageContext(requestMock, factoryMock);
		EndpointInvocationChain chain = mapping.getEndpoint(context);
		Assert.assertNotNull("MethodEndpoint not registered", chain);
		Method doIt = MyEndpoint.class.getMethod("doIt", new Class[0]);
		MethodEndpoint expected = new MethodEndpoint("endpoint", applicationContext, doIt);
		Assert.assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());

		verify(requestMock, factoryMock);
	}

	@Test
	public void registrationMultiple() throws Exception {
		SoapMessage requestMock = createMock(SoapMessage.class);
		expect(requestMock.getSoapAction()).andReturn("http://springframework.org/spring-ws/SoapAction1");
		expect(requestMock.getSoapAction()).andReturn("http://springframework.org/spring-ws/SoapAction2");
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		replay(requestMock, factoryMock);

		Method doItMultiple = MyEndpoint.class.getMethod("doItMultiple");
		MethodEndpoint expected = new MethodEndpoint("endpoint", applicationContext, doItMultiple);

		MessageContext context = new DefaultMessageContext(requestMock, factoryMock);
		EndpointInvocationChain chain = mapping.getEndpoint(context);
		Assert.assertNotNull("MethodEndpoint not registered", chain);
		Assert.assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());

		chain = mapping.getEndpoint(context);
		Assert.assertNotNull("MethodEndpoint not registered", chain);
		Assert.assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());

		verify(requestMock, factoryMock);
	}

	@Test
	public void registrationRepeatable() throws Exception {
		SoapMessage requestMock = createMock(SoapMessage.class);
		expect(requestMock.getSoapAction()).andReturn("http://springframework.org/spring-ws/SoapAction3");
		expect(requestMock.getSoapAction()).andReturn("http://springframework.org/spring-ws/SoapAction4");
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		replay(requestMock, factoryMock);

		Method doItRepeatable = MyEndpoint.class.getMethod("doItRepeatable");
		MethodEndpoint expected = new MethodEndpoint("endpoint", applicationContext, doItRepeatable);

		MessageContext context = new DefaultMessageContext(requestMock, factoryMock);
		EndpointInvocationChain chain = mapping.getEndpoint(context);
		Assert.assertNotNull("MethodEndpoint not registered", chain);
		Assert.assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());

		chain = mapping.getEndpoint(context);
		Assert.assertNotNull("MethodEndpoint not registered", chain);
		Assert.assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());

		verify(requestMock, factoryMock);
	}

	@Endpoint
	private static class MyEndpoint {

		@SoapAction("http://springframework.org/spring-ws/SoapAction")
		public void doIt() {

		}

		@SoapActions({ @SoapAction("http://springframework.org/spring-ws/SoapAction1"),
				@SoapAction("http://springframework.org/spring-ws/SoapAction2") })
		public void doItMultiple() {}

		@SoapAction("http://springframework.org/spring-ws/SoapAction3")
		@SoapAction("http://springframework.org/spring-ws/SoapAction4")
		public void doItRepeatable() {

		}

	}
}
