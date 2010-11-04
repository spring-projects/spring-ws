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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.test.support.creator.PayloadMessageCreator;
import org.springframework.ws.test.support.creator.WebServiceMessageCreator;
import org.springframework.xml.transform.ResourceSource;

/**
 * @author Arjen Poutsma
 */
public abstract class ResponseCreators {

    private ResponseCreators() {
    }

    /**
     * Respond with the given {@link javax.xml.transform.Source} XML as payload response.
     *
     * @param payload the response payload
     * @return the response callback
     */
    public static ResponseCreator withPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new WebServiceMessageCreatorAdapter(new PayloadMessageCreator(payload));
    }

    /**
     * Respond with the given {@link org.springframework.core.io.Resource} XML as payload response.
     *
     * @param payload the response payload
     * @return the response callback
     */
    public static ResponseCreator withPayload(Resource payload) throws IOException {
        Assert.notNull(payload, "'payload' must not be null");
        return withPayload(new ResourceSource(payload));
    }

    /**
     * Respond with an error.
     *
     * @param errorMessage the error message
     * @return the response callback
     * @see org.springframework.ws.transport.WebServiceConnection#hasError()
     * @see org.springframework.ws.transport.WebServiceConnection#getErrorMessage()
     */
    public static ResponseCreator withError(String errorMessage) {
        Assert.hasLength(errorMessage, "'errorMessage' must not be empty");
        return new ErrorResponseCreator(errorMessage);
    }

    /**
     * Respond with an {@link java.io.IOException}.
     *
     * @param ioException the exception to be thrown
     * @return the response callback
     */
    public static ResponseCreator withException(IOException ioException) {
        Assert.notNull(ioException, "'ioException' must not be null");
        return new ExceptionResponseCreator(ioException);
    }

    /**
     * Respond with an {@link RuntimeException}.
     *
     * @param ex the runtime exception to be thrown
     * @return the response callback
     */
    public static ResponseCreator withException(RuntimeException ex) {
        Assert.notNull(ex, "'ex' must not be null");
        return new ExceptionResponseCreator(ex);
    }

    /**
     * Respond with a {@code MustUnderstand} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addMustUnderstandFault(String, java.util.Locale)
     */
    public static ResponseCreator withMustUnderstandFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createMustUnderstandFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code Client} (SOAP 1.1) or {@code Sender} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, Locale)
     */
    public static ResponseCreator withClientOrSenderFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createClientOrSenderFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code Server} (SOAP 1.1) or {@code Receiver} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addServerOrReceiverFault(String, Locale)
     */
    public static ResponseCreator withServerOrReceiverFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createServerOrReceiverFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code VersionMismatch} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addVersionMismatchFault(String, Locale)
     */
    public static ResponseCreator withVersionMismatchFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCreator.createVersionMismatchFault(faultStringOrReason, locale);
    }

    /**
     * Adapts a {@link WebServiceMessageCreator} to the {@link ResponseCreator} contract.
     */
    private static class WebServiceMessageCreatorAdapter implements ResponseCreator {

        private final WebServiceMessageCreator adaptee;

        private WebServiceMessageCreatorAdapter(WebServiceMessageCreator adaptee) {
            this.adaptee = adaptee;
        }

        public WebServiceMessage createResponse(URI uri,
                                                WebServiceMessage request,
                                                WebServiceMessageFactory messageFactory) throws IOException {
            return adaptee.createMessage(messageFactory);
        }
    }


}
