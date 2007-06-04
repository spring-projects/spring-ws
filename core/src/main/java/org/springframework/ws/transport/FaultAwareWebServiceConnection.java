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
 * Sub-interface of {@link WebServiceConnection} that is aware of any SOAP Faults received. Typically, fault detection
 * is done by inspecting connection error codes, etc.
 *
 * @author Arjen Poutsma
 */
public interface FaultAwareWebServiceConnection extends WebServiceConnection {

    /**
     * Indicates whether this connection has a SOAP Fault.
     *
     * @return <code>true</code> if this connection has a fault; <code>false</code> otherwise.
     */
    boolean hasFault() throws IOException;

}
