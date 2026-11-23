/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
