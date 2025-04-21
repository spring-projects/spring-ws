/*
 * Copyright 2005-2025 the original author or authors.
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

import java.lang.reflect.Method;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

@Deprecated
class MarshallingMethodEndpointAdapterTests {

	private MarshallingMethodEndpointAdapter adapter;

	private boolean noResponseInvoked;

	private Marshaller marshallerMock;

	private Unmarshaller unmarshallerMock;

	private MessageContext messageContext;

	private boolean responseInvoked;

	@BeforeEach
	void setUp() throws Exception {

		this.adapter = new MarshallingMethodEndpointAdapter();
		this.marshallerMock = createMock(Marshaller.class);
		this.adapter.setMarshaller(this.marshallerMock);
		this.unmarshallerMock = createMock(Unmarshaller.class);
		this.adapter.setUnmarshaller(this.unmarshallerMock);
		this.adapter.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		this.messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
	}

	@Test
	void testNoResponse() throws Exception {

		Method noResponse = getClass().getMethod("noResponse", MyType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(this.unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyType());

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.noResponseInvoked).isFalse();

		this.adapter.invoke(this.messageContext, methodEndpoint);

		assertThat(this.noResponseInvoked).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testNoRequestPayload() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		this.messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		Method noResponse = getClass().getMethod("noResponse", MyType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.noResponseInvoked).isFalse();

		this.adapter.invoke(this.messageContext, methodEndpoint);
		assertThat(this.noResponseInvoked).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testResponse() throws Exception {

		Method response = getClass().getMethod("response", MyType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
		expect(this.unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyType());
		this.marshallerMock.marshal(isA(MyType.class), isA(Result.class));

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.responseInvoked).isFalse();

		this.adapter.invoke(this.messageContext, methodEndpoint);

		assertThat(this.responseInvoked).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testSupportedNoResponse() throws NoSuchMethodException {

		Method noResponse = getClass().getMethod("noResponse", MyType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(this.unmarshallerMock.supports(MyType.class)).andReturn(true);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(methodEndpoint)).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testSupportedResponse() throws NoSuchMethodException {

		Method response = getClass().getMethod("response", MyType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
		expect(this.unmarshallerMock.supports(MyType.class)).andReturn(true);
		expect(this.marshallerMock.supports(MyType.class)).andReturn(true);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(methodEndpoint)).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedMultipleParams", String.class, String.class);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testUnsupportedMethodWrongParam() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedWrongParam", String.class);
		expect(this.unmarshallerMock.supports(String.class)).andReturn(false);
		expect(this.marshallerMock.supports(String.class)).andReturn(true);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testUnsupportedMethodWrongReturnType() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedWrongParam", String.class);
		expect(this.marshallerMock.supports(String.class)).andReturn(false);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	public void noResponse(MyType type) {
		this.noResponseInvoked = true;

	}

	public MyType response(MyType type) {

		this.responseInvoked = true;
		return new MyType();
	}

	public void unsupportedMultipleParams(String s1, String s2) {
	}

	public String unsupportedWrongParam(String s) {
		return s;
	}

	private static final class MyType {

	}

}
