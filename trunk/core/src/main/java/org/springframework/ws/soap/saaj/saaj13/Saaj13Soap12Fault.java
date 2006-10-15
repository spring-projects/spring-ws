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

package org.springframework.ws.soap.saaj.saaj13;

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.saaj.SaajSoapFaultException;
import org.springframework.ws.soap.soap12.Soap12Fault;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>Soap12Fault</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj13Soap12Fault implements Soap12Fault {

    private final SOAPFault saajFault;

    Saaj13Soap12Fault(SOAPFault saajFault) {
        this.saajFault = saajFault;
    }

    public QName getName() {
        return saajFault.getElementQName();
    }

    public Source getSource() {
        return new DOMSource(saajFault);
    }

    public QName getFaultCode() {
        return saajFault.getFaultCodeAsQName();
    }

    public SoapFaultDetail getFaultDetail() {
        Detail saajDetail = saajFault.getDetail();
        return saajDetail != null ? new Saaj13SoapFaultDetail(saajDetail) : null;
    }

    public SoapFaultDetail addFaultDetail() {
        try {
            Detail saajDetail = saajFault.addDetail();
            return saajDetail != null ? new Saaj13SoapFaultDetail(saajDetail) : null;
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public Iterator getFaultSubcodes() {
        return saajFault.getFaultSubcodes();
    }

    public void addFaultSubcode(QName subcode) {
        try {
            saajFault.appendFaultSubcode(subcode);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultActorOrRole() {
        return saajFault.getFaultRole();
    }

    public void setFaultActorOrRole(String uri) {
        try {
            saajFault.setFaultRole(uri);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultNode() {
        return saajFault.getFaultNode();
    }

    public void setFaultNode(String uri) {
        try {
            saajFault.setFaultNode(uri);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public void setFaultReasonText(Locale locale, String text) {
        try {
            saajFault.addFaultReasonText(text, locale);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public String getFaultReasonText(Locale locale) {
        try {
            return saajFault.getFaultReasonText(locale);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }
}
