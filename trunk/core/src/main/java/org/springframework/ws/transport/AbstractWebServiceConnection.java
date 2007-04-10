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

package org.springframework.ws.transport;

import java.io.IOException;

/**
 * Abstract base class for {@link WebServiceConnection} implementations.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractWebServiceConnection implements WebServiceConnection {

    private TransportOutputStream tos;

    private TransportInputStream tis;

    public final TransportOutputStream getTransportOutputStream() throws IOException {
        if (tos == null) {
            tos = createTransportOutputStream();
        }
        return tos;
    }

    public final TransportInputStream getTransportInputStream() throws IOException {
        if (hasResponse()) {
            if (tis == null) {
                tis = createTransportInputStream();
            }
            return tis;
        }
        else {
            return null;
        }
    }

    /** Creates a new <code>TransportOutputStream</code>. The result is cached in a local variable. */
    protected abstract TransportOutputStream createTransportOutputStream() throws IOException;

    /** Creates a new <code>TransportInputStream</code>. The result is cached in a local variable. */
    protected abstract TransportInputStream createTransportInputStream() throws IOException;

    /** Indicates whether this connection has a response. */
    protected abstract boolean hasResponse() throws IOException;

}
