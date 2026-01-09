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
 * Tests for {@link DefaultSoapClientObservationConvention}.
 *
 * @author Stephane Nicoll
 */
class DefaultSoapClientObservationConventionTests {

	FaultAwareWebServiceConnection connection = mock(FaultAwareWebServiceConnection.class);

	private final SoapClientObservationConvention convention = new DefaultSoapClientObservationConvention();

	private final MockWebServiceMessageFactory messageFactory = new MockWebServiceMessageFactory();

	private final MockWebServiceMessage response = this.messageFactory.createWebServiceMessage();

	private final MessageContext messageContext = new DefaultMessageContext(
			this.messageFactory.createWebServiceMessage(), this.messageFactory);

	private final SoapClientObservationContext context = new SoapClientObservationContext(this.messageContext,
			this.connection);

	@BeforeEach
	void setup() {
		this.messageContext.setResponse(this.response);
	}

	@Test
	void observationName() {
		assertThat(this.convention.getName()).isEqualTo("soap.client.requests");
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
	void namespace() {
		this.context.setNamespace("https://spring.io/guides/gs-producing-web-service");
		assertThat(this.convention.getLowCardinalityKeyValues(this.context))
			.contains(KeyValue.of("namespace", "https://spring.io/guides/gs-producing-web-service"));
	}

	@Test
	void operationName() {
		this.context.setOperationName("getCountry");
		assertThat(this.convention.getLowCardinalityKeyValues(this.context))
			.contains(KeyValue.of("operation.name", "getCountry"));
	}

	@Test
	void httpTransport() throws Exception {
		when(this.connection.getUri()).thenReturn(URI.create("https://localhost:443/services"));
		assertThat(this.convention.getLowCardinalityKeyValues(this.context)).contains(KeyValue.of("protocol", "https"));
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
			.contains(KeyValue.of("uri", "https://localhost:443/services"));
	}

	@Test
	void mailTransport() throws Exception {
		when(this.connection.getUri()).thenReturn(URI.create("mailto:server@localhost?subject=SOAP%20Test"));
		assertThat(this.convention.getLowCardinalityKeyValues(this.context))
			.contains(KeyValue.of("protocol", "mailto"));
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
			.contains(KeyValue.of("uri", "mailto:server@localhost?subject=SOAP%20Test"));
	}

	@Test
	void jmsTransport() throws Exception {
		when(this.connection.getUri()).thenReturn(URI.create("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT"));
		assertThat(this.convention.getLowCardinalityKeyValues(this.context)).contains(KeyValue.of("protocol", "jms"));
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
