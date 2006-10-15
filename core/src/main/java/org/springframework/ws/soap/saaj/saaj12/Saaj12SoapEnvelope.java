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

package org.springframework.ws.soap.saaj.saaj12;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.saaj.SaajSoapBodyException;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;

/**
 * Internal class that uses SAAJ 1.2 to implement the <code>SoapEnvelope</code> interface. Used by
 * <code>SaajSoapMessage</code>.
 *
 * @author Arjen Poutsma
 */
class Saaj12SoapEnvelope extends Saaj12SoapElement implements SoapEnvelope {

    private Saaj12Soap11Body body;

    private Saaj12SoapHeader header;

    public Saaj12SoapEnvelope(SOAPEnvelope saajEnvelope) {
        super(saajEnvelope);
    }

    public final SoapBody getBody() {
        if (body == null) {
            try {
                body = new Saaj12Soap11Body(getSaajEnvelope().getBody());
            }
            catch (SOAPException ex) {
                throw new SaajSoapBodyException(ex);
            }
        }
        return body;
    }

    public final SoapHeader getHeader() {
        if (header == null) {
            try {
                SOAPHeader saajHeader = getSaajEnvelope().getHeader();
                if (saajHeader != null) {
                    header = new Saaj12SoapHeader(saajHeader);
                }
                else {
                    header = null;
                }
            }
            catch (SOAPException ex) {
                throw new SaajSoapHeaderException(ex);
            }
        }
        return header;
    }

    protected SOAPEnvelope getSaajEnvelope() {
        return (SOAPEnvelope) getSaajElement();
    }
}
