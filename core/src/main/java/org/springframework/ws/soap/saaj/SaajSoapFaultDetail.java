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
import javax.xml.soap.SOAPFaultElement;
import javax.xml.transform.Result;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * SAAJ-specific implementation of the <code>SoapFaultDetail</code> interface. Wraps a {@link
 * javax.xml.soap.SOAPFaultElement}.
 *
 * @author Arjen Poutsma
 */
class SaajSoapFaultDetail extends SaajSoapElement implements SoapFaultDetail {

    public SaajSoapFaultDetail(SOAPFaultElement faultElement) {
        super(faultElement);
    }

    public Result getResult() {
        return getImplementation().getResult(getSaajDetail());
    }

    public SoapFaultDetailElement addFaultDetailElement(QName name) {
        try {
            DetailEntry detailEntry = getImplementation().addDetailEntry(getSaajDetail(), name);
            return new SaajSoapFaultDetailElement(detailEntry);
        }
        catch (SOAPException ex) {
            throw new SaajSoapFaultException(ex);
        }
    }

    public Iterator getDetailEntries() {
        Iterator iterator = getImplementation().getDetailEntries(getSaajDetail());
        return new SaajSoapFaultDetailElementIterator(iterator);
    }

    protected Detail getSaajDetail() {
        return (Detail) getSaajElement();
    }

    private static class SaajSoapFaultDetailElementIterator implements Iterator {

        private final Iterator iterator;

        private SaajSoapFaultDetailElementIterator(Iterator iterator) {
            Assert.notNull(iterator, "No iterator given");
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            DetailEntry saajDetailEntry = (DetailEntry) iterator.next();
            return new SaajSoapFaultDetailElement(saajDetailEntry);
        }

        public void remove() {
            iterator.remove();
        }
    }


}
