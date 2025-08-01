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

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.ws.WebServiceMessage;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

class UriMatcherTests {

	private static final URI GOOD_URI = URI.create("http://localhost");

	@Test
	void match() {

		WebServiceMessage message = createMock(WebServiceMessage.class);

		expect(message.getPayloadSource()).andReturn(null);

		replay(message);

		UriMatcher matcher = new UriMatcher(GOOD_URI);
		matcher.match(GOOD_URI, message);
	}

	@Test
	void nonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			WebServiceMessage message = createMock(WebServiceMessage.class);
			expect(message.getPayloadSource()).andReturn(null);
			replay(message);
			UriMatcher matcher = new UriMatcher(GOOD_URI);
			matcher.match(URI.create("http://www.example.org"), message);
		});
	}

}
