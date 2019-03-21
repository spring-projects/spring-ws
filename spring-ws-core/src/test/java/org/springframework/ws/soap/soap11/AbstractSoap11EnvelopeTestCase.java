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

package org.springframework.ws.soap.soap11;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.AbstractSoapEnvelopeTestCase;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;

import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

public abstract class AbstractSoap11EnvelopeTestCase extends AbstractSoapEnvelopeTestCase {

	@Test
	public void testGetName() throws Exception {
		assertEquals("Invalid qualified name",
				new QName(SoapVersion.SOAP_11.getEnvelopeNamespaceUri(), "Envelope"),
				soapEnvelope.getName());
	}

	@Test
	public void testGetContent() throws Exception {
		StringResult result = new StringResult();
		transformer.transform(soapEnvelope.getSource(), result);
		assertXMLEqual("<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body/></Envelope>",
				result.toString());
	}

}
