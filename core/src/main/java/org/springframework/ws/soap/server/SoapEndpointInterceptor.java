/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap.server;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;

/**
 * SOAP-specific extension of the <code>EndpointInterceptor</code> interface. Allows for handling of SOAP faults, which
 * are considered different from regular responses.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapEndpointInterceptor extends EndpointInterceptor {

    /**
     * Given a <code>SoapHeaderElement</code>, return whether or not this <code>SoapEndpointInterceptor</code>
     * understands it.
     *
     * @param header the header
     * @return <code>true</code> if understood, <code>false</code> otherwise
     */
    boolean understands(SoapHeaderElement header);

}
