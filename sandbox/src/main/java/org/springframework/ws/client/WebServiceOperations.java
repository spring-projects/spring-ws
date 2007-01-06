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

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;

/**
 * @author Arjen Poutsma
 */
public interface WebServiceOperations {

    /**
     * Sends a web service message that contains the given payload. Returns the payload of the response message, if
     * any.
     *
     * @param requestPayload the payload of the request message
     * @return the payload of the response message, or <code>null</code> if no response is given
     */
    Source sendAndReceive(Source requestPayload) throws IOException;

    /**
     * Sends a web service message that contains the given payload, marshalled by the configured
     * <code>Marshaller</code>. Returns the unmarshalled payload of the response message, if any.
     *
     * @param requestPayload the object to marshal into the request message payload
     * @return the unmarshalled payload of the response message, or <code>null</code> if no response is given
     */
    Object marshalAndSend(Object requestPayload) throws IOException;

    WebServiceMessage sendAndReceive(WebServiceMessageCallback callback) throws IOException;

    void sendAndReceive(Source requestPayload, Result result) throws IOException;
}
