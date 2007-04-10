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
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.springframework.ws.transport.AbstractSendingWebServiceConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Abstract base class for {@link WebServiceConnection} implementations that send request over HTTP.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractHttpSendingWebServiceConnection extends AbstractSendingWebServiceConnection
        implements FaultAwareWebServiceConnection {

    protected static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

    protected static final String ENCODING_GZIP = "gzip";

    protected static final int HTTP_STATUS_INTERNAL_ERROR = 500;

    protected final boolean hasResponse() throws IOException {
        return getResponseContentLength() > 0;
    }

    public final boolean hasFault() throws IOException {
        return getResponseCode() == HTTP_STATUS_INTERNAL_ERROR;
    }

    protected final InputStream getResponseInputStream() throws IOException {
        return isGzipResponse() ? new GZIPInputStream(getRawResponseInputStream()) : getRawResponseInputStream();
    }

    /** Determine whether the given response is a GZIP response. */
    private boolean isGzipResponse() throws IOException {
        for (Iterator iterator = getResponseHeaders(HTTP_HEADER_CONTENT_ENCODING); iterator.hasNext();) {
            String encodingHeader = (String) iterator.next();
            return encodingHeader.toLowerCase().indexOf(ENCODING_GZIP) != -1;
        }
        return false;
    }

    /** Returns the HTTP status code of the response. */
    protected abstract int getResponseCode() throws IOException;

    /** Returns the length of the response. */
    protected abstract long getResponseContentLength() throws IOException;

    protected abstract InputStream getRawResponseInputStream() throws IOException;


}
