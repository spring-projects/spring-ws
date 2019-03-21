/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap;

import java.util.Locale;
import javax.xml.transform.dom.DOMResult;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

public abstract class AbstractSoapBodyTestCase extends AbstractSoapElementTestCase {

	protected SoapBody soapBody;

	@Override
	protected final SoapElement createSoapElement() throws Exception {
		soapBody = createSoapBody();
		return soapBody;
	}

	protected abstract SoapBody createSoapBody() throws Exception;

	@Test
	public void testPayload() throws Exception {
		String payload = "<payload xmlns='https://www.springframework.org' />";
		transformer.transform(new StringSource(payload), soapBody.getPayloadResult());
		assertPayloadEqual(payload);
	}

	@Test
	public void testGetPayloadResultTwice() throws Exception {
		String payload = "<payload xmlns='https://www.springframework.org' />";
		transformer.transform(new StringSource(payload), soapBody.getPayloadResult());
		transformer.transform(new StringSource(payload), soapBody.getPayloadResult());
		DOMResult domResult = new DOMResult();
		transformer.transform(soapBody.getSource(), domResult);
		Element bodyElement = ((Document) domResult.getNode()).getDocumentElement();
		NodeList children = bodyElement.getChildNodes();
		assertEquals("Invalid amount of child nodes", 1, children.getLength());
	}

	@Test
	public void testNoFault() throws Exception {
		assertFalse("body has fault", soapBody.hasFault());
	}

	@Test
	public void testAddFaultWithExistingPayload() throws Exception {
		StringSource contents = new StringSource("<payload xmlns='https://www.springframework.org' />");
		transformer.transform(contents, soapBody.getPayloadResult());
		soapBody.addMustUnderstandFault("faultString", Locale.ENGLISH);
		assertTrue("Body has no fault", soapBody.hasFault());
	}

	protected void assertPayloadEqual(String expectedPayload) throws Exception {
		StringResult result = new StringResult();
		transformer.transform(soapBody.getPayloadSource(), result);
		assertXMLEqual("Invalid payload contents", expectedPayload, result.toString());
	}

}
