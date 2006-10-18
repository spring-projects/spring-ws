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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Source;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapElement;

abstract class SaajSoapElement implements SoapElement {

    private final SOAPElement saajElement;

    private final SaajImplementationStrategy strategy;

    protected SaajSoapElement(SOAPElement saajElement, SaajImplementationStrategy strategy) {
        Assert.notNull(saajElement, "No saajElement given");
        Assert.notNull(strategy, "No strategy given");
        this.saajElement = saajElement;
        this.strategy = strategy;
    }

    protected final SOAPElement getSaajElement() {
        return saajElement;
    }

    protected final SaajImplementationStrategy getStrategy() {
        return strategy;
    }

    public final QName getName() {
        return getStrategy().getName(saajElement);
    }

    public final Source getSource() {
        return getStrategy().getSource(saajElement);
    }
}
