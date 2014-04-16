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

package org.springframework.ws.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.EndpointAwareWebServiceConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on the Java 6 HttpServer {@link HttpExchange}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class HttpExchangeConnection extends AbstractReceiverConnection
        implements EndpointAwareWebServiceConnection, FaultAwareWebServiceConnection {

    private final HttpExchange httpExchange;

    private ByteArrayOutputStream responseBuffer;

    private int responseStatusCode = HttpTransportConstants.STATUS_ACCEPTED;

    private boolean chunkedEncoding;

    /** Constructs a new exchange connection with the given {@code HttpExchange}. */
    protected HttpExchangeConnection(HttpExchange httpExchange) {
        Assert.notNull(httpExchange, "'httpExchange' must not be null");
        this.httpExchange = httpExchange;
    }

    /** Returns the {@code HttpExchange} for this connection. */
    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    @Override
    public URI getUri() throws URISyntaxException {
        return httpExchange.getRequestURI();
    }

    void setChunkedEncoding(boolean chunkedEncoding) {
        this.chunkedEncoding = chunkedEncoding;
    }

    @Override
    public void endpointNotFound() {
        responseStatusCode = HttpTransportConstants.STATUS_NOT_FOUND;
    }

    /*
     * Errors
     */

    @Override
    public boolean hasError() throws IOException {
        return false;
    }

    @Override
    public String getErrorMessage() throws IOException {
        return null;
    }

    /*
     * Receiving request
     */

    @Override
    protected Iterator<String> getRequestHeaderNames() throws IOException {
        return httpExchange.getRequestHeaders().keySet().iterator();
    }

    @Override
    protected Iterator<String> getRequestHeaders(String name) throws IOException {
        List<String> headers = httpExchange.getRequestHeaders().get(name);
        return headers != null ? headers.iterator() : Collections.<String>emptyList().iterator();
    }

    @Override
    protected InputStream getRequestInputStream() throws IOException {
        return httpExchange.getRequestBody();
    }

    /*
     * Sending response
     */

    @Override
    protected void addResponseHeader(String name, String value) throws IOException {
        httpExchange.getResponseHeaders().add(name, value);
    }

    @Override
    protected OutputStream getResponseOutputStream() throws IOException {
        if (chunkedEncoding) {
            httpExchange.sendResponseHeaders(responseStatusCode, 0);
            return httpExchange.getResponseBody();
        }
        else {
            if (responseBuffer == null) {
                responseBuffer = new ByteArrayOutputStream();
            }
            return responseBuffer;
        }
    }

    @Override
    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        if (!chunkedEncoding) {
            byte[] buf = responseBuffer.toByteArray();
            httpExchange.sendResponseHeaders(responseStatusCode, buf.length);
            OutputStream responseBody = httpExchange.getResponseBody();
            FileCopyUtils.copy(buf, responseBody);
        }
        responseBuffer = null;
    }

    @Override
    public void onClose() throws IOException {
        if (responseStatusCode == HttpTransportConstants.STATUS_ACCEPTED ||
                responseStatusCode == HttpTransportConstants.STATUS_NOT_FOUND) {
            httpExchange.sendResponseHeaders(responseStatusCode, -1);
        }
        httpExchange.close();
    }

    /*
     * Faults
     */

    @Override
    public boolean hasFault() throws IOException {
        return responseStatusCode == HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR;
    }

    @Override
    public void setFault(boolean fault) throws IOException {
        if (fault) {
            responseStatusCode = HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR;
        }
        else {
            responseStatusCode = HttpTransportConstants.STATUS_OK;
        }
    }

}
