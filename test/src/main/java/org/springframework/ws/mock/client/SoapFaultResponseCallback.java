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
import java.net.URI;
import java.util.Locale;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;

import static org.springframework.ws.mock.client.Assert.fail;

/**
 * Implementation of {@link ResponseCreator} that responds with a SOAP fault.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class SoapFaultResponseCallback extends AbstractResponseCreator<SoapMessage> {

    @Override
    protected void doWithResponse(URI uri, SoapMessage request, SoapMessage response) throws IOException {
        SoapBody responseBody = response.getSoapBody();
        if (responseBody == null) {
            fail("SOAP message [" + response + "] does not contain SOAP body");
        }
        addSoapFault(responseBody);
    }

    public abstract void addSoapFault(SoapBody soapBody);

    public static SoapFaultResponseCallback createMustUnderstandFault(final String faultStringOrReason,
                                                                      final Locale locale) {
        return new SoapFaultResponseCallback() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addMustUnderstandFault(faultStringOrReason, locale);
            }
        };

    }

    public static SoapFaultResponseCallback createClientOrSenderFault(final String faultStringOrReason,
                                                                      final Locale locale) {
        return new SoapFaultResponseCallback() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addClientOrSenderFault(faultStringOrReason, locale);
            }
        };
    }

    public static SoapFaultResponseCallback createServerOrReceiverFault(final String faultStringOrReason,
                                                                        final Locale locale) {
        return new SoapFaultResponseCallback() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addServerOrReceiverFault(faultStringOrReason, locale);
            }
        };

    }

    public static SoapFaultResponseCallback createVersionMismatchFault(final String faultStringOrReason,
                                                                       final Locale locale) {
        return new SoapFaultResponseCallback() {
            @Override
            public void addSoapFault(SoapBody soapBody) {
                soapBody.addVersionMismatchFault(faultStringOrReason, locale);
            }
        };

    }


}
