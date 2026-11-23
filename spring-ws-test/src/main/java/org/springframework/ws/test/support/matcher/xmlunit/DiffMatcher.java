/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support.matcher.xmlunit;

import org.xmlunit.diff.Diff;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.AssertionErrors;
import org.springframework.ws.test.support.matcher.WebServiceMessageMatcher;

/**
 * Implementation of {@link WebServiceMessageMatcher} based on XMLUnit's {@link Diff}.
 *
 * @author Greg Turnquist
 * @since 4.0
 */
public abstract class DiffMatcher implements WebServiceMessageMatcher {

	@Override
	public final void match(WebServiceMessage message) throws AssertionError {
		Diff diff = createDiff(message);
		AssertionErrors.assertTrue("Messages are different, " + diff.toString(), !diff.hasDifferences(), "Payload",
				message.getPayloadSource());
	}

	/**
	 * Creates a {@link Diff} for the given message.
	 * @param message the message
	 * @return the diff
	 */
	protected abstract Diff createDiff(WebServiceMessage message);

}
