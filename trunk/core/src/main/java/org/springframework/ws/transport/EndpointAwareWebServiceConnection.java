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

import org.springframework.ws.NoEndpointFoundException;

/**
 * Sub-interface of {@link WebServiceConnection} that is aware of any server-side situations where an endpoint is not
 * found. Typically, this results in a special error codes.
 *
 * @author Arjen Poutsma
 * @see NoEndpointFoundException
 * @since 1.0
 */
public interface EndpointAwareWebServiceConnection extends WebServiceConnection {

    /** Called when an endpoint is not found. */
    void endpointNotFound();

}
