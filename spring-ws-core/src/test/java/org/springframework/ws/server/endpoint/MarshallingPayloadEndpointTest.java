/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.server.endpoint;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.oxm.mime.MimeMarshaller;
import org.springframework.oxm.mime.MimeUnmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.xmlunit.assertj.XmlAssert;

public class MarshallingPayloadEndpointTest {

	private Transformer transformer;

	private MessageContext context;

	private WebServiceMessageFactory factoryMock;

	@BeforeEach
	public void setUp() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
		transformer = TransformerFactoryUtils.newInstance().newTransformer();
		factoryMock = createMock(WebServiceMessageFactory.class);
		context = new DefaultMessageContext(request, factoryMock);
	}

	@Test
	public void testInvoke() throws Exception {

		Unmarshaller unmarshaller = new SimpleMarshaller() {

			@Override
			public Object unmarshal(Source source) throws XmlMappingException {

				try {
					StringWriter writer = new StringWriter();
					transformer.transform(source, new StreamResult(writer));

					XmlAssert.assertThat(writer.toString()).and("<request/>").ignoreWhitespace().areIdentical();

					return 42L;
				} catch (Exception e) {

					fail(e.getMessage());
					return null;
				}
			}
		};

		Marshaller marshaller = new SimpleMarshaller() {

			@Override
			public void marshal(Object graph, Result result) throws XmlMappingException {

				assertThat(graph).isEqualTo("result");

				try {
					transformer.transform(new StreamSource(new StringReader("<result/>")), result);
				} catch (TransformerException e) {
					fail(e.getMessage());
				}
			}
		};

		AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {

			@Override
			protected Object invokeInternal(Object requestObject) throws Exception {

				assertThat(requestObject).isEqualTo(42L);
				return "result";
			}
		};

		endpoint.setMarshaller(marshaller);
		endpoint.setUnmarshaller(unmarshaller);
		endpoint.afterPropertiesSet();

		expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(factoryMock);

		endpoint.invoke(context);
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();

		assertThat(response).isNotNull();
		XmlAssert.assertThat(response.getPayloadAsString()).and("<result/>").ignoreWhitespace().areIdentical();

		verify(factoryMock);
	}

	@Test
	public void testInvokeNullResponse() throws Exception {

		Unmarshaller unmarshaller = new SimpleMarshaller() {

			@Override
			public Object unmarshal(Source source) throws XmlMappingException {

				try {
					StringWriter writer = new StringWriter();
					transformer.transform(source, new StreamResult(writer));

					XmlAssert.assertThat(writer.toString()).and("<request/>").ignoreWhitespace().areIdentical();

					return (long) 42;
				} catch (Exception e) {
					fail(e.getMessage());
					return null;
				}
			}
		};

		Marshaller marshaller = new SimpleMarshaller() {

			@Override
			public void marshal(Object graph, Result result) throws XmlMappingException {
				fail("marshal not expected");
			}
		};

		AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {

			@Override
			protected Object invokeInternal(Object requestObject) throws Exception {

				assertThat(requestObject).isEqualTo(42L);
				return null;
			}
		};

		endpoint.setMarshaller(marshaller);
		endpoint.setUnmarshaller(unmarshaller);
		endpoint.afterPropertiesSet();
		replay(factoryMock);
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isFalse();

		verify(factoryMock);
	}

	@Test
	public void testInvokeNoRequest() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage((StringBuilder) null);
		context = new DefaultMessageContext(request, factoryMock);

		AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {

			@Override
			protected Object invokeInternal(Object requestObject) throws Exception {

				assertThat(requestObject).isNull();
				return null;
			}
		};

		endpoint.setMarshaller(new SimpleMarshaller());
		endpoint.setUnmarshaller(new SimpleMarshaller());
		endpoint.afterPropertiesSet();
		replay(factoryMock);
		endpoint.invoke(context);

		assertThat(context.hasResponse()).isFalse();

		verify(factoryMock);
	}

	@Test
	public void testInvokeMimeMarshaller() throws Exception {

		MimeUnmarshaller unmarshaller = createMock(MimeUnmarshaller.class);
		MimeMarshaller marshaller = createMock(MimeMarshaller.class);
		MimeMessage request = createMock("request", MimeMessage.class);
		MimeMessage response = createMock("response", MimeMessage.class);
		Source requestSource = new StringSource("<request/>");
		expect(request.getPayloadSource()).andReturn(requestSource);
		expect(factoryMock.createWebServiceMessage()).andReturn(response);
		expect(unmarshaller.unmarshal(eq(requestSource), isA(MimeContainer.class))).andReturn(42L);
		Result responseResult = new StringResult();
		expect(response.getPayloadResult()).andReturn(responseResult);
		marshaller.marshal(eq("result"), eq(responseResult), isA(MimeContainer.class));

		replay(factoryMock, unmarshaller, marshaller, request, response);

		AbstractMarshallingPayloadEndpoint endpoint = new AbstractMarshallingPayloadEndpoint() {

			@Override
			protected Object invokeInternal(Object requestObject) throws Exception {

				assertThat(requestObject).isEqualTo(42L);
				return "result";
			}
		};

		endpoint.setMarshaller(marshaller);
		endpoint.setUnmarshaller(unmarshaller);
		endpoint.afterPropertiesSet();

		context = new DefaultMessageContext(request, factoryMock);
		endpoint.invoke(context);

		assertThat(response).isNotNull();

		verify(factoryMock, unmarshaller, marshaller, request, response);
	}

	private static class SimpleMarshaller implements Marshaller, Unmarshaller {

		@Override
		public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
			fail("Not expected");
		}

		@Override
		public Object unmarshal(Source source) throws XmlMappingException, IOException {
			fail("Not expected");
			return null;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return false;
		}
	}

}
