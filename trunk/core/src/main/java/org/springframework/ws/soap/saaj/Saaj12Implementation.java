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

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ 1.2 specific implementation of the <code>SaajImplementation</code> interface.
 *
 * @author Arjen Poutsma
 */
public class Saaj12Implementation extends SaajImplementation {

    private static final Saaj12Implementation INSTANCE = new Saaj12Implementation();

    private Saaj12Implementation() {
    }

    public static Saaj12Implementation getInstance() {
        return INSTANCE;
    }

    public QName getName(SOAPElement element) {
        return SaajUtils.toQName(element.getElementName());
    }

    public QName getFaultCode(SOAPFault fault) {
        return SaajUtils.toQName(fault.getFaultCodeAsName());
    }

    public boolean isSoap11(SOAPElement element) {
        return true;
    }

    public DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException {
        Name detailEntryName = SaajUtils.toName(name, detail);
        return detail.addDetailEntry(detailEntryName);
    }

    public SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException {
        Name saajName = SaajUtils.toName(name, header);
        return header.addHeaderElement(saajName);
    }

    public SOAPFault addFault(SOAPBody body, QName faultCode, String faultString, Locale locale) throws SOAPException {
        Name name = SaajUtils.toName(faultCode, body);
        if (locale == null) {
            return body.addFault(name, faultString);
        }
        else {
            return body.addFault(name, faultString, locale);
        }
    }

    //
    // SOAP 1.2
    //

    public String getFaultRole(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public void setFaultRole(SOAPFault fault, String role) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public SOAPHeaderElement addNotUnderstoodHeaderElement(SOAPHeader header, QName name) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public SOAPHeaderElement addUpgradeHeaderElement(SOAPHeader header, String[] supportedSoapUris) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public Iterator getFaultSubcodes(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public void appendFaultSubcode(SOAPFault fault, QName subcode) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public String getFaultNode(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public void setFaultNode(SOAPFault fault, String uri) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public String getFaultReasonText(SOAPFault fault, Locale locale) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

    public void setFaultReasonText(SOAPFault fault, Locale locale, String text) {
        throw new UnsupportedOperationException("SAAJ 1.2 does not support SOAP 1.2");
    }

}
