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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.xml.transform.StaxSource;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.SoapFaultDetail</code>.
 *
 * @author Arjen Poutsma
 */
class AxiomSoapFaultDetail implements SoapFaultDetail {

    private final SOAPFaultDetail axiomFaultDetail;

    private final SOAPFactory axiomFactory;

    public AxiomSoapFaultDetail(SOAPFaultDetail axiomFaultDetail, SOAPFactory axiomFactory) {
        Assert.notNull(axiomFaultDetail, "No axiomFaultDetail given");
        Assert.notNull(axiomFactory, "No axiomFactory given");
        this.axiomFaultDetail = axiomFaultDetail;
        this.axiomFactory = axiomFactory;
    }

    public SoapFaultDetailElement addFaultDetailElement(QName name) {
        try {
            OMElement element = axiomFactory.createOMElement(name, axiomFaultDetail);
            return new AxiomSoapFaultDetailElement(element);
        }
        catch (OMException ex) {
            throw new AxiomSoapFaultException(ex);
        }

    }

    public Iterator getDetailEntries() {
        return new AxiomSoapFaultDetailElementIterator(axiomFaultDetail.getAllDetailEntries());
    }

    public QName getName() {
        return axiomFaultDetail.getQName();
    }

    public Source getSource() {
        try {
            return new StaxSource(axiomFaultDetail.getXMLStreamReader());
        }
        catch (OMException ex) {
            throw new AxiomSoapFaultException(ex);
        }
    }

    public Result getResult() {
        return new SAXResult(new AxiomContentHandler(axiomFaultDetail));
    }

    private class AxiomSoapFaultDetailElementIterator implements Iterator {

        private final Iterator axiomIterator;

        private AxiomSoapFaultDetailElementIterator(Iterator axiomIterator) {
            this.axiomIterator = axiomIterator;
        }

        public boolean hasNext() {
            return axiomIterator.hasNext();
        }

        public Object next() {
            try {
                OMElement axiomElement = (OMElement) axiomIterator.next();
                return new AxiomSoapFaultDetailElement(axiomElement);
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public void remove() {
            axiomIterator.remove();
        }
    }

}
