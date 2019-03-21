/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("jdk-proxy-registration.xml")
public class JdkProxyRegistrationTest {

	@Autowired
	private PayloadRootAnnotationMethodEndpointMapping mapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void registration() throws NoSuchMethodException {
		MethodEndpoint jdkProxy = mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));
		assertNotNull("jdk proxy endpoint not registered", jdkProxy);
		Method doIt = MyEndpointImpl.class.getMethod("doIt", Source.class);
		MethodEndpoint expected = new MethodEndpoint("jdkProxyEndpoint", applicationContext, doIt);
		assertEquals("Invalid endpoint registered", expected, jdkProxy);
	}

	@Endpoint
	public interface MyEndpoint {

		@PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
		@Log
		void doIt(Source payload);
	}

	public static class MyEndpointImpl implements MyEndpoint {

		@Override
		public void doIt(@RequestPayload Source payload) {
		}

	}

}