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

package org.springframework.ws;

import java.io.IOException;
import java.io.InputStream;

/**
 * The <code>WebServiceMessageFactory</code> serves as factory for {@link org.springframework.ws.WebServiceMessage
 * WebServiceMessages}. Allows creation of empty messages, or messages based on <code>InputStream</code>s.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.WebServiceMessage
 */
public interface WebServiceMessageFactory {

    /**
     * Creates a new, empty {@link WebServiceMessage}.
     *
     * @return the empty message
     */
    WebServiceMessage createWebServiceMessage();

    /**
     * Reads {@link WebServiceMessage} from the given input stream.
     * <p/>
     * If the given stream is an instance of {@link org.springframework.ws.transport.TransportOutputStream
     * TransportOutputStream}, the headers will be read from the request.
     *
     * @param inputStream the inputstream to read the message from
     * @return the created message
     * @throws java.io.IOException if an I/O exception occurs
     */
    WebServiceMessage createWebServiceMessage(InputStream inputStream) throws IOException;

}
