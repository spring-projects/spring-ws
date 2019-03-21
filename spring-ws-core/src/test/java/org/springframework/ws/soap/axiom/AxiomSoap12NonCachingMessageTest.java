/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMSourcedElement;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageTestCase;

import static org.junit.Assert.*;

public class AxiomSoap12NonCachingMessageTest extends AbstractSoap12MessageTestCase {

	@Override
	protected String getNS() {
		return "soapenv";
	}

	@Override
	protected SoapMessage createSoapMessage() throws Exception {
		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.setSoapVersion(SoapVersion.SOAP_12);

		return messageFactory.createWebServiceMessage();
	}

	@Override
	public void testWriteToTransportOutputStream() throws Exception {
		super.testWriteToTransportOutputStream();

		SoapBody body = soapMessage.getSoapBody();
		OMSourcedElement axiomPayloadEle =
				(OMSourcedElement) ((AxiomSoapBody) body).getAxiomElement().getFirstElement();
		assertFalse("Non-cached body should not be expanded now", axiomPayloadEle.isExpanded());
		axiomPayloadEle.getFirstElement();
		assertTrue("Non-cached body should now be expanded", axiomPayloadEle.isExpanded());
		assertEquals("Invalid payload", "payload", axiomPayloadEle.getLocalName());
	}
}
