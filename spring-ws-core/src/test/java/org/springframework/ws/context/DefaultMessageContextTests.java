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

package org.springframework.ws.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class DefaultMessageContextTests {

	private DefaultMessageContext context;

	private WebServiceMessageFactory factoryMock;

	private WebServiceMessage request;

	@BeforeEach
	void setUp() {

		this.factoryMock = createMock(WebServiceMessageFactory.class);
		this.request = new MockWebServiceMessage();
		this.context = new DefaultMessageContext(this.request, this.factoryMock);
	}

	@Test
	void testRequest() {
		assertThat(this.context.getRequest()).isEqualTo(this.request);
	}

	@Test
	void testResponse() {

		WebServiceMessage response = new MockWebServiceMessage();
		expect(this.factoryMock.createWebServiceMessage()).andReturn(response);
		replay(this.factoryMock);

		WebServiceMessage result = this.context.getResponse();

		assertThat(result).isEqualTo(response);

		verify(this.factoryMock);
	}

	@Test
	void testProperties() {

		assertThat(this.context.getPropertyNames()).hasSize(0);

		String name = "name";

		assertThat(this.context.containsProperty(name)).isFalse();

		String value = "value";
		this.context.setProperty(name, value);

		assertThat(this.context.containsProperty(name)).isTrue();
		assertThat(this.context.getPropertyNames()).containsExactly(name);
		assertThat(this.context.getProperty(name)).isEqualTo(value);

		this.context.removeProperty(name);

		assertThat(this.context.containsProperty(name)).isFalse();
		assertThat(this.context.getPropertyNames()).isEmpty();
	}

}
