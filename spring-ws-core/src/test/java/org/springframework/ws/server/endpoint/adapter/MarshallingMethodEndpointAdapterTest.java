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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.*;

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

public class MarshallingMethodEndpointAdapterTest {

	private MarshallingMethodEndpointAdapter adapter;

	private boolean noResponseInvoked;

	private Marshaller marshallerMock;

	private Unmarshaller unmarshallerMock;

	private MessageContext messageContext;

	private boolean responseInvoked;

	@BeforeEach
	public void setUp() throws Exception {

		adapter = new MarshallingMethodEndpointAdapter();
		marshallerMock = createMock(Marshaller.class);
		adapter.setMarshaller(marshallerMock);
		unmarshallerMock = createMock(Unmarshaller.class);
		adapter.setUnmarshaller(unmarshallerMock);
		adapter.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
	}

	@Test
	public void testNoResponse() throws Exception {

		Method noResponse = getClass().getMethod("noResponse", new Class[] { MyType.class });
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyType());

		replay(marshallerMock, unmarshallerMock);

		assertThat(noResponseInvoked).isFalse();

		adapter.invoke(messageContext, methodEndpoint);

		assertThat(noResponseInvoked).isTrue();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testNoRequestPayload() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		Method noResponse = getClass().getMethod("noResponse", new Class[] { MyType.class });
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);

		replay(marshallerMock, unmarshallerMock);

		assertThat(noResponseInvoked).isFalse();

		adapter.invoke(messageContext, methodEndpoint);
		assertThat(noResponseInvoked).isTrue();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testResponse() throws Exception {

		Method response = getClass().getMethod("response", new Class[] { MyType.class });
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
		expect(unmarshallerMock.unmarshal(isA(Source.class))).andReturn(new MyType());
		marshallerMock.marshal(isA(MyType.class), isA(Result.class));

		replay(marshallerMock, unmarshallerMock);

		assertThat(responseInvoked).isFalse();

		adapter.invoke(messageContext, methodEndpoint);

		assertThat(responseInvoked).isTrue();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testSupportedNoResponse() throws NoSuchMethodException {

		Method noResponse = getClass().getMethod("noResponse", new Class[] { MyType.class });
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
		expect(unmarshallerMock.supports(MyType.class)).andReturn(true);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(methodEndpoint)).isTrue();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testSupportedResponse() throws NoSuchMethodException {

		Method response = getClass().getMethod("response", new Class[] { MyType.class });
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
		expect(unmarshallerMock.supports(MyType.class)).andReturn(true);
		expect(marshallerMock.supports(MyType.class)).andReturn(true);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(methodEndpoint)).isTrue();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedMultipleParams", new Class[] { String.class, String.class });

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedWrongParam", new Class[] { String.class });
		expect(unmarshallerMock.supports(String.class)).andReturn(false);
		expect(marshallerMock.supports(String.class)).andReturn(true);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(marshallerMock, unmarshallerMock);
	}

	@Test
	public void testUnsupportedMethodWrongReturnType() throws NoSuchMethodException {

		Method unsupported = getClass().getMethod("unsupportedWrongParam", new Class[] { String.class });
		expect(marshallerMock.supports(String.class)).andReturn(false);

		replay(marshallerMock, unmarshallerMock);

		assertThat(adapter.supportsInternal(new MethodEndpoint(this, unsupported))).isFalse();

		verify(marshallerMock, unmarshallerMock);
	}

	public void noResponse(MyType type) {
		noResponseInvoked = true;

	}

	public MyType response(MyType type) {

		responseInvoked = true;
		return new MyType();
	}

	public void unsupportedMultipleParams(String s1, String s2) {}

	public String unsupportedWrongParam(String s) {
		return s;
	}

	private static class MyType {

	}

}
