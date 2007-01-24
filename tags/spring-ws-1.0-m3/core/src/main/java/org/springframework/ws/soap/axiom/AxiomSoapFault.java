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

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.*;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StaxSource;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

/**
 * @author Arjen Poutsma
 */
abstract class AxiomSoapFault implements SoapFault {

    protected final SOAPFault axiomFault;

    protected final SOAPFactory axiomFactory;

    protected AxiomSoapFault(SOAPFault axiomFault, SOAPFactory axiomFactory) {
        Assert.notNull(axiomFault, "No axiomFault given");
        Assert.notNull(axiomFactory, "No axiomFactory given");
        this.axiomFault = axiomFault;
        this.axiomFactory = axiomFactory;
    }

    public QName getName() {
        return axiomFault.getQName();
    }

    public Source getSource() {
        return new StaxSource(axiomFault.getXMLStreamReader());
    }

    public QName getFaultCode() {
        return getFaultCode(axiomFault.getCode().getValue());
    }

    /**
     * Axiom 1.0's getTextAsQName is broken for SOAPFaultValues, hence this.
     */
    protected QName getFaultCode(SOAPFaultValue value) {
        String text = value.getText();
        int idx = text.indexOf(':');
        String prefix = text.substring(0, idx);
        String localPart = text.substring(idx + 1);
        String namespaceUri = axiomFault.getCode().getValue().findNamespaceURI(prefix).getNamespaceURI();
        return QNameUtils.createQName(namespaceUri, localPart, prefix);
    }

    public String getFaultActorOrRole() {
        SOAPFaultRole faultRole = axiomFault.getRole();
        return faultRole != null ? faultRole.getRoleValue() : null;
    }

    public void setFaultActorOrRole(String actor) {
        try {
            SOAPFaultRole axiomFaultRole = axiomFactory.createSOAPFaultRole(axiomFault);
            axiomFaultRole.setRoleValue(actor);
        }
        catch (SOAPProcessingException ex) {
            throw new AxiomSoapFaultException(ex);
        }

    }

    public SoapFaultDetail getFaultDetail() {
        try {
            SOAPFaultDetail axiomFaultDetail = axiomFault.getDetail();
            return axiomFaultDetail != null ? new AxiomSoapFaultDetail(axiomFaultDetail, axiomFactory) : null;
        }
        catch (OMException ex) {
            throw new AxiomSoapFaultException(ex);
        }

    }

    public SoapFaultDetail addFaultDetail() {
        try {
            SOAPFaultDetail axiomFaultDetail = axiomFactory.createSOAPFaultDetail(axiomFault);
            return new AxiomSoapFaultDetail(axiomFaultDetail, axiomFactory);
        }
        catch (OMException ex) {
            throw new AxiomSoapFaultException(ex);
        }

    }
}
