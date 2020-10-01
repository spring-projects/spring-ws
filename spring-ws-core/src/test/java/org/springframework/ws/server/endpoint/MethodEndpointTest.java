/*
 * Copyright 2005-2012 the original author or authors.
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

package org.springframework.ws.server.endpoint;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MethodEndpointTest {

	private MethodEndpoint endpoint;

	private boolean myMethodInvoked;

	private Method method;

	@BeforeEach
	public void setUp() throws Exception {

		myMethodInvoked = false;
		method = getClass().getMethod("myMethod", String.class);
		endpoint = new MethodEndpoint(this, method);
	}

	@Test
	public void testGetters() {

		assertThat(endpoint.getBean()).isEqualTo(this);
		assertThat(endpoint.getMethod()).isEqualTo(method);
	}

	@Test
	public void testInvoke() throws Exception {

		assertThat(myMethodInvoked).isFalse();

		endpoint.invoke("arg");

		assertThat(myMethodInvoked).isTrue();
	}

	@Test
	public void testEquals() throws Exception {

		assertThat(endpoint).isEqualTo(endpoint);
		assertThat(endpoint).isEqualTo(new MethodEndpoint(this, method));

		Method otherMethod = getClass().getMethod("testEquals");

		assertThat(new MethodEndpoint(this, otherMethod).equals(endpoint)).isFalse();
	}

	@Test
	public void testHashCode() throws Exception {

		assertThat(endpoint.hashCode()).isEqualTo(new MethodEndpoint(this, method).hashCode());

		Method otherMethod = getClass().getMethod("testEquals");

		assertThat(new MethodEndpoint(this, otherMethod).hashCode() == endpoint.hashCode()).isFalse();
	}

	@Test
	public void testToString() {
		assertThat(endpoint.toString()).isNotNull();
	}

	public void myMethod(String arg) {

		assertThat(arg).isEqualTo("arg");

		myMethodInvoked = true;
	}
}
