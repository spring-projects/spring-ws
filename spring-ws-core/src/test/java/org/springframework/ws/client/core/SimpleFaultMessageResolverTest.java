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

package org.springframework.ws.client.core;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class SimpleFaultMessageResolverTest {

	private SimpleFaultMessageResolver resolver;

	@Before
	public void setUp() throws Exception {
		resolver = new SimpleFaultMessageResolver();
	}

	@Test
	public void testResolveFault() throws Exception {
		FaultAwareWebServiceMessage messageMock = createMock(FaultAwareWebServiceMessage.class);
		String message = "message";
		expect(messageMock.getFaultReason()).andReturn(message);

		replay(messageMock);

		try {
			resolver.resolveFault(messageMock);
			Assert.fail("WebServiceFaultExcpetion expected");
		}
		catch (WebServiceFaultException ex) {
			// expected
			Assert.assertEquals("Invalid exception message", message, ex.getMessage());
		}
		verify(messageMock);
	}
}