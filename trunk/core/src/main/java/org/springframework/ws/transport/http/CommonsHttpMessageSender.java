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
import java.net.URI;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * <code>WebServiceMessageSender</code> implementation that uses <a href="http://jakarta.apache.org/commons/httpclient">Jakarta
 * Commons HttpClient</a> to execute POST requests.
 * <p/>
 * Allows to use a preconfigured HttpClient instance, potentially with authentication, HTTP connection pooling, etc.
 * Authentication can also be set by injecting a {@link Credentials} instance (such as the {@link
 * UsernamePasswordCredentials}).
 *
 * @author Arjen Poutsma
 * @see HttpUrlConnectionMessageSender
 * @see HttpClient
 * @see #setCredentials(Credentials)
 * @since 1.0.0
 */
public class CommonsHttpMessageSender extends AbstractHttpWebServiceMessageSender
        implements InitializingBean, DisposableBean {

    private static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = (60 * 1000);

    private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (60 * 1000);

    private HttpClient httpClient;

    private Credentials credentials;

    private AuthScope authScope;

    /**
     * Create a new instance of the <code>CommonsHttpMessageSender</code> with a default {@link HttpClient} that uses a
     * default {@link MultiThreadedHttpConnectionManager}.
     */
    public CommonsHttpMessageSender() {
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS);
        setReadTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS);
    }

    /**
     * Create a new instance of the <code>CommonsHttpMessageSender</code> with the given  {@link HttpClient} instance.
     *
     * @param httpClient the HttpClient instance to use for this sender
     */
    public CommonsHttpMessageSender(HttpClient httpClient) {
        Assert.notNull(httpClient, "httpClient must not be null");
        this.httpClient = httpClient;
    }

    /** Returns the <code>HttpClient</code> used by this message sender. */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /** Set the <code>HttpClient</code> used by this message sender. */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /** Returns the credentials to be used. */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the credentials to be used. If not set, no authentication is done.
     *
     * @see UsernamePasswordCredentials
     * @see NTCredentials
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the timeout until a connection is etablished. A value of 0 means <em>never</em> timeout.
     *
     * @param timeout the timeout value in milliseconds
     * @see org.apache.commons.httpclient.params.HttpConnectionManagerParams#setConnectionTimeout(int)
     */
    public void setConnectionTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be a non-negative value");
        }
        getHttpClient().getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
    }

    /**
     * Set the socket read timeout for the underlying HttpClient. A value of 0 means <em>never</em> timeout.
     *
     * @param timeout the timeout value in milliseconds
     * @see org.apache.commons.httpclient.params.HttpConnectionManagerParams#setSoTimeout(int)
     */
    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be a non-negative value");
        }
        getHttpClient().getHttpConnectionManager().getParams().setSoTimeout(timeout);
    }

    /**
     * Returns the authentication scope to be used. Only used when the <code>credentials</code> property has been set.
     * <p/>
     * By default, the {@link AuthScope#ANY} is returned.
     */
    public AuthScope getAuthScope() {
        return authScope != null ? authScope : AuthScope.ANY;
    }

    /**
     * Sets the authentication scope to be used. Only used when the <code>credentials</code> property has been set.
     * <p/>
     * By default, the {@link AuthScope#ANY} is used.
     *
     * @see #setCredentials(Credentials)
     */
    public void setAuthScope(AuthScope authScope) {
        this.authScope = authScope;
    }

    public void afterPropertiesSet() throws Exception {
        if (getCredentials() != null) {
            getHttpClient().getState().setCredentials(getAuthScope(), getCredentials());
        }
    }

    public void destroy() throws Exception {
        HttpConnectionManager connectionManager = getHttpClient().getHttpConnectionManager();
        if (connectionManager instanceof MultiThreadedHttpConnectionManager) {
            ((MultiThreadedHttpConnectionManager) connectionManager).shutdown();
        }
    }

    public WebServiceConnection createConnection(URI uri) throws IOException {
        PostMethod postMethod = new PostMethod(uri.toString());
        if (isAcceptGzipEncoding()) {
            postMethod.addRequestHeader(HttpTransportConstants.HEADER_ACCEPT_ENCODING,
                    HttpTransportConstants.CONTENT_ENCODING_GZIP);
        }
        return new CommonsHttpConnection(getHttpClient(), postMethod);
    }

}

