/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.AssertionErrors;

/**
 * Matches {@link URI}s.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class UriMatcher implements RequestMatcher {

	private final URI expected;

	UriMatcher(URI expected) {
		this.expected = expected;
	}

	@Override
	public void match(URI actual, WebServiceMessage request) {
		AssertionErrors.assertEquals("Unexpected connection", this.expected, actual, "Payload",
				request.getPayloadSource());
	}

}
