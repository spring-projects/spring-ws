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

package org.springframework.ws.server.endpoint;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodEndpointTests {

	private MethodEndpoint endpoint;

	private boolean myMethodInvoked;

	private Method method;

	@BeforeEach
	void setUp() throws Exception {

		this.myMethodInvoked = false;
		this.method = getClass().getMethod("myMethod", String.class);
		this.endpoint = new MethodEndpoint(this, this.method);
	}

	@Test
	void testGetters() {

		assertThat(this.endpoint.getBean()).isEqualTo(this);
		assertThat(this.endpoint.getMethod()).isEqualTo(this.method);
	}

	@Test
	void testInvoke() throws Exception {

		assertThat(this.myMethodInvoked).isFalse();

		this.endpoint.invoke("arg");

		assertThat(this.myMethodInvoked).isTrue();
	}

	@Test
	void testEquals() throws Exception {

		assertThat(this.endpoint).isEqualTo(new MethodEndpoint(this, this.method));

		Method otherMethod = getClass().getDeclaredMethod("testEquals");

		assertThat(new MethodEndpoint(this, otherMethod).equals(this.endpoint)).isFalse();
	}

	@Test
	void testHashCode() throws Exception {

		assertThat(this.endpoint.hashCode()).isEqualTo(new MethodEndpoint(this, this.method).hashCode());

		Method otherMethod = getClass().getDeclaredMethod("testEquals");

		assertThat(new MethodEndpoint(this, otherMethod).hashCode() == this.endpoint.hashCode()).isFalse();
	}

	@Test
	void testToString() {
		assertThat(this.endpoint.toString()).isNotNull();
	}

	public void myMethod(String arg) {

		assertThat(arg).isEqualTo("arg");

		this.myMethodInvoked = true;
	}

}
