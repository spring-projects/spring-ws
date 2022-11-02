/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.client.core;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;

public class SimpleFaultMessageResolverTest {

	private SimpleFaultMessageResolver resolver;

	@BeforeEach
	public void setUp() throws Exception {
		resolver = new SimpleFaultMessageResolver();
	}

	@Test
	public void testResolveFault() {

		FaultAwareWebServiceMessage messageMock = createMock(FaultAwareWebServiceMessage.class);
		String message = "message";
		expect(messageMock.getFaultReason()).andReturn(message);

		replay(messageMock);

		assertThatExceptionOfType(WebServiceFaultException.class).isThrownBy(() -> resolver.resolveFault(messageMock))
				.withMessage(message);

		verify(messageMock);
	}
}
