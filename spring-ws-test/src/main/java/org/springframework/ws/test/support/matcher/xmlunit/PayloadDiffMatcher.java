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

package org.springframework.ws.test.support.matcher.xmlunit;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.AssertionErrors;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Matches {@link Source} payloads.
 *
 * @author Greg Turnquist
 * @author Mikołaj Fejzer
 * @author Sharang Gupta
 * @since 4.0
 */
public class PayloadDiffMatcher extends DiffMatcher {

	private final Source expected;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	private @Nullable Document expectedDocument;

	public PayloadDiffMatcher(Source expected) {

		Assert.notNull(expected, "'expected' must not be null");
		this.expected = expected;
	}

	@Override
	protected final Diff createDiff(WebServiceMessage message) {

		Source payload = message.getPayloadSource();

		if (payload == null) {
			AssertionErrors.fail("Request message does not contain payload");
		}

		return createDiff(payload);
	}

	protected Diff createDiff(Source payload) {

		Document actualDocument = createDocumentFromSource(payload);

		return DiffBuilder.compare(getExpectedDocument())
			.withTest(actualDocument)
			.ignoreWhitespace()
			.withDifferenceEvaluator(
					DifferenceEvaluators.chain(new PlaceholderDifferenceEvaluator(), DifferenceEvaluators.Default))
			.checkForSimilar()
			.build();
	}

	/**
	 * Return the expected payload as a document. The expected source is transformed only
	 * once, so that a source that can be read only once can be matched more than once.
	 * @return the expected document
	 */
	private Document getExpectedDocument() {
		if (this.expectedDocument == null) {
			this.expectedDocument = createDocumentFromSource(this.expected);
		}
		return this.expectedDocument;
	}

	private Document createDocumentFromSource(Source source) {

		try {

			DOMResult result = new DOMResult();
			this.transformerHelper.transform(source, result);
			return (Document) result.getNode();
		}
		catch (TransformerException ex) {
			throw new AssertionError("Could not transform source to DOMResult" + ex.getMessage());
		}
	}

}
