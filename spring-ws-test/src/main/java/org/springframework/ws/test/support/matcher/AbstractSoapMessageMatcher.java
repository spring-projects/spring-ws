/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support.matcher;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.test.support.AssertionErrors;

/**
 * Abstract base class for SOAP-specific {@link WebServiceMessageMatcher} implementations.
 * <p>
 * Asserts that the message given to {@link #match(WebServiceMessage)} is a
 * {@link SoapMessage}, and invokes {@link #match(SoapMessage)} with it if so.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class AbstractSoapMessageMatcher implements WebServiceMessageMatcher {

	@Override
	public final void match(WebServiceMessage message) throws IOException, AssertionError {
		AssertionErrors.assertTrue("Message is not a SOAP message", message instanceof SoapMessage);
		match((SoapMessage) message);
	}

	/**
	 * Abstract template method that gets invoked from {@link #match(WebServiceMessage)}
	 * if the given message is a {@link SoapMessage}.
	 * @param soapMessage the soap message
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if expectations are not met
	 */
	protected abstract void match(SoapMessage soapMessage) throws IOException, AssertionError;

}
