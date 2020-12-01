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

package org.springframework.ws.server.endpoint.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.PayloadEndpoint;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.xmlunit.assertj.XmlAssert;

public class PayloadEndpointAdapterTest {

	private PayloadEndpointAdapter adapter;

	private PayloadEndpoint endpointMock;

	@BeforeEach
	public void setUp() throws Exception {

		adapter = new PayloadEndpointAdapter();
		endpointMock = createMock(PayloadEndpoint.class);
	}

	@Test
	public void testSupports() {
		assertThat(adapter.supports(endpointMock)).isTrue();
	}

	@Test
	public void testInvoke() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		final Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();

		PayloadEndpoint endpoint = request1 -> {

			StringWriter writer = new StringWriter();
			transformer.transform(request1, new StreamResult(writer));
			XmlAssert.assertThat(writer.toString()).and("<request/>").ignoreWhitespace().areIdentical();
			return new StreamSource(new StringReader("<response/>"));
		};

		endpoint.invoke(request.getPayloadSource());

		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		adapter.invoke(messageContext, endpoint);

		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();

		assertThat(response).isNotNull();
		XmlAssert.assertThat(response.getPayloadAsString()).and("<response/>").ignoreWhitespace().areIdentical();
	}

	@Test
	public void testInvokeNoResponse() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		expect(endpointMock.invoke(isA(Source.class))).andReturn(null);

		replay(endpointMock);

		adapter.invoke(messageContext, endpointMock);

		verify(endpointMock);

		assertThat(messageContext.hasResponse()).isFalse();
	}

}
