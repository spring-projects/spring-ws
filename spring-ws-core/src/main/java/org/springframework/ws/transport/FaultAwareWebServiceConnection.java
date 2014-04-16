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

import org.springframework.ws.soap.SoapFault;

/**
 * Sub-interface of {@link WebServiceConnection} that is aware of any Fault messages received. Fault messages (such as
 * {@link SoapFault} SOAP Faults) often require different processing rules. Typically, fault detection is done by
 * inspecting connection error codes, etc.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface FaultAwareWebServiceConnection extends WebServiceConnection {

    /**
     * Indicates whether this connection received a fault.
     *
     * <p>Typically implemented by looking at an HTTP status code.
     *
     * @return {@code true} if this connection received a fault; {@code false} otherwise.
     * @throws IOException in case of I/O errors
     */
    boolean hasFault() throws IOException;

    /**
     * Sets whether this connection will send a fault.
     *
     * <p>Typically implemented by setting an HTTP status code.
     *
     * @param fault {@code true} if this will send a fault; {@code false} otherwise.
     * @throws IOException in case of I/O errors
     */
    void setFault(boolean fault) throws IOException;
}
