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

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Represents a point-to-point connection that a client can use for sending {@link WebServiceMessage} objects directly
 * to a remote party.
 * <p/>
 * A <code>WebServiceConnection</code> can be obtained using a {@link WebServiceMessageSender}.
 *
 * @author Arjen Poutsma
 * @see WebServiceMessageSender#createConnection()
 */
public interface WebServiceConnection {

    /**
     * Sends the given message and blocks until it has returned the response. The response message, if any, is stored in
     * the context.
     *
     * @param messageContext the context which contains the request message to be sent, and which will contain the
     *                       response afterwards
     * @throws IOException in case of I/O errors
     */
    void sendAndReceive(MessageContext messageContext) throws IOException;

    /**
     * Closes the <code>WebServiceConnection</code>.
     *
     * @throws IOException in case of I/O errors
     */
    void close() throws IOException;

}
