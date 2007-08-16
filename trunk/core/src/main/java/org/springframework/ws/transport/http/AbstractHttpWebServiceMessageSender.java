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

import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Abstract base class for {@link org.springframework.ws.transport.WebServiceMessageSender} implementations that use
 * HTTP.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
public abstract class AbstractHttpWebServiceMessageSender implements WebServiceMessageSender {

    private boolean acceptGzipEncoding = true;

    protected static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    protected static final String ENCODING_GZIP = "gzip";

    protected static final String HTTP_SCHEME = "http://";

    protected static final String HTTPS_SCHEME = "https://";

    /**
     * Return whether to accept GZIP encoding, that is, whether to send the HTTP <code>Accept-Encoding</code> header
     * with <code>gzip</code> as value.
     */
    public boolean isAcceptGzipEncoding() {
        return acceptGzipEncoding;
    }

    /**
     * Set whether to accept GZIP encoding, that is, whether to send the HTTP <code>Accept-Encoding</code> header with
     * <code>gzip</code> as value.
     * <p/>
     * Default is <code>true</code>. Turn this flag off if you do not want GZIP response compression even if enabled on
     * the HTTP server.
     */
    public void setAcceptGzipEncoding(boolean acceptGzipEncoding) {
        this.acceptGzipEncoding = acceptGzipEncoding;
    }

    public boolean supports(String uri) {
        return StringUtils.hasLength(uri) && (uri.startsWith(HTTP_SCHEME) || uri.startsWith(HTTPS_SCHEME));
    }
}
