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
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * @author Arjen Poutsma
 */
class Saaj13SoapFaultDetail implements SoapFaultDetail {

    private Detail saajDetail;

    Saaj13SoapFaultDetail(Detail saajDetail) {
        Assert.notNull(saajDetail, "No saajDetail given");
        this.saajDetail = saajDetail;
    }

    public QName getName() {
        return saajDetail.getElementQName();
    }

    public Source getSource() {
        return new DOMSource(saajDetail);
    }

    public SoapFaultDetailElement addFaultDetailElement(QName name) {
        try {
            DetailEntry saajDetailEntry = saajDetail.addDetailEntry(name);
            return new Saaj13SoapFaultDetailElement(saajDetailEntry);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public Iterator getDetailEntries() {
        return new Saaj13SoapFaultDetailIterator(saajDetail.getDetailEntries());
    }

    private static class Saaj13SoapFaultDetailElement implements SoapFaultDetailElement {

        private DetailEntry saajDetailEntry;

        private Saaj13SoapFaultDetailElement(DetailEntry saajDetailEntry) {
            Assert.notNull(saajDetailEntry, "No saajDetailEntry given");
            this.saajDetailEntry = saajDetailEntry;
        }

        public Result getResult() {
            return new DOMResult(saajDetailEntry);
        }

        public void addText(String text) {
            try {
                saajDetailEntry.addTextNode(text);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public QName getName() {
            return saajDetailEntry.getElementQName();
        }

        public Source getSource() {
            return new DOMSource(saajDetailEntry);
        }
    }

    private static class Saaj13SoapFaultDetailIterator implements Iterator {

        private final Iterator saajIterator;

        public Saaj13SoapFaultDetailIterator(Iterator saajIterator) {
            Assert.notNull(saajIterator, "No saajIterator given");
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            DetailEntry saajDetailEntry = (DetailEntry) saajIterator.next();
            return new Saaj13SoapFaultDetailElement(saajDetailEntry);
        }

        public void remove() {
            saajIterator.remove();
        }

    }

}
