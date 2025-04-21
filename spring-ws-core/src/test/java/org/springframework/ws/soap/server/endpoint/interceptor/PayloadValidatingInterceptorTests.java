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

package org.springframework.ws.soap.server.endpoint.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.validation.ValidationErrorHandler;
import org.springframework.xml.xsd.SimpleXsdSchema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PayloadValidatingInterceptorTests {

	private PayloadValidatingInterceptor interceptor;

	private MessageContext context;

	private SaajSoapMessageFactory soap11Factory;

	private SaajSoapMessageFactory soap12Factory;

	private Transformer transformer;

	private static final String INVALID_MESSAGE = "invalidMessage.xml";

	private static final String SCHEMA = "schema.xsd";

	private static final String VALID_MESSAGE = "validMessage.xml";

	private static final String PRODUCT_SCHEMA = "productSchema.xsd";

	private static final String SIZE_SCHEMA = "sizeSchema.xsd";

	private static final String VALID_SOAP_MESSAGE = "validSoapMessage.xml";

	private static final String SCHEMA2 = "schema2.xsd";

	@BeforeEach
	void setUp() throws Exception {

		this.interceptor = new PayloadValidatingInterceptor();
		this.interceptor.setSchema(new ClassPathResource(SCHEMA, getClass()));
		this.interceptor.setValidateRequest(true);
		this.interceptor.setValidateResponse(true);
		this.interceptor.afterPropertiesSet();

		this.soap11Factory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
		this.soap12Factory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL));
		this.transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	void testHandleInvalidRequestSoap11() throws Exception {

		SoapMessage invalidMessage = this.soap11Factory.createWebServiceMessage();
		InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
		this.transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
		this.context = new DefaultMessageContext(invalidMessage, this.soap11Factory);

		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isFalse();
		assertThat(this.context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) this.context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(fault.getFaultStringOrReason())
			.isEqualTo(PayloadValidatingInterceptor.DEFAULT_FAULTSTRING_OR_REASON);
		assertThat(fault.getFaultDetail()).isNotNull();
	}

	@Test
	void testHandleInvalidRequestSoap12() throws Exception {

		SoapMessage invalidMessage = this.soap12Factory.createWebServiceMessage();
		InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
		this.transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
		this.context = new DefaultMessageContext(invalidMessage, this.soap12Factory);

		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isFalse();
		assertThat(this.context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) this.context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_12.getClientOrSenderFaultName());
		assertThat(fault.getFaultReasonText(Locale.ENGLISH))
			.isEqualTo(PayloadValidatingInterceptor.DEFAULT_FAULTSTRING_OR_REASON);
		assertThat(fault.getFaultDetail()).isNotNull();
	}

	@Test
	void testHandleInvalidRequestOverridenProperties() throws Exception {

		String faultString = "fout";
		Locale locale = new Locale("nl");
		this.interceptor.setFaultStringOrReason(faultString);
		this.interceptor.setFaultStringOrReasonLocale(locale);
		this.interceptor.setAddValidationErrorDetail(false);

		SoapMessage invalidMessage = this.soap11Factory.createWebServiceMessage();
		InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
		this.transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
		this.context = new DefaultMessageContext(invalidMessage, this.soap11Factory);

		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isFalse();
		assertThat(this.context.hasResponse()).isTrue();

		SoapMessage response = (SoapMessage) this.context.getResponse();

		assertThat(response.getSoapBody().hasFault()).isTrue();

		Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(fault.getFaultCode()).isEqualTo(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(fault.getFaultStringOrReason()).isEqualTo(faultString);
		assertThat(fault.getFaultStringLocale()).isEqualTo(locale);
		assertThat(fault.getFaultDetail()).isNull();
	}

	@Test
	void testHandlerInvalidRequest() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isFalse();
	}

	@Test
	void testHandleValidRequest() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isTrue();
		assertThat(this.context.hasResponse()).isFalse();
	}

	@Test
	void testHandleInvalidResponse() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) this.context.getResponse();
		response.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
		boolean result = this.interceptor.handleResponse(this.context, null);

		assertThat(result).isFalse();
	}

	@Test
	void testHandleValidResponse() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage();
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) this.context.getResponse();
		response.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		boolean result = this.interceptor.handleResponse(this.context, null);

		assertThat(result).isTrue();
	}

	@Test
	void testNamespacesInType() throws Exception {

		// Make sure we use Xerces for this testcase: the JAXP implementation used
		// internally by JDK 1.5 has a bug
		// See http://opensource.atlassian.com/projects/spring/browse/SWS-35
		String previousSchemaFactory = System
			.getProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI, "");
		System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
				"org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		try {
			PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
			interceptor.setSchema(new ClassPathResource(SCHEMA2, PayloadValidatingInterceptorTests.class));
			interceptor.afterPropertiesSet();
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage saajMessage = SaajUtils.loadMessage(new ClassPathResource(VALID_SOAP_MESSAGE, getClass()),
					messageFactory);
			this.context = new DefaultMessageContext(new SaajSoapMessage(saajMessage),
					new SaajSoapMessageFactory(messageFactory));

			boolean result = interceptor.handleRequest(this.context, null);

			assertThat(result).isTrue();
			assertThat(this.context.hasResponse()).isFalse();
		}
		finally {
			// Reset the property
			System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
					previousSchemaFactory);
		}
	}

	@Test
	void testNonExistingSchema() {

		assertThatIllegalArgumentException().isThrownBy(() -> {

			this.interceptor.setSchema(new ClassPathResource("invalid"));
			this.interceptor.afterPropertiesSet();
		});
	}

	@Test
	void testHandlerInvalidRequestMultipleSchemas() throws Exception {

		this.interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(new ClassPathResource(INVALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isFalse();
	}

	@Test
	void testHandleValidRequestMultipleSchemas() throws Exception {

		this.interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(new ClassPathResource(VALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isTrue();
		assertThat(this.context.hasResponse()).isFalse();
	}

	@Test
	void testHandleInvalidResponseMultipleSchemas() throws Exception {

		this.interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage();
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) this.context.getResponse();
		response.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
		boolean result = this.interceptor.handleResponse(this.context, null);

		assertThat(result).isFalse();
	}

	@Test
	void testHandleValidResponseMultipleSchemas() throws Exception {

		this.interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage();
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) this.context.getResponse();
		response.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		boolean result = this.interceptor.handleResponse(this.context, null);

		assertThat(result).isTrue();
	}

	@Test
	void testCreateRequestValidationFaultAxiom() throws Exception {
		LocatorImpl locator = new LocatorImpl();
		locator.setLineNumber(0);
		locator.setColumnNumber(0);
		SAXParseException[] exceptions = new SAXParseException[] { new SAXParseException("Message 1", locator),
				new SAXParseException("Message 2", locator), };
		MessageContext messageContext = new DefaultMessageContext(new AxiomSoapMessageFactory());
		this.interceptor.handleRequestValidationErrors(messageContext, exceptions);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		messageContext.getResponse().writeTo(os);

		XmlAssert.assertThat(os.toString())
			.and("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Header/>"
					+ "<soapenv:Body>" + "<soapenv:Fault>" + "<faultcode>soapenv:Client</faultcode>"
					+ "<faultstring xml:lang='en'>Validation error</faultstring>" + "<detail>"
					+ "<spring-ws:ValidationError xmlns:spring-ws=\"http://springframework.org/spring-ws\">Message 1</spring-ws:ValidationError>"
					+ "<spring-ws:ValidationError xmlns:spring-ws=\"http://springframework.org/spring-ws\">Message 2</spring-ws:ValidationError>"
					+ "</detail>" + "</soapenv:Fault>" + "</soapenv:Body>" + "</soapenv:Envelope>")
			.ignoreWhitespace()
			.areIdentical();

	}

	@Test
	void testXsdSchema() throws Exception {

		PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
		SimpleXsdSchema schema = new SimpleXsdSchema(new ClassPathResource(SCHEMA, getClass()));
		schema.afterPropertiesSet();
		interceptor.setXsdSchema(schema);
		interceptor.setValidateRequest(true);
		interceptor.setValidateResponse(true);
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		boolean result = interceptor.handleRequest(this.context, null);

		assertThat(result).isTrue();
		assertThat(this.context.hasResponse()).isFalse();
	}

	@Test
	void testAxiom() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(true);
		messageFactory.afterPropertiesSet();

		PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
		interceptor.setSchema(new ClassPathResource("codexws.xsd", getClass()));
		interceptor.afterPropertiesSet();

		Resource resource = new ClassPathResource("axiom.xml", getClass());
		TransportInputStream tis = new MockTransportInputStream(resource.getInputStream());
		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
		MessageContext context = new DefaultMessageContext(message, messageFactory);
		boolean result = interceptor.handleRequest(context, null);

		assertThat(result).isTrue();
	}

	@Test
	void testMultipleNamespacesAxiom() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(true);
		messageFactory.afterPropertiesSet();

		PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
		interceptor.setSchema(new ClassPathResource("multipleNamespaces.xsd", getClass()));
		interceptor.afterPropertiesSet();

		Resource resource = new ClassPathResource("multipleNamespaces.xml", getClass());
		TransportInputStream tis = new MockTransportInputStream(resource.getInputStream());
		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
		MessageContext context = new DefaultMessageContext(message, messageFactory);
		boolean result = interceptor.handleRequest(context, null);

		assertThat(result).isTrue();
	}

	@Test
	void customErrorHandler() throws Exception {

		ValidationErrorHandler errorHandler = new ValidationErrorHandler() {
			public SAXParseException[] getErrors() {
				return new SAXParseException[0];
			}

			public void warning(SAXParseException exception) {
			}

			public void error(SAXParseException exception) {
			}

			public void fatalError(SAXParseException exception) {
			}
		};

		this.interceptor.setErrorHandler(errorHandler);
		SoapMessage invalidMessage = this.soap11Factory.createWebServiceMessage();
		InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
		this.transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
		this.context = new DefaultMessageContext(invalidMessage, this.soap11Factory);

		boolean result = this.interceptor.handleRequest(this.context, null);

		assertThat(result).isTrue();
		assertThat(this.context.hasResponse()).isFalse();
	}

}
