/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.observation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
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

	@Test
	void soapActionIsReturnedWithoutTheQuotesOfTheWireFormat() {
		assertThat(soapAction("\"urn:example:action\"")).isEqualTo("urn:example:action");
	}

	@Test
	void soapActionWithoutQuotesIsReturnedAsIs() {
		assertThat(soapAction("urn:example:action")).isEqualTo("urn:example:action");
	}

	@Test
	void emptySoapActionIsNotAvailable() {
		assertThat(soapAction("\"\"")).isNull();
		assertThat(soapAction("")).isNull();
	}

	@Test
	void soapActionWithASingleQuoteIsReturnedAsIs() {
		assertThat(soapAction("\"")).isEqualTo("\"");
	}

	private @Nullable String soapAction(String value) {
		SoapMessage request = mock(SoapMessage.class);
		when(request.getSoapAction()).thenReturn(value);
		this.context.setAsCurrent(new DefaultMessageContext(request, new MockWebServiceMessageFactory()));
		return this.context.getSoapAction();
	}

}
