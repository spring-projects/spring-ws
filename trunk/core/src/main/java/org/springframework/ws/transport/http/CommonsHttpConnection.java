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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on Jakarta Commons HttpClient. Exposes a {@link
 * PostMethod}.
 *
 * @author Arjen Poutsma
 */
public class CommonsHttpConnection implements FaultAwareWebServiceConnection {

    private final HttpClient httpClient;

    private final PostMethod postMethod;

    private byte[] bufferedInput;

    public CommonsHttpConnection(HttpClient httpClient, PostMethod postMethod) {
        Assert.notNull(httpClient, "httpClient must not be null");
        Assert.notNull(postMethod, "postMethod must not be null");
        this.httpClient = httpClient;
        this.postMethod = postMethod;
    }

    /** Returns the wrapped <code>PostMethod</code>. */
    public HttpMethod getPostMethod() {
        return postMethod;
    }

    public void close() throws IOException {
        postMethod.releaseConnection();
    }

    public TransportOutputStream getTransportOutputStream() {
        return new CommonsHttpTransportOutputStream();
    }

    public TransportInputStream getTransportInputStream() throws IOException {
        return getContentLength() > 0 ? new CommonsHttpTransportInputStream() : null;
    }

    public boolean hasFault() throws IOException {
        return postMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    private long getContentLength() throws IOException {
        if (postMethod.getResponseContentLength() != -1) {
            return postMethod.getResponseContentLength();
        }
        else if (bufferedInput != null) {
            return bufferedInput.length;
        }
        else {
            bufferedInput = FileCopyUtils.copyToByteArray(getInputStream());
            return bufferedInput.length;
        }
    }

    private InputStream getInputStream() throws IOException {
        if (postMethod.getStatusCode() != HttpStatus.SC_INTERNAL_SERVER_ERROR &&
                postMethod.getStatusCode() / 100 != 2) {
            throw new HttpTransportException("Did not receive successful HTTP response: status code = " +
                    postMethod.getStatusCode() + ", status message = [" + postMethod.getStatusText() + "]");
        }
        return postMethod.getResponseBodyAsStream();
    }

    /**
     * Implementation of {@link TransportInputStream} based on the {@link PostMethod} field.
     *
     * @see CommonsHttpConnection#postMethod
     */
    class CommonsHttpTransportInputStream extends TransportInputStream {

        protected InputStream createInputStream() throws IOException {
            if (bufferedInput != null) {
                return new ByteArrayInputStream(bufferedInput);
            }
            else {
                return getInputStream();
            }
        }

        public Iterator getHeaderNames() throws IOException {
            Header[] headers = postMethod.getResponseHeaders();
            String[] names = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                names[i] = headers[i].getName();
            }
            return Arrays.asList(names).iterator();
        }

        public Iterator getHeaders(String name) throws IOException {
            Header[] headers = postMethod.getResponseHeaders(name);
            String[] values = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                values[i] = headers[i].getValue();
            }
            return Arrays.asList(values).iterator();
        }
    }

    /**
     * Implementation of {@link TransportOutputStream} based on the {@link PostMethod} field.
     *
     * @see CommonsHttpConnection#postMethod
     */
    class CommonsHttpTransportOutputStream extends TransportOutputStream {

        private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        public void addHeader(String name, String value) throws IOException {
            postMethod.addRequestHeader(name, value);
        }

        protected OutputStream createOutputStream() throws IOException {
            return bos;
        }

        public void close() throws IOException {
            super.close();
            postMethod.setRequestEntity(new ByteArrayRequestEntity(bos.toByteArray()));
            httpClient.executeMethod(postMethod);
        }

    }


}
