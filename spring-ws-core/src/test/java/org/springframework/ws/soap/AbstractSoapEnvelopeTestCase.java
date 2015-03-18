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

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public abstract class AbstractSoapEnvelopeTestCase extends AbstractSoapElementTestCase {

	protected SoapEnvelope soapEnvelope;

	@Override
	protected final SoapElement createSoapElement() throws Exception {
		soapEnvelope = createSoapEnvelope();
		return soapEnvelope;
	}

	protected abstract SoapEnvelope createSoapEnvelope() throws Exception;

	@Test
	public void testGetHeader() throws Exception {
		SoapHeader header = soapEnvelope.getHeader();
		assertNotNull("No header returned", header);
	}

	@Test
	public void testGetBody() throws Exception {
		SoapBody body = soapEnvelope.getBody();
		assertNotNull("No body returned", body);
	}
}
