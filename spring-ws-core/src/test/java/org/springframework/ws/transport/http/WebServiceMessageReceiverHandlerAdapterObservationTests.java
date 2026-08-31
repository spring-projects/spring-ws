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

package org.springframework.ws.transport.http;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.transport.support.EchoPayloadEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the observation support of the HTTP transport, through
 * {@link WebServiceMessageReceiverHandlerAdapter}.
 *
 * @author Brian Clozel
 */
class WebServiceMessageReceiverHandlerAdapterObservationTests {

	private static final String PAYLOAD = """
			<root xmlns="%s"><child/></root>""".formatted(EchoPayloadEndpoint.NAMESPACE);

	private static final String ENVELOPE = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
			    <SOAP-ENV:Body>%s</SOAP-ENV:Body>
			</SOAP-ENV:Envelope>""".formatted(PAYLOAD);

	private final TestObservationRegistry observationRegistry = TestObservationRegistry.create();

	private final WebServiceMessageReceiverHandlerAdapter adapter = new WebServiceMessageReceiverHandlerAdapter();

	private final MessageDispatcher messageDispatcher = new SoapMessageDispatcher();

	@BeforeEach
	void setUp() throws Exception {
		SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
		messageFactory.afterPropertiesSet();
		this.adapter.setMessageFactory(messageFactory);
		this.adapter.setObservationRegistry(this.observationRegistry);
		this.adapter.afterPropertiesSet();
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("endpoint", EchoPayloadEndpoint.class);
		applicationContext.registerSingleton("endpointMapping", PayloadRootAnnotationMethodEndpointMapping.class);
		applicationContext.registerSingleton("endpointAdapter", DefaultMethodEndpointAdapter.class);
		applicationContext.refresh();
		this.messageDispatcher.setApplicationContext(applicationContext);
	}

	@Test
	void recordObservationForMappedRequest() throws Exception {
		handle(ENVELOPE);
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasNameEqualTo("soap.server.requests")
			.hasBeenStopped()
			.doesNotHaveError()
			.hasContextualNameEqualTo("soap " + EchoPayloadEndpoint.LOCAL_PART)
			.hasLowCardinalityKeyValue("exception", "none")
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("namespace", EchoPayloadEndpoint.NAMESPACE)
			.hasLowCardinalityKeyValue("operation.name", EchoPayloadEndpoint.LOCAL_PART)
			.hasLowCardinalityKeyValue("outcome", "SUCCESS")
			.hasLowCardinalityKeyValue("protocol", "http")
			.hasHighCardinalityKeyValue("fault.reason", "none")
			.hasHighCardinalityKeyValue("uri", "http://localhost:80/service");
	}

	@Test
	void recordObservationForUnmappedRequest() throws Exception {
		handle(ENVELOPE.replace(EchoPayloadEndpoint.NAMESPACE, "https://example.com/attacker"));
		assertThat(this.observationRegistry).hasSingleObservationThat()
			.hasNameEqualTo("soap.server.requests")
			.hasBeenStopped()
			.hasContextualNameEqualTo("soap")
			.hasLowCardinalityKeyValue("exception", "NoEndpointFoundException")
			.hasLowCardinalityKeyValue("namespace", "none")
			.hasLowCardinalityKeyValue("operation.name", "none")
			.hasLowCardinalityKeyValue("outcome", "ERROR");
	}

	private void handle(String envelope) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/service");
		request.setContentType("text/xml");
		request.setContent(envelope.getBytes());
		this.adapter.handle(request, new MockHttpServletResponse(), this.messageDispatcher);
	}

}
