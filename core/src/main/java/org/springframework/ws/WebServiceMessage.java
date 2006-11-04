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

package org.springframework.ws;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Represents a protocol agnostic XML message. Contains methods that provide access to the payload of the message.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.SoapMessage
 */
public interface WebServiceMessage {

    /**
     * Returns the contents of the message as a <code>java.xml.transform.Source</code>. Depending on the implementation,
     * this can be retrieved multiple times, or just a single time.
     *
     * @return the message contents
     */
    Source getPayloadSource();

    /**
     * Returns the contents of the message as a <code>java.xml.transform.Result</code>. Some implementations are
     * read-only, and may throws an <code>UnsupportedOperationException</code>.
     *
     * @return the messaage contents
     * @throws UnsupportedOperationException if the message is read-only
     */
    Result getPayloadResult();

    /**
     * Writes the entire message to the given output stream. If the given stream is an instance of {@link
     * org.springframework.ws.transport.TransportOutputStream TransportOutputStream}, the corresponding headers will be
     * writen as well.
     *
     * @param outputStream the stream to write to
     * @throws IOException if an I/O exception occurs
     */
    void writeTo(OutputStream outputStream) throws IOException;

}
