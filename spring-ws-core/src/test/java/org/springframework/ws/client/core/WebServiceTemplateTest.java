/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.client.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

@SuppressWarnings("unchecked")
public class WebServiceTemplateTest {

	private WebServiceTemplate template;

	private FaultAwareWebServiceConnection connectionMock;

	private MockWebServiceMessageFactory messageFactory;

	@BeforeEach
	public void setUp() throws Exception {

		messageFactory = new MockWebServiceMessageFactory();
		template = new WebServiceTemplate(messageFactory);
		connectionMock = mock(FaultAwareWebServiceConnection.class);
		final URI expectedUri = new URI("http://www.springframework.org/spring-ws");
		when(connectionMock.getUri()).thenReturn(expectedUri);
		template.setMessageSender(new WebServiceMessageSender() {

			@Override
			public WebServiceConnection createConnection(URI uri) throws IOException {
				return connectionMock;
			}

			@Override
			public boolean supports(URI uri) {

				assertThat(uri).isEqualTo(expectedUri);
				return true;
			}
		});

		template.setDefaultUri(expectedUri.toString());
	}

	@Test
	public void testMarshalAndSendNoMarshallerSet() throws Exception {

		connectionMock.close();

		template.setMarshaller(null);

		assertThatIllegalStateException().isThrownBy(() -> template.marshalSendAndReceive(new Object()));
	}

	@Test
	public void testMarshalAndSendNoUnmarshallerSet() throws Exception {

		connectionMock.close();

		template.setUnmarshaller(null);
		assertThatIllegalStateException().isThrownBy(() -> template.marshalSendAndReceive(new Object()));
	}

	@Test
	public void testSendAndReceiveMessageResponse() throws Exception {

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(isA(WebServiceMessage.class));

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);
		Object extracted = new Object();
		when(extractorMock.extractData(isA(WebServiceMessage.class))).thenReturn(extracted);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(connectionMock.hasFault()).thenReturn(false);
		connectionMock.close();

		Object result = template.sendAndReceive(requestCallback, extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	public void testSendAndReceiveMessageNoResponse() throws Exception {

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(null);
		connectionMock.close();

		Object result = template.sendAndReceive(null, extractorMock);

		assertThat(result).isNull();
	}

	@Test
	public void testSendAndReceiveMessageFault() throws Exception {

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);

		FaultMessageResolver faultMessageResolverMock = mock(FaultMessageResolver.class);
		template.setFaultMessageResolver(faultMessageResolverMock);
		faultMessageResolverMock.resolveFault(isA(WebServiceMessage.class));

		MockWebServiceMessage response = new MockWebServiceMessage("<response/>");
		response.setFault(true);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.hasFault()).thenReturn(true);
		when(connectionMock.receive(messageFactory)).thenReturn(response);
		connectionMock.close();

		Object result = template.sendAndReceive(null, extractorMock);

		assertThat(result).isNull();
	}

	@Test
	public void testSendAndReceiveConnectionError() throws Exception {

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);

		template.setFaultMessageResolver(null);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(true);
		when(connectionMock.hasFault()).thenReturn(false);
		String errorMessage = "errorMessage";
		when(connectionMock.getErrorMessage()).thenReturn(errorMessage);
		connectionMock.close();

		assertThatExceptionOfType(WebServiceTransportException.class)
				.isThrownBy(() -> template.sendAndReceive(null, extractorMock)).withMessage(errorMessage);
	}

	@Test
	public void testSendAndReceiveSourceResponse() throws Exception {

		SourceExtractor extractorMock = mock(SourceExtractor.class);
		Object extracted = new Object();
		when(extractorMock.extractData(isA(Source.class))).thenReturn(extracted);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(connectionMock.hasFault()).thenReturn(false);
		connectionMock.close();

		Object result = template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	public void testSendAndReceiveSourceNoResponse() throws Exception {

		SourceExtractor extractorMock = mock(SourceExtractor.class);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(null);
		connectionMock.close();

		Object result = template.sendSourceAndReceive(new StringSource("<request />"), extractorMock);

		assertThat(result).isNull();
	}

	@Test
	public void testSendAndReceiveResultResponse() throws Exception {

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(connectionMock.hasFault()).thenReturn(false);
		connectionMock.close();

		StringResult result = new StringResult();
		boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);

		assertThat(b).isTrue();
	}

	@Test
	public void testSendAndReceiveResultNoResponse() throws Exception {

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(null);
		connectionMock.close();

		StringResult result = new StringResult();
		boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);

		assertThat(b).isFalse();
	}

	@Test
	public void testSendAndReceiveResultNoResponsePayload() throws Exception {

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		WebServiceMessage response = mock(WebServiceMessage.class);
		when(connectionMock.receive(messageFactory)).thenReturn(response);
		when(connectionMock.hasFault()).thenReturn(false);
		when(response.getPayloadSource()).thenReturn(null);
		connectionMock.close();

		StringResult result = new StringResult();
		boolean b = template.sendSourceAndReceiveToResult(new StringSource("<request />"), result);

		assertThat(b).isTrue();
	}

	@Test
	public void testSendAndReceiveMarshalResponse() throws Exception {

		Marshaller marshallerMock = mock(Marshaller.class);
		template.setMarshaller(marshallerMock);
		marshallerMock.marshal(isA(Object.class), isA(Result.class));

		Unmarshaller unmarshallerMock = mock(Unmarshaller.class);
		template.setUnmarshaller(unmarshallerMock);
		Object unmarshalled = new Object();
		when(unmarshallerMock.unmarshal(isA(Source.class))).thenReturn(unmarshalled);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(connectionMock.hasFault()).thenReturn(false);
		connectionMock.close();

		Object result = template.marshalSendAndReceive(new Object());

		assertThat(result).isEqualTo(unmarshalled);
	}

	@Test
	public void testSendAndReceiveMarshalNoResponse() throws Exception {

		Marshaller marshallerMock = mock(Marshaller.class);
		template.setMarshaller(marshallerMock);
		marshallerMock.marshal(isA(Object.class), isA(Result.class));

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(null);
		connectionMock.close();

		Object result = template.marshalSendAndReceive(new Object());

		assertThat(result).isNull();
	}

	@Test
	public void testSendAndReceiveCustomUri() throws Exception {

		final URI customUri = new URI("http://www.springframework.org/spring-ws/custom");
		template.setMessageSender(new WebServiceMessageSender() {

			@Override
			public WebServiceConnection createConnection(URI uri) throws IOException {
				return connectionMock;
			}

			@Override
			public boolean supports(URI uri) {

				assertThat(uri).isEqualTo(customUri);
				return true;
			}
		});
		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(isA(WebServiceMessage.class));

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);
		Object extracted = new Object();
		when(extractorMock.extractData(isA(WebServiceMessage.class))).thenReturn(extracted);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(connectionMock.hasFault()).thenReturn(false);
		connectionMock.close();

		Object result = template.sendAndReceive(customUri.toString(), requestCallback, extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	public void testInterceptors() throws Exception {

		ClientInterceptor interceptorMock1 = mock(ClientInterceptor.class);
		ClientInterceptor interceptorMock2 = mock(ClientInterceptor.class);
		template.setInterceptors(new ClientInterceptor[] { interceptorMock1, interceptorMock2 });
		when(interceptorMock1.handleRequest(isA(MessageContext.class))).thenReturn(true);
		when(interceptorMock2.handleRequest(isA(MessageContext.class))).thenReturn(true);
		when(interceptorMock2.handleResponse(isA(MessageContext.class))).thenReturn(true);
		when(interceptorMock1.handleResponse(isA(MessageContext.class))).thenReturn(true);
		interceptorMock2.afterCompletion(isA(MessageContext.class), (Exception) isNull());
		interceptorMock1.afterCompletion(isA(MessageContext.class), (Exception) isNull());

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(isA(WebServiceMessage.class));

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);
		Object extracted = new Object();
		when(extractorMock.extractData(isA(WebServiceMessage.class))).thenReturn(extracted);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(new MockWebServiceMessage("<response/>"));
		when(connectionMock.hasFault()).thenReturn(false);
		connectionMock.close();

		Object result = template.sendAndReceive(requestCallback, extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	public void testInterceptorsInterceptedNoResponse() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(messageFactory);

		ClientInterceptor interceptorMock1 = mock(ClientInterceptor.class);
		ClientInterceptor interceptorMock2 = mock(ClientInterceptor.class);
		template.setInterceptors(new ClientInterceptor[] { interceptorMock1, interceptorMock2 });
		when(interceptorMock1.handleRequest(isA(MessageContext.class))).thenReturn(false);
		interceptorMock1.afterCompletion(isA(MessageContext.class), (Exception) isNull());

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(messageContext.getRequest());

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);

		Object result = template.doSendAndReceive(messageContext, connectionMock, requestCallback, extractorMock);

		assertThat(result).isNull();
	}

	@Test
	public void testInterceptorsInterceptedCreateResponse() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(messageFactory);
		// force creation of response
		messageContext.getResponse();

		ClientInterceptor interceptorMock1 = mock(ClientInterceptor.class);
		ClientInterceptor interceptorMock2 = mock(ClientInterceptor.class);
		template.setInterceptors(new ClientInterceptor[] { interceptorMock1, interceptorMock2 });
		when(interceptorMock1.handleRequest(isA(MessageContext.class))).thenReturn(false);
		when(interceptorMock1.handleResponse(isA(MessageContext.class))).thenReturn(true);
		interceptorMock1.afterCompletion(isA(MessageContext.class), (Exception) isNull());

		WebServiceMessageCallback requestCallback = mock(WebServiceMessageCallback.class);
		requestCallback.doWithMessage(messageContext.getRequest());

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);
		Object extracted = new Object();
		when(extractorMock.extractData(messageContext.getResponse())).thenReturn(extracted);

		when(connectionMock.hasFault()).thenReturn(false);

		Object result = template.doSendAndReceive(messageContext, connectionMock, requestCallback, extractorMock);

		assertThat(result).isEqualTo(extracted);
	}

	@Test
	public void testDestinationResolver() throws Exception {

		DestinationProvider providerMock = mock(DestinationProvider.class);
		template.setDestinationProvider(providerMock);
		final URI providerUri = new URI("http://www.springframework.org/spring-ws/destinationProvider");
		when(providerMock.getDestination()).thenReturn(providerUri);

		template.setMessageSender(new WebServiceMessageSender() {

			@Override
			public WebServiceConnection createConnection(URI uri) {
				return connectionMock;
			}

			@Override
			public boolean supports(URI uri) {

				assertThat(uri).isEqualTo(providerUri);
				return true;
			}
		});

		WebServiceMessageExtractor extractorMock = mock(WebServiceMessageExtractor.class);

		reset(connectionMock);

		connectionMock.send(isA(WebServiceMessage.class));
		when(connectionMock.hasError()).thenReturn(false);
		when(connectionMock.receive(messageFactory)).thenReturn(null);
		when(connectionMock.getUri()).thenReturn(new URI("http://example.com"));
		connectionMock.close();

		Object result = template.sendAndReceive(null, extractorMock);

		assertThat(result).isNull();
	}
}
