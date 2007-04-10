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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.springframework.ws.transport.AbstractWebServiceConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Abstract base class for {@link WebServiceConnection} implementations that use HTTP.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractHttpWebServiceConnection extends AbstractWebServiceConnection
        implements FaultAwareWebServiceConnection {

    protected static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

    protected static final String ENCODING_GZIP = "gzip";

    protected static final int HTTP_STATUS_INTERNAL_ERROR = 500;

    protected final TransportOutputStream createTransportOutputStream() throws IOException {
        return new HttpClientTransportOutputStream();
    }

    protected final boolean hasResponse() throws IOException {
        return getResponseContentLength() > 0;
    }

    protected final TransportInputStream createTransportInputStream() throws IOException {
        return new HttpClientTransportInputStream();
    }

    public final boolean hasFault() throws IOException {
        return getResponseCode() == HTTP_STATUS_INTERNAL_ERROR;
    }

    private InputStream getUncompressedResponseInputStream() throws IOException {
        return isGzipResponse() ? new GZIPInputStream(getResponseInputStream()) : getResponseInputStream();
    }

    /** Determine whether the given response is a GZIP response. */
    private boolean isGzipResponse() throws IOException {
        for (Iterator iterator = getResponseHeaders(HTTP_HEADER_CONTENT_ENCODING); iterator.hasNext();) {
            String encodingHeader = (String) iterator.next();
            return encodingHeader.toLowerCase().indexOf(ENCODING_GZIP) != -1;
        }
        return false;
    }

    protected abstract void addRequestHeader(String name, String value) throws IOException;

    protected abstract OutputStream getRequestOutputStream() throws IOException;

    protected abstract void open() throws IOException;

    protected abstract int getResponseCode() throws IOException;

    protected abstract long getResponseContentLength() throws IOException;

    protected abstract Iterator getResponseHeaderNames() throws IOException;

    protected abstract Iterator getResponseHeaders(String name) throws IOException;

    protected abstract InputStream getResponseInputStream() throws IOException;

    /** Implementation of {@link TransportOutputStream} for client-side HTTP. */
    class HttpClientTransportOutputStream extends TransportOutputStream {

        public void addHeader(String name, String value) throws IOException {
            addRequestHeader(name, value);
        }

        protected OutputStream createOutputStream() throws IOException {
            return getRequestOutputStream();
        }

        public void close() throws IOException {
            super.close();
            open();
        }
    }

    /** Implementation of {@link TransportInputStream} for client-side HTTP. */
    class HttpClientTransportInputStream extends TransportInputStream {

        protected InputStream createInputStream() throws IOException {
            return getUncompressedResponseInputStream();
        }

        public Iterator getHeaderNames() throws IOException {
            return getResponseHeaderNames();
        }

        public Iterator getHeaders(String name) throws IOException {
            return getResponseHeaders(name);
        }

    }


}
