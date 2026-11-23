/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support.matcher.xmlunit;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.test.support.AssertionErrors;
import org.springframework.ws.test.support.matcher.AbstractSoapMessageMatcher;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Matches {@link Source} SOAP envelopes.
 *
 * @author Greg Turnquist
 * @since 4.0
 */
public class SoapEnvelopeDiffMatcher extends AbstractSoapMessageMatcher {

	private final Source expected;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	public SoapEnvelopeDiffMatcher(Source expected) {

		Assert.notNull(expected, "'expected' must not be null");
		this.expected = expected;
	}

	@Override
	protected void match(SoapMessage soapMessage) throws IOException, AssertionError {

		Document actualDocument = soapMessage.getDocument();
		Document expectedDocument = createDocumentFromSource(this.expected);
		Diff diff = DiffBuilder.compare(expectedDocument)
			.ignoreWhitespace()
			.withTest(actualDocument)
			.withDifferenceEvaluator(
					DifferenceEvaluators.chain(new PlaceholderDifferenceEvaluator(), DifferenceEvaluators.Default))
			.checkForSimilar()
			.build();
		AssertionErrors.assertTrue("Envelopes are different, " + diff.toString(), !diff.hasDifferences());
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
