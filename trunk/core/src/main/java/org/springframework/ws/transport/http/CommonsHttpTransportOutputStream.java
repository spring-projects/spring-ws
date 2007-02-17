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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * Implementation of the <code>TransportOutputStream</code> interface based on {@link
 * org.apache.commons.httpclient.methods.PostMethod}. Exposes the <code>PostMethod</code>.
 *
 * @author Arjen Poutsma
 */
public class CommonsHttpTransportOutputStream extends TransportOutputStream {

    private final PostMethod postMethod;

    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    /**
     * Constructs a new instance of the <code>CommonsHttpTransportOutputStream</code> with a given
     * <code>PostMethod</code>.
     */
    public CommonsHttpTransportOutputStream(PostMethod postMethod) {
        this.postMethod = postMethod;
    }

    public void addHeader(String name, String value) throws IOException {
        postMethod.addRequestHeader(name, value);
    }

    public void close() throws IOException {
        postMethod.setRequestEntity(new ByteArrayRequestEntity(bos.toByteArray()));
    }

    protected OutputStream createOutputStream() throws IOException {
        return bos;
    }

    /**
     * Returns the wrapped <code>PostMethod</code>.
     */
    public HttpMethod getPostMethod() {
        return postMethod;
    }

}
