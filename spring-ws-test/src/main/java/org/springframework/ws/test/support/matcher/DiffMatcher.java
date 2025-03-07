/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.test.support.matcher;

import org.custommonkey.xmlunit.Diff;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.AssertionErrors;

/**
 * Implementation of {@link WebServiceMessageMatcher} based on XMLUnit's {@link Diff}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 * @deprecated Migrate to
 * {@link org.springframework.ws.test.support.matcher.xmlunit2.DiffMatcher}.
 */
@Deprecated
public abstract class DiffMatcher implements WebServiceMessageMatcher {

	@Override
	public final void match(WebServiceMessage message) throws AssertionError {

		Diff diff = createDiff(message);
		AssertionErrors.assertTrue("Messages are different, " + diff.toString(), diff.similar(), "Payload",
				message.getPayloadSource());
	}

	/**
	 * Creates a {@link Diff} for the given message.
	 * @param message the message
	 * @return the diff
	 */
	protected abstract Diff createDiff(WebServiceMessage message);

}
