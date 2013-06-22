/*
 * Copyright 2002-2013 the original author or authors.
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
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation that uses <a href="http://hc.apache.org/httpcomponents-client">Apache
 * HttpClient</a> to execute POST requests.
 * <p/>
 * Allows to use a pre-configured HttpClient instance, potentially with authentication, HTTP connection pooling, etc.
 * Authentication can also be set by injecting a {@link Credentials} instance (such as the {@link
 * UsernamePasswordCredentials}).
 *
 * @author Alan Stewart
 * @author Barry Pitman
 * @author Arjen Poutsma
 * @see HttpClient
 * @since 2.1.0
 */
public class HttpComponentsMessageSender extends AbstractHttpWebServiceMessageSender
        implements InitializingBean, DisposableBean {

    private static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = (60 * 1000);

    private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (60 * 1000);

    private HttpClient httpClient;

    private Credentials credentials;

    private AuthScope authScope = AuthScope.ANY;

    /**
     * Create a new instance of the {@code HttpClientMessageSender} with a default {@link HttpClient} that uses a
     * default {@link SingleClientConnManager}.
     */
    public HttpComponentsMessageSender() {
        DefaultHttpClient defaultClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
        defaultClient.addRequestInterceptor(new RemoveSoapHeadersInterceptor(), 0);

        this.httpClient = defaultClient;
        setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS);
        setReadTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS);
    }

    /**
     * Create a new instance of the {@code HttpClientMessageSender} with the given
     * {@link HttpClient} instance.
     * <p>
     * This constructor does not change the given {@code HttpClient} in any way. As such,
     * it does not set timeouts, nor does it
     * {@linkplain DefaultHttpClient#addRequestInterceptor(org.apache.http.HttpRequestInterceptor) add}
     * the {@link RemoveSoapHeadersInterceptor}.
     *
     * @param httpClient the HttpClient instance to use for this sender
     */
    public HttpComponentsMessageSender(HttpClient httpClient) {
        Assert.notNull(httpClient, "httpClient must not be null");
        this.httpClient = httpClient;
    }

    /**
     * Sets the credentials to be used. If not set, no authentication is done.
     *
     * @see UsernamePasswordCredentials
     * @see org.apache.http.auth.NTCredentials
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Returns the <code>HttpClient</code> used by this message sender.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Set the {@code HttpClient} used by this message sender.
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the timeout until a connection is established. A value of 0 means <em>never</em> timeout.
     *
     * @param timeout the timeout value in milliseconds
     * @see org.apache.http.params.HttpConnectionParams#setConnectionTimeout(org.apache.http.params.HttpParams, int)
     */
    public void setConnectionTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be a non-negative value");
        }
        HttpConnectionParams.setConnectionTimeout(getHttpClient().getParams(), timeout);
    }

    /**
     * Set the socket read timeout for the underlying HttpClient. A value of 0 means <em>never</em> timeout.
     *
     * @param timeout the timeout value in milliseconds
     * @see org.apache.http.params.HttpConnectionParams#setSoTimeout(org.apache.http.params.HttpParams, int)
     */
    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be a non-negative value");
        }
        HttpConnectionParams.setSoTimeout(getHttpClient().getParams(), timeout);
    }

    /**
     * Sets the maximum number of connections allowed for the underlying HttpClient.
     *
     * @param maxTotalConnections the maximum number of connections allowed
     * @see ThreadSafeClientConnManager#setMaxTotal(int)
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
        if (maxTotalConnections <= 0) {
            throw new IllegalArgumentException("maxTotalConnections must be a positive value");
        }
        ClientConnectionManager connectionManager = getHttpClient().getConnectionManager();
        if (!(connectionManager instanceof ThreadSafeClientConnManager)) {
            throw new IllegalArgumentException("maxTotalConnections is not supported on " +
                    connectionManager.getClass().getName() + ". Use " + ThreadSafeClientConnManager.class.getName() +
                    " instead");
        }
        ((ThreadSafeClientConnManager) connectionManager).setMaxTotal(maxTotalConnections);
    }

    /**
     * Sets the maximum number of connections per host for the underlying HttpClient. The maximum number of connections
     * per host can be set in a form accepted by the {@code java.util.Properties} class, like as follows:
     * <p/>
     * <pre>
     * https://www.example.com=1
     * http://www.example.com:8080=7
     * http://www.springframework.org=10
     * </pre>
     * <p/>
     * The host can be specified as a URI (with scheme and port).
     *
     * @param maxConnectionsPerHost a properties object specifying the maximum number of connection
     * @see org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager#setMaxForRoute(org.apache.http.conn.routing.HttpRoute,
     *      int)
     */
    public void setMaxConnectionsPerHost(Map<String, String> maxConnectionsPerHost) throws URISyntaxException {
        ClientConnectionManager connectionManager = getHttpClient().getConnectionManager();
        if (!(connectionManager instanceof ThreadSafeClientConnManager)) {
            throw new IllegalArgumentException("maxConnectionsPerHost is not supported on " +
                    connectionManager.getClass().getName() + ". Use " + ThreadSafeClientConnManager.class.getName() +
                    " instead");
        }

        for (Object o : maxConnectionsPerHost.keySet()) {
            String host = (String) o;
            URI uri = new URI(host);
            HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            int maxHostConnections = Integer.parseInt(maxConnectionsPerHost.get(host));
            ((ThreadSafeClientConnManager) connectionManager)
                    .setMaxForRoute(new HttpRoute(httpHost), maxHostConnections);
        }
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
        if (credentials != null && getHttpClient() instanceof DefaultHttpClient) {
            ((DefaultHttpClient) getHttpClient()).getCredentialsProvider().setCredentials(authScope, credentials);
        }
    }

    public WebServiceConnection createConnection(URI uri) throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        if (isAcceptGzipEncoding()) {
            httpPost.addHeader(HttpTransportConstants.HEADER_ACCEPT_ENCODING,
                    HttpTransportConstants.CONTENT_ENCODING_GZIP);
        }
        HttpContext httpContext = createContext(uri);
        return new HttpComponentsConnection(getHttpClient(), httpPost, httpContext);
    }

    /**
     * Template method that allows for creation of a {@link HttpContext} for the given uri. Default implementation
     * returns {@code null}.
     *
     * @param uri the URI to create the context for
     * @return the context, or {@code null}
     */
    protected HttpContext createContext(URI uri) {
        return null;
    }

    public void destroy() throws Exception {
        getHttpClient().getConnectionManager().shutdown();
    }

    /**
     * HttpClient {@link org.apache.http.HttpRequestInterceptor} implementation that removes {@code Content-Length} and
     * {@code Transfer-Encoding} headers from the request. Necessary, because some SAAJ and other SOAP implementations set these
     * headers themselves, and HttpClient throws an exception if they have been set.
     */
    public static class RemoveSoapHeadersInterceptor implements HttpRequestInterceptor {

        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            if (request instanceof HttpEntityEnclosingRequest) {
                if (request.containsHeader(HTTP.TRANSFER_ENCODING)) {
                    request.removeHeaders(HTTP.TRANSFER_ENCODING);
                }
                if (request.containsHeader(HTTP.CONTENT_LEN)) {
                    request.removeHeaders(HTTP.CONTENT_LEN);
                }
            }
        }
    }
}