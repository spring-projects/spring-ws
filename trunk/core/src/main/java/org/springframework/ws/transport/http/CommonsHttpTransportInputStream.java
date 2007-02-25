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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.util.Assert;
import org.springframework.ws.transport.TransportInputStream;

/**
 * Implementation of the <code>TransportInputStream</code> interface based on {@link
 * org.apache.commons.httpclient.methods.PostMethod}. Exposes the <code>PostMethod</code>.
 *
 * @author Arjen Poutsma
 */
class CommonsHttpTransportInputStream extends TransportInputStream {

    private final PostMethod postMethod;

    /**
     * Constructs a new instance of the <code>CommonsHttpTransportInputStream</code> with a given
     * <code>PostMethod</code>.
     */
    public CommonsHttpTransportInputStream(PostMethod postMethod) {
        Assert.notNull(postMethod, "postMethod must not be null");
        this.postMethod = postMethod;
    }

    protected InputStream createInputStream() throws IOException {
        return postMethod.getResponseBodyAsStream();
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
        String[] names = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            names[i] = headers[i].getValue();
        }
        return Arrays.asList(names).iterator();
    }

    /**
     * Returns the wrapped <code>PostMethod</code>.
     */
    public HttpMethod getPostMethod() {
        return postMethod;
    }
}
