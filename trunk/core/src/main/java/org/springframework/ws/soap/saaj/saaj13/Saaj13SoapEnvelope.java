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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.saaj.SaajSoapBodyException;
import org.springframework.ws.soap.saaj.SaajSoapEnvelope;
import org.springframework.ws.soap.saaj.SaajSoapEnvelopeException;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;

/**
 * Internal class that uses SAAJ 1.3 to implement the <code>SoapEnvelope</code> interface. Used by
 * <code>SaajSoapMessage</code>.
 *
 * @author Arjen Poutsma
 */
public class Saaj13SoapEnvelope extends SaajSoapEnvelope {

    private Saaj13SoapHeader header;

    private Saaj13SoapBody body;

    public Saaj13SoapEnvelope(SOAPEnvelope saajEnvelope) {
        super(saajEnvelope);
        Assert.notNull(saajEnvelope, "No saajEnvelope given");
    }

    public QName getName() {
        return getSaajEnvelope().getElementQName();
    }

    public SoapHeader getHeader() {
        if (header == null) {
            try {
                if (getSaajEnvelope().getHeader() == null) {
                    return null;
                }
                else {
                    SOAPHeader saajHeader = getSaajEnvelope().getHeader();
                    String namespaceURI = getSaajEnvelope().getElementQName().getNamespaceURI();
                    if (SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(namespaceURI)) {
                        header = new Saaj13SoapHeader(saajHeader);
                    }
                    else if (SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(namespaceURI)) {
                        header = new Saaj13Soap12Header(saajHeader);
                    }
                    else {
                        throw new SaajSoapEnvelopeException("Unknown SOAP namespace \"" + namespaceURI + "\"");
                    }
                }
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
                SOAPBody saajBody = getSaajEnvelope().getBody();
                String namespaceURI = getSaajEnvelope().getElementQName().getNamespaceURI();
                if (SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(namespaceURI)) {
                    body = new Saaj13Soap11Body(saajBody);
                }
                else if (SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(namespaceURI)) {
                    body = new Saaj13Soap12Body(saajBody);
                }
                else {
                    throw new SaajSoapEnvelopeException("Unknown SOAP namespace \"" + namespaceURI + "\"");
                }

            }
            catch (SOAPException ex) {
                throw new SaajSoapBodyException(ex);
            }
        }
        return body;
    }

}
