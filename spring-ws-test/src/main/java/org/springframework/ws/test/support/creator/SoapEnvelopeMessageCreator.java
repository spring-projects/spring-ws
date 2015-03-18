/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.creator;

import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.TransformerHelper;

import org.w3c.dom.Document;

import static org.springframework.ws.test.support.AssertionErrors.assertTrue;
import static org.springframework.ws.test.support.AssertionErrors.fail;

/**
 * Implementation of {@link WebServiceMessageCreator} that creates a request based on a SOAP envelope {@link Source}.
 *
 * @author Alexander Shutyaev
 * @since 2.1.1
 */
public class SoapEnvelopeMessageCreator extends AbstractMessageCreator {
	
	private final Source soapEnvelope;

	private final TransformerHelper transformerHelper = new TransformerHelper();
	
	/**
	 * Creates a new instance of the {@code SoapEnvelopeMessageCreator} with the given SOAP envelope source.
	 *
	 * @param soapEnvelope the SOAP envelope source
	 */
	public SoapEnvelopeMessageCreator(Source soapEnvelope) {
		Assert.notNull(soapEnvelope, "'soapEnvelope' must not be null");
		this.soapEnvelope = soapEnvelope;
	}

	@Override
	protected void doWithMessage(WebServiceMessage message) throws IOException {
		assertTrue("Message created with factory is not a SOAP message", message instanceof SoapMessage);
		SoapMessage soapMessage = (SoapMessage) message;
		try {
			DOMResult result = new DOMResult();
			transformerHelper.transform(soapEnvelope, result);
			soapMessage.setDocument((Document) result.getNode());
		}
		catch (TransformerException ex) {
			fail("Could not transform request SOAP envelope to message: " + ex.getMessage());
		}		
	}

}
