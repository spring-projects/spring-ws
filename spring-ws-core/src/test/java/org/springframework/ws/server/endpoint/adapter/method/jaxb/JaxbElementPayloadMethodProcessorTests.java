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

package org.springframework.ws.server.endpoint.adapter.method.jaxb;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JaxbElementPayloadMethodProcessorTests {

	private JaxbElementPayloadMethodProcessor processor;

	private MethodParameter supportedParameter;

	private MethodParameter supportedReturnType;

	private MethodParameter stringReturnType;

	@BeforeEach
	void setUp() throws Exception {

		this.processor = new JaxbElementPayloadMethodProcessor();
		this.supportedParameter = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), 0);
		this.supportedReturnType = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), -1);
		this.stringReturnType = new MethodParameter(getClass().getMethod("string"), -1);
	}

	@Test
	void supportsParameter() {
		assertThat(this.processor.supportsParameter(this.supportedParameter)).isTrue();
	}

	@Test
	void supportsReturnType() {
		assertThat(this.processor.supportsReturnType(this.supportedReturnType)).isTrue();
	}

	@Test
	void resolveArgument() throws JAXBException {

		WebServiceMessage request = new MockWebServiceMessage(
				"<myType xmlns='http://springframework.org'><string>Foo</string></myType>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		JAXBElement<?> result = this.processor.resolveArgument(messageContext, this.supportedParameter);

		assertThat(result.getValue()).isInstanceOf(MyType.class);

		MyType type = (MyType) result.getValue();

		assertThat(type.getString()).isEqualTo("Foo");
	}

	@Test
	void handleReturnValue() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MyType type = new MyType();
		type.setString("Foo");
		JAXBElement<MyType> element = new JAXBElement<>(new QName("http://springframework.org", "type"), MyType.class,
				type);
		this.processor.handleReturnValue(messageContext, this.supportedReturnType, element);

		assertThat(messageContext.hasResponse()).isTrue();

		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();

		XmlAssert.assertThat(response.getPayloadAsString())
			.and("<type xmlns='http://springframework.org'><string>Foo</string></type>")
			.ignoreWhitespace()
			.areIdentical();
	}

	@Test
	void handleReturnValueString() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		String s = "Foo";
		JAXBElement<String> element = new JAXBElement<>(new QName("http://springframework.org", "string"), String.class,
				s);
		this.processor.handleReturnValue(messageContext, this.stringReturnType, element);

		assertThat(messageContext.hasResponse()).isTrue();

		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();

		XmlAssert.assertThat(response.getPayloadAsString())
			.and("<string xmlns='http://springframework.org'>Foo</string>")
			.ignoreWhitespace()
			.areIdentical();
	}

	@Test
	void handleNullReturnValue() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		this.processor.handleReturnValue(messageContext, this.stringReturnType, null);

		assertThat(messageContext.hasResponse()).isFalse();
	}

	@Test
	void handleReturnValueAxiom() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		MessageContext messageContext = new DefaultMessageContext(messageFactory);

		MyType type = new MyType();
		type.setString("Foo");
		JAXBElement<MyType> element = new JAXBElement<>(new QName("http://springframework.org", "type"), MyType.class,
				type);

		this.processor.handleReturnValue(messageContext, this.supportedReturnType, element);

		assertThat(messageContext.hasResponse()).isTrue();

		AxiomSoapMessage response = (AxiomSoapMessage) messageContext.getResponse();

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		StringResult payloadResult = new StringResult();
		transformer.transform(response.getPayloadSource(), payloadResult);

		XmlAssert.assertThat(payloadResult.toString())
			.and("<type xmlns='http://springframework.org'><string>Foo</string></type>")
			.ignoreWhitespace()
			.areIdentical();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		response.writeTo(bos);
		String messageResult = bos.toString("UTF-8");

		XmlAssert.assertThat(messageResult)
			.and("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Header/><soapenv:Body>"
					+ "<type xmlns='http://springframework.org'><string>Foo</string></type>"
					+ "</soapenv:Body></soapenv:Envelope>")
			.ignoreWhitespace()
			.areIdentical();

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
			return this.string;
		}

		public void setString(String string) {
			this.string = string;
		}

	}

}
