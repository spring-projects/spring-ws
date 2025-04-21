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

package org.springframework.ws.client.support.interceptor;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.xsd.SimpleXsdSchema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PayloadValidatingInterceptorTests {

	private PayloadValidatingInterceptor interceptor;

	private MessageContext context;

	private SaajSoapMessageFactory soap11Factory;

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

		this.transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	void testHandleInvalidRequest() throws Exception {

		SoapMessage invalidMessage = this.soap11Factory.createWebServiceMessage();
		InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
		this.transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
		this.context = new DefaultMessageContext(invalidMessage, this.soap11Factory);

		assertThatExceptionOfType(WebServiceClientException.class)
			.isThrownBy(() -> this.interceptor.handleRequest(this.context))
			.withMessageContaining("XML validation error on request");
	}

	@Test
	void testHandlerInvalidRequest() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		assertThatExceptionOfType(WebServiceClientException.class)
			.isThrownBy(() -> this.interceptor.handleRequest(this.context))
			.withMessageContaining("XML validation error on request");
	}

	@Test
	void testHandleValidRequest() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		boolean result = this.interceptor.handleRequest(this.context);

		assertThat(result).isTrue();
		assertThat(this.context.hasResponse()).isFalse();
	}

	@Test
	void testHandleInvalidResponse() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) this.context.getResponse();
		response.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));

		boolean result = this.interceptor.handleResponse(this.context);

		assertThat(result).isFalse();
	}

	@Test
	void testHandleValidResponse() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage();
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) this.context.getResponse();
		response.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		boolean result = this.interceptor.handleResponse(this.context);

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

			boolean result = interceptor.handleRequest(this.context);

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

		assertThatExceptionOfType(WebServiceClientException.class)
			.isThrownBy(() -> this.interceptor.handleRequest(this.context))
			.withMessageContaining("XML validation error on request");
	}

	@Test
	void testHandleValidRequestMultipleSchemas() throws Exception {

		this.interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		this.interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(new ClassPathResource(VALID_MESSAGE, getClass()));
		this.context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean result = this.interceptor.handleRequest(this.context);

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
		boolean result = this.interceptor.handleResponse(this.context);

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
		boolean result = this.interceptor.handleResponse(this.context);

		assertThat(result).isTrue();
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
		boolean result = interceptor.handleRequest(this.context);

		assertThat(result).isTrue();
		assertThat(this.context.hasResponse()).isFalse();
	}

}
