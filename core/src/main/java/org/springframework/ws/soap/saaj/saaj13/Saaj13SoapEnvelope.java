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

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.saaj.SaajSoapBodyException;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>SoapEnvelope</code> interface. Used by
 * <code>SaajSoapMessage</code>.
 *
 * @author Arjen Poutsma
 */
class Saaj13SoapEnvelope extends Saaj13SoapElement implements SoapEnvelope {

    private Saaj13SoapBody body;

    private Saaj13SoapHeader header;

    public Saaj13SoapEnvelope(SOAPEnvelope saajEnvelope) {
        super(saajEnvelope);
    }

    public final SoapBody getBody() {
        if (body == null) {
            try {
                SOAPBody saajBody = getSaajEnvelope().getBody();
                Saaj13SoapBody result;
                if (isSoap11()) {
                    result = new Saaj13Soap11Body(saajBody);
                }
                else {
                    result = new Saaj13Soap12Body(saajBody);
                }
                body = result;
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
                    if (isSoap11()) {
                        header = new Saaj13SoapHeader(saajHeader);
                    }
                    else {
                        header = new Saaj13Soap12Header(saajHeader);
                    }
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

    /**
     * Returns <code>true</code> if this is a SOAP 1.1 message, <code>false</code> if SOAP 1.2. Throws an exception
     * otherwise.
     */
    private boolean isSoap11() {
        String namespaceURI = getSaajEnvelope().getElementQName().getNamespaceURI();
        if (SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(namespaceURI)) {
            return true;
        }
        else if (SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(namespaceURI)) {
            return false;
        }
        else {
            throw new IllegalStateException("Unknown SOAP namespace \"" + namespaceURI + "\"");
        }
    }
}
