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

import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;

/**
 * SAAJ-specific implementation of the <code>Soap12Body</code> interface. Wraps a {@link javax.xml.soap.SOAPBody}.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
class SaajSoap12Body extends SaajSoapBody implements Soap12Body {

    public SaajSoap12Body(SOAPBody body) {
        super(body);
    }

    public SoapFault getFault() {
        SOAPFault fault = getImplementation().getFault(getSaajBody());
        return new SaajSoap12Fault(fault);
    }

    public SoapFault addClientOrSenderFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_12.getClientOrSenderFaultName(), faultString, locale);
    }

    public SoapFault addMustUnderstandFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_12.getMustUnderstandFaultName(), faultString, locale);
    }

    public SoapFault addServerOrReceiverFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_12.getServerOrReceiverFaultName(), faultString, locale);
    }

    public SoapFault addVersionMismatchFault(String faultString, Locale locale) {
        return addFault(SoapVersion.SOAP_12.getVersionMismatchFaultName(), faultString, locale);
    }

    public Soap12Fault addDataEncodingUnknownFault(QName[] subcodes, String reason, Locale locale) {
        QName name = new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "DataEncodingUnknown");
        Soap12Fault fault = addFault(name, reason, locale);
        for (int i = 0; i < subcodes.length; i++) {
            fault.addFaultSubcode(subcodes[i]);
        }
        return fault;
    }

    protected Soap12Fault addFault(QName faultCode, String faultString, Locale faultStringLocale) {
        Assert.notNull(faultCode, "No faultCode given");
        Assert.hasLength(faultString, "faultString cannot be empty");
        Assert.hasLength(faultCode.getLocalPart(), "faultCode's localPart cannot be empty");
        Assert.hasLength(faultCode.getNamespaceURI(), "faultCode's namespaceUri cannot be empty");
        try {
            getImplementation().removeContents(getSaajBody());
            SOAPFault saajFault =
                    getImplementation().addFault(getSaajBody(), faultCode, faultString, faultStringLocale);
            return new SaajSoap12Fault(saajFault);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

}
