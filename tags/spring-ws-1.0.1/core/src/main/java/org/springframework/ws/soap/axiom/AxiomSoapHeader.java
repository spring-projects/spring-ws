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
import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.SoapHeader</code>.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class AxiomSoapHeader extends AxiomSoapElement implements SoapHeader {

    AxiomSoapHeader(SOAPHeader axiomHeader, SOAPFactory axiomFactory) {
        super(axiomHeader, axiomFactory);
    }

    public Result getResult() {
        return new SAXResult(new AxiomContentHandler(getAxiomHeader()));
    }

    public SoapHeaderElement addHeaderElement(QName name) {
        try {
            OMNamespace namespace =
                    getAxiomFactory().createOMNamespace(name.getNamespaceURI(), QNameUtils.getPrefix(name));
            SOAPHeaderBlock axiomHeaderBlock = getAxiomHeader().addHeaderBlock(name.getLocalPart(), namespace);
            return new AxiomSoapHeaderElement(axiomHeaderBlock, getAxiomFactory());
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    public Iterator examineMustUnderstandHeaderElements(String role) {
        try {
            return new AxiomSoapHeaderElementIterator(getAxiomHeader().examineMustUnderstandHeaderBlocks(role));
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    public Iterator examineAllHeaderElements() {
        try {
            return new AxiomSoapHeaderElementIterator(getAxiomHeader().examineAllHeaderBlocks());
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    public Iterator examineHeaderElements(QName name) throws SoapHeaderException {
        try {
            return new AxiomSoapHeaderElementIterator(getAxiomHeader().getChildrenWithName(name));
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    protected SOAPHeader getAxiomHeader() {
        return (SOAPHeader) getAxiomElement();
    }

    protected class AxiomSoapHeaderElementIterator implements Iterator {

        private final Iterator axiomIterator;

        protected AxiomSoapHeaderElementIterator(Iterator axiomIterator) {
            this.axiomIterator = axiomIterator;
        }

        public boolean hasNext() {
            return axiomIterator.hasNext();
        }

        public Object next() {
            try {
                SOAPHeaderBlock axiomHeaderBlock = (SOAPHeaderBlock) axiomIterator.next();
                return new AxiomSoapHeaderElement(axiomHeaderBlock, getAxiomFactory());
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
