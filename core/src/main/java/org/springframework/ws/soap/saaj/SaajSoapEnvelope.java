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

import javax.xml.soap.SOAPEnvelope;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapEnvelope;

/**
 * Abstract base class for implementations of the <code>SoapEnvelope</code> interface that use SAAJ. Used by
 * <code>SaajSoapMessage</code>.
 *
 * @author Arjen Poutsma
 */
public abstract class SaajSoapEnvelope implements SoapEnvelope {

    private final SOAPEnvelope saajEnvelope;

    protected SaajSoapEnvelope(SOAPEnvelope saajEnvelope) {
        Assert.notNull(saajEnvelope, "No saajEnvelope given");
        this.saajEnvelope = saajEnvelope;
    }

    protected SOAPEnvelope getSaajEnvelope() {
        return saajEnvelope;
    }

    public Source getSource() {
        return new DOMSource(saajEnvelope);
    }
}
