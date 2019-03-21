/*
 * Copyright 2005-2010 the original author or authors.
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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;

import org.junit.Before;
import org.junit.Test;

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

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JaxbElementPayloadMethodProcessorTest {

	private JaxbElementPayloadMethodProcessor processor;

	private MethodParameter supportedParameter;

	private MethodParameter supportedReturnType;

	private MethodParameter stringReturnType;

	@Before
	public void setUp() throws Exception {
		processor = new JaxbElementPayloadMethodProcessor();
		supportedParameter = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), 0);
		supportedReturnType = new MethodParameter(getClass().getMethod("supported", JAXBElement.class), -1);
		stringReturnType = new MethodParameter(getClass().getMethod("string"), -1);
	}

	@Test
	public void supportsParameter() {
		assertTrue("processor does not support @JAXBElement parameter",
				processor.supportsParameter(supportedParameter));
	}

	@Test
	public void supportsReturnType() {
		assertTrue("processor does not support @JAXBElement return type",
				processor.supportsReturnType(supportedReturnType));
	}

	@Test
	public void resolveArgument() throws JAXBException {
		WebServiceMessage request = new MockWebServiceMessage("<myType xmlns='http://springframework.org'><string>Foo</string></myType>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		JAXBElement<?> result = processor.resolveArgument(messageContext, supportedParameter);
		assertTrue("result not a MyType", result.getValue() instanceof MyType);
		MyType type = (MyType) result.getValue();
		assertEquals("invalid result", "Foo", type.getString());
	}

	@Test
	public void handleReturnValue() throws Exception {
		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MyType type = new MyType();
		type.setString("Foo");
		JAXBElement<MyType> element = new JAXBElement<MyType>(new QName("http://springframework.org", "type"), MyType.class, type);
		processor.handleReturnValue(messageContext, supportedReturnType, element);
		assertTrue("context has no response", messageContext.hasResponse());
		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
		assertXMLEqual("<type xmlns='http://springframework.org'><string>Foo</string></type>", response.getPayloadAsString());
	}

	@Test
	public void handleReturnValueString() throws Exception {
		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		String s = "Foo";
		JAXBElement<String> element = new JAXBElement<String>(new QName("http://springframework.org", "string"), String.class, s);
		processor.handleReturnValue(messageContext, stringReturnType, element);
		assertTrue("context has no response", messageContext.hasResponse());
		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
		assertXMLEqual("<string xmlns='http://springframework.org'>Foo</string>", response.getPayloadAsString());
	}

	@Test
	public void handleNullReturnValue() throws Exception {
		MessageContext messageContext =
				new DefaultMessageContext(new MockWebServiceMessageFactory());

		processor.handleReturnValue(messageContext, stringReturnType, null);
		assertFalse("context has response", messageContext.hasResponse());
	}

	@Test
	public void handleReturnValueAxiom() throws Exception {
		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		MessageContext messageContext = new DefaultMessageContext(messageFactory);

		MyType type = new MyType();
		type.setString("Foo");
		JAXBElement<MyType> element = new JAXBElement<MyType>(new QName("http://springframework.org", "type"), MyType.class, type);

		processor.handleReturnValue(messageContext, supportedReturnType, element);
		assertTrue("context has no response", messageContext.hasResponse());
		AxiomSoapMessage response = (AxiomSoapMessage) messageContext.getResponse();

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		StringResult payloadResult = new StringResult();
		transformer.transform(response.getPayloadSource(), payloadResult);

		assertXMLEqual("<type xmlns='http://springframework.org'><string>Foo</string></type>",
				payloadResult.toString());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		response.writeTo(bos);
		String messageResult = bos.toString("UTF-8");

		assertXMLEqual("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Header/><soapenv:Body>" +
				"<type xmlns='http://springframework.org'><string>Foo</string></type>" +
				"</soapenv:Body></soapenv:Envelope>", messageResult);

	}


	@ResponsePayload
	public JAXBElement<MyType> supported(@RequestPayload JAXBElement<MyType> element) {
		return element;
	}

	@ResponsePayload
	public JAXBElement<String> string() {
		return new JAXBElement<String>(new QName("string"), String.class, "Foo");
	}

	@XmlType(name="myType", namespace = "http://springframework.org")
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
