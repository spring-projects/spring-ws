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
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>Soap11Body</code> interface. Used by
 * <code>Saaj13SoapEnvelope</code>.
 *
 * @author Arjen Poutsma
 */
class Saaj13Soap11Body extends Saaj13SoapBody implements Soap11Body {

    Saaj13Soap11Body(SOAPBody saajBody) {
        super(saajBody);
    }

    public Soap11Fault addFault(QName faultCode, String faultString, Locale faultStringLocale) {
        Assert.notNull(faultCode, "No faultCode given");
        Assert.notNull(faultString, "No faultString given");
        if (!StringUtils.hasLength(faultCode.getNamespaceURI())) {
            throw new IllegalArgumentException("fault code has no namespace");
        }
        try {
            saajBody.removeContents();
            SOAPFault saajFault;
            if (faultStringLocale == null) {
                saajFault = saajBody.addFault(faultCode, faultString);
            }
            else {
                saajFault = saajBody.addFault(faultCode, faultString, faultStringLocale);
            }
            return new Saaj13Soap11Fault(saajFault);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public SoapFault addMustUnderstandFault(String faultString, Locale locale) {
        return addStandardFault("MustUnderstand", faultString, locale);
    }

    public SoapFault addClientOrSenderFault(String faultString, Locale locale) {
        return addStandardFault("Client", faultString, locale);
    }

    public SoapFault addServerOrReceiverFault(String faultString, Locale locale) {
        return addStandardFault("Server", faultString, locale);
    }

    public SoapFault addVersionMismatchFault(String faultString, Locale locale) {
        return addStandardFault("VersionMismatch", faultString, locale);

    }

    private Soap11Fault addStandardFault(String localName, String faultString, Locale faultStringLocale) {
        try {
            QName faultCode = QNameUtils.createQName(saajBody.getElementQName().getNamespaceURI(),
                    localName,
                    QNameUtils.getPrefix(saajBody.getElementQName()));
            saajBody.removeContents();
            SOAPFault saajFault = faultStringLocale == null ? saajBody.addFault(faultCode, faultString) :
                    saajBody.addFault(faultCode, faultString, faultStringLocale);
            return new Saaj12Soap11Fault(saajFault);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public SoapFault getFault() {
        return new Saaj13Soap11Fault(saajBody.getFault());
    }

}
