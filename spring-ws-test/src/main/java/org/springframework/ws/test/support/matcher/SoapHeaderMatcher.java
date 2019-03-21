/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.matcher;

import java.io.IOException;
import java.util.Iterator;
import javax.xml.namespace.QName;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;

import static org.springframework.ws.test.support.AssertionErrors.assertTrue;

/**
 * Matches SOAP headers.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class SoapHeaderMatcher extends AbstractSoapMessageMatcher {

	private final QName soapHeaderName;

	/**
	 * Creates a new instance of the {@code SoapHeaderMatcher} that checks for the presence of the given SOAP header
	 * name.
	 *
	 * @param soapHeaderName the header name to check for
	 */
	public SoapHeaderMatcher(QName soapHeaderName) {
		Assert.notNull(soapHeaderName, "'soapHeaderName' must not be null");
		this.soapHeaderName = soapHeaderName;
	}

	@Override
	protected void match(SoapMessage soapMessage) throws IOException, AssertionError {
		SoapHeader soapHeader = soapMessage.getSoapHeader();
		assertTrue("SOAP message [" + soapMessage + "] does not contain SOAP header", soapHeader != null, "Envelope",
				soapMessage.getEnvelope().getSource());

		Iterator<SoapHeaderElement> soapHeaderElementIterator = soapHeader.examineAllHeaderElements();
		boolean found = false;
		while (soapHeaderElementIterator.hasNext()) {
			SoapHeaderElement soapHeaderElement = soapHeaderElementIterator.next();
			if (soapHeaderName.equals(soapHeaderElement.getName())) {
				found = true;
				break;
			}
		}
		assertTrue("SOAP header [" + soapHeaderName + "] not found", found, "Envelope",
				soapMessage.getEnvelope().getSource());
	}
}
