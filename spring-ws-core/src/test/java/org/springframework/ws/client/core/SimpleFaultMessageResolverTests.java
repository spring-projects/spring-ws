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

package org.springframework.ws.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class SimpleFaultMessageResolverTests {

	private SimpleFaultMessageResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new SimpleFaultMessageResolver();
	}

	@Test
	void testResolveFault() {

		FaultAwareWebServiceMessage messageMock = createMock(FaultAwareWebServiceMessage.class);
		String message = "message";
		expect(messageMock.getFaultReason()).andReturn(message);

		replay(messageMock);

		assertThatExceptionOfType(WebServiceFaultException.class)
			.isThrownBy(() -> this.resolver.resolveFault(messageMock))
			.withMessage(message);

		verify(messageMock);
	}

}
