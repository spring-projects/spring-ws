/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.mock.client;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;

/**
 * Allows for setting expectations on the request. Implementations of this interface are returned by {@link
 * MockWebServiceMessageSender#whenConnectingTo(java.net.URI)}, and by {@link MockWebServiceMessageSender#whenConnecting()}.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public interface RequestExpectations {

    /**
     * Records that the mock will expect the given String XML payload. Returns a {@link ResponseActions} object that
     * allows for setting up the response.
     *
     * @param payload the String XML payload
     * @return the response actions
     */
    ResponseActions expectPayload(String payload);

    /**
     * Records that the mock will expect the given {@link Source} payload. Returns a {@link ResponseActions} object that
     * allows for setting up the response.
     *
     * @param payload the payload
     * @return the response actions
     */
    ResponseActions expectPayload(Source payload);

    /**
     * Records that the mock will expect the given {@link Resource} payload. Returns a {@link ResponseActions} object
     * that allows for setting up the response.
     *
     * @param payload the String XML payload
     * @return the response actions
     */
    ResponseActions expectPayload(Resource payload);

    /**
     * Records that the mock will expect the given SOAP header to exist on the outgoing message. Returns a {@link
     * ResponseActions} object that allows for setting up the response.
     *
     * @param soapHeaderName the qualified name of the SOAP header to expect
     * @return the response actions
     */
    ResponseActions expectSoapHeader(QName soapHeaderName);

    /**
     * Adds the {@link RequestMatcher} to the list of expectations. Returns a {@link ResponseActions} object that allows
     * for setting up the response.
     *
     * @param requestMatcher the request matcher
     * @return the response actions
     */
    ResponseActions addRequestMatcher(RequestMatcher requestMatcher);
}
