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
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

class SaajSoapHeader extends SaajSoapElement implements SoapHeader {

    public SaajSoapHeader(SOAPHeader header, SaajImplementationStrategy strategy) {
        super(header, strategy);
    }

    public SoapHeaderElement addHeaderElement(QName name) throws SoapHeaderException {
        try {
            SOAPHeaderElement saajHeaderElement = getStrategy().addHeaderElement(getSaajHeader(), name);
            return new SaajSoapHeaderElement(saajHeaderElement, getStrategy());
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    public Iterator examineMustUnderstandHeaderElements(String role) {
        return new SaajSoapHeaderElementIterator(getStrategy().examineMustUnderstandHeaderElements(getSaajHeader(), role));
    }

    protected final SOAPHeader getSaajHeader() {
        return (SOAPHeader) getSaajElement();
    }

    private class SaajSoapHeaderElementIterator implements Iterator {

        private final Iterator saajIterator;

        private SaajSoapHeaderElementIterator(Iterator saajIterator) {
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            SOAPHeaderElement saajHeaderElement = (SOAPHeaderElement) saajIterator.next();
            return new SaajSoapHeaderElement(saajHeaderElement, getStrategy());
        }

        public void remove() {
            saajIterator.remove();
        }
    }

}
