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

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Abstract base class for {@link org.springframework.ws.transport.WebServiceMessageSender} implementations that use
 * HTTP.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractHttpWebServiceMessageSender implements WebServiceMessageSender {

    /**
     * Logger available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private boolean acceptGzipEncoding = true;

    /**
     * Return whether to accept GZIP encoding, that is, whether to send the HTTP {@code Accept-Encoding} header
     * with {@code gzip} as value.
     */
    public boolean isAcceptGzipEncoding() {
        return acceptGzipEncoding;
    }

    /**
     * Set whether to accept GZIP encoding, that is, whether to send the HTTP {@code Accept-Encoding} header with
     * {@code gzip} as value.
     *
     * <p>Default is {@code true}. Turn this flag off if you do not want GZIP response compression even if enabled on
     * the HTTP server.
     */
    public void setAcceptGzipEncoding(boolean acceptGzipEncoding) {
        this.acceptGzipEncoding = acceptGzipEncoding;
    }

    @Override
    public boolean supports(URI uri) {
        return uri.getScheme().equals(HttpTransportConstants.HTTP_URI_SCHEME) ||
                uri.getScheme().equals(HttpTransportConstants.HTTPS_URI_SCHEME);
    }
}
