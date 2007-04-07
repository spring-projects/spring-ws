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
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

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
public class CommonsHttpMessageSender extends AbstractHttpWebServiceMessageSender implements DisposableBean {

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
     * Create a new instance of the <code>CommonsHttpMessageSender</code> with the given <code>HttpClient</code>
     * instance.
     *
     * @param httpClient the HttpClient instance to use for this sender
     */
    public CommonsHttpMessageSender(HttpClient httpClient) {
        Assert.notNull(httpClient, "httpClient must not be null");
        this.httpClient = httpClient;
    }

    public void destroy() throws Exception {
        HttpConnectionManager connectionManager = httpClient.getHttpConnectionManager();
        if (connectionManager instanceof MultiThreadedHttpConnectionManager) {
            ((MultiThreadedHttpConnectionManager) connectionManager).shutdown();
        }
    }

    /** Returns the <code>HttpClient</code> used by this message sender. */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /** Set the <code>HttpClient</code> used by this message sender. */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public WebServiceConnection createConnection() throws IOException {
        PostMethod method = new PostMethod(getUrl().toString());
        return new CommonsHttpConnection(getHttpClient(), method);
    }

}
