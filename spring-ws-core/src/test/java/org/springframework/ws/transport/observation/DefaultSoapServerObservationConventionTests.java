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
		this.context.setNamespace("CountriesPort");
		assertThat(this.convention.getLowCardinalityKeyValues(this.context))
			.contains(KeyValue.of("namespace", "CountriesPort"));
	}

	@Test
	void uri() throws Exception {
		when(this.connectionMock.getUri()).thenReturn(URI.create("https://localhost:443/services"));
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
			.contains(KeyValue.of("uri", "https://localhost:443/services"));
	}

	@Test
	void faultReason() {
		this.response.setFaultReason("Invalid country format");
		assertThat(this.convention.getHighCardinalityKeyValues(this.context))
			.contains(KeyValue.of("fault.reason", "Invalid country format"));
	}

}
