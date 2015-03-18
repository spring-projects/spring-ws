/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.matcher.WebServiceMessageMatcher;

/**
 * Adapts a {@link WebServiceMessageMatcher} to the {@link RequestMatcher} contract.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class WebServiceMessageMatcherAdapter implements RequestMatcher {

	private final WebServiceMessageMatcher adaptee;

	WebServiceMessageMatcherAdapter(WebServiceMessageMatcher adaptee) {
		Assert.notNull(adaptee, "'adaptee' must not be null");
		this.adaptee = adaptee;
	}

	@Override
	public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
		adaptee.match(request);
	}
}
