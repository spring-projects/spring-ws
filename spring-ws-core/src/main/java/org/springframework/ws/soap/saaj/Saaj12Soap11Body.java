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
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * Internal class that uses SAAJ 1.2 to implement the <code>Soap11Body</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj12Soap11Body implements Soap11Body {

    private final SOAPBody saajBody;

    Saaj12Soap11Body(SOAPBody saajBody) {
        Assert.notNull(saajBody, "No saajBody given");
        this.saajBody = saajBody;
    }

    public Source getPayloadSource() {
        SOAPBodyElement payloadElement = getPayloadElement();
        return payloadElement != null ? new DOMSource(payloadElement) : null;
    }

    public Result getPayloadResult() {
        saajBody.removeContents();
        return new DOMResult(saajBody);
    }

    public Soap11Fault addFault(QName faultCode, String faultString, Locale faultStringLocale) {
        Assert.notNull(faultCode, "No faultCode given");
        Assert.hasLength(faultString, "faultString cannot be empty");
        if (!StringUtils.hasLength(faultCode.getNamespaceURI())) {
            throw new IllegalArgumentException(
                    "A fault code with namespace and local part must be specific for a custom fault code");
        }
        try {
            Name name = SaajUtils.toName(faultCode, saajBody, getEnvelope());
            saajBody.removeContents();
            SOAPFault saajFault;
            if (faultStringLocale == null) {
                saajFault = saajBody.addFault(name, faultString);
            }
            else {
                saajFault = saajBody.addFault(name, faultString, faultStringLocale);
            }
            return new Saaj12Soap11Fault(saajFault);
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

    private Soap11Fault addStandardFault(String localName, String faultString, Locale locale) {
        try {
            Name faultCode = getEnvelope()
                    .createName(localName, saajBody.getElementName().getPrefix(), saajBody.getElementName().getURI());
            saajBody.removeContents();
            SOAPFault saajFault;
            if (locale == null) {
                saajFault = saajBody.addFault(faultCode, faultString);
            }
            else {
                saajFault = saajBody.addFault(faultCode, faultString, locale);
            }
            return new Saaj12Soap11Fault(saajFault);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public boolean hasFault() {
        return saajBody.hasFault();
    }

    public SoapFault getFault() {
        return new Saaj12Soap11Fault(saajBody.getFault());
    }

    public QName getName() {
        return SaajUtils.toQName(saajBody.getElementName());
    }

    public Source getSource() {
        return new DOMSource(saajBody);
    }

    private SOAPEnvelope getEnvelope() {
        return (SOAPEnvelope) saajBody.getParentElement();
    }

    /**
     * Retrieves the payload of the wrapped SAAJ message as a single DOM element. The payload of a message is the
     * contents of the SOAP body.
     *
     * @return the message payload, or <code>null</code> if none is set.
     */
    private SOAPBodyElement getPayloadElement() {
        for (Iterator iterator = saajBody.getChildElements(); iterator.hasNext();) {
            Object child = iterator.next();
            if (child instanceof SOAPBodyElement) {
                return (SOAPBodyElement) child;
            }
        }
        return null;
    }
}
