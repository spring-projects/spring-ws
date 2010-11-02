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

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;

import static org.springframework.ws.test.support.AssertionErrors.fail;

/**
 * Implementation of {@link ResponseCreator} that responds with a SOAP fault.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class SoapFaultResponseCreator extends AbstractResponseCreator<SoapMessage> {

    @Override
    protected void doWithResponse(URI uri, SoapMessage request, SoapMessage response) throws IOException {
        SoapBody responseBody = response.getSoapBody();
        if (responseBody == null) {
            fail("SOAP message [" + response + "] does not contain SOAP body");
        }
        addSoapFault(responseBody);
    }

    public abstract void addSoapFault(SoapBody soapBody);

    public static SoapFaultResponseCreator createMustUnderstandFault(final String faultStringOrReason,
                                                                     final Locale locale) {
        return new SoapFaultResponseCreator() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addMustUnderstandFault(faultStringOrReason, locale);
            }
        };

    }

    public static SoapFaultResponseCreator createClientOrSenderFault(final String faultStringOrReason,
                                                                     final Locale locale) {
        return new SoapFaultResponseCreator() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addClientOrSenderFault(faultStringOrReason, locale);
            }
        };
    }

    public static SoapFaultResponseCreator createServerOrReceiverFault(final String faultStringOrReason,
                                                                       final Locale locale) {
        return new SoapFaultResponseCreator() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addServerOrReceiverFault(faultStringOrReason, locale);
            }
        };

    }

    public static SoapFaultResponseCreator createVersionMismatchFault(final String faultStringOrReason,
                                                                      final Locale locale) {
        return new SoapFaultResponseCreator() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addVersionMismatchFault(faultStringOrReason, locale);
            }
        };

    }


}
