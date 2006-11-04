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

/**
 * Simple implementation of the <code>TransportContext</code> interface.
 *
 * @author Arjen Poutsma
 */
public class SimpleTransportContext implements TransportContext {

    private final TransportInputStream transportInputStream;

    private final TransportOutputStream transportOutputStream;

    /**
     * Creates a new <code>SimpleTransportContext</code> that exposes the given streams.
     */
    public SimpleTransportContext(TransportInputStream transportInputStream,
                                  TransportOutputStream transportOutputStream) {
        this.transportInputStream = transportInputStream;
        this.transportOutputStream = transportOutputStream;
    }

    public TransportInputStream getTransportInputStream() {
        return transportInputStream;
    }

    public TransportOutputStream getTransportOutputStream() {
        return transportOutputStream;
    }
}
