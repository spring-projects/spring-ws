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

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapVersion;

/**
 * SAAJ-specific implementation of the <code>SoapEnvelope</code> interface. Wraps a {@link
 * javax.xml.soap.SOAPEnvelope}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapEnvelope extends SaajSoapElement implements SoapEnvelope {

    private SaajSoapBody body;

    private SaajSoapHeader header;

    private final boolean langAttributeOnSoap11FaulString;

    SaajSoapEnvelope(SOAPElement element, boolean langAttributeOnSoap11FaulString) {
        super(element);
        this.langAttributeOnSoap11FaulString = langAttributeOnSoap11FaulString;
    }

    public SoapBody getBody() {
        if (body == null) {
            try {
                SOAPBody saajBody = getImplementation().getBody(getSaajEnvelope());
                if (getImplementation().getName(saajBody).getNamespaceURI()
                        .equals(SoapVersion.SOAP_11.getEnvelopeNamespaceUri())) {
                    body = new SaajSoap11Body(saajBody, langAttributeOnSoap11FaulString);
                }
                else {
                    body = new SaajSoap12Body(saajBody);
                }
            }
            catch (SOAPException ex) {
                throw new SaajSoapBodyException(ex);
            }
        }
        return body;
    }

    public SoapHeader getHeader() {
        if (header == null) {
            try {
                SOAPHeader saajHeader = getImplementation().getHeader(getSaajEnvelope());
                if (saajHeader != null) {
                    if (getImplementation().getName(saajHeader).getNamespaceURI()
                            .equals(SoapVersion.SOAP_11.getEnvelopeNamespaceUri())) {
                        header = new SaajSoap11Header(saajHeader);
                    }
                    else {
                        header = new SaajSoap12Header(saajHeader);
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

}
