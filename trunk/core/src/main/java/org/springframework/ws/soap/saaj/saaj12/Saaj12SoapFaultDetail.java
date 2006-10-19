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

package org.springframework.ws.soap.saaj.saaj12;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.saaj.SaajSoapFaultException;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * Internal class that uses SAAJ 1.2 to implement the <code>SoapFaultDetail</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj12SoapFaultDetail extends Saaj12SoapElement implements SoapFaultDetail {

    Saaj12SoapFaultDetail(Detail saajDetail) {
        super(saajDetail);
    }

    public SoapFaultDetailElement addFaultDetailElement(QName name) {
        try {
            Name detailEntryName = SaajUtils.toName(name, getSaajDetail());
            DetailEntry saajDetailEntry = getSaajDetail().addDetailEntry(detailEntryName);
            return new Saaj12SoapFaultDetailElement(saajDetailEntry);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public Iterator getDetailEntries() {
        return new Saaj12SoapFaultDetailIterator(getSaajDetail().getDetailEntries());
    }

    public Result getResult() {
        return new DOMResult(getSaajDetail());
    }

    private Detail getSaajDetail() {
        return (Detail) getSaajElement();
    }

    private static class Saaj12SoapFaultDetailIterator implements Iterator {

        private final Iterator saajIterator;

        public Saaj12SoapFaultDetailIterator(Iterator saajIterator) {
            Assert.notNull(saajIterator, "No saajIterator given");
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            DetailEntry saajDetailEntry = (DetailEntry) saajIterator.next();
            return new Saaj12SoapFaultDetailElement(saajDetailEntry);
        }

        public void remove() {
            saajIterator.remove();
        }
    }
}
