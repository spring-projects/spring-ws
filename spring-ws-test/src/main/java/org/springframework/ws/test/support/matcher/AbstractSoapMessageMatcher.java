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

package org.springframework.ws.test.support.matcher;

import java.io.IOException;

import static org.springframework.ws.test.support.AssertionErrors.assertTrue;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;

/**
 * Abstract base class for SOAP-specific {@link WebServiceMessageMatcher} implementations.
 *
 * <p>Asserts that the message given to {@link #match(WebServiceMessage)} is a {@link SoapMessage}, and invokes {@link
 * #match(SoapMessage)} with it if so.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class AbstractSoapMessageMatcher implements WebServiceMessageMatcher {

	@Override
	public final void match(WebServiceMessage message) throws IOException, AssertionError {
		assertTrue("Message is not a SOAP message", message instanceof SoapMessage);
		match((SoapMessage) message);
	}

	/**
	 * Abstract template method that gets invoked from {@link #match(WebServiceMessage)} if the given message is a
	 * {@link SoapMessage}.
	 *
	 * @param soapMessage the soap message
	 * @throws IOException	  in case of I/O errors
	 * @throws AssertionError if expectations are not met
	 */
	protected abstract void match(SoapMessage soapMessage) throws IOException, AssertionError;
}
