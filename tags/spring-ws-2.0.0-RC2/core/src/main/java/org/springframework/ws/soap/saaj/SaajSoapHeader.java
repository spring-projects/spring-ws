/*
 * Copyright 2005-2010 the original author or authors.
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
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.transform.Result;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * SAAJ-specific implementation of the <code>SoapHeader</code> interface. Wraps a {@link javax.xml.soap.SOAPHeader}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class SaajSoapHeader extends SaajSoapElement<SOAPHeader> implements SoapHeader {

    SaajSoapHeader(SOAPHeader header) {
        super(header);
    }

    public Iterator<SoapHeaderElement> examineAllHeaderElements() throws SoapHeaderException {
        Iterator<SOAPHeaderElement> iterator = getImplementation().examineAllHeaderElements(getSaajHeader());
        return new SaajSoapHeaderElementIterator(iterator);
    }

    public Iterator<SoapHeaderElement> examineMustUnderstandHeaderElements(String actorOrRole) throws SoapHeaderException {
        Iterator<SOAPHeaderElement> iterator = getImplementation().examineMustUnderstandHeaderElements(getSaajHeader(), actorOrRole);
        return new SaajSoapHeaderElementIterator(iterator);
    }

    public SoapHeaderElement addHeaderElement(QName name) throws SoapHeaderException {
        try {
            SOAPHeaderElement headerElement = getImplementation().addHeaderElement(getSaajHeader(), name);
            return new SaajSoapHeaderElement(headerElement);
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    public void removeHeaderElement(QName name) throws SoapHeaderException {
        try {
            Iterator<SOAPElement> iterator = getImplementation().getChildElements(getSaajHeader(), name);
            if (iterator.hasNext()) {
                SOAPElement element = iterator.next();
                element.detachNode();
            }
        }
        catch (SOAPException ex) {
            throw new SaajSoapHeaderException(ex);
        }
    }

    protected SOAPHeader getSaajHeader() {
        return getSaajElement();
    }

    public Result getResult() {
        return getImplementation().getResult(getSaajHeader());
    }

    protected static class SaajSoapHeaderElementIterator implements Iterator<SoapHeaderElement> {

        private final Iterator<SOAPHeaderElement> iterator;

        protected SaajSoapHeaderElementIterator(Iterator<SOAPHeaderElement> iterator) {
            Assert.notNull(iterator, "iterator must not be null");
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public SoapHeaderElement next() {
            SOAPHeaderElement saajHeaderElement = iterator.next();
            return new SaajSoapHeaderElement(saajHeaderElement);
        }

        public void remove() {
            iterator.remove();
        }
    }

}
