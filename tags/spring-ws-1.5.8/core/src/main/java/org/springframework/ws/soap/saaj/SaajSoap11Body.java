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
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * SAAJ-specific implementation of the <code>Soap11Body</code> interface. Wraps a {@link javax.xml.soap.SOAPBody}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoap11Body extends SaajSoapBody implements Soap11Body {

    private final boolean langAttributeOnSoap11FaulString;

    SaajSoap11Body(SOAPBody body, boolean langAttributeOnSoap11FaulString) {
        super(body);
        this.langAttributeOnSoap11FaulString = langAttributeOnSoap11FaulString;
    }

    public SoapFault getFault() {
        SOAPFault fault = getImplementation().getFault(getSaajBody());
        return fault != null ? new SaajSoap11Fault(fault) : null;
    }

    public Soap11Fault addFault(QName faultCode, String faultString, Locale faultStringLocale) {
        Assert.notNull(faultCode, "No faultCode given");
        Assert.hasLength(faultString, "faultString cannot be empty");
        Assert.hasLength(faultCode.getLocalPart(), "faultCode's localPart cannot be empty");
        Assert.hasLength(faultCode.getNamespaceURI(), "faultCode's namespaceUri cannot be empty");
        if (!langAttributeOnSoap11FaulString) {
            faultStringLocale = null;
        }
        try {
            getImplementation().removeContents(getSaajBody());
            SOAPFault saajFault =
                    getImplementation().addFault(getSaajBody(), faultCode, faultString, faultStringLocale);
            return new SaajSoap11Fault(saajFault);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public SoapFault addClientOrSenderFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_11.getClientOrSenderFaultName(), faultString, locale);
    }

    public SoapFault addMustUnderstandFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_11.getMustUnderstandFaultName(), faultString, locale);
    }

    public SoapFault addServerOrReceiverFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_11.getServerOrReceiverFaultName(), faultString, locale);
    }

    public SoapFault addVersionMismatchFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_11.getVersionMismatchFaultName(), faultString, locale);
    }

}
