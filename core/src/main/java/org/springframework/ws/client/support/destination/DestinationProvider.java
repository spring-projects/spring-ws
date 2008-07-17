/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.client.support.destination;

import java.net.URI;

/**
 * Strategy interface for providing a {@link org.springframework.ws.client.core.WebServiceTemplate} destination URI at
 * runtime.
 * <p/>
 * Typically implemented by providers that use WSDL, a UDDI registry, or some other form to determine the destination
 * URI.
 *
 * @author Arjen Poutsma
 * @since 1.5.4
 */
public interface DestinationProvider {

    /**
     * Return the destination URI.
     *
     * @return the destination URI
     */
    URI getUri();

}
