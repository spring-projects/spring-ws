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

package org.springframework.ws.server.endpoint.mapping;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

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
@ContextConfiguration("cglib-proxy-registration.xml")
public class CgLibProxyRegistrationTest {

	@Autowired private PayloadRootAnnotationMethodEndpointMapping mapping;

	@Autowired private ApplicationContext applicationContext;

	@Test
	public void registration() throws NoSuchMethodException {
		MethodEndpoint cglibProxy = mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));
		assertNotNull("cg lib proxy endpoint not registered", cglibProxy);
		Method doIt = MyEndpoint.class.getMethod("doIt", Source.class);
		MethodEndpoint expected = new MethodEndpoint("cgLibProxyEndpoint", applicationContext, doIt);
		assertEquals("Invalid endpoint registered", expected, cglibProxy);
	}

	@Endpoint
	public static class MyEndpoint {

		@PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
		@Log
		public void doIt(@RequestPayload Source payload) {}

	}

}
