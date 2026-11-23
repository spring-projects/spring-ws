/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.test.support.matcher.XPathExpectationsHelper;

/**
 * Adapts {@link XPathExpectationsHelper} into the {@link ResponseXPathExpectations}
 * contract.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class XPathExpectationsHelperAdapter implements ResponseXPathExpectations {

	private final XPathExpectationsHelper helper;

	XPathExpectationsHelperAdapter(String expression, @Nullable Map<String, String> namespaces) {
		this.helper = new XPathExpectationsHelper(expression, namespaces);
	}

	@Override
	public ResponseMatcher exists() {
		return new WebServiceMessageMatcherAdapter(this.helper.exists());
	}

	@Override
	public ResponseMatcher doesNotExist() {
		return new WebServiceMessageMatcherAdapter(this.helper.doesNotExist());
	}

	@Override
	public ResponseMatcher evaluatesTo(boolean expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

	@Override
	public ResponseMatcher evaluatesTo(int expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

	@Override
	public ResponseMatcher evaluatesTo(double expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

	@Override
	public ResponseMatcher evaluatesTo(String expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

}
