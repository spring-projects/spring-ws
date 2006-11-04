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
import java.io.OutputStream;

/**
 * A <code>TransportOutputStream</code> is an output stream with MIME input headers. It is used to write {@link
 * org.springframework.ws.WebServiceMessage WebServiceMessages} to a transport.
 *
 * @author Arjen Poutsma
 * @see #addHeader(String,String)
 */
public abstract class TransportOutputStream extends OutputStream {

    protected TransportOutputStream() {
    }

    public void close() throws IOException {
        getOutputStream().close();
    }

    public void flush() throws IOException {
        getOutputStream().flush();
    }

    public void write(byte b[]) throws IOException {
        getOutputStream().write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        getOutputStream().write(b, off, len);
    }

    public void write(int b) throws IOException {
        getOutputStream().write(b);
    }

    /**
     * Adds a response header with the given name and value. This method can be called multiple times, to allow for
     * headers with multiple values.
     *
     * @param name  the name of the header
     * @param value the value of the header
     */
    public abstract void addHeader(String name, String value) throws IOException;

    /**
     * Returns the output stream to write to.
     */
    protected abstract OutputStream getOutputStream() throws IOException;
}
