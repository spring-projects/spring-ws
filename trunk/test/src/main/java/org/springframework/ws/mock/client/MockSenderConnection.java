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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.StringSource;

import static org.junit.Assert.fail;

/**
 * Mock implementation of {@link WebServiceConnection}. Implements {@link RequestExpectations} and {@link
 * ResponseActions} to form a fluent API.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
class MockSenderConnection implements FaultAwareWebServiceConnection, RequestExpectations, ResponseActions {

    private static final URI ANY_URI = URI.create("ANY");

    private final List<RequestMatcher> requestMatchers = new LinkedList<RequestMatcher>();

    private URI uri;

    private ResponseCallback responseCallback;

    private String errorMessage;

    /** Creates a new {@code MockSenderConnection} for use with any URI. */
    MockSenderConnection() {
        this.uri = ANY_URI;
    }

    /** Creates a new {@code MockSenderConnection} for use with the specified URI. */
    MockSenderConnection(URI uri) {
        this.uri = uri;
    }

    // RequestExpectations implementation

    public ResponseActions expectPayload(String payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return addRequestMatcher(new PayloadMatcher(new StringSource(payload)));
    }

    public ResponseActions expectPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return addRequestMatcher(new PayloadMatcher(payload));
    }

    public ResponseActions expectPayload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        try {
            return addRequestMatcher(new PayloadMatcher(new ResourceSource(payload)));
        }
        catch (IOException ex) {
            throw new IllegalArgumentException(payload + " could not be opened", ex);
        }
    }

    public ResponseActions expectSoapHeader(QName soapHeaderName) {
        Assert.notNull(soapHeaderName, "'soapHeaderName' must not be null");
        return addRequestMatcher(new SoapHeaderMatcher(soapHeaderName));
    }

    public ResponseActions addRequestMatcher(RequestMatcher requestMatcher) {
        requestMatchers.add(requestMatcher);
        return this;
    }

    // ResponseActions implementation

    public RequestExpectations and() {
        return this;
    }

    public void andRespondWithPayload(Resource resource) {
        Assert.notNull(resource, "'resource' must not be null");
        try {
            this.responseCallback = new PayloadResponseCallback(resource);
        }
        catch (IOException ex) {
            fail("Could not open [" + resource + "]: " + ex.getMessage());
        }
    }

    public void andRespondWithPayload(String payload) {
        Assert.notNull(payload, "'payload' must not be null");
        this.responseCallback = new PayloadResponseCallback(payload);
    }

    public void andRespondWithError(String errorMessage) {
        Assert.hasLength(errorMessage, "'errorMessage' must not be empty");
        this.errorMessage = errorMessage;
    }

    public void andThrowException(IOException ioException) {
        Assert.notNull(ioException, "'ex' must not be null");
        this.responseCallback = new ExceptionResponseCallback(ioException);
    }

    public void andThrowException(RuntimeException ex) {
        Assert.notNull(ex, "'ex' must not be null");
        this.responseCallback = new ExceptionResponseCallback(ex);
    }

    public void andRespondWithMustUnderstandFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        this.responseCallback = SoapFaultResponseCallback.createMustUnderstandFault(faultStringOrReason, locale);
    }

    public void andRespondWithClientOrSenderFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        this.responseCallback = SoapFaultResponseCallback.createClientOrSenderFault(faultStringOrReason, locale);
    }

    public void andRespondWithServerOrReceiverFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        this.responseCallback = SoapFaultResponseCallback.createServerOrReceiverFault(faultStringOrReason, locale);
    }

    public void andRespondWithVersionMismatchFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        this.responseCallback = SoapFaultResponseCallback.createVersionMismatchFault(faultStringOrReason, locale);
    }

    public void setResponseCallback(ResponseCallback responseCallback) {
        Assert.notNull(responseCallback, "'responseCallback' must not be null");
        this.responseCallback = responseCallback;
    }

    // WebServiceConnection implementation

    public void send(WebServiceMessage message) throws IOException {
        if (!requestMatchers.isEmpty()) {
            for (RequestMatcher requestMatcher : requestMatchers) {
                requestMatcher.match(message);
            }
        }
        else {
            throw new AssertionError("Unexpected send() for [" + message + "]");
        }

    }

    public WebServiceMessage receive(WebServiceMessageFactory messageFactory) throws IOException {
        if (responseCallback != null) {
            WebServiceMessage response = messageFactory.createWebServiceMessage();
            responseCallback.doWithResponse(response);
            return response;
        }
        else {
            return null;
        }
    }

    public URI getUri() {
        return uri;
    }

    public boolean hasAnyUri() {
        return ANY_URI.equals(uri);
    }

    public boolean hasError() throws IOException {
        return errorMessage != null;
    }

    public String getErrorMessage() throws IOException {
        return errorMessage;
    }

    public boolean hasFault() throws IOException {
        return responseCallback instanceof SoapFaultResponseCallback;
    }

    public void setFault(boolean fault) throws IOException {
        // Do nothing
    }

    public void close() throws IOException {
        requestMatchers.clear();
        responseCallback = null;
        errorMessage = null;
        uri = null;
    }


}
