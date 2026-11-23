/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.PayloadEndpoint;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class PayloadEndpointAdapterTests {

	private PayloadEndpointAdapter adapter;

	private PayloadEndpoint endpointMock;

	@BeforeEach
	void setUp() {

		this.adapter = new PayloadEndpointAdapter();
		this.endpointMock = createMock(PayloadEndpoint.class);
	}

	@Test
	void testSupports() {
		assertThat(this.adapter.supports(this.endpointMock)).isTrue();
	}

	@Test
	void testInvoke() throws Exception {

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
		this.adapter.invoke(messageContext, endpoint);

		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();

		assertThat(response).isNotNull();
		XmlAssert.assertThat(response.getPayloadAsString()).and("<response/>").ignoreWhitespace().areIdentical();
	}

	@Test
	void testInvokeNoResponse() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		expect(this.endpointMock.invoke(isA(Source.class))).andReturn(null);

		replay(this.endpointMock);

		this.adapter.invoke(messageContext, this.endpointMock);

		verify(this.endpointMock);

		assertThat(messageContext.hasResponse()).isFalse();
	}

}
