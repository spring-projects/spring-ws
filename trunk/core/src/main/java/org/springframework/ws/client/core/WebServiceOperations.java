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

package org.springframework.ws.client.core;

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Specifies a basic set of Web service operations. Implemented by {@link WebServiceTemplate}. Not often used directly,
 * but a useful option to enhance testability, as it can easily be mocked or stubbed.
 *
 * @author Arjen Poutsma
 * @see WebServiceTemplate
 */
public interface WebServiceOperations {

    /**
     * Sends a web service message that contains the given payload, marshalled by the configured
     * <code>Marshaller</code>. Returns the unmarshalled payload of the response message, if any.
     *
     * @param requestPayload the object to marshal into the request message payload
     * @return the unmarshalled payload of the response message, or <code>null</code> if no response is given
     * @throws IOException in case of I/O errors
     * @see WebServiceTemplate#setMarshaller(org.springframework.oxm.Marshaller)
     * @see WebServiceTemplate#setUnmarshaller(org.springframework.oxm.Unmarshaller)
     */
    Object marshalSendAndReceive(Object requestPayload) throws IOException;

    /**
     * Sends a web service message that contains the given payload, marshalled by the configured
     * <code>Marshaller</code>. Returns the unmarshalled payload of the response message, if any. The given callback
     * allows changing of the request message after the payload has been marshalled to it.
     *
     * @param requestPayload  the object to marshal into the request message payload
     * @param requestCallback callback to change message, can be <code>null</code>
     * @return the unmarshalled payload of the response message, or <code>null</code> if no response is given
     * @throws IOException in case of I/O errors
     * @see WebServiceTemplate#setMarshaller(org.springframework.oxm.Marshaller)
     * @see WebServiceTemplate#setUnmarshaller(org.springframework.oxm.Unmarshaller)
     */
    Object marshalSendAndReceive(Object requestPayload, WebServiceMessageCallback requestCallback) throws IOException;

    /**
     * Sends a web service message that contains the given payload, reading the result with a
     * <code>SourceExtractor</code>.
     *
     * @param requestPayload    the payload of the request message
     * @param responseExtractor object that will extract results
     * @return an arbitrary result object, as returned by the <code>SourceExtractor</code>
     */
    Object sendAndReceive(Source requestPayload, SourceExtractor responseExtractor) throws IOException;

    /**
     * Sends a web service message that contains the given payload, reading the result with a
     * <code>SourceExtractor</code>.
     * <p/>
     * The given callback allows changing of the request message after the payload has been written to it.
     *
     * @param requestPayload    the payload of the request message
     * @param requestCallback   callback to change message, can be <code>null</code>
     * @param responseExtractor object that will extract results
     * @return an arbitrary result object, as returned by the <code>SourceExtractor</code>
     */
    Object sendAndReceive(Source requestPayload,
                          WebServiceMessageCallback requestCallback,
                          SourceExtractor responseExtractor) throws IOException;

    /**
     * Sends a web service message that contains the given payload. Writes the response, if any, to the given
     * <code>Result</code>.
     *
     * @param requestPayload the payload of the request message
     * @param responseResult the result to write the response payload to
     * @throws IOException in case of I/O errors
     */
    void sendAndReceive(Source requestPayload, Result responseResult) throws IOException;

    /**
     * Sends a web service message that contains the given payload. Writes the response, if any, to the given
     * <code>Result</code>.
     * <p/>
     * The given callback allows changing of the request message after the payload has been written to it.
     *
     * @param requestPayload  the payload of the request message
     * @param requestCallback callback to change message, can be <code>null</code>
     * @param responseResult  the result to write the response payload to
     * @throws IOException in case of I/O errors
     */
    void sendAndReceive(Source requestPayload, WebServiceMessageCallback requestCallback, Result responseResult)
            throws IOException;

    /**
     * Sends a web service message that can be manipulated with the given callback, reading the result with a
     * <code>WebServiceMessageExtractor</code>.
     *
     * @param requestCallback   the requestCallback to be used for manipulating the request message
     * @param responseExtractor object that will extract results
     * @return an arbitrary result object, as returned by the <code>WebServiceMessageExtractor</code>
     * @throws IOException in case of I/O errors
     */
    Object sendAndReceive(WebServiceMessageCallback requestCallback, WebServiceMessageExtractor responseExtractor)
            throws IOException;

    /**
     * Sends a web service message that can be manipulated with the given callback, reading the result with a
     * <code>WebServiceMessageExtractor</code>.
     *
     * @param requestCallback  the callback to be used for manipulating the request message
     * @param responseCallback the callback to be used for manipulating the response message
     * @throws IOException in case of I/O errors
     */
    void sendAndReceive(WebServiceMessageCallback requestCallback, WebServiceMessageCallback responseCallback)
            throws IOException;
}
