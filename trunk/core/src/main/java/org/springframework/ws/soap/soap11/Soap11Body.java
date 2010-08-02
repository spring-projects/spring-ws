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

package org.springframework.ws.soap.soap11;

import java.util.Locale;
import javax.xml.namespace.QName;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFaultException;

/**
 * Subinterface of <code>SoapBody</code> that exposes SOAP 1.1 functionality. Necessary because SOAP 1.1 differs from
 * SOAP 1.2 with respect to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @see Soap11Fault
 * @since 1.0.0
 */
public interface Soap11Body extends SoapBody {

    /**
     * Adds a SOAP 1.1 <faultCode>Fault</faultCode> to the body with a localized message. Adding a fault removes the
     * current content of the body.
     *
     * @param faultCode         the fully qualified fault faultCode
     * @param faultString       the faultString
     * @param faultStringLocale the faultString locale. May be <code>null</code>
     * @return the added <faultCode>Soap11Fault</faultCode>
     * @throws IllegalArgumentException if the fault faultCode is not fully qualified
     */
    Soap11Fault addFault(QName faultCode, String faultString, Locale faultStringLocale) throws SoapFaultException;

    Soap11Fault getFault();

    Soap11Fault addMustUnderstandFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

    Soap11Fault addClientOrSenderFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

    Soap11Fault addServerOrReceiverFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

    Soap11Fault addVersionMismatchFault(String faultStringOrReason, Locale locale) throws SoapFaultException;
}
