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

package org.springframework.ws.transport.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.TransportInputStream;

/**
 * <code>WebServiceMessageSender</code> implementation that uses <a href="http://jakarta.apache.org/commons/httpclient">Jakarta
 * Commons HttpClient</a> to execute POST requests.
 * <p/>
 * Allows to use a preconfigured HttpClient instance, potentially with authentication, HTTP connection pooling, etc.
 * Also designed for easy subclassing, customizing specific template methods.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.transport.http.HttpUrlConnectionMessageSender
 */
public class CommonsHttpMessageSender extends AbstractHttpWebServiceMessageSender {

    private HttpClient httpClient;

    /**
     * Create a new instance of the <code>CommonsHttpMessageSender</code> with a default <code>HttpClient</code> that
     * uses a default <code>MultiThreadedHttpConnectionManager</code>.
     *
     * @see org.apache.commons.httpclient.HttpClient
     * @see org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
     */
    public CommonsHttpMessageSender() {
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    }

    /**
     * Returns the <code>HttpClient</code> used by this message sender.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Set the <code>HttpClient</code> used by this message sender.
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void sendAndReceive(MessageContext messageContext) throws IOException {
        PostMethod postMethod = createPostMethod();
        try {
            writeRequestMessage(postMethod, messageContext.getRequest());
            executePostMethod(getHttpClient(), postMethod);
            validateResponse(postMethod);
            readResponse(postMethod, messageContext);
        }
        finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Create a PostMethod for the given configuration.
     *
     * @return the PostMethod instance
     * @throws IOException if thrown by I/O methods
     */
    protected PostMethod createPostMethod() throws IOException {
        return new PostMethod(getUrl().toString());
    }

    /**
     * Set the given message as request body.
     * <p/>
     * The default implementation simply sets the message as the PostMethod's request body. This can be overridden, for
     * example, to write a specific encoding and potentially set appropriate HTTP request headers.
     *
     * @param postMethod the PostMethod to set the request message on
     * @param message    the request message to be set
     * @throws IOException if thrown by I/O methods
     */
    protected void writeRequestMessage(PostMethod postMethod, WebServiceMessage message) throws IOException {
        CommonsHttpTransportOutputStream tos = new CommonsHttpTransportOutputStream(postMethod);
        try {
            message.writeTo(tos);
            tos.flush();
        }
        finally {
            tos.close();
        }
    }

    /**
     * Execute the given PostMethod instance.
     *
     * @param httpClient the HttpClient to execute on
     * @param postMethod the PostMethod to execute
     * @throws IOException if thrown by I/O methods
     * @see org.apache.commons.httpclient.HttpClient#executeMethod(org.apache.commons.httpclient.HttpMethod)
     */
    protected void executePostMethod(HttpClient httpClient, PostMethod postMethod) throws IOException {
        httpClient.executeMethod(postMethod);
    }

    /**
     * Validate the given response as contained in the PostMethod object, throwing an exception if it does not
     * correspond to a successful HTTP response.
     * <p/>
     * Default implementation rejects any HTTP status code that does not start with 2, excluding 500 (Internal Server
     * Error, used for SOAP Faults).
     *
     * @param postMethod the executed PostMethod to validate
     * @throws IOException if validation failed
     * @see org.apache.commons.httpclient.methods.PostMethod#getStatusCode()
     */
    protected void validateResponse(PostMethod postMethod) throws IOException {
        int statusCode = postMethod.getStatusCode();
        if (statusCode != HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode / 100 != 2) {
            throw new IOException("Did not receive successful HTTP response: status code = " + statusCode +
                    ", status message = [" + postMethod.getStatusText() + "]");
        }

    }

    /**
     * Read the response from the given executed remote invocation request.
     *
     * @param postMethod the PostMethod to read the response message from
     * @return an InputStream for the response body
     * @throws IOException if thrown by I/O methods
     */
    protected void readResponse(PostMethod postMethod, MessageContext messageContext) throws IOException {
        if (postMethod.getStatusCode() == HttpStatus.SC_NO_CONTENT || postMethod.getResponseContentLength() == 0) {
            return;
        }
        TransportInputStream tis = new CommonsHttpTransportInputStream(postMethod);
        try {
            messageContext.readResponse(tis);
        }
        finally {
            if (tis != null) {
                tis.close();
            }
        }
    }
}
