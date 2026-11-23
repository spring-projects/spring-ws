/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.matcher.WebServiceMessageMatcher;

/**
 * Adapts a {@link WebServiceMessageMatcher} to the {@link ResponseMatcher} contract.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class WebServiceMessageMatcherAdapter implements ResponseMatcher {

	private final WebServiceMessageMatcher adaptee;

	WebServiceMessageMatcherAdapter(WebServiceMessageMatcher adaptee) {
		Assert.notNull(adaptee, "'adaptee' must not be null");
		this.adaptee = adaptee;
	}

	@Override
	public void match(@Nullable WebServiceMessage request, WebServiceMessage response)
			throws IOException, AssertionError {
		this.adaptee.match(response);
	}

}
