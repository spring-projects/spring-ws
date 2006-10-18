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
 *  * limitations under the License.
 */

package org.springframework.ws.soap.saaj;

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.springframework.ws.soap.soap12.Soap12Fault;

class SaajSoap12Fault extends SaajSoapFault implements Soap12Fault {

    public SaajSoap12Fault(SOAPFault saajFault) {
        super(saajFault);
    }

    public Iterator getFaultSubcodes() {
        return getSaajFault().getFaultSubcodes();
    }

    public void addFaultSubcode(QName subcode) {
        try {
            getSaajFault().appendFaultSubcode(subcode);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultActorOrRole() {
        return getSaajFault().getFaultRole();
    }

    public void setFaultActorOrRole(String uri) {
        try {
            getSaajFault().setFaultRole(uri);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultNode() {
        return getSaajFault().getFaultNode();
    }

    public void setFaultNode(String uri) {
        try {
            getSaajFault().setFaultNode(uri);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public void setFaultReasonText(Locale locale, String text) {
        try {
            getSaajFault().addFaultReasonText(text, locale);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultReasonText(Locale locale) {
        try {
            return getSaajFault().getFaultReasonText(locale);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

}
