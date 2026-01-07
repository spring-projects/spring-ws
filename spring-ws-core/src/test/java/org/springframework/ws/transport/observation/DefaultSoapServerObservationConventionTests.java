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

import java.net.URI;

import io.micrometer.common.KeyValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultSoapServerObservationConvention}.
 *
 * @author Brian Clozel
 */
class DefaultSoapServerObservationConventionTests {

	FaultAwareWebServiceConnection connectionMock = mock(FaultAwareWebServiceConnection.class);

	SoapServerObservationConvention convention = new DefaultSoapServerObservationConvention();

	SoapServerObservationContext context = new SoapServerObservationContext(this.connectionMock);

	MockWebServiceMessage request = new MockWebServiceMessage();

	MockWebServiceMessage response = new MockWebServiceMessage();

	MockWebServiceMessageFactory messageFactory = new MockWebServiceMessageFactory();

	MessageContext messageContext = new DefaultMessageContext(this.request, this.messageFactory);

	@BeforeEach
	void setup() {
		this.messageContext.setResponse(this.response);
		this.context.setAsCurrent(this.messageContext);
	}

	@Test
	void observationName() {
		assertThat(this.convention.getName()).isEqualTo("soap.server.requests");
	}

	@Test
	void contextualName() {
		this.context.setOperationName("getCountry");
		assertThat(this.convention.getContextualName(this.context)).isEqualTo("soap getCountry");
	}

	@Test
	void faultCode() {
		this.response.setFaultCode(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(this.convention.getLowCardinalityKeyValues(this.context))
			.contains(KeyValue.of("fault.code", "{http://schemas.xmlsoap.org/soap/envelope/}Client"));
	}

	@Test
	void operationName() {
		this.context.setOperationName("getCountry");
		assertThat(this.convention.getLowCardinalityKeyValues(this.context))
			.contains(KeyValue.of("operation.name", "getCountry"));
	}

	@Test
	void protocol() throws Exception {
		when(this.connectionMock.getUri()).thenReturn(URI.create("https://localhost:443/services"));
		assertThat(this.convention.getLowCardinalityKeyValues(this.context)).contains(KeyValue.of("protocol", "https"));
	}

	@Test
	void namespace() {
		this.context.setNamespace("https://spring.io/guides/gs-producing-web-service");
		assertThat(this.convention.getLowCardinalityKeyValues(this.context))
			.contains(KeyValue.of("namespace", "https://spring.io/guides/gs-producing-web-service"));
	}

	@Test
	void httpUri() throws Exception {
		when(this.connectionMock.getUri()).thenReturn(URI.create("https://localhost:443/services"));
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
			.contains(KeyValue.of("uri", "https://localhost:443/services"));
	}

	@Test
	void mailUri() throws Exception {
		when(this.connectionMock.getUri()).thenReturn(URI.create("mailto:server@localhost?subject=SOAP%20Test"));
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
				.contains(KeyValue.of("uri", "mailto:server@localhost?subject=SOAP%20Test"));
	}

	@Test
	void jmsUri() throws Exception {
		when(this.connectionMock.getUri()).thenReturn(URI.create("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT"));
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
				.contains(KeyValue.of("uri", "jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT"));
	}

	@Test
	void faultReason() {
		this.response.setFaultReason("Invalid country format");
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
			.contains(KeyValue.of("fault.reason", "Invalid country format"));
	}

}
