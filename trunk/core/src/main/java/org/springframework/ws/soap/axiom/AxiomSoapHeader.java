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
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StaxSource;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.SoapHeader</code>.
 *
 * @author Arjen Poutsma
 */
class AxiomSoapHeader implements SoapHeader {

    protected final SOAPHeader axiomHeader;

    protected final SOAPFactory axiomFactory;

    AxiomSoapHeader(SOAPHeader axiomHeader, SOAPFactory axiomFactory) {
        Assert.notNull(axiomHeader, "No axiomHeader given");
        Assert.notNull(axiomFactory, "No axiomFactory given");
        this.axiomHeader = axiomHeader;
        this.axiomFactory = axiomFactory;
    }

    public QName getName() {
        return axiomHeader.getQName();
    }

    public Source getSource() {
        return new StaxSource(axiomHeader.getXMLStreamReader());
    }

    public Result getResult() {
        return new SAXResult(new AxiomContentHandler(axiomHeader));
    }

    public SoapHeaderElement addHeaderElement(QName name) {
        try {
            OMNamespace namespace = axiomFactory.createOMNamespace(name.getNamespaceURI(), QNameUtils.getPrefix(name));
            SOAPHeaderBlock axiomHeaderBlock = axiomHeader.addHeaderBlock(name.getLocalPart(), namespace);
            return new AxiomSoapHeaderElement(axiomHeaderBlock, axiomFactory);
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    public Iterator examineMustUnderstandHeaderElements(String role) {
        try {
            return new AxiomSoapHeaderElementIterator(axiomHeader.examineMustUnderstandHeaderBlocks(role));
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    public Iterator examineAllHeaderElements() {
        try {
            return new AxiomSoapHeaderElementIterator(axiomHeader.examineAllHeaderBlocks());
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }

    }

    private class AxiomSoapHeaderElementIterator implements Iterator {

        private final Iterator axiomIterator;

        private AxiomSoapHeaderElementIterator(Iterator axiomIterator) {
            this.axiomIterator = axiomIterator;
        }

        public boolean hasNext() {
            return axiomIterator.hasNext();
        }

        public Object next() {
            try {
                SOAPHeaderBlock axiomHeaderBlock = (SOAPHeaderBlock) axiomIterator.next();
                return new AxiomSoapHeaderElement(axiomHeaderBlock, axiomFactory);
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }
        }

        public void remove() {
            axiomIterator.remove();
        }
    }
}
