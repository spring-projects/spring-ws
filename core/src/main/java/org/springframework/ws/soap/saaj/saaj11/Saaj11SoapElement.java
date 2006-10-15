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

package org.springframework.ws.soap.saaj.saaj11;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapElement;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.xml.sax.InputSource;

abstract class Saaj11SoapElement implements SoapElement {

    private final SOAPElement saajElement;

    protected Saaj11SoapElement(SOAPElement saajElement) {
        Assert.notNull(saajElement, "No saajElement given");
        this.saajElement = saajElement;
    }

    protected SOAPElement getSaajElement() {
        return saajElement;
    }

    public final Source getSource() {
        return new SAXSource(new Saaj12XmlReader(saajElement), new InputSource());
    }

    public final QName getName() {
        return SaajUtils.toQName(saajElement.getElementName());
    }

}
