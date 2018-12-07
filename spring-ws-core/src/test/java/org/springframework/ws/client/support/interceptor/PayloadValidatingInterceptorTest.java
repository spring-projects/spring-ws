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

package org.springframework.ws.client.support.interceptor;

import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

public class PayloadValidatingInterceptorTest {

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

	@Before
	public void setUp() throws Exception {
		interceptor = new PayloadValidatingInterceptor();
		interceptor.setSchema(new ClassPathResource(SCHEMA, getClass()));
		interceptor.setValidateRequest(true);
		interceptor.setValidateResponse(true);
		interceptor.afterPropertiesSet();

		soap11Factory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));

		transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	public void testHandleInvalidRequest() throws Exception {
		SoapMessage invalidMessage = soap11Factory.createWebServiceMessage();
		InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
		transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
		context = new DefaultMessageContext(invalidMessage, soap11Factory);

		boolean validated;
		try {
			validated = interceptor.handleRequest(context);
		}
		catch (WebServiceClientException e) {
			validated = false;
			Assert.assertNotNull("No exception details provided in WebServiceClientException", e.getMessage());
		}
		Assert.assertFalse("Invalid response from interceptor", validated);
	}

	@Test
	public void testHandlerInvalidRequest() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean validated;
		try {
			validated = interceptor.handleRequest(context);
		}
		catch (WebServiceClientException e) {
			validated = false;
			Assert.assertNotNull("No exception details provided in WebServiceClientException", e.getMessage());
		}
		Assert.assertFalse("Invalid response from interceptor", validated);
	}

	@Test
	public void testHandleValidRequest() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		boolean result = interceptor.handleRequest(context);
		Assert.assertTrue("Invalid response from interceptor", result);
		Assert.assertFalse("Response set", context.hasResponse());
	}

	@Test
	public void testHandleInvalidResponse() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage();
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));

		boolean result = interceptor.handleResponse(context);
		Assert.assertFalse("Invalid response from interceptor", result);
	}

	@Test
	public void testHandleValidResponse() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage();
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		boolean result = interceptor.handleResponse(context);
		Assert.assertTrue("Invalid response from interceptor", result);
	}

	@Test
	public void testNamespacesInType() throws Exception {
		// Make sure we use Xerces for this testcase: the JAXP implementation used internally by JDK 1.5 has a bug
		// See http://opensource.atlassian.com/projects/spring/browse/SWS-35
		String previousSchemaFactory =
				System.getProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI, "");
		System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
				"org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		try {
			PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
			interceptor.setSchema(new ClassPathResource(SCHEMA2, PayloadValidatingInterceptorTest.class));
			interceptor.afterPropertiesSet();
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage saajMessage =
					SaajUtils.loadMessage(new ClassPathResource(VALID_SOAP_MESSAGE, getClass()), messageFactory);
			context = new DefaultMessageContext(new SaajSoapMessage(saajMessage),
					new SaajSoapMessageFactory(messageFactory));

			boolean result = interceptor.handleRequest(context);
			Assert.assertTrue("Invalid response from interceptor", result);
			Assert.assertFalse("Response set", context.hasResponse());
		}
		finally {
			// Reset the property
			System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
					previousSchemaFactory);
		}
	}

	@Test
	public void testNonExistingSchema() throws Exception {
		try {
			interceptor.setSchema(new ClassPathResource("invalid"));
			interceptor.afterPropertiesSet();
			Assert.fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void testHandlerInvalidRequestMultipleSchemas() throws Exception {
		interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(new ClassPathResource(INVALID_MESSAGE, getClass()));
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean validated;
		try {
			validated = interceptor.handleRequest(context);
		}
		catch (WebServiceClientException e) {
			validated = false;
			Assert.assertNotNull("No exception details provided in WebServiceClientException", e.getMessage());
		}
		Assert.assertFalse("Invalid response from interceptor", validated);
	}

	@Test
	public void testHandleValidRequestMultipleSchemas() throws Exception {
		interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage(new ClassPathResource(VALID_MESSAGE, getClass()));
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		boolean result = interceptor.handleRequest(context);
		Assert.assertTrue("Invalid response from interceptor", result);
		Assert.assertFalse("Response set", context.hasResponse());
	}

	@Test
	public void testHandleInvalidResponseMultipleSchemas() throws Exception {
		interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage();
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
		boolean result = interceptor.handleResponse(context);
		Assert.assertFalse("Invalid response from interceptor", result);
	}

	@Test
	public void testHandleValidResponseMultipleSchemas() throws Exception {
		interceptor.setSchemas(new ClassPathResource(PRODUCT_SCHEMA, getClass()),
				new ClassPathResource(SIZE_SCHEMA, getClass()));
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage();
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
		response.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		boolean result = interceptor.handleResponse(context);
		Assert.assertTrue("Invalid response from interceptor", result);
	}

	@Test
	public void testXsdSchema() throws Exception {
		PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
		SimpleXsdSchema schema = new SimpleXsdSchema(new ClassPathResource(SCHEMA, getClass()));
		schema.afterPropertiesSet();
		interceptor.setXsdSchema(schema);
		interceptor.setValidateRequest(true);
		interceptor.setValidateResponse(true);
		interceptor.afterPropertiesSet();
		MockWebServiceMessage request = new MockWebServiceMessage();
		request.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
		context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		boolean result = interceptor.handleRequest(context);
		Assert.assertTrue("Invalid response from interceptor", result);
		Assert.assertFalse("Response set", context.hasResponse());
	}
}
