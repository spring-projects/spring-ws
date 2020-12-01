/*
 * Copyright 2005-2010 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.StringSource;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractMessageEndpointTestCase extends AbstractEndpointTestCase {

	private MessageEndpoint endpoint;

	@BeforeEach
	public void createEndpoint() throws Exception {
		endpoint = createResponseEndpoint();
	}

	@Test
	public void testNoResponse() throws Exception {

		endpoint = createNoResponseEndpoint();
		StringSource requestSource = new StringSource(REQUEST);

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessage(requestSource),
				new MockWebServiceMessageFactory());
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isFalse();
	}

	@Test
	public void testNoRequestPayload() throws Exception {

		endpoint = createNoRequestPayloadEndpoint();

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessage((StringBuilder) null),
				new MockWebServiceMessageFactory());
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isFalse();
	}

	@Override
	protected final void testSource(Source requestSource) throws Exception {

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessage(requestSource),
				new MockWebServiceMessageFactory());
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isTrue();
		XmlAssert.assertThat(((MockWebServiceMessage) context.getResponse()).getPayloadAsString()).and(RESPONSE)
				.ignoreWhitespace().areSimilar();
	}

	protected abstract MessageEndpoint createNoResponseEndpoint();

	protected abstract MessageEndpoint createNoRequestPayloadEndpoint();

	protected abstract MessageEndpoint createResponseEndpoint();

}
