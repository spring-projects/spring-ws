/*
 * Copyright 2007 the original author or authors.
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

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

/**
 * SAAJ 1.3 specific implementation of the <code>SaajImplementation</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj13Implementation extends SaajImplementation {

    private static final Saaj13Implementation INSTANCE = new Saaj13Implementation();

    private Saaj13Implementation() {
    }

    public static Saaj13Implementation getInstance() {
        return INSTANCE;
    }

    public QName getName(SOAPElement element) {
        return element.getElementQName();
    }

    public QName getFaultCode(SOAPFault fault) {
        return fault.getFaultCodeAsQName();
    }

    public boolean isSoap11(SOAPElement element) {
        return SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(element.getNamespaceURI());
    }

    public DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException {
        return detail.addDetailEntry(name);
    }

    public SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException {
        return header.addHeaderElement(name);
    }

    public String getFaultRole(SOAPFault fault) {
        return fault.getFaultRole();
    }

    public void setFaultRole(SOAPFault fault, String role) throws SOAPException {
        fault.setFaultRole(role);
    }

    public SOAPHeaderElement addNotUnderstoodHeaderElement(SOAPHeader header, QName name) throws SOAPException {
        return header.addNotUnderstoodHeaderElement(name);
    }

    public SOAPHeaderElement addUpgradeHeaderElement(SOAPHeader header, String[] supportedSoapUris)
            throws SOAPException {
        return header.addUpgradeHeaderElement(supportedSoapUris);
    }

    public Iterator getFaultSubcodes(SOAPFault fault) {
        return fault.getFaultSubcodes();
    }

    public void appendFaultSubcode(SOAPFault fault, QName subcode) throws SOAPException {
        fault.appendFaultSubcode(subcode);
    }

    public String getFaultNode(SOAPFault fault) {
        return fault.getFaultNode();
    }

    public void setFaultNode(SOAPFault fault, String uri) throws SOAPException {
        fault.setFaultNode(uri);
    }

    public String getFaultReasonText(SOAPFault fault, Locale locale) throws SOAPException {
        return fault.getFaultReasonText(locale);
    }

    public void setFaultReasonText(SOAPFault fault, Locale locale, String text) throws SOAPException {
        fault.addFaultReasonText(text, locale);
    }

    public SOAPFault addFault(SOAPBody body, QName faultCode, String faultString, Locale locale) throws SOAPException {
        if (locale == null) {
            return body.addFault(faultCode, faultString);
        }
        else {
            return body.addFault(faultCode, faultString, locale);
        }
    }
}
