/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractMessageEndpointTests extends AbstractEndpointTests {

	private MessageEndpoint endpoint;

	@BeforeEach
	void createEndpoint() {
		this.endpoint = createResponseEndpoint();
	}

	@Test
	void testNoResponse() throws Exception {

		this.endpoint = createNoResponseEndpoint();
		StringSource requestSource = new StringSource(REQUEST);

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessage(requestSource),
				new MockWebServiceMessageFactory());
		this.endpoint.invoke(context);

		assertThat(context.hasResponse()).isFalse();
	}

	@Test
	void testNoRequestPayload() throws Exception {

		this.endpoint = createNoRequestPayloadEndpoint();

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessage((StringBuilder) null),
				new MockWebServiceMessageFactory());
		this.endpoint.invoke(context);

		assertThat(context.hasResponse()).isFalse();
	}

	@Override
	protected final void testSource(Source requestSource) throws Exception {

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessage(requestSource),
				new MockWebServiceMessageFactory());
		this.endpoint.invoke(context);

		assertThat(context.hasResponse()).isTrue();
		XmlAssert.assertThat(((MockWebServiceMessage) context.getResponse()).getPayloadAsString())
			.and(RESPONSE)
			.ignoreWhitespace()
			.areSimilar();
	}

	protected abstract MessageEndpoint createNoResponseEndpoint();

	protected abstract MessageEndpoint createNoRequestPayloadEndpoint();

	protected abstract MessageEndpoint createResponseEndpoint();

}
