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

package org.springframework.ws.transport.support;

import javax.xml.namespace.QName;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.tck.TestObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.observation.DefaultSoapServerObservationConvention;
import org.springframework.ws.transport.observation.SoapServerObservationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the observation support in {@link WebServiceMessageReceiverObjectSupport}.
 *
 * @author Brian Clozel
 */
class WebServiceMessageReceiverObjectSupportObservationTests {

	private final TestObservationRegistry observationRegistry = TestObservationRegistry.create();

	private final MockWebServiceMessageFactory messageFactory = new MockWebServiceMessageFactory();

	private final MockWebServiceMessage request = new MockWebServiceMessage();

	private final FaultAwareWebServiceConnection connection = mock(FaultAwareWebServiceConnection.class);

	private final TestMessageReceiverObjectSupport receiverSupport = new TestMessageReceiverObjectSupport();

	@BeforeEach
	void setUp() throws Exception {
		this.receiverSupport.setMessageFactory(this.messageFactory);
		this.receiverSupport.setObservationRegistry(this.observationRegistry);
		this.receiverSupport.afterPropertiesSet();
		when(this.connection.receive(this.messageFactory)).thenReturn(this.request);
	}

	@Test
	void observationRegistryMustNotBeNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.receiverSupport.setObservationRegistry(null))
			.withMessageContaining("observationRegistry");
	}

	@Test
	void observationConventionMustNotBeNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.receiverSupport.setObservationConvention(null))
			.withMessageContaining("observationConvention");
	}

	@Test
	void recordObservation() throws Exception {
		handleConnection(messageContext -> {
		});
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasNameEqualTo("soap.server.requests")
			.hasBeenStopped()
			.doesNotHaveError()
			.hasLowCardinalityKeyValue("outcome", "UNKNOWN")
			.hasLowCardinalityKeyValue("exception", "none")
			.hasLowCardinalityKeyValue("namespace", "none")
			.hasLowCardinalityKeyValue("operation.name", "none");
	}

	@Test
	void recordObservationWithCustomConvention() throws Exception {
		this.receiverSupport.setObservationConvention(new DefaultSoapServerObservationConvention("custom.name") {

			@Override
			public KeyValues getLowCardinalityKeyValues(SoapServerObservationContext context) {
				return super.getLowCardinalityKeyValues(context).and("custom.key", "custom.value");
			}

		});
		handleConnection(messageContext -> {
		});
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasNameEqualTo("custom.name")
			.hasLowCardinalityKeyValue("custom.key", "custom.value");
	}

	@Test
	void observationIsInScopeWhileTheMessageIsHandled() throws Exception {
		handleConnection(messageContext -> assertThat(this.observationRegistry.getCurrentObservation()).isNotNull()
			.extracting(observation -> observation.getContextView().getName())
			.isEqualTo("soap.server.requests"));
	}

	@Test
	void observationContextIsAvailableFromTheMessageContext() throws Exception {
		handleConnection(
				messageContext -> assertThat(SoapServerObservationContext.findCurrentObservationContext(messageContext))
					.isNotEmpty());
	}

	@Test
	void recordNoEndpointFoundException() throws Exception {
		handleConnection(messageContext -> {
			throw new NoEndpointFoundException(messageContext.getRequest());
		});
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasBeenStopped()
			.hasLowCardinalityKeyValue("exception", "NoEndpointFoundException")
			.hasLowCardinalityKeyValue("outcome", "ERROR")
			.assertThatError()
			.isInstanceOf(NoEndpointFoundException.class);
	}

	@Test
	void recordExceptionThrownWhileHandlingTheMessage() throws Exception {
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> handleConnection(messageContext -> {
			throw new IllegalStateException("Endpoint failure");
		})).withMessage("Endpoint failure");
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasBeenStopped()
			.hasLowCardinalityKeyValue("exception", "IllegalStateException")
			.hasLowCardinalityKeyValue("outcome", "ERROR")
			.assertThatError()
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void resolveOperationFromQualifiedLookupKey() throws Exception {
		QName lookupKey = QName.valueOf("{https://spring.io/guides/gs-producing-web-service}getCountryRequest");
		handleConnection(
				messageContext -> messageContext.setProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY, lookupKey));
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasLowCardinalityKeyValue("namespace", "https://spring.io/guides/gs-producing-web-service")
			.hasLowCardinalityKeyValue("operation.name", "getCountryRequest")
			.hasContextualNameEqualTo("soap getCountryRequest");
	}

	@Test
	void resolveOperationFromStringLookupKey() throws Exception {
		handleConnection(messageContext -> messageContext.setProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY,
				"http://example.com/SoapAction"));
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasLowCardinalityKeyValue("namespace", "none")
			.hasLowCardinalityKeyValue("operation.name", "http://example.com/SoapAction");
	}

	@Test
	void doNotResolveOperationFromThePayloadOfAnUnmappedRequest() throws Exception {
		this.request.setPayload("<attackerControlled xmlns='https://example.com/attacker'/>");
		handleConnection(messageContext -> {
			throw new NoEndpointFoundException(messageContext.getRequest());
		});
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasLowCardinalityKeyValue("namespace", "none")
			.hasLowCardinalityKeyValue("operation.name", "none")
			.hasContextualNameEqualTo("soap");
	}

	private void handleConnection(WebServiceMessageReceiver receiver) throws Exception {
		this.receiverSupport.handleConnection(this.connection, receiver);
	}

	private static final class TestMessageReceiverObjectSupport extends WebServiceMessageReceiverObjectSupport {

	}

}
