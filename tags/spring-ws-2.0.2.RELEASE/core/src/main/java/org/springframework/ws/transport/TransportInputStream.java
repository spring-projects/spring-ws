/*
 * Copyright 2005-2010 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * A <code>TransportInputStream</code> is an input stream with MIME input headers. It is used to construct {@link
 * org.springframework.ws.WebServiceMessage WebServiceMessages} from a transport.
 *
 * @author Arjen Poutsma
 * @see #getHeaderNames()
 * @see #getHeaders(String)
 * @since 1.0.0
 */
public abstract class TransportInputStream extends InputStream {

    private InputStream inputStream;

    protected TransportInputStream() {
    }

    private InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = createInputStream();
            Assert.notNull(inputStream, "inputStream must not be null");
        }
        return inputStream;
    }

    @Override
    public void close() throws IOException {
        getInputStream().close();
    }

    @Override
    public int available() throws IOException {
        return getInputStream().available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            getInputStream().mark(readlimit);
        }
        catch (IOException e) {
            // ignored
        }
    }

    @Override
    public boolean markSupported() {
        try {
            return getInputStream().markSupported();
        }
        catch (IOException e) {
            return false;
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        return getInputStream().read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return getInputStream().read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        getInputStream().reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return getInputStream().skip(n);
    }

    @Override
    public int read() throws IOException {
        return getInputStream().read();
    }

    /** Returns the input stream to read from. */
    protected abstract InputStream createInputStream() throws IOException;

    /**
     * Returns an iteration over all the header names this stream contains. Returns an empty <code>Iterator</code> if
     * there are no headers.
     */
    public abstract Iterator<String> getHeaderNames() throws IOException;

    /**
     * Returns an iteration over all the string values of the specified header. Returns an empty <code>Iterator</code>
     * if there are no headers of the specified name.
     */
    public abstract Iterator<String> getHeaders(String name) throws IOException;
}
