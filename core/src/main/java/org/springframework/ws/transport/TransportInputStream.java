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

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * A <code>TransportInputStream</code> is an input stream with MIME input headers. It is used to construct {@link
 * org.springframework.ws.WebServiceMessage WebServiceMessages} from a transport.
 *
 * @author Arjen Poutsma
 * @see #getHeaderNames()
 * @see #getHeaders(String)
 */
public abstract class TransportInputStream extends InputStream {

    private InputStream inputStream;

    protected TransportInputStream() {
    }

    private InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = createInputStream();
        }
        return inputStream;
    }

    public void close() throws IOException {
        getInputStream().close();
    }

    public int available() throws IOException {
        return getInputStream().available();
    }

    public synchronized void mark(int readlimit) {
        try {
            getInputStream().mark(readlimit);
        }
        catch (IOException e) {
        }
    }

    public boolean markSupported() {
        try {
            return getInputStream().markSupported();
        }
        catch (IOException e) {
            return false;
        }
    }

    public int read(byte b[]) throws IOException {
        return getInputStream().read(b);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return getInputStream().read(b, off, len);
    }

    public synchronized void reset() throws IOException {
        getInputStream().reset();
    }

    public long skip(long n) throws IOException {
        return getInputStream().skip(n);
    }

    public int read() throws IOException {
        return getInputStream().read();
    }

    /**
     * Returns the input stream to read from.
     */
    protected abstract InputStream createInputStream() throws IOException;

    /**
     * Returns an iteration over all the header names this request contains. Returns an empty <code>Iterator</code> if
     * the request has no headers.
     */
    public abstract Iterator getHeaderNames() throws IOException;

    /**
     * Returns an iteration over all the string values of the specified request header. Returns an empty
     * <code>Iterator</code> if the request did not include any headers of the specified name.
     */
    public abstract Iterator getHeaders(String name) throws IOException;
}
