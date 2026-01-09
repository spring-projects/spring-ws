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

package org.springframework.ws.client.core;

import java.io.IOException;
import java.net.URI;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.AssertProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class WebServiceTemplateTests {

	private static final URI CONNECTION_URI = URI.create("http://www.springframework.org/spring-ws");

	private WebServiceTemplate template;

	private FaultAwareWebServiceConnection connectionMock;

	private MockWebServiceMessageFactory messageFactory;

	@BeforeEach
	void setUp() throws Exception {

		this.messageFactory = new MockWebServiceMessageFactory();
		this.template = new WebServiceTemplate(this.messageFactory);
		this.connectionMock = mock(FaultAwareWebServiceConnection.class);
		when(this.connectionMock.getUri()).thenReturn(CONNECTION_URI);
		this.template.setMessageSender(new WebServiceMessageSender() {

			@Override
			public WebServiceConnection createConnection(URI uri) {
				return WebServiceTemplateTests.this.connectionMock;
			}

			@Override
			public boolean supports(URI uri) {

				assertThat(uri).isEqualTo(CONNECTION_URI);
				return true;
			}
		});

		this.template.setDefaultUri(CONNECTION_URI.toString());
	}

	@Test
	void testMarshalAndSendNoMarshallerSet() throws Exception {

		this.connectionMock.close();

		this.template.setMarshaller(null);

		assertThatIllegalStateException().isThrownBy(() -> this.template.marshalSendAndReceive(new Object()));
	}

	@Test
	void testMarshalAndSendNoUnmarshallerSet() throws Exception {

		this.connectionMock.close();

		this.template.setUnmarshaller(null);
		assertThatIllegalStateException().isThrownBy(() -> this.template.marshalSendAndReceive(new Object()));
	}

	@Test
	void testSendAndReceiveMessageResponse() throws Exception {

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(isA(WebServiceMessage.class));

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();
		Object extracted = new Object();
		when(extractorMock.extractData(isA(WebServiceMessage.class))).thenReturn(extracted);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(requestCallback, extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	void testSendAndReceiveMessageNoResponse() throws Exception {

		WebServiceMessageExtractor<?> extractorMock = mock(WebServiceMessageExtractor.class);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(null);
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(null, extractorMock);

		assertThat(result).isNull();
	}

	@Test
	void testSendAndReceiveMessageFault() throws Exception {

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();

		FaultMessageResolver faultMessageResolverMock = mock(FaultMessageResolver.class);
		this.template.setFaultMessageResolver(faultMessageResolverMock);
		faultMessageResolverMock.resolveFault(isA(WebServiceMessage.class));

		MockWebServiceMessage response = new MockWebServiceMessage("<response/>");
		response.setFault(true);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.hasFault()).thenReturn(true);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(response);
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(null, extractorMock);

		assertThat(result).isNull();
	}

	@Test
	void testSendAndReceiveConnectionError() throws Exception {

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();

		this.template.setFaultMessageResolver(null);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(true);
		when(this.connectionMock.hasFault()).thenReturn(false);
		String errorMessage = "errorMessage";
		when(this.connectionMock.getErrorMessage()).thenReturn(errorMessage);
		this.connectionMock.close();

		assertThatExceptionOfType(WebServiceTransportException.class)
			.isThrownBy(() -> this.template.sendAndReceive(null, extractorMock))
			.withMessage(errorMessage);
	}

	@Test
	void testSendAndReceiveSourceResponse() throws Exception {

		SourceExtractor<Object> extractorMock = mockSourceExtractor();
		Object extracted = new Object();
		when(extractorMock.extractData(isA(Source.class))).thenReturn(extracted);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		Object result = this.template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	void testSendAndReceiveSourceNoResponse() throws Exception {

		SourceExtractor<Object> extractorMock = mockSourceExtractor();

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(null);
		this.connectionMock.close();

		Object result = this.template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);

		assertThat(result).isNull();
	}

	@Test
	void testSendAndReceiveResultResponse() throws Exception {

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		StringResult result = new StringResult();
		boolean b = this.template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);

		assertThat(b).isTrue();
	}

	@Test
	void testSendAndReceiveResultNoResponse() throws Exception {

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(null);
		this.connectionMock.close();

		StringResult result = new StringResult();
		boolean b = this.template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);

		assertThat(b).isFalse();
	}

	@Test
	void testSendAndReceiveResultNoResponsePayload() throws Exception {

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		WebServiceMessage response = mock(WebServiceMessage.class);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(response);
		when(this.connectionMock.hasFault()).thenReturn(false);
		when(response.getPayloadSource()).thenReturn(null);
		this.connectionMock.close();

		StringResult result = new StringResult();
		boolean b = this.template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);

		assertThat(b).isTrue();
	}

	@Test
	void testSendAndReceiveMarshalResponse() throws Exception {
		Object unmarshalled = new Object();
		setupMarshallerAndUnmarshaller(unmarshalled);
		Object result = this.template.marshalSendAndReceive(new Object());
		assertThat(result).isEqualTo(unmarshalled);
	}

	@Test
	void testSendAndReceiveInvokesCallback() throws Exception {
		WebServiceMessageCallback callback = mock(WebServiceMessageCallback.class);
		setupMarshallerAndUnmarshaller(new Object());
		this.template.marshalSendAndReceive(new Object(), callback);
		verify(callback).doWithMessage(isA(MockWebServiceMessage.class));
	}

	@Test
	void testSendAndReceiveWithNoBodyInvokesCallback() throws Exception {
		WebServiceMessageCallback callback = mock(WebServiceMessageCallback.class);
		setupMarshallerAndUnmarshaller(new Object());
		Object requestPayload = null;
		this.template.marshalSendAndReceive(requestPayload, callback);
		verify(callback).doWithMessage(isA(MockWebServiceMessage.class));
	}

	private void setupMarshallerAndUnmarshaller(Object unmarshalled) throws Exception {
		Marshaller marshallerMock = mock(Marshaller.class);
		this.template.setMarshaller(marshallerMock);
		marshallerMock.marshal(isA(Object.class), isA(Result.class));

		Unmarshaller unmarshallerMock = mock(Unmarshaller.class);
		this.template.setUnmarshaller(unmarshallerMock);
		when(unmarshallerMock.unmarshal(isA(Source.class))).thenReturn(unmarshalled);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();
	}

	@Test
	void testSendAndReceiveMarshalNoResponse() throws Exception {

		Marshaller marshallerMock = mock(Marshaller.class);
		this.template.setMarshaller(marshallerMock);
		marshallerMock.marshal(isA(Object.class), isA(Result.class));

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(null);
		this.connectionMock.close();

		Object result = this.template.marshalSendAndReceive(new Object());

		assertThat(result).isNull();
	}

	@Test
	void testSendAndReceiveCustomUri() throws Exception {

		final URI customUri = new URI("http://www.springframework.org/spring-ws/custom");
		this.template.setMessageSender(new WebServiceMessageSender() {

			@Override
			public WebServiceConnection createConnection(URI uri) {
				return WebServiceTemplateTests.this.connectionMock;
			}

			@Override
			public boolean supports(URI uri) {

				assertThat(uri).isEqualTo(customUri);
				return true;
			}
		});
		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(isA(WebServiceMessage.class));

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();
		Object extracted = new Object();
		when(extractorMock.extractData(isA(WebServiceMessage.class))).thenReturn(extracted);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(customUri.toString(), requestCallback, extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	void testInterceptors() throws Exception {

		ClientInterceptor interceptorMock1 = mock(ClientInterceptor.class);
		ClientInterceptor interceptorMock2 = mock(ClientInterceptor.class);
		this.template.setInterceptors(new ClientInterceptor[] { interceptorMock1, interceptorMock2 });
		when(interceptorMock1.handleRequest(isA(MessageContext.class))).thenReturn(true);
		when(interceptorMock2.handleRequest(isA(MessageContext.class))).thenReturn(true);
		when(interceptorMock2.handleResponse(isA(MessageContext.class))).thenReturn(true);
		when(interceptorMock1.handleResponse(isA(MessageContext.class))).thenReturn(true);
		interceptorMock2.afterCompletion(isA(MessageContext.class), isNull());
		interceptorMock1.afterCompletion(isA(MessageContext.class), isNull());

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(isA(WebServiceMessage.class));

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();
		Object extracted = new Object();
		when(extractorMock.extractData(isA(WebServiceMessage.class))).thenReturn(extracted);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(requestCallback, extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	void testInterceptorsInterceptedNoResponse() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(this.messageFactory);

		ClientInterceptor interceptorMock1 = mock(ClientInterceptor.class);
		ClientInterceptor interceptorMock2 = mock(ClientInterceptor.class);
		this.template.setInterceptors(new ClientInterceptor[] { interceptorMock1, interceptorMock2 });
		when(interceptorMock1.handleRequest(isA(MessageContext.class))).thenReturn(false);
		interceptorMock1.afterCompletion(isA(MessageContext.class), isNull());

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(messageContext.getRequest());

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();

		Object result = this.template.doSendAndReceive(messageContext, this.connectionMock, requestCallback,
				extractorMock);

		assertThat(result).isNull();
	}

	@Test
	void testInterceptorsInterceptedCreateResponse() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(this.messageFactory);
		// force creation of response
		messageContext.getResponse();

		ClientInterceptor interceptorMock1 = mock(ClientInterceptor.class);
		ClientInterceptor interceptorMock2 = mock(ClientInterceptor.class);
		this.template.setInterceptors(new ClientInterceptor[] { interceptorMock1, interceptorMock2 });
		when(interceptorMock1.handleRequest(isA(MessageContext.class))).thenReturn(false);
		when(interceptorMock1.handleResponse(isA(MessageContext.class))).thenReturn(true);
		interceptorMock1.afterCompletion(isA(MessageContext.class), isNull());

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(messageContext.getRequest());

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();
		Object extracted = new Object();
		when(extractorMock.extractData(messageContext.getResponse())).thenReturn(extracted);

		when(this.connectionMock.hasFault()).thenReturn(false);

		Object result = this.template.doSendAndReceive(messageContext, this.connectionMock, requestCallback,
				extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	void afterCompletionInvokedOnlyOnceWithSuccess() throws Exception {
		NoOpClientInterceptor clientInterceptor1 = new NoOpClientInterceptor();
		NoOpClientInterceptor clientInterceptor2 = new NoOpClientInterceptor();
		this.template.setInterceptors(new ClientInterceptor[] { clientInterceptor1, clientInterceptor2 });

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(any(WebServiceMessage.class));
		Object extracted = new Object();
		WebServiceMessageExtractor<Object> extract = createSimpleExtractor(extracted);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(requestCallback, extract);

		assertThat(result).isEqualTo(extracted);
		assertThat(clientInterceptor1).hasHandledExchange();
		assertThat(clientInterceptor2).hasHandledExchange();
	}

	@Test
	void afterCompletionInvokedOnlyOnceWitFailureInAfterCompletion() throws Exception {
		IllegalStateException testException = new IllegalStateException("test");
		NoOpClientInterceptor clientInterceptor1 = new NoOpClientInterceptor() {
			@Override
			public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
				super.afterCompletion(messageContext, ex);
				throw testException;
			}
		};
		NoOpClientInterceptor clientInterceptor2 = new NoOpClientInterceptor();
		this.template.setInterceptors(new ClientInterceptor[] { clientInterceptor1, clientInterceptor2 });

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(any(WebServiceMessage.class));
		Object extracted = new Object();
		WebServiceMessageExtractor<Object> extract = createSimpleExtractor(extracted);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(requestCallback, extract);

		assertThat(result).isEqualTo(extracted);
		assertThat(clientInterceptor1).hasHandledExchange().hasNoCompletionException();
		assertThat(clientInterceptor2).hasHandledExchange().hasNoCompletionException();
	}

	@Test
	void afterCompletionInvokedOnlyOnceWithError() throws Exception {
		NoOpClientInterceptor clientInterceptor1 = new NoOpClientInterceptor();
		NoOpClientInterceptor clientInterceptor2 = new NoOpClientInterceptor();
		this.template.setInterceptors(new ClientInterceptor[] { clientInterceptor1, clientInterceptor2 });

		Object extracted = new Object();
		WebServiceMessageExtractor<Object> extract = createSimpleExtractor(extracted);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(true);
		when(this.connectionMock.hasFault()).thenReturn(false);
		String errorMessage = "errorMessage";
		when(this.connectionMock.getErrorMessage()).thenReturn(errorMessage);
		this.connectionMock.close();

		assertThatExceptionOfType(WebServiceTransportException.class)
			.isThrownBy(() -> this.template.sendAndReceive(null, extract))
			.satisfies(exception -> {
				assertThat(clientInterceptor1).hasHandledError().completionException().isSameAs(exception);
				assertThat(clientInterceptor2).hasHandledError().completionException().isSameAs(exception);
			});
	}

	@Test
	void afterCompletionInvokedOnlyOnceWithFault() throws Exception {
		NoOpClientInterceptor clientInterceptor1 = new NoOpClientInterceptor();
		NoOpClientInterceptor clientInterceptor2 = new NoOpClientInterceptor();
		this.template.setInterceptors(new ClientInterceptor[] { clientInterceptor1, clientInterceptor2 });
		this.template.setFaultMessageResolver(null);

		WebServiceMessageExtractor<Object> extractorMock = createSimpleExtractor(new Object());
		MockWebServiceMessage response = new MockWebServiceMessage("<response/>");
		response.setFault(true);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.hasFault()).thenReturn(true);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(response);
		this.connectionMock.close();

		assertThatExceptionOfType(WebServiceTransportException.class)
			.isThrownBy(() -> this.template.sendAndReceive(null, extractorMock))
			.satisfies(exception -> {
				assertThat(clientInterceptor1).hasHandledFault().completionException().isSameAs(exception);
				assertThat(clientInterceptor2).hasHandledFault().completionException().isSameAs(exception);
			});
	}

	@Test
	void afterCompletionInvokedOnlyOnceWithFaultAndFaultMessageResolver() throws Exception {
		NoOpClientInterceptor clientInterceptor1 = new NoOpClientInterceptor();
		NoOpClientInterceptor clientInterceptor2 = new NoOpClientInterceptor();
		this.template.setInterceptors(new ClientInterceptor[] { clientInterceptor1, clientInterceptor2 });

		WebServiceMessageExtractor<Object> extractorMock = createSimpleExtractor(new Object());
		FaultMessageResolver faultMessageResolverMock = mock(FaultMessageResolver.class);
		faultMessageResolverMock.resolveFault(isA(WebServiceMessage.class));
		this.template.setFaultMessageResolver(faultMessageResolverMock);

		MockWebServiceMessage response = new MockWebServiceMessage("<response/>");
		response.setFault(true);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.hasFault()).thenReturn(true);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(response);
		this.connectionMock.close();

		this.template.sendAndReceive(null, extractorMock);
		assertThat(clientInterceptor1).hasHandledFault().hasNoCompletionException();
		assertThat(clientInterceptor2).hasHandledFault().hasNoCompletionException();
	}

	private <T> WebServiceMessageExtractor<T> createSimpleExtractor(T target) throws IOException, TransformerException {
		WebServiceMessageExtractor<T> extractor = mock(WebServiceMessageExtractor.class);
		given(extractor.extractData(any(WebServiceMessage.class))).willReturn(target);
		return extractor;
	}

	@Test
	void testDestinationResolver() throws Exception {

		DestinationProvider providerMock = mock(DestinationProvider.class);
		this.template.setDestinationProvider(providerMock);
		final URI providerUri = new URI("http://www.springframework.org/spring-ws/destinationProvider");
		when(providerMock.getDestination()).thenReturn(providerUri);

		this.template.setMessageSender(new WebServiceMessageSender() {

			@Override
			public WebServiceConnection createConnection(URI uri) {
				return WebServiceTemplateTests.this.connectionMock;
			}

			@Override
			public boolean supports(URI uri) {

				assertThat(uri).isEqualTo(providerUri);
				return true;
			}
		});

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();

		reset(this.connectionMock);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(null);
		when(this.connectionMock.getUri()).thenReturn(new URI("http://example.com"));
		this.connectionMock.close();

		Object result = this.template.sendAndReceive(null, extractorMock);

		assertThat(result).isNull();
	}

	@Test
	void testObservationWithSendAndReceiveMarshallResponse() throws Exception {
		TestObservationRegistry observationRegistry = TestObservationRegistry.create();
		this.template.setObservationRegistry(observationRegistry);
		Object unmarshalled = new Object();
		setupMarshallerAndUnmarshaller(unmarshalled);
		this.template.marshalSendAndReceive(new Object());
		assertThat(observationRegistry).hasObservationWithNameEqualTo("soap.client.requests")
			.that()
			.hasBeenStopped()
			.doesNotHaveError()
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("protocol", "http")
			.hasLowCardinalityKeyValue("namespace", "none")
			.hasLowCardinalityKeyValue("operation.name", "none")
			.hasHighCardinalityKeyValue("fault.reason", "none")
			.hasHighCardinalityKeyValue("uri", CONNECTION_URI.toString());
	}

	@Test
	void testObservationWithSendAndReceiveResultResponse() throws IOException {
		TestObservationRegistry observationRegistry = TestObservationRegistry.create();
		this.template.setObservationRegistry(observationRegistry);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(this.connectionMock.hasFault()).thenReturn(false);
		this.connectionMock.close();

		StringResult result = new StringResult();
		StreamSource source = new StreamSource(new ClassPathResource("country-body.xml", getClass()).getInputStream());
		assertThat(this.template.sendSourceAndReceiveToResult(source, result)).isTrue();

		assertThat(observationRegistry).hasObservationWithNameEqualTo("soap.client.requests")
			.that()
			.hasBeenStopped()
			.doesNotHaveError()
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("protocol", "http")
			.hasLowCardinalityKeyValue("namespace", "http://www.springframework.org/spring-ws")
			.hasLowCardinalityKeyValue("operation.name", "getCountryRequest")
			.hasHighCardinalityKeyValue("fault.reason", "none")
			.hasHighCardinalityKeyValue("uri", CONNECTION_URI.toString());
	}

	@Test
	void testObservationWithFault() throws IOException {
		TestObservationRegistry observationRegistry = TestObservationRegistry.create();
		this.template.setObservationRegistry(observationRegistry);

		FaultMessageResolver faultMessageResolverMock = mock(FaultMessageResolver.class);
		this.template.setFaultMessageResolver(faultMessageResolverMock);
		doThrow(new IllegalStateException("test")).when(faultMessageResolverMock)
			.resolveFault(isA(WebServiceMessage.class));

		WebServiceMessageExtractor<Object> extractorMock = mockWebServiceMessageExtractor();
		MockWebServiceMessage response = new MockWebServiceMessage("<response/>");
		response.setFaultCode(FaultCode.RECEIVER.value());
		response.setFaultReason("Test");
		response.setFault(true);

		this.connectionMock.send(isA(WebServiceMessage.class));
		when(this.connectionMock.hasError()).thenReturn(false);
		when(this.connectionMock.hasFault()).thenReturn(true);
		when(this.connectionMock.receive(this.messageFactory)).thenReturn(response);
		this.connectionMock.close();

		assertThatIllegalStateException().isThrownBy(() -> this.template.sendAndReceive(null, extractorMock))
			.withMessage("test");

		assertThat(observationRegistry).hasObservationWithNameEqualTo("soap.client.requests")
			.that()
			.hasBeenStopped()
			.hasError()
			.hasLowCardinalityKeyValue("fault.code", "RECEIVER")
			.hasHighCardinalityKeyValue("fault.reason", "Test");
	}

	private static WebServiceMessageExtractor<Object> mockWebServiceMessageExtractor() {
		return mock(WebServiceMessageExtractor.class);
	}

	private static SourceExtractor<Object> mockSourceExtractor() {
		return mock(SourceExtractor.class);
	}

	private static class NoOpClientInterceptor
			implements ClientInterceptor, AssertProvider<NoOpClientInterceptorAssert> {

		private boolean handledRequest;

		private boolean handledResponse;

		private boolean handledFault;

		private boolean afterCompletion;

		private Exception afterCompletionException;

		@Override
		public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
			if (this.handledRequest) {
				throw new IllegalStateException("handleRequest has already been called");
			}
			this.handledRequest = true;
			return true;
		}

		@Override
		public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
			if (this.handledResponse) {
				throw new IllegalStateException("handleResponse has already been called");
			}
			this.handledResponse = true;
			return true;
		}

		@Override
		public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
			if (this.handledFault) {
				throw new IllegalStateException("handleFault has already been called");
			}
			this.handledFault = true;
			return true;
		}

		@Override
		public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
			if (this.afterCompletion) {
				throw new IllegalStateException("afterCompletion has already been called");
			}
			this.afterCompletion = true;
			this.afterCompletionException = ex;
		}

		@Override
		public NoOpClientInterceptorAssert assertThat() {
			return new NoOpClientInterceptorAssert(this);
		}

	}

	private static class NoOpClientInterceptorAssert
			extends AbstractObjectAssert<NoOpClientInterceptorAssert, NoOpClientInterceptor> {

		NoOpClientInterceptorAssert(NoOpClientInterceptor actual) {
			super(actual, NoOpClientInterceptorAssert.class);
		}

		NoOpClientInterceptorAssert hasHandledExchange() {
			assertThat(this.actual.handledRequest).isTrue();
			assertThat(this.actual.handledResponse).isTrue();
			assertThat(this.actual.handledFault).isFalse();
			assertThat(this.actual.afterCompletion).isTrue();
			return this.myself;
		}

		NoOpClientInterceptorAssert hasHandledError() {
			assertThat(this.actual.handledRequest).isTrue();
			assertThat(this.actual.handledResponse).isFalse();
			assertThat(this.actual.handledFault).isFalse();
			assertThat(this.actual.afterCompletion).isTrue();
			return this.myself;
		}

		NoOpClientInterceptorAssert hasHandledFault() {
			assertThat(this.actual.handledRequest).isTrue();
			assertThat(this.actual.handledResponse).isFalse();
			assertThat(this.actual.handledFault).isTrue();
			assertThat(this.actual.afterCompletion).isTrue();
			return this.myself;
		}

		NoOpClientInterceptorAssert hasNoCompletionException() {
			assertThat(this.actual.afterCompletionException).isNull();
			return this.myself;
		}

		AbstractThrowableAssert<?, ? extends Exception> completionException() {
			return Assertions.assertThat(this.actual.afterCompletionException);
		}

	}

}
