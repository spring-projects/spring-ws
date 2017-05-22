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

package org.springframework.ws.soap;

import org.springframework.ws.AbstractWebServiceMessageFactoryTestCase;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessage;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public abstract class AbstractSoapMessageFactoryTestCase extends AbstractWebServiceMessageFactoryTestCase {

	@Test
	public void testCreateEmptySoapMessage() throws Exception {
		WebServiceMessage message = messageFactory.createWebServiceMessage();
		assertTrue("Not a SoapMessage", message instanceof SoapMessage);
	}

	@Test(expected = InvalidXmlException.class)
	public abstract void testCreateSoapMessageIllFormedXml() throws Exception;

	@Test
	public abstract void testCreateSoapMessageNoAttachment() throws Exception;

	@Test
	public abstract void testCreateSoapMessageSwA() throws Exception;

	@Test
	public abstract void testCreateSoapMessageMtom() throws Exception;

	@Test
	public abstract void testCreateSoapMessageMissingContentType() throws Exception;
}
