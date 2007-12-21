/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
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
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public class PayloadValidatingInterceptorTest extends XMLTestCase {

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

    protected void setUp() throws Exception {
        interceptor = new PayloadValidatingInterceptor();
        interceptor.setSchema(new ClassPathResource(SCHEMA, getClass()));
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        interceptor.afterPropertiesSet();

        soap11Factory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
        soap12Factory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL));
        transformer = TransformerFactory.newInstance().newTransformer();
    }

    public void testHandleInvalidRequestSoap11() throws Exception {
        SoapMessage invalidMessage = (SoapMessage) soap11Factory.createWebServiceMessage();
        InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
        transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
        context = new DefaultMessageContext(invalidMessage, soap11Factory);

        boolean result = interceptor.handleRequest(context, null);
        assertFalse("Invalid response from interceptor", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", PayloadValidatingInterceptor.DEFAULT_FAULTSTRING_OR_REASON,
                fault.getFaultStringOrReason());
        assertNotNull("No Detail on fault", fault.getFaultDetail());
    }

    public void testHandleInvalidRequestSoap12() throws Exception {
        SoapMessage invalidMessage = (SoapMessage) soap12Factory.createWebServiceMessage();
        InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
        transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
        context = new DefaultMessageContext(invalidMessage, soap12Factory);

        boolean result = interceptor.handleRequest(context, null);
        assertFalse("Invalid response from interceptor", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_12.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", PayloadValidatingInterceptor.DEFAULT_FAULTSTRING_OR_REASON,
                fault.getFaultReasonText(Locale.ENGLISH));
        assertNotNull("No Detail on fault", fault.getFaultDetail());
    }

    public void testHandleInvalidRequestOverridenProperties() throws Exception {
        String faultString = "fout";
        Locale locale = new Locale("nl");
        interceptor.setFaultStringOrReason(faultString);
        interceptor.setFaultStringOrReasonLocale(locale);
        interceptor.setAddValidationErrorDetail(false);

        SoapMessage invalidMessage = (SoapMessage) soap11Factory.createWebServiceMessage();
        InputStream inputStream = getClass().getResourceAsStream(INVALID_MESSAGE);
        transformer.transform(new StreamSource(inputStream), invalidMessage.getPayloadResult());
        context = new DefaultMessageContext(invalidMessage, soap11Factory);

        boolean result = interceptor.handleRequest(context, null);
        assertFalse("Invalid response from interceptor", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", faultString, fault.getFaultStringOrReason());
        assertEquals("Invalid fault string locale on fault", locale, fault.getFaultStringLocale());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testHandlerInvalidRequest() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage();
        request.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        boolean result = interceptor.handleRequest(context, null);
        assertFalse("Invalid response from interceptor", result);
    }

    public void testHandleValidRequest() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage();
        request.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        boolean result = interceptor.handleRequest(context, null);
        assertTrue("Invalid response from interceptor", result);
        assertFalse("Response set", context.hasResponse());
    }

    public void testHandleInvalidResponse() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage();
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
        boolean result = interceptor.handleResponse(context, null);
        assertFalse("Invalid response from interceptor", result);
    }

    public void testHandleValidResponse() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage();
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
        boolean result = interceptor.handleResponse(context, null);
        assertTrue("Invalid response from interceptor", result);
    }

    public void testNamespacesInType() throws Exception {
        // Make sure we use Xerces for this testcase: the JAXP implementation used internally by JDK 1.5 has a bug
        // See http://opensource.atlassian.com/projects/spring/browse/SWS-35
        String previousSchemaFactory =
                System.getProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI, "");
        System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            interceptor.setSchema(new ClassPathResource(SCHEMA2, PayloadValidatingInterceptorTest.class));
            interceptor.afterPropertiesSet();
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage saajMessage =
                    SaajUtils.loadMessage(new ClassPathResource(VALID_SOAP_MESSAGE, getClass()), messageFactory);
            context = new DefaultMessageContext(new SaajSoapMessage(saajMessage),
                    new SaajSoapMessageFactory(messageFactory));

            boolean result = interceptor.handleRequest(context, null);
            assertTrue("Invalid response from interceptor", result);
            assertFalse("Response set", context.hasResponse());
        }
        finally {
            // Reset the property
            System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
                    previousSchemaFactory);
        }
    }

    public void testNonExistingSchema() throws Exception {
        try {
            interceptor.setSchema(new ClassPathResource("invalid"));
            interceptor.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testHandlerInvalidRequestMultipleSchemas() throws Exception {
        interceptor.setSchemas(new Resource[]{new ClassPathResource(PRODUCT_SCHEMA, getClass()),
                new ClassPathResource(SIZE_SCHEMA, getClass())});
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(new ClassPathResource(INVALID_MESSAGE, getClass()));
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        boolean result = interceptor.handleRequest(context, null);
        assertFalse("Invalid response from interceptor", result);
    }

    public void testHandleValidRequestMultipleSchemas() throws Exception {
        interceptor.setSchemas(new Resource[]{new ClassPathResource(PRODUCT_SCHEMA, getClass()),
                new ClassPathResource(SIZE_SCHEMA, getClass())});
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(new ClassPathResource(VALID_MESSAGE, getClass()));
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        boolean result = interceptor.handleRequest(context, null);
        assertTrue("Invalid response from interceptor", result);
        assertFalse("Response set", context.hasResponse());
    }

    public void testHandleInvalidResponseMultipleSchemas() throws Exception {
        interceptor.setSchemas(new Resource[]{new ClassPathResource(PRODUCT_SCHEMA, getClass()),
                new ClassPathResource(SIZE_SCHEMA, getClass())});
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage();
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(new ClassPathResource(INVALID_MESSAGE, getClass()));
        boolean result = interceptor.handleResponse(context, null);
        assertFalse("Invalid response from interceptor", result);
    }

    public void testHandleValidResponseMultipleSchemas() throws Exception {
        interceptor.setSchemas(new Resource[]{new ClassPathResource(PRODUCT_SCHEMA, getClass()),
                new ClassPathResource(SIZE_SCHEMA, getClass())});
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage();
        context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(new ClassPathResource(VALID_MESSAGE, getClass()));
        boolean result = interceptor.handleResponse(context, null);
        assertTrue("Invalid response from interceptor", result);
    }

    public void testCreateRequestValidationFaultAxiom() throws Exception {
        LocatorImpl locator = new LocatorImpl();
        locator.setLineNumber(0);
        locator.setColumnNumber(0);
        SAXParseException[] exceptions = new SAXParseException[]{new SAXParseException("Message 1", locator),
                new SAXParseException("Message 2", locator),};
        MessageContext messageContext = new DefaultMessageContext(new AxiomSoapMessageFactory());
        interceptor.handleRequestValidationErrors(messageContext, exceptions);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        messageContext.getResponse().writeTo(os);
        assertXMLEqual("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Body>" + "<soapenv:Fault>" + "<faultcode>soapenv:Client</faultcode>" +
                "<faultstring xml:lang='en'>Validation error</faultstring>" + "<detail>" +
                "<spring-ws:ValidationError xmlns:spring-ws=\"http://springframework.org/spring-ws\">Message 1</spring-ws:ValidationError>" +
                "<spring-ws:ValidationError xmlns:spring-ws=\"http://springframework.org/spring-ws\">Message 2</spring-ws:ValidationError>" +
                "</detail>" + "</soapenv:Fault>" + "</soapenv:Body>" + "</soapenv:Envelope>", os.toString());

    }

}