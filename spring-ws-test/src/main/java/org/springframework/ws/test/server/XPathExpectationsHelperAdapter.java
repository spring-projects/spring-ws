/*
 * Copyright 2005-2014 the original author or authors.
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

import java.util.Map;

import org.springframework.ws.test.support.matcher.XPathExpectationsHelper;

/**
 * Adapts {@link XPathExpectationsHelper} into the {@link ResponseXPathExpectations} contract.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class XPathExpectationsHelperAdapter implements ResponseXPathExpectations {

	private final XPathExpectationsHelper helper;

	XPathExpectationsHelperAdapter(String expression, Map<String, String> namespaces) {
		helper = new XPathExpectationsHelper(expression, namespaces);
	}

	@Override
	public ResponseMatcher exists() {
		return new WebServiceMessageMatcherAdapter(helper.exists());
	}

	@Override
	public ResponseMatcher doesNotExist() {
		return new WebServiceMessageMatcherAdapter(helper.doesNotExist());
	}

	@Override
	public ResponseMatcher evaluatesTo(boolean expectedValue) {
		return new WebServiceMessageMatcherAdapter(helper.evaluatesTo(expectedValue));
	}

	@Override
	public ResponseMatcher evaluatesTo(int expectedValue) {
		return new WebServiceMessageMatcherAdapter(helper.evaluatesTo(expectedValue));
	}

	@Override
	public ResponseMatcher evaluatesTo(double expectedValue) {
		return new WebServiceMessageMatcherAdapter(helper.evaluatesTo(expectedValue));
	}

	@Override
	public ResponseMatcher evaluatesTo(String expectedValue) {
		return new WebServiceMessageMatcherAdapter(helper.evaluatesTo(expectedValue));
	}

}
