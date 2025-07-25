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

package org.springframework.ws.test.server;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.test.support.AssertionErrors;

/**
 * Abstract Implementation of {@link ResponseMatcher} that checks for a SOAP fault.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class SoapFaultResponseMatcher implements ResponseMatcher {

	private final @Nullable String expectedFaultStringOrReason;

	SoapFaultResponseMatcher(@Nullable String expectedFaultStringOrReason) {
		this.expectedFaultStringOrReason = expectedFaultStringOrReason;
	}

	@Override
	public void match(WebServiceMessage request, WebServiceMessage response) throws IOException, AssertionError {
		AssertionErrors.assertTrue("Response is not a SOAP message", response instanceof SoapMessage);
		SoapMessage soapResponse = (SoapMessage) response;
		SoapBody responseBody = soapResponse.getSoapBody();
		AssertionErrors.assertTrue("Response has no SOAP Body", responseBody != null);
		AssertionErrors.assertTrue("Response has no SOAP Fault", responseBody.hasFault());
		SoapFault soapFault = responseBody.getFault();
		QName expectedFaultCode = getExpectedFaultCode(soapResponse.getVersion());
		AssertionErrors.assertEquals("Invalid SOAP Fault code", expectedFaultCode,
				(soapFault != null) ? soapFault.getFaultCode() : null);
		if (this.expectedFaultStringOrReason != null) {
			AssertionErrors.assertEquals("Invalid SOAP Fault string/reason", this.expectedFaultStringOrReason,
					(soapFault != null) ? soapFault.getFaultStringOrReason() : null);
		}
	}

	/**
	 * Returns the SOAP fault code to check for, given the SOAP version.
	 */
	protected abstract QName getExpectedFaultCode(SoapVersion version);

}
