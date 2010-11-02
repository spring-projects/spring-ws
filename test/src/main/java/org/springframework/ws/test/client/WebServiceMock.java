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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.support.PayloadDiffMatcher;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;

/**
 * <strong>Main entry point for client-side Web service testing</strong>. Typically used to mock a {@link
 * WebServiceTemplate}, set up expectations on request messages, and create response messages.
 * <p/>
 * The typical usage of this mock is similar to any other mocking library (such as EasyMock), that is: <ol>
 * <li>Statically import {@link org.springframework.ws.test.client.WebServiceMock org.springframework.ws.mock.client.WebServiceMock.*}.
 * <li>Use the {@link #mockWebServiceTemplate(WebServiceTemplate)} method to mock a web service template. Typically,
 * this template is configured as a Spring bean, either explicitly or as a property of a class that extends {@link
 * org.springframework.ws.client.core.support.WebServiceGatewaySupport WebServiceGatewaySupport}.</li> <li>Set up
 * expectations on the outgoing request message by calling {@link #expect(RequestMatcher)} and {@link #payload(Source)},
 * {@link #connectionTo(String)}, {@link #xpath(String)}, or any of the other {@linkplain RequestMatcher request
 * matcher} methods. Multiple expectations can be set up by calling {@link ResponseActions#andExpect(RequestMatcher)
 * andExpect(RequestMatcher)}.</li> <li>Indicate the desired response actions by calling {@link
 * ResponseActions#andRespond(ResponseCreator) andRespond(ResponseCreator)}. See {@link #withPayload(Source)}, {@link
 * #withError(String)}, {@link #withClientOrSenderFault(String, Locale)}, or any of the other {@linkplain
 * ResponseCreator response creator} methods.</li> <li>Use the {@code WebServiceTemplate} as normal, either directly of
 * through client code. <li>Call {@link #verifyConnections()}. </ol> Note that because of the 'fluent' API offered by
 * this class, you can typically use the Code Completion features (i.e. ctrl-space) in your IDE to set up the mocks.
 * <p/>
 * For example:
 * <blockquote><pre>
 * import org.junit.Before;
 * import org.junit.Test;
 * import org.junit.runner.RunWith;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.test.context.ContextConfiguration;
 * import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 * import org.springframework.xml.transform.StringSource;
 * <strong>import static org.springframework.ws.mock.client.WebServiceMock.*</strong>;
 * <p/>
 * <p/>
 * &#064;RunWith(SpringJUnit4ClassRunner.class)
 * &#064;ContextConfiguration("applicationContext.xml")
 * public class IntegrationTest {
 * <p/>
 *   // MyWebServiceClient extends WebServiceGatewaySupport, and is configured in applicationContext.xml
 *   &#064;Autowired
 *   private MyWebServiceClient client;
 * <p/>
 *   &#064;Before
 *   public void setUpMocks() throws Exception {
 *     <strong>mockWebServiceTemplate(client.getWebServiceTemplate())</strong>;
 *   }
 * <p/>
 *   &#064;Test
 *   public void getCustomerCount() throws Exception {
 *     Source requestPayload =
 *       new StringSource("&lt;customerCountRequest xmlns='http://springframework.org/spring-ws/test' /&gt;";
 *     Source responsePayload = new StringSource("&lt;customerCountResponse xmlns='http://springframework.org/spring-ws/test'&gt;"
 * +
 *       "&lt;customerCount&gt;10&lt;/customerCount&gt;" +
 *       "&lt;/customerCountResponse&gt;");
 * <p/>
 *     <strong>expect(payload(requestPayload)).andRespond(withPayload(responsePayload));</strong>
 * <p/>
 *     // client.getCustomerCount() uses the WebServiceTemplate
 *     int customerCount = client.getCustomerCount();
 *     assertEquals(10, response.getCustomerCount());
 * <p/>
 *     <strong>verifyConnections();</strong>
 *   }
 * }
 * </pre></blockquote>
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public abstract class WebServiceMock {

    /**
     * Mocks the given {@link WebServiceTemplate}. Typically done in a setup method of the test class.
     *
     * @param webServiceTemplate the template to mock.
     */
    public static void mockWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        Assert.notNull(webServiceTemplate, "'webServiceTemplate' must not be null");

        MockWebServiceMessageSender mockMessageSender = new MockWebServiceMessageSender();
        MockWebServiceMessageSenderHolder.set(mockMessageSender);

        webServiceTemplate.setMessageSender(new ThreadLocalMockWebServiceMessageSender());
    }

    /**
     * Records an expectation specified by the given {@link RequestMatcher}. Returns a {@link ResponseActions} object
     * that allows for setting up the response, or more expectations.
     *
     * @param requestMatcher the request matcher expected
     * @return the response actions
     */
    public static ResponseActions expect(RequestMatcher requestMatcher) {
        MockWebServiceMessageSender messageSender = MockWebServiceMessageSenderHolder.get();
        Assert.state(messageSender != null,
                "WebServiceTemplate has not been mocked. Did you call mockWebServiceTemplate() ?");

        MockSenderConnection connection = messageSender.expectNewConnection();
        connection.addRequestMatcher(requestMatcher);
        return connection;
    }

    // RequestMatchers

    /**
     * Expects any request.
     *
     * @return the request matcher
     */
    public static RequestMatcher anything() {
        return new RequestMatcher() {
            public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
            }
        };
    }

    /**
     * Expects the given {@link Source} XML payload.
     *
     * @param payload the XML payload
     * @return the request matcher
     */
    public static RequestMatcher payload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return createPayloadDiffMatcher(payload);
    }

    /**
     * Expects the given {@link Resource} XML payload.
     *
     * @param payload the XML payload
     * @return the request matcher
     */
    public static RequestMatcher payload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return createPayloadDiffMatcher(createResourceSource(payload));
    }

    private static RequestMatcher createPayloadDiffMatcher(Source payload) {
        final PayloadDiffMatcher matcher = new PayloadDiffMatcher(payload);
        return new RequestMatcher() {
            public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
                matcher.match(request);
            }
        };
    }

    /**
     * Expects the payload to validate against the given XSD schema(s).
     *
     * @param schema         the schema
     * @param furtherSchemas further schemas, if necessary
     * @return the request matcher
     */
    public static RequestMatcher validPayload(Resource schema, Resource... furtherSchemas) {
        try {
            Resource[] joinedSchemas = new Resource[furtherSchemas.length + 1];
            joinedSchemas[0] = schema;
            System.arraycopy(furtherSchemas, 0, joinedSchemas, 1, furtherSchemas.length);
            XmlValidator validator =
                    XmlValidatorFactory.createValidator(joinedSchemas, XmlValidatorFactory.SCHEMA_W3C_XML);
            return new SchemaValidatingRequestMatcher(validator);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Schema(s) could not be opened", ex);
        }
    }

    /**
     * Expects the given XPath expression to (not) exist or be evaluated to a value.
     *
     * @param xpathExpression the XPath expression
     * @return the XPath expectations, to be further configured
     */
    public static XPathExpectations xpath(String xpathExpression) {
        return new DefaultXPathExpectations(xpathExpression, null);
    }

    /**
     * Expects the given XPath expression to (not) exist or be evaluated to a value.
     *
     * @param xpathExpression  the XPath expression
     * @param namespaceMapping the namespaces
     * @return the XPath expectations, to be further configured
     */
    public static XPathExpectations xpath(String xpathExpression, Map<String, String> namespaceMapping) {
        return new DefaultXPathExpectations(xpathExpression, namespaceMapping);
    }

    /**
     * Expects the given SOAP header in the outgoing message.
     *
     * @param soapHeaderName the qualified name of the SOAP header to expect
     * @return the request matcher
     */
    public static RequestMatcher soapHeader(QName soapHeaderName) {
        Assert.notNull(soapHeaderName, "'soapHeaderName' must not be null");
        return new SoapHeaderMatcher(soapHeaderName);
    }

    /**
     * Expects a connection to the given URI.
     *
     * @param uri the String uri expected to connect to
     * @return the request matcher
     */
    public static RequestMatcher connectionTo(String uri) {
        Assert.notNull(uri, "'uri' must not be null");
        return connectionTo(URI.create(uri));
    }

    /**
     * Expects a connection to the given URI.
     *
     * @param uri the String uri expected to connect to
     * @return the request matcher
     */
    public static RequestMatcher connectionTo(URI uri) {
        Assert.notNull(uri, "'uri' must not be null");
        return new UriMatcher(uri);
    }

    // ResponseCreators

    /**
     * Respond with the given {@link Source} XML as payload response.
     *
     * @param payload the response payload
     * @return the response callback
     */
    public static ResponseCreator withPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadResponseCreator(payload);
    }

    /**
     * Respond with the given {@link Resource} XML as payload response.
     *
     * @param payload the response payload
     * @return the response callback
     */
    public static ResponseCreator withPayload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadResponseCreator(createResourceSource(payload));
    }

    /**
     * Respond with an error.
     *
     * @param errorMessage the error message
     * @return the response callback
     * @see org.springframework.ws.transport.WebServiceConnection#hasError()
     * @see org.springframework.ws.transport.WebServiceConnection#getErrorMessage()
     */
    public static ResponseCreator withError(String errorMessage) {
        Assert.hasLength(errorMessage, "'errorMessage' must not be empty");
        return new ErrorResponseCreator(errorMessage);
    }

    /**
     * Respond with an {@link IOException}.
     *
     * @param ioException the exception to be thrown
     * @return the response callback
     */
    public static ResponseCreator withException(IOException ioException) {
        Assert.notNull(ioException, "'ioException' must not be null");
        return new ExceptionResponseCreator(ioException);
    }

    /**
     * Respond with an {@link RuntimeException}.
     *
     * @param ex the runtime exception to be thrown
     * @return the response callback
     */
    public static ResponseCreator withException(RuntimeException ex) {
        Assert.notNull(ex, "'ex' must not be null");
        return new ExceptionResponseCreator(ex);
    }

    /**
     * Respond with a {@code MustUnderstand} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addMustUnderstandFault(String, Locale)
     */
    public static ResponseCreator withMustUnderstandFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createMustUnderstandFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code Client} (SOAP 1.1) or {@code Sender} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, Locale)
     */
    public static ResponseCreator withClientOrSenderFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createClientOrSenderFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code Server} (SOAP 1.1) or {@code Receiver} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addServerOrReceiverFault(String, Locale)
     */
    public static ResponseCreator withServerOrReceiverFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createServerOrReceiverFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code VersionMismatch} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addVersionMismatchFault(String, Locale)
     */
    public static ResponseCreator withVersionMismatchFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createVersionMismatchFault(faultStringOrReason, locale);
    }

    // Verification

    /**
     * Verifies that all connections were used.
     *
     * @throws AssertionError in case of unused connections.
     */
    public static void verifyConnections() {
        MockWebServiceMessageSender messageSender = MockWebServiceMessageSenderHolder.get();
        if (messageSender != null) {
            messageSender.verifyConnections();
        }
    }

    private static ResourceSource createResourceSource(Resource resource) {
        try {
            return new ResourceSource(resource);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException(resource + " could not be opened", ex);
        }
    }

}
