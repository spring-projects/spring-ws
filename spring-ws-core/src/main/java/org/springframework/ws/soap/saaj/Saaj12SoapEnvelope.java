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
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * Internal class that uses SAAJ 1.2 to implement the <code>SoapEnvelope</code> interface. Used by
 * <code>SaajSoapMessage</code>.
 *
 * @author Arjen Poutsma
 */
class Saaj12SoapEnvelope implements SoapEnvelope {

    private final SOAPEnvelope saajEnvelope;

    private Saaj12SoapHeader header;

    private Saaj12Soap11Body body;

    Saaj12SoapEnvelope(SOAPEnvelope saajEnvelope) {
        Assert.notNull(saajEnvelope, "No saajEnvelope given");
        this.saajEnvelope = saajEnvelope;
    }

    public QName getName() {
        return SaajUtils.toQName(saajEnvelope.getElementName());
    }

    public Source getSource() {
        return new DOMSource(saajEnvelope);
    }

    public SoapHeader getHeader() {
        if (header != null) {
            try {
                header = saajEnvelope.getHeader() != null ? new Saaj12SoapHeader(saajEnvelope.getHeader()) : null;
            }
            catch (SOAPException ex) {
                throw new SaajSoapHeaderException(ex);
            }
        }
        return header;
    }

    public SoapBody getBody() {
        if (body == null) {
            try {
                body = new Saaj12Soap11Body(saajEnvelope.getBody());
            }
            catch (SOAPException ex) {
                throw new SaajSoapBodyException(ex);
            }
        }
        return body;
    }


}
