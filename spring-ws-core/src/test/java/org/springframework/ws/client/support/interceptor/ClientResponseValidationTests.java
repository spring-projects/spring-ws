/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.support.interceptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

/**
 * Tests that a response rejected by {@link PayloadValidatingInterceptor} is not handed to
 * the caller of a {@link WebServiceTemplate}.
 *
 * @author Stephane Nicoll
 */
class ClientResponseValidationTests {

	private final List<String> calls = new ArrayList<>();

	@Test
	void invalidResponseIsNotExtracted() throws Exception {
		WebServiceTemplate template = createTemplate("invalidMessage.xml");
		assertThatExceptionOfType(WebServiceValidationException.class)
			.isThrownBy(() -> template.sendAndReceive((message) -> {
			}, new RecordingExtractor()));
		assertThat(this.calls).doesNotContain("extractData");
	}

	@Test
	void validResponseIsExtracted() throws Exception {
		WebServiceTemplate template = createTemplate("validMessage.xml");
		template.sendAndReceive((message) -> {
		}, new RecordingExtractor());
		assertThat(this.calls).contains("extractData");
	}

	private WebServiceTemplate createTemplate(String responsePayload) throws Exception {
		MockWebServiceMessageFactory messageFactory = new MockWebServiceMessageFactory();
		MockWebServiceMessage response = new MockWebServiceMessage();
		response.setPayload(new ClassPathResource(responsePayload, getClass()));

		URI uri = new URI("http://www.springframework.org/spring-ws");
		final FaultAwareWebServiceConnection connection = createNiceMock(FaultAwareWebServiceConnection.class);
		expect(connection.getUri()).andReturn(uri).anyTimes();
		expect(connection.receive(isA(WebServiceMessageFactory.class))).andReturn(response).anyTimes();
		replay(connection);

		WebServiceTemplate template = new WebServiceTemplate(messageFactory);
		template.setMessageSender(new WebServiceMessageSender() {

			@Override
			public WebServiceConnection createConnection(URI uri) {
				return connection;
			}

			@Override
			public boolean supports(URI uri, UriSource uriSource) {
				return true;
			}
		});
		template.setDefaultUri(uri.toString());

		// stands in for an interceptor configured before the validating one, such as a
		// security interceptor validating the response
		ClientInterceptor outer = new ClientInterceptorAdapter() {

			@Override
			public boolean handleResponse(MessageContext messageContext) {
				ClientResponseValidationTests.this.calls.add("outer.handleResponse");
				return true;
			}
		};

		PayloadValidatingInterceptor validating = new PayloadValidatingInterceptor();
		validating.setSchema(new ClassPathResource("schema.xsd", getClass()));
		validating.setValidateRequest(false);
		validating.setValidateResponse(true);
		validating.afterPropertiesSet();

		template.setInterceptors(new ClientInterceptor[] { outer, validating });
		return template;
	}

	private final class RecordingExtractor implements WebServiceMessageExtractor<Object> {

		@Override
		public Object extractData(WebServiceMessage message) {
			ClientResponseValidationTests.this.calls.add("extractData");
			return message;
		}

	}

}
