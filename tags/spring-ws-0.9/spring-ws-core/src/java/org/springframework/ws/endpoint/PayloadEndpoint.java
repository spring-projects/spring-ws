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

package org.springframework.ws.endpoint;

import javax.xml.transform.Source;

/**
 * Defines the basic contract for Web Services interested in the mesage payload.
 * <p/>
 * The main entrypoint is <code>invoke</code>, which gets invoked with the contents of the requesting message.
 *
 * @author Arjen Poutsma
 * @see #invoke(javax.xml.transform.Source)
 */
public interface PayloadEndpoint {

    /**
     * Invokes an operation.
     *
     * @param request the request message
     * @return the response message, may be <code>null</code>
     * @throws Exception if an exception occurs
     */
    Source invoke(Source request) throws Exception;
}
