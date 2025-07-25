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

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.test.support.matcher.XPathExpectationsHelper;

/**
 * Adapts {@link XPathExpectationsHelper} into the {@link RequestXPathExpectations}
 * contract.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class XPathExpectationsHelperAdapter implements RequestXPathExpectations {

	private final XPathExpectationsHelper helper;

	XPathExpectationsHelperAdapter(String expression, @Nullable Map<String, String> namespaces) {
		this.helper = new XPathExpectationsHelper(expression, namespaces);
	}

	@Override
	public RequestMatcher exists() {
		return new WebServiceMessageMatcherAdapter(this.helper.exists());
	}

	@Override
	public RequestMatcher doesNotExist() {
		return new WebServiceMessageMatcherAdapter(this.helper.doesNotExist());
	}

	@Override
	public RequestMatcher evaluatesTo(boolean expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

	@Override
	public RequestMatcher evaluatesTo(int expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

	@Override
	public RequestMatcher evaluatesTo(double expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

	@Override
	public RequestMatcher evaluatesTo(String expectedValue) {
		return new WebServiceMessageMatcherAdapter(this.helper.evaluatesTo(expectedValue));
	}

}
