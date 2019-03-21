/*
 * Copyright 2005-2014 the original author or authors.
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
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

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
import org.springframework.xml.sax.AbstractXmlReader;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class XmlRootElementPayloadMethodProcessorTest {

	private XmlRootElementPayloadMethodProcessor processor;

	private MethodParameter rootElementParameter;

	private MethodParameter typeParameter;

	private MethodParameter rootElementReturnType;

	@Before
	public void setUp() throws Exception {
		processor = new XmlRootElementPayloadMethodProcessor();
		rootElementParameter = new MethodParameter(getClass().getMethod("rootElement", MyRootElement.class), 0);
		typeParameter = new MethodParameter(getClass().getMethod("type", MyType.class), 0);
		rootElementReturnType = new MethodParameter(getClass().getMethod("rootElement", MyRootElement.class), -1);
	}

	@Test
	public void supportsParameter() {
		assertTrue("processor does not support @XmlRootElement parameter",
				processor.supportsParameter(rootElementParameter));
		assertTrue("processor does not support @XmlType parameter", processor.supportsParameter(
				typeParameter));
	}

	@Test
	public void supportsReturnType() {
		assertTrue("processor does not support @XmlRootElement return type",
				processor.supportsReturnType(rootElementReturnType));
	}

	@Test
	public void resolveArgumentRootElement() throws JAXBException {
		WebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'><string>Foo</string></root>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = processor.resolveArgument(messageContext, rootElementParameter);
		assertTrue("result not a MyRootElement", result instanceof MyRootElement);
		MyRootElement rootElement = (MyRootElement) result;
		assertEquals("invalid result", "Foo", rootElement.getString());
	}

	@Test
	public void resolveArgumentType() throws JAXBException {
		WebServiceMessage request = new MockWebServiceMessage("<type xmlns='http://springframework.org'><string>Foo</string></type>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		Object result = processor.resolveArgument(messageContext, typeParameter);
		assertTrue("result not a MyType", result instanceof MyType);
		MyType type = (MyType) result;
		assertEquals("invalid result", "Foo", type.getString());
	}

	@Test
	public void resolveArgumentFromCustomSAXSource() throws JAXBException {
		// Create a custom SAXSource that generates an appropriate sequence of events.
		XMLReader xmlReader = new AbstractXmlReader() {
			@Override
			public void parse(String systemId) throws IOException, SAXException {
				parse();
			}
			
			@Override
			public void parse(InputSource input) throws IOException, SAXException {
				parse();
			}
			
			private void parse() throws SAXException {
				ContentHandler handler = getContentHandler();
				// <root xmlns='http://springframework.org'><string>Foo</string></root>
				handler.startDocument();
				handler.startPrefixMapping("", "http://springframework.org");
				handler.startElement("http://springframework.org", "root", "root", new AttributesImpl());
				handler.startElement("http://springframework.org", "string", "string", new AttributesImpl());
				handler.characters("Foo".toCharArray(), 0, 3);
				handler.endElement("http://springframework.org", "string", "string");
				handler.endElement("http://springframework.org", "root", "root");
				handler.endPrefixMapping("");
				handler.endDocument();
			}
		};
		final SAXSource source = new SAXSource(xmlReader, new InputSource());
		
		// Create a mock WebServiceMessage that returns the SAXSource as payload source.
		WebServiceMessage request = new WebServiceMessage() {
			@Override
			public void writeTo(OutputStream outputStream) throws IOException {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Source getPayloadSource() {
				return source;
			}
			
			@Override
			public Result getPayloadResult() {
				throw new UnsupportedOperationException();
			}
		};
		// Create a message context with that request. Note that the message factory doesn't matter here:
		// it is required but not used.
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		
		Object result = processor.resolveArgument(messageContext, rootElementParameter);
		assertTrue("result not a MyRootElement", result instanceof MyRootElement);
		MyRootElement rootElement = (MyRootElement) result;
		assertEquals("invalid result", "Foo", rootElement.getString());
	}

	@Test
	public void handleReturnValue() throws Exception {
		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MyRootElement rootElement = new MyRootElement();
		rootElement.setString("Foo");
		processor.handleReturnValue(messageContext, rootElementReturnType, rootElement);
		assertTrue("context has no response", messageContext.hasResponse());
		MockWebServiceMessage response = (MockWebServiceMessage) messageContext.getResponse();
		assertXMLEqual("<root xmlns='http://springframework.org'><string>Foo</string></root>", response.getPayloadAsString());
	}

	@Test
	public void handleNullReturnValue() throws Exception {
		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MyRootElement rootElement = null;
		processor.handleReturnValue(messageContext, rootElementReturnType, rootElement);
		assertFalse("context has response", messageContext.hasResponse());
	}

	@Test
	public void handleReturnValueAxiom() throws Exception {
		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		MessageContext messageContext = new DefaultMessageContext(messageFactory);

		MyRootElement rootElement = new MyRootElement();
		rootElement.setString("Foo");

		processor.handleReturnValue(messageContext, rootElementReturnType, rootElement);
		assertTrue("context has no response", messageContext.hasResponse());
		AxiomSoapMessage response = (AxiomSoapMessage) messageContext.getResponse();

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		StringResult payloadResult = new StringResult();
		transformer.transform(response.getPayloadSource(), payloadResult);

		assertXMLEqual("<root xmlns='http://springframework.org'><string>Foo</string></root>",
				payloadResult.toString());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		response.writeTo(bos);
		String messageResult = bos.toString("UTF-8");
		
		assertXMLEqual("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Header/><soapenv:Body>" +
				"<root xmlns='http://springframework.org'><string>Foo</string></root>" +
				"</soapenv:Body></soapenv:Envelope>", messageResult);

	}

	@Test
	public void handleReturnValueAxiomNoPayloadCaching() throws Exception {
		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		MessageContext messageContext = new DefaultMessageContext(messageFactory);

		MyRootElement rootElement = new MyRootElement();
		rootElement.setString("Foo");

		processor.handleReturnValue(messageContext, rootElementReturnType, rootElement);
		assertTrue("context has no response", messageContext.hasResponse());
		AxiomSoapMessage response = (AxiomSoapMessage) messageContext.getResponse();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		response.writeTo(bos);
		String messageResult = bos.toString("UTF-8");

		assertXMLEqual("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Header/><soapenv:Body>" +
				"<root xmlns='http://springframework.org'><string>Foo</string></root>" +
				"</soapenv:Body></soapenv:Envelope>", messageResult);

	}

	@ResponsePayload
	public MyRootElement rootElement(@RequestPayload MyRootElement rootElement) {
		return rootElement;
	}

	public void type(@RequestPayload MyType type) {
	}

	@XmlRootElement(name = "root", namespace = "http://springframework.org")
	public static class MyRootElement {

		private String string;

		@XmlElement(name = "string", namespace = "http://springframework.org")
		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
	}

	@XmlType(name = "root", namespace = "http://springframework.org")
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
