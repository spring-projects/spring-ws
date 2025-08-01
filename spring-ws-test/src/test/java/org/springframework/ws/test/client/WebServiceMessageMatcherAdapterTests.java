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

package org.springframework.ws.test.client;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.matcher.WebServiceMessageMatcher;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author Arjen Poutsma
 */
class WebServiceMessageMatcherAdapterTests {

	private WebServiceMessage message;

	private WebServiceMessageMatcher adaptee;

	private WebServiceMessageMatcherAdapter adapter;

	@BeforeEach
	void setUp() {

		this.message = createMock(WebServiceMessage.class);
		this.adaptee = createMock(WebServiceMessageMatcher.class);
		this.adapter = new WebServiceMessageMatcherAdapter(this.adaptee);
	}

	@Test
	void match() throws IOException {

		this.adaptee.match(this.message);

		replay(this.message, this.adaptee);

		this.adapter.match(null, this.message);

		verify(this.message, this.adaptee);
	}

}
