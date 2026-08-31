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
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.xml.namespace.QName;

import io.micrometer.common.KeyValue;
import io.micrometer.common.docs.KeyName;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.observation.SoapClientObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ws.client.core.observation.SoapClientObservationDocumentation.LowCardinalityKeyNames;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
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

	private final SoapClientObservationConvention convention = new DefaultSoapClientObservationConvention();

	private final FaultAwareWebServiceConnection connection = mock(FaultAwareWebServiceConnection.class);

	private final MockWebServiceMessageFactory messageFactory = new MockWebServiceMessageFactory();

	private final MockWebServiceMessage response = this.messageFactory.createWebServiceMessage();

	private final MessageContext messageContext = new DefaultMessageContext(
			this.messageFactory.createWebServiceMessage(), this.messageFactory);

	private final SoapClientObservationContext context = new SoapClientObservationContext(this.messageContext,
			this.connection);

	@Test
	void observationName() {
		assertThat(this.convention.getName()).isEqualTo("soap.client.requests");
	}

	@Test
	void observationNameCanBeCustomized() {
		assertThat(new DefaultSoapClientObservationConvention("custom.name").getName()).isEqualTo("custom.name");
	}

	@Test
	void keyValuesMatchTheDocumentedKeyNames() {
		withResponse();
		assertThat(this.convention.getLowCardinalityKeyValues(this.context).stream().map(KeyValue::getKey))
			.containsExactlyInAnyOrderElementsOf(keyNames(LowCardinalityKeyNames.values()));
		assertThat(this.convention.getHighCardinalityKeyValues(this.context).stream().map(KeyValue::getKey))
			.containsExactlyInAnyOrderElementsOf(keyNames(HighCardinalityKeyNames.values()));
	}

	@Test
	void contextualName() {
		this.context.setOperationName("getCountry");
		assertThat(this.convention.getContextualName(this.context)).isEqualTo("soap getCountry");
	}

	@Test
	void contextualNameWithUnknownOperation() {
		assertThat(this.convention.getContextualName(this.context)).isEqualTo("soap");
	}

	@Test
	void exception() {
		this.context.setError(new WebServiceIOException("Connection refused"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("exception", "WebServiceIOException"));
	}

	@Test
	void exceptionWithNoError() {
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("exception", "none"));
	}

	@Test
	void faultCode() {
		withResponse();
		this.response.setFaultCode(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(lowCardinalityKeyValues())
			.contains(KeyValue.of("fault.code", "{http://schemas.xmlsoap.org/soap/envelope/}Client"));
	}

	@Test
	void faultCodeWithRegularResponse() {
		withResponse();
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("fault.code", "none"));
	}

	@Test
	void namespace() {
		this.context.setNamespace("https://spring.io/guides/gs-producing-web-service");
		assertThat(lowCardinalityKeyValues())
			.contains(KeyValue.of("namespace", "https://spring.io/guides/gs-producing-web-service"));
	}

	@Test
	void namespaceWithUnknownOperation() {
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "none"));
	}

	@Test
	void operationName() {
		this.context.setOperationName("getCountry");
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("operation.name", "getCountry"));
	}

	@Test
	void operationNameWithUnknownOperation() {
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("operation.name", "none"));
	}

	@Test
	void payloadRootQNameSetsNamespaceAndOperationName() {
		this.context.setPayloadRootQName(QName.valueOf("{https://spring.io/spring-ws}getCountryRequest"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "https://spring.io/spring-ws"),
				KeyValue.of("operation.name", "getCountryRequest"));
	}

	@Test
	void payloadRootQNameWithNoNamespace() {
		this.context.setPayloadRootQName(QName.valueOf("getCountryRequest"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "none"),
				KeyValue.of("operation.name", "getCountryRequest"));
	}

	@Test
	void outcomeSuccess() {
		withResponse();
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("outcome", "SUCCESS"));
	}

	@Test
	void outcomeFault() {
		withResponse();
		this.response.setFault(true);
		this.response.setFaultCode(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("outcome", "FAULT"));
	}

	@Test
	void outcomeFaultTakesPrecedenceOverTheErrorRaisedByTheFaultMessageResolver() {
		withResponse();
		this.response.setFault(true);
		this.context.setError(new IllegalStateException("Fault!"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("outcome", "FAULT"),
				KeyValue.of("exception", "IllegalStateException"));
	}

	@Test
	void outcomeError() {
		this.context.setError(new RuntimeException("Test exception"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("outcome", "ERROR"));
	}

	@Test
	void outcomeUnknownWithNoResponse() {
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("outcome", "UNKNOWN"));
	}

	@Test
	void httpTransport() throws Exception {
		when(this.connection.getUri()).thenReturn(URI.create("https://localhost:443/services"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("protocol", "https"));
		assertThat(highCardinalityKeyValues()).contains(KeyValue.of("uri", "https://localhost:443/services"));
	}

	@Test
	void mailTransport() throws Exception {
		when(this.connection.getUri()).thenReturn(URI.create("mailto:server@localhost?subject=SOAP%20Test"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("protocol", "mailto"));
		assertThat(highCardinalityKeyValues())
			.contains(KeyValue.of("uri", "mailto:server@localhost?subject=SOAP%20Test"));
	}

	@Test
	void jmsTransport() throws Exception {
		when(this.connection.getUri()).thenReturn(URI.create("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("protocol", "jms"));
		assertThat(highCardinalityKeyValues())
			.contains(KeyValue.of("uri", "jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT"));
	}

	@Test
	void transportWithInvalidUri() throws Exception {
		when(this.connection.getUri()).thenThrow(new URISyntaxException("not a uri", "test"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("protocol", "none"));
		assertThat(highCardinalityKeyValues()).contains(KeyValue.of("uri", "none"));
	}

	@Test
	void soapAction() {
		SoapMessage soapMessage = mock(SoapMessage.class);
		when(soapMessage.getSoapAction()).thenReturn("http://example.com/action");
		SoapClientObservationContext context = new SoapClientObservationContext(
				new DefaultMessageContext(soapMessage, this.messageFactory), this.connection);
		assertThat(this.convention.getLowCardinalityKeyValues(context))
			.contains(KeyValue.of("soap.action", "http://example.com/action"));
	}

	@Test
	void soapActionWithNonSoapMessage() {
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("soap.action", "none"));
	}

	@Test
	void faultReason() {
		withResponse();
		this.response.setFaultReason("Invalid country format");
		assertThat(highCardinalityKeyValues()).contains(KeyValue.of("fault.reason", "Invalid country format"));
	}

	@Test
	void faultReasonWithRegularResponse() {
		withResponse();
		assertThat(highCardinalityKeyValues()).contains(KeyValue.of("fault.reason", "none"));
	}

	private void withResponse() {
		this.messageContext.setResponse(this.response);
	}

	private Iterable<KeyValue> lowCardinalityKeyValues() {
		return this.convention.getLowCardinalityKeyValues(this.context);
	}

	private Iterable<KeyValue> highCardinalityKeyValues() {
		return this.convention.getHighCardinalityKeyValues(this.context);
	}

	private static Iterable<String> keyNames(KeyName[] keyNames) {
		return Arrays.stream(keyNames).map(KeyName::asString).toList();
	}

}
