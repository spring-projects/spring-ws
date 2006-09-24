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

package org.springframework.ws.soap.endpoint;

import java.util.Locale;

import javax.xml.namespace.QName;

/**
 * Defines properties for a SOAP Fault.
 *
 * @author Arjen Poutsma
 */
public class SoapFaultDefinition {

    /**
     * Constant <code>QName</code> used to indicate that a <code>Client</code> or <code>Sender</code> fault must be
     * created.
     *
     * @see org.springframework.ws.soap.support.SoapMessageUtils#addSenderFault(org.springframework.ws.soap.SoapMessage,
     *      String)
     */
    public static final QName SENDER = new QName("SENDER");

    /**
     * Constant <code>QName</code> used to indicate that a <code>Server</code> or <code>Receiver</code> fault must be
     * created.
     *
     * @see org.springframework.ws.soap.support.SoapMessageUtils#addReceiverFault(org.springframework.ws.soap.SoapMessage,
     *      String)
     */
    public static final QName RECEIVER = new QName("RECEIVER");

    private QName faultCode;

    private String faultString;

    private Locale faultStringLocale = Locale.ENGLISH;

    /**
     * Returns the fault code.
     */
    public QName getFaultCode() {
        return faultCode;
    }

    /**
     * Sets the fault code.
     */
    public void setFaultCode(QName faultCode) {
        this.faultCode = faultCode;
    }

    /**
     * Returns the fault string.
     */
    public String getFaultString() {
        return faultString;
    }

    /**
     * Sets the fault string.
     */
    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    /**
     * Gets the fault string locale. By default, it is English.
     *
     * @see Locale#ENGLISH
     */
    public Locale getFaultStringLocale() {
        return faultStringLocale;
    }

    /**
     * Sets the fault string locale. By default, it is English.
     *
     * @see Locale#ENGLISH
     */
    public void setFaultStringLocale(Locale faultStringLocale) {
        this.faultStringLocale = faultStringLocale;
    }
}
