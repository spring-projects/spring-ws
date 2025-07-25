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
