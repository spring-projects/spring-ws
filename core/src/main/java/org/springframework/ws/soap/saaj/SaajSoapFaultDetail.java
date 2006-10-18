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
 *  * limitations under the License.
 */

package org.springframework.ws.soap.saaj;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;

class SaajSoapFaultDetail extends SaajSoapElement implements SoapFaultDetail {

    public SaajSoapFaultDetail(Detail detail, SaajImplementationStrategy strategy) {
        super(detail, strategy);
    }

    public SoapFaultDetailElement addFaultDetailElement(QName name) {
        try {
            DetailEntry detailEntry = getStrategy().addDetailEntry(getSaajDetail(), name);
            return new SaajSoapFaultDetailElement(detailEntry, getStrategy());
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public Result getResult() {
        return getStrategy().getResult(getSaajDetail());
    }

    public Iterator getDetailEntries() {
        return new SaajSoapFaultDetailIterator(getSaajDetail().getDetailEntries());
    }

    protected Detail getSaajDetail() {
        return (Detail) getSaajElement();
    }

    private class SaajSoapFaultDetailIterator implements Iterator {

        private final Iterator saajIterator;

        public SaajSoapFaultDetailIterator(Iterator saajIterator) {
            Assert.notNull(saajIterator, "No saajIterator given");
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            DetailEntry saajDetailEntry = (DetailEntry) saajIterator.next();
            return new SaajSoapFaultDetailElement(saajDetailEntry, getStrategy());
        }

        public void remove() {
            saajIterator.remove();
        }
    }

}
