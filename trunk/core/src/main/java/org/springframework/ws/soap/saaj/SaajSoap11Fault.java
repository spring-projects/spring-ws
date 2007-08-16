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

package org.springframework.ws.soap.saaj;

import java.util.Locale;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * SAAJ-specific implementation of the <code>Soap11Fault</code> interface. Wraps a {@link javax.xml.soap.SOAPFault}.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
class SaajSoap11Fault extends SaajSoapFault implements Soap11Fault {

    SaajSoap11Fault(SOAPFault fault) {
        super(fault);
    }

    public String getFaultActorOrRole() {
        return getImplementation().getFaultActor(getSaajFault());
    }

    public void setFaultActorOrRole(String faultActor) {
        try {
            getImplementation().setFaultActor(getSaajFault(), faultActor);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultStringOrReason() {
        return getImplementation().getFaultString(getSaajFault());
    }

    public Locale getFaultStringLocale() {
        return getImplementation().getFaultStringLocale(getSaajFault());
    }
}
