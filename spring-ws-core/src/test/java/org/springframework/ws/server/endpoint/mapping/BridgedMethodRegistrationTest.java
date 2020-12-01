/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("bridged-method-registration.xml")
public class BridgedMethodRegistrationTest {

	@Autowired private PayloadRootAnnotationMethodEndpointMapping mapping;

	@Autowired private ApplicationContext applicationContext;

	@Test
	public void registration() throws NoSuchMethodException {

		MethodEndpoint bridgedMethod = mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));

		assertThat(bridgedMethod).isNotNull();

		Method doIt = B.class.getMethod("doIt");
		MethodEndpoint expected = new MethodEndpoint("bridgedMethodEndpoint", applicationContext, doIt);

		assertThat(bridgedMethod).isEqualTo(expected);
	}

	@Endpoint
	public static class A {

		@PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
		public A doIt() {
			return this;
		}
	}

	public static class B extends A {

		@Override
		public B doIt() {
			return this;
		}
	}

}
