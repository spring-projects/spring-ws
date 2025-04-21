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

import org.springframework.oxm.GenericMarshaller;
import org.springframework.oxm.GenericUnmarshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

@Deprecated
class GenericMarshallingMethodEndpointAdapterTests {

	private GenericMarshallingMethodEndpointAdapter adapter;

	private boolean noResponseInvoked;

	private GenericMarshaller marshallerMock;

	private GenericUnmarshaller unmarshallerMock;

	private boolean responseInvoked;

	@BeforeEach
	void setUp() throws Exception {

		this.adapter = new GenericMarshallingMethodEndpointAdapter();
		this.marshallerMock = createMock(GenericMarshaller.class);
		this.adapter.setMarshaller(this.marshallerMock);
		this.unmarshallerMock = createMock(GenericUnmarshaller.class);
		this.adapter.setUnmarshaller(this.unmarshallerMock);
		this.adapter.afterPropertiesSet();
	}

	@Test
	void testNoResponse() throws Exception {

		WebServiceMessage messageMock = createMock(WebServiceMessage.class);
		expect(messageMock.getPayloadSource()).andReturn(new StringSource("<request/>"));
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		MessageContext messageContext = new DefaultMessageContext(messageMock, factoryMock);

		Method noResponse = getClass().getMethod("noResponse", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(this.unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyGenericType<MyType>());

		replay(this.marshallerMock, this.unmarshallerMock, messageMock, factoryMock);

		assertThat(this.noResponseInvoked).isFalse();

		this.adapter.invoke(messageContext, methodEndpoint);

		assertThat(this.noResponseInvoked).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock, messageMock, factoryMock);
	}

	@Test
	void testNoRequestPayload() throws Exception {

		WebServiceMessage messageMock = createMock(WebServiceMessage.class);
		expect(messageMock.getPayloadSource()).andReturn(null);
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		MessageContext messageContext = new DefaultMessageContext(messageMock, factoryMock);

		Method noResponse = getClass().getMethod("noResponse", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);

		replay(this.marshallerMock, this.unmarshallerMock, messageMock, factoryMock);

		assertThat(this.noResponseInvoked).isFalse();

		this.adapter.invoke(messageContext, methodEndpoint);

		assertThat(this.noResponseInvoked).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock, messageMock, factoryMock);
	}

	@Test
	void testResponse() throws Exception {

		WebServiceMessage requestMock = createMock(WebServiceMessage.class);
		expect(requestMock.getPayloadSource()).andReturn(new StringSource("<request/>"));
		WebServiceMessage responseMock = createMock(WebServiceMessage.class);
		expect(responseMock.getPayloadResult()).andReturn(new StringResult());
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
		MessageContext messageContext = new DefaultMessageContext(requestMock, factoryMock);

		Method response = getClass().getMethod("response", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
		expect(this.unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyGenericType<MyType>());
		this.marshallerMock.marshal(isA(MyGenericType.class), isA(Result.class));

		replay(this.marshallerMock, this.unmarshallerMock, requestMock, responseMock, factoryMock);

		assertThat(this.responseInvoked).isFalse();

		this.adapter.invoke(messageContext, methodEndpoint);

		assertThat(this.responseInvoked).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock, requestMock, responseMock, factoryMock);
	}

	@Test
	void testSupportedNoResponse() throws NoSuchMethodException {

		Method noResponse = getClass().getMethod("noResponse", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(this.unmarshallerMock.supports(noResponse.getGenericParameterTypes()[0])).andReturn(true);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(methodEndpoint)).isTrue();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testSupportedResponse() throws NoSuchMethodException {

		Method response = getClass().getMethod("response", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);

		expect(this.unmarshallerMock.supports(response.getGenericParameterTypes()[0])).andReturn(true);
		expect(this.marshallerMock.supports(response.getGenericReturnType())).andReturn(true);

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
		expect(this.unmarshallerMock.supports(unsupported.getGenericParameterTypes()[0])).andReturn(false);
		expect(this.marshallerMock.supports(unsupported.getGenericReturnType())).andReturn(true);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	@Test
	void testUnsupportedMethodWrongReturnType() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedWrongParam", String.class);
		expect(this.marshallerMock.supports(unsupported.getGenericReturnType())).andReturn(false);

		replay(this.marshallerMock, this.unmarshallerMock);

		assertThat(this.adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(this.marshallerMock, this.unmarshallerMock);
	}

	public void noResponse(MyGenericType<MyType> type) {
		this.noResponseInvoked = true;

	}

	public MyGenericType<MyType> response(MyGenericType<MyType> type) {

		this.responseInvoked = true;
		return type;
	}

	public void unsupportedMultipleParams(String s1, String s2) {
	}

	public String unsupportedWrongParam(String s) {
		return s;
	}

	private static final class MyType {

	}

	private static final class MyGenericType<T> {

	}

}
