/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.PayloadEndpoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.easymock.EasyMock.*;

public class PayloadEndpointAdapterTest {

	private PayloadEndpointAdapter adapter;

	private PayloadEndpoint endpointMock;

	@Before
	public void setUp() throws Exception {
		adapter = new PayloadEndpointAdapter();
		endpointMock = createMock(PayloadEndpoint.class);
	}

	@Test
	public void testSupports() throws Exception {
		Assert.assertTrue("PayloadEndpointAdapter does not support PayloadEndpoint", adapter.supports(endpointMock));
	}

	@Test
	public void testInvoke() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		final Transformer transformer = TransformerFactory.newInstance().newTransformer();
		PayloadEndpoint endpoint = new PayloadEndpoint() {
			public Source invoke(Source request) throws Exception {
				StringWriter writer = new StringWriter();
				transformer.transform(request, new StreamResult(writer));
				assertXMLEqual("Invalid request", "<request/>", writer.toString());
				return new StreamSource(new StringReader("<response/>"));
			}
		};
		endpoint.invoke(request.getPayloadSource());
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		adapter.invoke(messageContext, endpoint);
		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
		Assert.assertNotNull("No response created", response);
		assertXMLEqual("Invalid payload", "<response/>", response.getPayloadAsString());
	}

	@Test
	public void testInvokeNoResponse() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		expect(endpointMock.invoke(isA(Source.class))).andReturn(null);

		replay(endpointMock);

		adapter.invoke(messageContext, endpointMock);

		verify(endpointMock);

		Assert.assertFalse("Response created", messageContext.hasResponse());
	}

}
