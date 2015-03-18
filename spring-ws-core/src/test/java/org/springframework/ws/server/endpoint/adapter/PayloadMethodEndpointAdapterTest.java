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

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PayloadMethodEndpointAdapterTest {

	private PayloadMethodEndpointAdapter adapter;

	private boolean noResponseInvoked;

	private boolean responseInvoked;

	private MessageContext messageContext;

	@Before
	public void setUp() throws Exception {
		adapter = new PayloadMethodEndpointAdapter();
		messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@Test
	public void testSupportedNoResponse() throws NoSuchMethodException {
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "noResponse", new Class[]{DOMSource.class});
		Assert.assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
	}

	@Test
	public void testSupportedResponse() throws NoSuchMethodException {
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "response", new Class[]{StreamSource.class});
		Assert.assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
	}

	@Test
	public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {
		Assert.assertFalse("Method supported", adapter.supportsInternal(
				new MethodEndpoint(this, "unsupportedMultipleParams", new Class[]{Source.class, Source.class})));
	}

	@Test
	public void testUnsupportedMethodWrongReturnType() throws NoSuchMethodException {
		Assert.assertFalse("Method supported", adapter.supportsInternal(
				new MethodEndpoint(this, "unsupportedWrongReturnType", new Class[]{Source.class})));
	}

	@Test
	public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {
		Assert.assertFalse("Method supported",
				adapter.supportsInternal(new MethodEndpoint(this, "unsupportedWrongParam", new Class[]{String.class})));
	}

	@Test
	public void testNoResponse() throws Exception {
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "noResponse", new Class[]{DOMSource.class});
		Assert.assertFalse("Method invoked", noResponseInvoked);
		adapter.invoke(messageContext, methodEndpoint);
		Assert.assertTrue("Method not invoked", noResponseInvoked);
	}

	@Test
	public void testResponse() throws Exception {
		WebServiceMessage request = new MockWebServiceMessage("<request/>");
		messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "response", new Class[]{StreamSource.class});
		Assert.assertFalse("Method invoked", responseInvoked);
		adapter.invoke(messageContext, methodEndpoint);
		Assert.assertTrue("Method not invoked", responseInvoked);
	}

	public void noResponse(DOMSource request) {
		noResponseInvoked = true;
	}

	public Source response(StreamSource request) {
		responseInvoked = true;
		return request;
	}

	public void unsupportedMultipleParams(Source s1, Source s2) {
	}

	public Source unsupportedWrongParam(String request) {
		return null;
	}

	public String unsupportedWrongReturnType(Source request) {
		return null;
	}

}