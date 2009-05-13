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
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Abstract base class for {@link WebServiceConnection} implementations that send request over HTTP.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractHttpSenderConnection extends AbstractSenderConnection
        implements FaultAwareWebServiceConnection {

    /** Buffer used for reading the response, when the content length is invalid. */
    private byte[] responseBuffer;

    public final boolean hasError() throws IOException {
        return getResponseCode() / 100 != 2;
    }

    public final String getErrorMessage() throws IOException {
        StringBuffer buffer = new StringBuffer();
        String responseMessage = getResponseMessage();
        if (StringUtils.hasLength(responseMessage)) {
            buffer.append(responseMessage);
        }
        buffer.append(" [");
        buffer.append(getResponseCode());
        buffer.append(']');
        return buffer.toString();
    }

    /*
     * Receiving response
     */
    protected final boolean hasResponse() throws IOException {
        if (HttpTransportConstants.STATUS_ACCEPTED == getResponseCode()) {
            return false;
        }
        long contentLength = getResponseContentLength();
        if (contentLength < 0) {
            if (responseBuffer == null) {
                responseBuffer = FileCopyUtils.copyToByteArray(getRawResponseInputStream());
            }
            contentLength = responseBuffer.length;
        }
        return contentLength > 0;
    }

    protected final InputStream getResponseInputStream() throws IOException {
        InputStream inputStream;
        if (responseBuffer != null) {
            inputStream = new ByteArrayInputStream(responseBuffer);
        }
        else {
            inputStream = getRawResponseInputStream();
        }
        return isGzipResponse() ? new GZIPInputStream(inputStream) : inputStream;
    }

    /** Determine whether the response is a GZIP response. */
    private boolean isGzipResponse() throws IOException {
        Iterator iterator = getResponseHeaders(HttpTransportConstants.HEADER_CONTENT_ENCODING);
        if (iterator.hasNext()) {
            String encodingHeader = (String) iterator.next();
            return encodingHeader.toLowerCase().indexOf(HttpTransportConstants.CONTENT_ENCODING_GZIP) != -1;
        }
        return false;
    }

    /** Returns the HTTP status code of the response. */
    protected abstract int getResponseCode() throws IOException;

    /** Returns the HTTP status message of the response. */
    protected abstract String getResponseMessage() throws IOException;

    /** Returns the length of the response. */
    protected abstract long getResponseContentLength() throws IOException;

    /** Returns the raw, possibly compressed input stream to read the response from. */
    protected abstract InputStream getRawResponseInputStream() throws IOException;

    /*
     * Faults
     */

    public final boolean hasFault() throws IOException {
        return HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR == getResponseCode() && isXmlResponse();
    }

    /** Determine whether the response is a XML message. */
    private boolean isXmlResponse() throws IOException {
        Iterator iterator = getResponseHeaders(HttpTransportConstants.HEADER_CONTENT_TYPE);
        if (iterator.hasNext()) {
            String contentType = ((String) iterator.next()).toLowerCase();
            return contentType.indexOf("xml") != -1;
        }
        return false;
    }

    public final void setFault(boolean fault) {
    }

}
