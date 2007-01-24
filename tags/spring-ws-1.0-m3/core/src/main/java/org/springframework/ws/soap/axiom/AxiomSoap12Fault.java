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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.Soap12Fault</code>.
 */
class AxiomSoap12Fault extends AxiomSoapFault implements Soap12Fault {

    AxiomSoap12Fault(SOAPFault axiomFault, SOAPFactory axiomFactory) {
        super(axiomFault, axiomFactory);
    }

    public Iterator getFaultSubcodes() {
        List subcodes = new ArrayList();
        SOAPFaultSubCode subcode = axiomFault.getCode().getSubCode();
        while (subcode != null) {
            subcodes.add(getFaultCode(subcode.getValue()));
            subcode = subcode.getSubCode();
        }
        return subcodes.iterator();
    }

    public void addFaultSubcode(QName subcode) {
        SOAPFaultCode faultCode = axiomFault.getCode();
        SOAPFaultSubCode faultSubCode = null;
        if (faultCode.getSubCode() == null) {
            faultSubCode = axiomFactory.createSOAPFaultSubCode(faultCode);
        }
        else {
            faultSubCode = faultCode.getSubCode();
            while (true) {
                if (faultSubCode.getSubCode() != null) {
                    faultSubCode = faultSubCode.getSubCode();
                }
                else {
                    faultSubCode = axiomFactory.createSOAPFaultSubCode(faultSubCode);
                    break;
                }
            }
        }
        SOAPFaultValue faultValue = axiomFactory.createSOAPFaultValue(faultSubCode);
        setValueText(subcode, faultValue);
    }

    private void setValueText(QName code, SOAPFaultValue faultValue) {
        String prefix = QNameUtils.getPrefix(code);
        if (StringUtils.hasLength(code.getNamespaceURI()) && StringUtils.hasLength(prefix)) {
            OMNamespace namespace = axiomFault.findNamespaceURI(prefix);
            if (namespace == null) {
                axiomFault.declareNamespace(code.getNamespaceURI(), prefix);
            }
        }
        else if (StringUtils.hasLength(code.getNamespaceURI())) {
            OMNamespace namespace = axiomFault.findNamespace(code.getNamespaceURI(), null);
            if (namespace == null) {
                throw new IllegalArgumentException("Could not resolve namespace of code [" + code + "]");
            }
            code = QNameUtils.createQName(code.getNamespaceURI(), code.getLocalPart(), namespace.getPrefix());
        }
        faultValue.setText(prefix + ":" + code.getLocalPart());
    }

    public String getFaultNode() {
        SOAPFaultNode faultNode = axiomFault.getNode();
        if (faultNode == null) {
            return null;
        }
        else {
            return faultNode.getNodeValue();
        }
    }

    public void setFaultNode(String uri) {
        try {
            SOAPFaultNode faultNode = axiomFactory.createSOAPFaultNode(axiomFault);
            faultNode.setNodeValue(uri);
            axiomFault.setNode(faultNode);
        }
        catch (SOAPProcessingException ex) {
            throw new AxiomSoapFaultException(ex);
        }
    }

    public String getFaultStringOrReason() {
        return getFaultReasonText(Locale.getDefault());
    }

    public String getFaultReasonText(Locale locale) {
        SOAPFaultReason faultReason = axiomFault.getReason();
        String language = AxiomUtils.toLanguage(locale);
        SOAPFaultText faultText = faultReason.getSOAPFaultText(language);
        return faultText != null ? faultText.getText() : null;
    }

    public void setFaultReasonText(Locale locale, String text) {
        SOAPFaultReason faultReason = axiomFault.getReason();
        String language = AxiomUtils.toLanguage(locale);
        try {
            SOAPFaultText faultText = axiomFactory.createSOAPFaultText(faultReason);
            faultText.setLang(language);
            faultText.setText(text);
        }
        catch (SOAPProcessingException ex) {
            throw new AxiomSoapFaultException(ex);
        }
    }

}
