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
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapElement;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ-specific implementation of the <code>SoapElement</code> interface. Wraps a {@link javax.xml.soap.SOAPElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapElement implements SoapElement {

    private final SOAPElement element;

    SaajSoapElement(SOAPElement element) {
        Assert.notNull(element, "element must not be null");
        this.element = element;
    }

    public Source getSource() {
        return getImplementation().getSource(element);
    }

    public QName getName() {
        return getImplementation().getName(element);
    }

    public void addAttribute(QName name, String value) {
        try {
            getImplementation().addAttribute(element, name, value);
        }
        catch (SOAPException ex) {
            throw new SaajSoapElementException(ex);
        }
    }

    public void removeAttribute(QName name) {
        try {
            getImplementation().removeAttribute(element, name);
        }
        catch (SOAPException ex) {
            throw new SaajSoapElementException(ex);
        }
    }

    public String getAttributeValue(QName name) {
        try {
            return getImplementation().getAttributeValue(element, name);
        }
        catch (SOAPException ex) {
            throw new SaajSoapElementException(ex);
        }
    }

    public Iterator getAllAttributes() {
        return getImplementation().getAllAttibutes(element);
    }

    public void addNamespaceDeclaration(String prefix, String namespaceUri) {
        try {
            getImplementation().addNamespaceDeclaration(element, prefix, namespaceUri);
        }
        catch (SOAPException ex) {
            throw new SaajSoapElementException(ex);
        }
    }

    protected final SOAPElement getSaajElement() {
        return element;
    }

    protected final SaajImplementation getImplementation() {
        if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_13) {
            return Saaj13Implementation.getInstance();
        }
        else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_12) {
            return Saaj12Implementation.getInstance();
        }
        else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_11) {
            return Saaj11Implementation.getInstance();
        }
        else {
            throw new IllegalStateException("Could not find SAAJ on the classpath");
        }
    }
}
