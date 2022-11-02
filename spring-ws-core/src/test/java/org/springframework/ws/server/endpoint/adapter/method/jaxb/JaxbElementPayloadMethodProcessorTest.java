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

package org.springframework.ws.server.endpoint.adapter.method.jaxb;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.xmlunit.assertj.XmlAssert;

public class JaxbElementPayloadMethodProcessorTest {

	private JaxbElementPayloadMethodProcessor processor;

	private MethodParameter supportedParameter;

	private MethodParameter supportedReturnType;

	private MethodParameter stringReturnType;

	@BeforeEach
	public void setUp() throws Exception {

		processor = new JaxbElementPayloadMethodProcessor();
		supportedParameter = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), 0);
		supportedReturnType = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), -1);
		stringReturnType = new MethodParameter(getClass().getMethod("string"), -1);
	}

	@Test
	public void supportsParameter() {
		assertThat(processor.supportsParameter(supportedParameter)).isTrue();
	}

	@Test
	public void supportsReturnType() {
		assertThat(processor.supportsReturnType(supportedReturnType)).isTrue();
	}

	@Test
	public void resolveArgument() throws JAXBException {

		WebServiceMessage request = new MockWebServiceMessage(
				"<myType xmlns='http://springframework.org'><string>Foo</string></myType>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		JAXBElement<?> result = processor.resolveArgument(messageContext, supportedParameter);

		assertThat(result.getValue()).isInstanceOf(MyType.class);

		MyType type = (MyType) result.getValue();

		assertThat(type.getString()).isEqualTo("Foo");
	}

	@Test
	public void handleReturnValue() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MyType type = new MyType();
		type.setString("Foo");
		JAXBElement<MyType> element = new JAXBElement<>(new QName("http://springframework.org", "type"), MyType.class,
				type);
		processor.handleReturnValue(messageContext, supportedReturnType, element);

		assertThat(messageContext.hasResponse()).isTrue();

		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();

		XmlAssert.assertThat(response.getPayloadAsString())
				.and("<type xmlns='http://springframework.org'><string>Foo</string></type>").ignoreWhitespace().areIdentical();
	}

	@Test
	public void handleReturnValueString() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		String s = "Foo";
		JAXBElement<String> element = new JAXBElement<>(new QName("http://springframework.org", "string"), String.class, s);
		processor.handleReturnValue(messageContext, stringReturnType, element);

		assertThat(messageContext.hasResponse()).isTrue();

		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();

		XmlAssert.assertThat(response.getPayloadAsString()).and("<string xmlns='http://springframework.org'>Foo</string>")
				.ignoreWhitespace().areIdentical();
	}

	@Test
	public void handleNullReturnValue() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		processor.handleReturnValue(messageContext, stringReturnType, null);

		assertThat(messageContext.hasResponse()).isFalse();
	}

	@ResponsePayload
	public JAXBElement<MyType> supported(@RequestPayload JAXBElement<MyType> element) {
		return element;
	}

	@ResponsePayload
	public JAXBElement<String> string() {
		return new JAXBElement<>(new QName("string"), String.class, "Foo");
	}

	@XmlType(name = "myType", namespace = "http://springframework.org")
	public static class MyType {

		private String string;

		@XmlElement(name = "string", namespace = "http://springframework.org")
		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
	}

}
