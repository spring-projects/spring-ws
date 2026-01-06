package org.springframework.ws.transport.support;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.NoEndpointFoundException;
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

	static class ObservationMessageReceiver extends WebServiceMessageReceiverObjectSupport {

	}

}
