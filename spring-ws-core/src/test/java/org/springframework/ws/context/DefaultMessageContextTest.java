/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.context;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

public class DefaultMessageContextTest {

	private DefaultMessageContext context;

	private WebServiceMessageFactory factoryMock;

	private WebServiceMessage request;

	@BeforeEach
	public void setUp() throws Exception {

		factoryMock = createMock(WebServiceMessageFactory.class);
		request = new MockWebServiceMessage();
		context = new DefaultMessageContext(request, factoryMock);
	}

	@Test
	public void testRequest() {
		assertThat(context.getRequest()).isEqualTo(request);
	}

	@Test
	public void testResponse() {

		WebServiceMessage response = new MockWebServiceMessage();
		expect(factoryMock.createWebServiceMessage()).andReturn(response);
		replay(factoryMock);

		WebServiceMessage result = context.getResponse();

		assertThat(result).isEqualTo(response);

		verify(factoryMock);
	}

	@Test
	public void testProperties() {

		assertThat(context.getPropertyNames()).hasSize(0);

		String name = "name";

		assertThat(context.containsProperty(name)).isFalse();

		String value = "value";
		context.setProperty(name, value);

		assertThat(context.containsProperty(name)).isTrue();
		assertThat(context.getPropertyNames()).containsExactly(name);
		assertThat(context.getProperty(name)).isEqualTo(value);

		context.removeProperty(name);

		assertThat(context.containsProperty(name)).isFalse();
		assertThat(context.getPropertyNames()).isEmpty();
	}
}
