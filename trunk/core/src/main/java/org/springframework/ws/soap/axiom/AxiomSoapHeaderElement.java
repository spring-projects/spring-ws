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

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StaxSource;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.SoapHeaderHeaderElement</code>.
 */
class AxiomSoapHeaderElement implements SoapHeaderElement {

    private final SOAPHeaderBlock axiomHeaderBlock;

    private final SOAPFactory axiomFactory;

    public AxiomSoapHeaderElement(SOAPHeaderBlock axiomHeaderBlock, SOAPFactory axiomFactory) {
        Assert.notNull(axiomHeaderBlock, "No axiomHeaderBlock given");
        Assert.notNull(axiomFactory, "No axiomFactory given");
        this.axiomHeaderBlock = axiomHeaderBlock;
        this.axiomFactory = axiomFactory;
    }

    public QName getName() {
        return axiomHeaderBlock.getQName();
    }

    public Source getSource() {
        try {
            return new StaxSource(axiomHeaderBlock.getXMLStreamReader());
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }

    }

    public String getActorOrRole() {
        return axiomHeaderBlock.getRole();
    }

    public void setActorOrRole(String role) {
        axiomHeaderBlock.setRole(role);
    }

    public boolean getMustUnderstand() {
        return axiomHeaderBlock.getMustUnderstand();
    }

    public void setMustUnderstand(boolean mustUnderstand) {
        axiomHeaderBlock.setMustUnderstand(mustUnderstand);
    }

    public Result getResult() {
        try {
            return new SAXResult(new AxiomContentHandler(axiomHeaderBlock));
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }

    }

    public void addAttribute(QName name, String value) throws SoapHeaderException {
        try {
            OMNamespace namespace = axiomFactory.createOMNamespace(name.getNamespaceURI(), QNameUtils.getPrefix(name));
            OMAttribute attribute = axiomFactory.createOMAttribute(name.getLocalPart(), namespace, value);
            axiomHeaderBlock.addAttribute(attribute);
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }
}
