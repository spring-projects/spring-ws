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

import java.io.IOException;
import java.util.Locale;

import org.springframework.core.io.Resource;

/**
 * Allows for setting up responses. Implementations of this interface are returned by {@link RequestExpectations}.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public interface ResponseActions {

    /**
     * Allows for further expectations to be set on the request.
     *
     * @return the request expectations
     */
    RequestExpectations and();

    /**
     * Records that the mock will receive the given String XML as payload response.
     *
     * @param payload the response payload
     */
    void andRespondWithPayload(String payload);

    /**
     * Records that the mock will receive the given {@link Resource} as payload response.
     *
     * @param resource the response payload
     */
    void andRespondWithPayload(Resource resource);

    /**
     * Records that the mock will respond with the given error message.
     *
     * @param errorMessage the error message
     * @see org.springframework.ws.transport.WebServiceConnection#hasError()
     * @see org.springframework.ws.transport.WebServiceConnection#getErrorMessage()
     */
    void andRespondWithError(String errorMessage);

    /**
     * Records that the mock will respond by throwing the given {@link IOException}.
     *
     * @param ex the I/O exception
     */
    void andThrowException(IOException ex);

    /**
     * Records that the mock will respond by throwing the given runtime exception.
     *
     * @param ex the runtime exception
     */
    void andThrowException(RuntimeException ex);

    /**
     * Records that the mock will respond with a {@code MustUnderstand} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addMustUnderstandFault(String, java.util.Locale)
     */
    void andRespondWithMustUnderstandFault(String faultStringOrReason, Locale locale);

    /**
     * Records that the mock will respond with a {@code Client} (SOAP 1.1) or {@code Sender} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, java.util.Locale)
     */
    void andRespondWithClientOrSenderFault(String faultStringOrReason, Locale locale);

    /**
     * Records that the mock will respond with a {@code Server} (SOAP 1.1) or {@code Receiver} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addServerOrReceiverFault(String, java.util.Locale)
     */
    void andRespondWithServerOrReceiverFault(String faultStringOrReason, Locale locale);

    /**
     * Records that the mock will respond with a {@code VersionMismatch} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addVersionMismatchFault(String, java.util.Locale)
     */
    void andRespondWithVersionMismatchFault(String faultStringOrReason, Locale locale);

    /**
     * Sets the {@link ResponseCallback} for this mock.
     *
     * @param responseCallback the response callback
     */
    void setResponseCallback(ResponseCallback responseCallback);

}
