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

class SaajSoap11Body extends SaajSoapBody implements Soap11Body {

    private static final String CLIENT = "Client";

    private static final String MUST_UNDERSTAND = "MustUnderstand";

    private static final String SERVER = "Server";

    private static final String VERSION_MISMATCH = "VersionMismatch";

    public SaajSoap11Body(SOAPBody body, SaajImplementationStrategy strategy) {
        super(body, strategy);
    }

    public Soap11Fault addFault(QName faultCode, String faultString, Locale faultStringLocale) {
        Assert.notNull(faultCode, "No faultCode given");
        Assert.hasLength(faultString, "faultString cannot be empty");
        if (!StringUtils.hasLength(faultCode.getNamespaceURI())) {
            throw new IllegalArgumentException(
                    "A fault code with namespace and local part must be specific for a custom fault code");
        }
        try {
            getStrategy().removeContents(getSaajBody());
            SOAPFault saajFault = getStrategy().addFault(getSaajBody(), faultCode, faultString, faultStringLocale);
            return new SaajSoap11Fault(saajFault, getStrategy());
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public SoapFault getFault() {
        SOAPFault fault = getSaajBody().getFault();
        return fault == null ? null : new SaajSoap11Fault(fault, getStrategy());
    }

    public SoapFault addClientOrSenderFault(String faultString, Locale locale) {
        return addFault(getStandardFaultCodeQName(CLIENT), faultString, locale);
    }

    public SoapFault addMustUnderstandFault(String faultString, Locale locale) {
        return addFault(getStandardFaultCodeQName(MUST_UNDERSTAND), faultString, locale);
    }

    public SoapFault addServerOrReceiverFault(String faultString, Locale locale) {
        return addFault(getStandardFaultCodeQName(SERVER), faultString, locale);
    }

    public SoapFault addVersionMismatchFault(String faultString, Locale locale) {
        return addFault(getStandardFaultCodeQName(VERSION_MISMATCH), faultString, locale);
    }

    private QName getStandardFaultCodeQName(String localName) {
        return QNameUtils.createQName(getSaajBody().getElementName().getURI(), localName, getSaajBody().getElementName().getPrefix());
    }
}
