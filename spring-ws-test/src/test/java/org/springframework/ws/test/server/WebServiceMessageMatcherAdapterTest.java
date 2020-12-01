/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.server;

import static org.easymock.EasyMock.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.matcher.WebServiceMessageMatcher;

/**
 * @author Arjen Poutsma
 */
public class WebServiceMessageMatcherAdapterTest {

	private WebServiceMessage message;

	private WebServiceMessageMatcher adaptee;

	private WebServiceMessageMatcherAdapter adapter;

	@BeforeEach
	public void setUp() {

		message = createMock(WebServiceMessage.class);
		adaptee = createMock(WebServiceMessageMatcher.class);
		adapter = new WebServiceMessageMatcherAdapter(adaptee);
	}

	@Test
	public void match() throws IOException {

		adaptee.match(message);

		replay(message, adaptee);

		adapter.match(null, message);

		verify(message, adaptee);
	}
}
