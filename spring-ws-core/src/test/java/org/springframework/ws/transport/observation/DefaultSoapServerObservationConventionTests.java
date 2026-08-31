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
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.xml.namespace.QName;

import io.micrometer.common.KeyValue;
import io.micrometer.common.docs.KeyName;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.observation.SoapServerObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ws.transport.observation.SoapServerObservationDocumentation.LowCardinalityKeyNames;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultSoapServerObservationConvention}.
 *
 * @author Brian Clozel
 */
class DefaultSoapServerObservationConventionTests {

	private final SoapServerObservationConvention convention = new DefaultSoapServerObservationConvention();

	private final FaultAwareWebServiceConnection connection = mock(FaultAwareWebServiceConnection.class);

	private final MockWebServiceMessageFactory messageFactory = new MockWebServiceMessageFactory();

	private final MockWebServiceMessage request = new MockWebServiceMessage();

	private final MockWebServiceMessage response = new MockWebServiceMessage();

	private final MessageContext messageContext = new DefaultMessageContext(this.request, this.messageFactory);

	private final SoapServerObservationContext context = createContext(this.messageContext);

	@Test
	void observationName() {
		assertThat(this.convention.getName()).isEqualTo("soap.server.requests");
	}

	@Test
	void observationNameCanBeCustomized() {
		assertThat(new DefaultSoapServerObservationConvention("custom.name").getName()).isEqualTo("custom.name");
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
		this.context.setOperation(null, "getCountry");
		assertThat(this.convention.getContextualName(this.context)).isEqualTo("soap getCountry");
	}

	@Test
	void contextualNameWithUnmappedRequest() {
		assertThat(this.convention.getContextualName(this.context)).isEqualTo("soap");
	}

	@Test
	void exception() {
		this.context.setError(new NoEndpointFoundException(this.request));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("exception", "NoEndpointFoundException"));
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
	void operationIsResolvedFromQualifiedLookupKey() {
		this.messageContext.setProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY,
				QName.valueOf("{https://spring.io/guides/gs-producing-web-service}getCountryRequest"));
		assertThat(lowCardinalityKeyValues()).contains(
				KeyValue.of("namespace", "https://spring.io/guides/gs-producing-web-service"),
				KeyValue.of("operation.name", "getCountryRequest"));
	}

	@Test
	void operationIsResolvedFromUnqualifiedLookupKey() {
		this.messageContext.setProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY,
				QName.valueOf("getCountryRequest"));
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "none"),
				KeyValue.of("operation.name", "getCountryRequest"));
	}

	@Test
	void operationIsResolvedFromStringLookupKey() {
		this.messageContext.setProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY, "http://example.com/SoapAction");
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "none"),
				KeyValue.of("operation.name", "http://example.com/SoapAction"));
	}

	@Test
	void operationIsNotResolvedFromThePayloadOfAnUnmappedRequest() {
		this.request.setPayload("<attackerControlled xmlns='https://example.com/attacker'/>");
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "none"),
				KeyValue.of("operation.name", "none"));
	}

	@Test
	void operationCanBeSetExplicitly() {
		this.context.setOperation("https://spring.io/spring-ws", "getCountry");
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "https://spring.io/spring-ws"),
				KeyValue.of("operation.name", "getCountry"));
	}

	@Test
	void operationSetExplicitlyTakesPrecedenceOverTheLookupKey() {
		this.messageContext.setProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY,
				QName.valueOf("{https://spring.io/spring-ws}getCountryRequest"));
		this.context.setOperation(null, "getCountry");
		assertThat(lowCardinalityKeyValues()).contains(KeyValue.of("namespace", "none"),
				KeyValue.of("operation.name", "getCountry"));
	}

	@Test
	void operationResolvedFromTheLookupKeyIsCached() {
		MessageContext messageContext = mock(MessageContext.class);
		when(messageContext.getProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY))
			.thenReturn(QName.valueOf("{https://spring.io/spring-ws}getCountryRequest"));
		SoapServerObservationContext context = createContext(messageContext);
		assertThat(context.getNamespace()).isEqualTo("https://spring.io/spring-ws");
		assertThat(context.getOperationName()).isEqualTo("getCountryRequest");
		verify(messageContext).getProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY);
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
	void outcomeFaultTakesPrecedenceOverTheErrorThatProducedIt() {
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
		SoapServerObservationContext context = createContext(
				new DefaultMessageContext(soapMessage, this.messageFactory));
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

	private SoapServerObservationContext createContext(MessageContext messageContext) {
		SoapServerObservationContext context = new SoapServerObservationContext(this.connection);
		context.setAsCurrent(messageContext);
		return context;
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
