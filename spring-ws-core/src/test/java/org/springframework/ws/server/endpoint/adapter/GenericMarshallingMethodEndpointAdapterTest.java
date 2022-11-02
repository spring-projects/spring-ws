/*
 * Copyright 2005-2022 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

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

public class GenericMarshallingMethodEndpointAdapterTest {

	private GenericMarshallingMethodEndpointAdapter adapter;

	private boolean noResponseInvoked;

	private GenericMarshaller marshallerMock;

	private GenericUnmarshaller unmarshallerMock;

	private boolean responseInvoked;

	@BeforeEach
	public void setUp() throws Exception {

		adapter = new GenericMarshallingMethodEndpointAdapter();
		marshallerMock = createMock(GenericMarshaller.class);
		adapter.setMarshaller(marshallerMock);
		unmarshallerMock = createMock(GenericUnmarshaller.class);
		adapter.setUnmarshaller(unmarshallerMock);
		adapter.afterPropertiesSet();
	}

	@Test
	public void testNoResponse() throws Exception {

		WebServiceMessage messageMock = createMock(WebServiceMessage.class);
		expect(messageMock.getPayloadSource()).andReturn(new StringSource("<request/>"));
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		MessageContext messageContext = new DefaultMessageContext(messageMock, factoryMock);

		Method noResponse = getClass().getMethod("noResponse", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyGenericType<MyType>());

		replay(marshallerMock, unmarshallerMock, messageMock, factoryMock);

		assertThat(noResponseInvoked).isFalse();

		adapter.invoke(messageContext, methodEndpoint);

		assertThat(noResponseInvoked).isTrue();

		verify(marshallerMock, unmarshallerMock, messageMock, factoryMock);
	}

	@Test
	public void testNoRequestPayload() throws Exception {

		WebServiceMessage messageMock = createMock(WebServiceMessage.class);
		expect(messageMock.getPayloadSource()).andReturn(null);
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		MessageContext messageContext = new DefaultMessageContext(messageMock, factoryMock);

		Method noResponse = getClass().getMethod("noResponse", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);

		replay(marshallerMock, unmarshallerMock, messageMock, factoryMock);

		assertThat(noResponseInvoked).isFalse();

		adapter.invoke(messageContext, methodEndpoint);

		assertThat(noResponseInvoked).isTrue();

		verify(marshallerMock, unmarshallerMock, messageMock, factoryMock);
	}

	@Test
	public void testResponse() throws Exception {

		WebServiceMessage requestMock = createMock(WebServiceMessage.class);
		expect(requestMock.getPayloadSource()).andReturn(new StringSource("<request/>"));
		WebServiceMessage responseMock = createMock(WebServiceMessage.class);
		expect(responseMock.getPayloadResult()).andReturn(new StringResult());
		WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
		expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
		MessageContext messageContext = new DefaultMessageContext(requestMock, factoryMock);

		Method response = getClass().getMethod("response", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
		expect(unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyGenericType<MyType>());
		marshallerMock.marshal(isA(MyGenericType.class), isA(Result.class));

		replay(marshallerMock, unmarshallerMock, requestMock, responseMock, factoryMock);

		assertThat(responseInvoked).isFalse();

		adapter.invoke(messageContext, methodEndpoint);

		assertThat(responseInvoked).isTrue();

		verify(marshallerMock, unmarshallerMock, requestMock, responseMock, factoryMock);
	}

	@Test
	public void testSupportedNoResponse() throws NoSuchMethodException {

		Method noResponse = getClass().getMethod("noResponse", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(unmarshallerMock.supports(noResponse.getGenericParameterTypes()[0])).andReturn(true);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(methodEndpoint)).isTrue();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testSupportedResponse() throws NoSuchMethodException {

		Method response = getClass().getMethod("response", MyGenericType.class);
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);

		expect(unmarshallerMock.supports(response.getGenericParameterTypes()[0])).andReturn(true);
		expect(marshallerMock.supports(response.getGenericReturnType())).andReturn(true);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(methodEndpoint)).isTrue();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedMultipleParams", String.class, String.class);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedWrongParam", String.class);
		expect(unmarshallerMock.supports(unsupported.getGenericParameterTypes()[0])).andReturn(false);
		expect(marshallerMock.supports(unsupported.getGenericReturnType())).andReturn(true);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testUnsupportedMethodWrongReturnType() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedWrongParam", String.class);
		expect(marshallerMock.supports(unsupported.getGenericReturnType())).andReturn(false);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(marshallerMock, unmarshallerMock);
	}

	public void noResponse(MyGenericType<MyType> type) {
		noResponseInvoked = true;

	}

	public MyGenericType<MyType> response(MyGenericType<MyType> type) {

		responseInvoked = true;
		return type;
	}

	public void unsupportedMultipleParams(String s1, String s2) {}

	public String unsupportedWrongParam(String s) {
		return s;
	}

	private static class MyType {

	}

	private static class MyGenericType<T> {

	}

}
