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

package org.springframework.ws.client.core.observation;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SoapClientObservationContext}.
 *
 * @author Stephane Nicoll
 */
class SoapClientObservationContextTests {

	private final MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

	@Test
	void createRegistersContextInMessageContext() {
		SoapClientObservationContext context = new SoapClientObservationContext(this.messageContext, mock());
		assertThat(SoapClientObservationContext.findCurrentObservationContext(this.messageContext)).isPresent()
			.get()
			.isSameAs(context);
	}

	@Test
	void findCurrentObservationContextWithNoObservation() {
		assertThat(SoapClientObservationContext.findCurrentObservationContext(this.messageContext)).isEmpty();
	}

	@Test
	void setHeaderWritesRequestHeaderOnHeadersAwareConnection() throws IOException {
		HeadersAwareSenderWebServiceConnection connection = mock(HeadersAwareSenderWebServiceConnection.class);
		new SoapClientObservationContext.HeaderSetter().set(connection, "traceparent", "00-trace-span-01");
		verify(connection).addRequestHeader("traceparent", "00-trace-span-01");
	}

	@Test
	void setHeaderIgnoresConnectionThatDoesNotSupportHeaders() {
		WebServiceConnection connection = mock(WebServiceConnection.class);
		new SoapClientObservationContext.HeaderSetter().set(connection, "traceparent", "00-trace-span-01");
	}

	@Test
	void setHeaderIgnoresFailureToWriteTheHeader() throws IOException {
		HeadersAwareSenderWebServiceConnection connection = mock(HeadersAwareSenderWebServiceConnection.class);
		doThrow(new IOException("Connection closed")).when(connection)
			.addRequestHeader("traceparent", "00-trace-span-01");
		new SoapClientObservationContext.HeaderSetter().set(connection, "traceparent", "00-trace-span-01");
	}

}
