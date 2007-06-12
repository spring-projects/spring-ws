/*
 * Copyright 2007 the original author or authors.
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
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on Jakarta Commons HttpClient. Exposes a {@link
 * PostMethod}.
 *
 * @author Arjen Poutsma
 */
public class CommonsHttpConnection extends AbstractHttpSenderConnection {

    private final HttpClient httpClient;

    private final PostMethod postMethod;

    private ByteArrayOutputStream requestBuffer;

    protected CommonsHttpConnection(HttpClient httpClient, PostMethod postMethod) {
        Assert.notNull(httpClient, "httpClient must not be null");
        Assert.notNull(postMethod, "postMethod must not be null");
        this.httpClient = httpClient;
        this.postMethod = postMethod;
    }

    public PostMethod getPostMethod() {
        return postMethod;
    }

    public void close() throws IOException {
        postMethod.releaseConnection();
    }

    /*
     * Sending request
     */

    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        requestBuffer = new ByteArrayOutputStream();
    }

    protected void addRequestHeader(String name, String value) throws IOException {
        postMethod.addRequestHeader(name, value);
    }

    protected OutputStream getRequestOutputStream() throws IOException {
        return requestBuffer;
    }

    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        postMethod.setRequestEntity(new ByteArrayRequestEntity(requestBuffer.toByteArray()));
        requestBuffer = null;
        httpClient.executeMethod(postMethod);
    }

    /*
     * Receiving response
     */

    protected int getResponseCode() throws IOException {
        return postMethod.getStatusCode();
    }

    protected String getResponseMessage() throws IOException {
        return postMethod.getStatusText();
    }

    protected long getResponseContentLength() throws IOException {
        return postMethod.getResponseContentLength();
    }

    protected InputStream getRawResponseInputStream() throws IOException {
        return postMethod.getResponseBodyAsStream();
    }

    protected Iterator getResponseHeaderNames() throws IOException {
        Header[] headers = postMethod.getResponseHeaders();
        String[] names = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            names[i] = headers[i].getName();
        }
        return Arrays.asList(names).iterator();
    }

    protected Iterator getResponseHeaders(String name) throws IOException {
        Header[] headers = postMethod.getResponseHeaders(name);
        String[] values = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            values[i] = headers[i].getValue();
        }
        return Arrays.asList(values).iterator();
    }

}
