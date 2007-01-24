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

package org.springframework.ws.soap.axiom;

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.xml.transform.StaxSource;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.Soap11Body</code>.
 *
 * @author Arjen Poutsma
 */
abstract class AxiomSoapBody implements SoapBody {

    protected final SOAPBody axiomBody;

    protected final SOAPFactory axiomFactory;

    private boolean payloadCaching;

    protected AxiomSoapBody(SOAPBody axiomBody, SOAPFactory axiomFactory, boolean payloadCaching) {
        Assert.notNull(axiomBody, "No axiomBody given");
        Assert.notNull(axiomFactory, "No axiomFactory given");
        this.axiomBody = axiomBody;
        this.axiomFactory = axiomFactory;
        this.payloadCaching = payloadCaching;
    }

    public Source getPayloadSource() {
        try {
            OMElement payloadElement = getPayloadElement();
            if (payloadElement == null) {
                return null;
            }
            else if (payloadCaching) {
                return new StaxSource(payloadElement.getXMLStreamReader());
            }
            else {
                return new StaxSource(payloadElement.getXMLStreamReaderWithoutCaching());
            }
        }
        catch (OMException ex) {
            throw new AxiomSoapBodyException(ex);
        }
    }

    public Result getPayloadResult() {
        return new SAXResult(new AxiomContentHandler(axiomBody));
    }

    public boolean hasFault() {
        return axiomBody.hasFault();
    }

    public SoapFault getFault() {
        SOAPFault axiomFault = axiomBody.getFault();
        return axiomFault != null ? new AxiomSoap11Fault(axiomFault, axiomFactory) : null;
    }

    public QName getName() {
        return axiomBody.getQName();
    }

    public Source getSource() {
        return new StaxSource(axiomBody.getXMLStreamReader());
    }

    private OMElement getPayloadElement() throws OMException {
        return axiomBody.getFirstElement();
    }

    protected void detachAllBodyChildren() {
        try {
            for (Iterator iterator = axiomBody.getChildElements(); iterator.hasNext();) {
                OMElement child = (OMElement) iterator.next();
                child.detach();
            }
        }
        catch (OMException ex) {
            throw new AxiomSoapBodyException(ex);
        }

    }

    protected SOAPFault addStandardFault(String localName, String faultStringOrReason, Locale locale) {
        Assert.notNull(faultStringOrReason, "No faultStringOrReason given");
        try {
            detachAllBodyChildren();
            SOAPFault fault = axiomFactory.createSOAPFault(axiomBody);
            SOAPFaultCode code = axiomFactory.createSOAPFaultCode(fault);
            SOAPFaultValue value = axiomFactory.createSOAPFaultValue(code);
            value.setText(fault.getNamespace().getPrefix() + ":" + localName);
            SOAPFaultReason reason = axiomFactory.createSOAPFaultReason(fault);
            SOAPFaultText text = axiomFactory.createSOAPFaultText(reason);
            if (locale != null) {
                text.setLang(AxiomUtils.toLanguage(locale));
            }
            text.setText(faultStringOrReason);
            return fault;
        }
        catch (SOAPProcessingException ex) {
            throw new AxiomSoapFaultException(ex);
        }
    }
}
