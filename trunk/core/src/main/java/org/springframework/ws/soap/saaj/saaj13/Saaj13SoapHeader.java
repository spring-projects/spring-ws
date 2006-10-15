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

package org.springframework.ws.soap.saaj.saaj13;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>SoapHeader</code> interface.
 *
 * @author Arjen Poutsma
 */
class Saaj13SoapHeader extends Saaj13SoapElement implements SoapHeader {

    Saaj13SoapHeader(SOAPHeader saajHeader) {
        super(saajHeader);
    }

    protected final SOAPHeader getSaajHeader() {
        return (SOAPHeader) getSaajElement();
    }

    public SoapHeaderElement addHeaderElement(QName name) {
        try {
            SOAPHeaderElement saajHeaderElement = getSaajHeader().addHeaderElement(name);
            return new Saaj13SoapHeaderElement(saajHeaderElement);
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    public Iterator examineMustUnderstandHeaderElements(String role) {
        return new SaajSoapHeaderElementIterator(getSaajHeader().examineMustUnderstandHeaderElements(role));
    }

    public Iterator examineAllHeaderElements() {
        return new SaajSoapHeaderElementIterator(getSaajHeader().examineAllHeaderElements());
    }

    private static class SaajSoapHeaderElementIterator implements Iterator {

        private final Iterator saajIterator;

        private SaajSoapHeaderElementIterator(Iterator saajIterator) {
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            SOAPHeaderElement saajHeaderElement = (SOAPHeaderElement) saajIterator.next();
            return new Saaj13SoapHeaderElement(saajHeaderElement);
        }

        public void remove() {
            saajIterator.remove();
        }
    }
}
