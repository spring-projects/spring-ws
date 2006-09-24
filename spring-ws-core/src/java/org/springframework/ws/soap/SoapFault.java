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

package org.springframework.ws.soap;

import java.util.Locale;
import javax.xml.namespace.QName;

/**
 * Represents the <code>Fault</code> element in the body of a SOAP message. A faul consists of a fault code, string, and
 * role.
 * <p/>
 * Though this interface uses SOAP 1.2 terminology, the underlying implementations can be both SOAP 1.1 and 1.2 based.
 *
 * @author Arjen Poutsma
 * @see #isMustUnderstandFault()
 * @see #isSenderFault()
 * @see #isReceiverFault()
 */
public interface SoapFault extends SoapElement {

    /**
     * Returns the fault code.
     *
     * @return a <code>QName</code> representing the fault code
     */
    QName getFaultCode();

    /**
     * Returns the fault string.
     *
     * @return the fault string
     */
    String getFaultString();

    /**
     * Sets the localized fault string.
     *
     * @param faultString the fault string
     * @param locale      the locale of the fault string
     */
    void setFaultString(String faultString, Locale locale);

    /**
     * Returns the locale of the fault string.
     *
     * @return the locale of the fault string
     */
    Locale getFaultStringLocale();

    /**
     * Returns the fault role.
     *
     * @return the fault actor.
     */
    String getFaultRole();

    /**
     * Sets the fault role.
     *
     * @param role the fault role
     */
    void setFaultRole(String role);

    /**
     * Indicates whether this is a <code>MustUnderstand</code> fault. A <code>MustUnderstand</code> is returned when a
     * SOAP header with a <code>MustUnderstand</code> attribute is not understood.
     *
     * @return <code>true</code> if this a <code>MustUnderstand</code> fault; <code>false</code> otherwise
     * @see SoapHeaderElement#getMustUnderstand()
     * @see SoapBody#addMustUnderstandFault(javax.xml.namespace.QName[])
     */
    boolean isMustUnderstandFault();

    /**
     * Indicates whether this is a <code>Sender</code>/<code>Client</code> fault. If the underlying message is SOAP 1.1
     * based, this methods checks for a <code>Client</code> fault code; in SOAP 1.2, it checks for a <code>Sender</code>
     * fault code.
     *
     * @return <code>true</code> if this a <code>Sender</code> or <code>Client</code> fault
     * @see SoapBody#addSenderFault()
     */
    boolean isSenderFault();

    /**
     * Indicates whether this is a <code>Receiver</code> fault. If the underlying message is SOAP 1.1 based, this
     * methods checks for a <code>Server</code> fault code; in SOAP 1.2, it checks for a <code>Receiver</code> fault
     * code.
     *
     * @return <code>true</code> if this is a <code>Receiver</code> or <code>Server</code> fault
     * @see SoapBody#addReceiverFault()
     */
    boolean isReceiverFault();
}
