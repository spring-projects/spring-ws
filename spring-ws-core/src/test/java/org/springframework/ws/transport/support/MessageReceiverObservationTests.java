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

import java.util.Objects;

import javax.xml.namespace.QName;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.server.endpoint.mapping.AbstractMethodEndpointMapping;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.observation.SoapServerObservationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for observation support in {@link WebServiceMessageReceiverObjectSupport}
 * implementations.
 *
 * @author Brian Clozel
 */
public class MessageReceiverObservationTests {

	TestObservationRegistry observationRegistry = TestObservationRegistry.create();

	private ObservationMessageReceiver receiver;

	private FaultAwareWebServiceConnection connectionMock;

	private MockWebServiceMessageFactory messageFactory;

	private MockWebServiceMessage request;

	@BeforeEach
	void setUp() throws Exception {

		this.receiver = new ObservationMessageReceiver();
		this.messageFactory = new MockWebServiceMessageFactory();
		this.receiver.setMessageFactory(this.messageFactory);
		this.receiver.setObservationRegistry(this.observationRegistry);
		this.connectionMock = mock(FaultAwareWebServiceConnection.class);
		this.request = new MockWebServiceMessage();
		this.receiver.afterPropertiesSet();
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(this.request);
	}

	@Test
	void shouldRecordObservation() throws Exception {
		WebServiceMessageReceiver receiver = messageContext -> {
			// no-op
		};
		this.receiver.handleConnection(this.connectionMock, receiver);

		assertThat(this.observationRegistry).hasSingleObservationThat().hasNameEqualTo("soap.server.requests");
	}

	@Test
	void shouldHaveCurrentObservationInScope() throws Exception {
		WebServiceMessageReceiver receiver = messageContext -> {
			assertThat(this.observationRegistry.getCurrentObservation().getContextView().getName())
				.isEqualTo("soap.server.requests");
		};
		this.receiver.handleConnection(this.connectionMock, receiver);
	}

	@Test
	void shouldHaveObservationContextInMessageContext() throws Exception {
		WebServiceMessageReceiver receiver = messageContext -> {
			assertThat(SoapServerObservationContext.findCurrentObservationContext(messageContext)).isNotEmpty();
		};
		this.receiver.handleConnection(this.connectionMock, receiver);
	}

	@Test
	void shouldRecordNoEndpointFoundExceptions() throws Exception {
		WebServiceMessageReceiver receiver = messageContext -> {
			throw new NoEndpointFoundException(messageContext.getRequest());
		};
		this.receiver.handleConnection(this.connectionMock, receiver);
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.assertThatError()
			.isInstanceOf(NoEndpointFoundException.class);
	}

	@Test
	void shouldSetQNameInObservationContext() throws Exception {
		QName qName = QName.valueOf("{https://spring.io/guides/gs-producing-web-service}getCountryRequest");
		WebServiceMessageReceiver receiver = messageContext -> messageContext
			.setProperty(AbstractMethodEndpointMapping.LOOKUP_KEY_PROPERTY, qName);
		this.receiver.handleConnection(this.connectionMock, receiver);
		assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.server.requests")
			.that()
			.matches(context -> {
				if (context instanceof SoapServerObservationContext observationContext) {
					return Objects.equals(observationContext.getNamespace(),
							"https://spring.io/guides/gs-producing-web-service")
							&& Objects.equals(observationContext.getOperationName(), "getCountryRequest");
				}
				return false;
			});
	}

	static class ObservationMessageReceiver extends WebServiceMessageReceiverObjectSupport {

	}

}
