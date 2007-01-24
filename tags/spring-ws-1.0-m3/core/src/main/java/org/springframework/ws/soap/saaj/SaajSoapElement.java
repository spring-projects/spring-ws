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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Source;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapElement;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ-specific implementation of the <code>SoapElement</code> interface. Wraps a {@link javax.xml.soap.SOAPElement}.
 *
 * @author Arjen Poutsma
 */
class SaajSoapElement implements SoapElement {

    private final SOAPElement element;

    public SaajSoapElement(SOAPElement element) {
        Assert.notNull(element, "element must not be null");
        this.element = element;
    }

    public Source getSource() {
        return getImplementation().getSource(element);
    }

    public QName getName() {
        return getImplementation().getName(element);
    }

    protected SOAPElement getSaajElement() {
        return element;
    }

    protected SaajImplementation getImplementation() {
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
