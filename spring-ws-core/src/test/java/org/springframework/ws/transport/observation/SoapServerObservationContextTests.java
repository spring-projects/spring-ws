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

package org.springframework.ws.transport.observation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.HeadersAwareReceiverWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpServletConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SoapServerObservationContext}.
 *
 * @author Brian Clozel
 */
class SoapServerObservationContextTests {

	private final MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

	private final SoapServerObservationContext context = new SoapServerObservationContext(mock());

	@Test
	void setAsCurrentRegistersContextInMessageContext() {
		this.context.setAsCurrent(this.messageContext);
		assertThat(SoapServerObservationContext.findCurrentObservationContext(this.messageContext)).isPresent()
			.get()
			.isSameAs(this.context);
	}

	@Test
	void findCurrentObservationContextWithNoObservation() {
		assertThat(SoapServerObservationContext.findCurrentObservationContext(this.messageContext)).isEmpty();
	}

	@Test
	void requestAndResponseAreNotAvailableBeforeTheMessageIsRead() {
		assertThat(this.context.getRequest()).isNull();
		assertThat(this.context.getResponse()).isNull();
		assertThat(this.context.getSoapAction()).isNull();
	}

	@Test
	void getHeaderReadsFirstRequestHeaderOnHeadersAwareConnection() throws IOException {
		HeadersAwareReceiverWebServiceConnection connection = mock(HeadersAwareReceiverWebServiceConnection.class);
		when(connection.getRequestHeaders("traceparent")).thenReturn(List.of("first", "second").iterator());
		assertThat(new SoapServerObservationContext.HeaderGetter().get(connection, "traceparent")).isEqualTo("first");
	}

	@Test
	void getHeaderWithNoValueReturnsNull() throws IOException {
		HeadersAwareReceiverWebServiceConnection connection = mock(HeadersAwareReceiverWebServiceConnection.class);
		when(connection.getRequestHeaders("traceparent")).thenReturn(Collections.emptyIterator());
		assertThat(new SoapServerObservationContext.HeaderGetter().get(connection, "traceparent")).isNull();
	}

	@Test
	void getHeaderIgnoresFailureToReadTheHeader() throws IOException {
		HeadersAwareReceiverWebServiceConnection connection = mock(HeadersAwareReceiverWebServiceConnection.class);
		when(connection.getRequestHeaders("traceparent")).thenThrow(new IOException("Connection closed"));
		assertThat(new SoapServerObservationContext.HeaderGetter().get(connection, "traceparent")).isNull();
	}

	@Test
	void getHeaderIgnoresHttpTransportAsTheServletLayerIsAlreadyInstrumented() throws IOException {
		HttpServletConnection connection = mock(HttpServletConnection.class);
		assertThat(new SoapServerObservationContext.HeaderGetter().get(connection, "traceparent")).isNull();
		verify(connection, never()).getRequestHeaders("traceparent");
	}

	@Test
	void getHeaderIgnoresConnectionThatDoesNotSupportHeaders() {
		WebServiceConnection connection = mock(WebServiceConnection.class);
		assertThat(new SoapServerObservationContext.HeaderGetter().get(connection, "traceparent")).isNull();
	}

}
