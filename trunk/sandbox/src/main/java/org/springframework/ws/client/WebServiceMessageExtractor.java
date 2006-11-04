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

package org.springframework.ws.client;

import org.springframework.ws.WebServiceMessage;

/**
 * @author Arjen Poutsma
 */
public interface WebServiceMessageExtractor {

    /**
     * Process the content in the given <code>WebServiceMessage</code>, creating a corresponding result object.
     *
     * @param message the message to extract data from
     * @return an arbitrary result object, or <code>null</code>  if none
     */
    Object extractData(WebServiceMessage message) throws Exception;
}
