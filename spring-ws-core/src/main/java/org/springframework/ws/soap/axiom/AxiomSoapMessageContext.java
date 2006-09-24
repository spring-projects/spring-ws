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

package org.springframework.ws.soap.axiom;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;
import org.springframework.ws.transport.TransportRequest;
import org.springframework.ws.transport.TransportResponse;

/**
 * AXIOM-specific implementation of the <code>SoapMessageContext</code> interface. Created by the
 * <code>AxiomSoapMessageContextFactory</code>.
 *
 * @author Arjen Poutsma
 * @see AxiomSoapMessageContextFactory
 */
public class AxiomSoapMessageContext extends AbstractSoapMessageContext {

    /**
     * Creates a new instance based on the given Axiom request message, and a SOAP factory.
     *
     * @param messageRequest the request message
     */
    public AxiomSoapMessageContext(AxiomSoapMessage messageRequest, TransportRequest transportRequest) {
        super(messageRequest, transportRequest);
    }

    protected SoapMessage createResponseSoapMessage() {
        SOAPFactory soapFactory = (SOAPFactory) getAxiomRequest().getSOAPEnvelope().getOMFactory();
        return new AxiomSoapMessage(soapFactory);
    }

    /**
     * Returns the request as an Axiom SOAP message.
     */
    public SOAPMessage getAxiomRequest() {
        return ((AxiomSoapMessage) getSoapRequest()).getAxiomMessage();
    }

    /**
     * Returns the response as an Axiom SOAP message.
     */
    public SOAPMessage getAxiomResponse() {
        return ((AxiomSoapMessage) getSoapResponse()).getAxiomMessage();
    }

    public void sendResponse(TransportResponse transportResponse) throws IOException {
        try {
            if (hasResponse()) {
                AxiomSoapMessage response = (AxiomSoapMessage) getSoapResponse();
                SOAPMessage axiomResponse = response.getAxiomMessage();
                String charsetEncoding = axiomResponse.getCharsetEncoding();

                OMOutputFormat format = new OMOutputFormat();
                format.setCharSetEncoding(charsetEncoding);
                format.setSOAP11(response.getVersion() == SoapVersion.SOAP_11);
                String contentType = format.getContentType();
                contentType += "; charset=\"" + charsetEncoding + "\"";

                transportResponse.addHeader("Content-Type", contentType);
                axiomResponse.serializeAndConsume(transportResponse.getOutputStream(), format);
            }
        }
        catch (XMLStreamException ex) {
            throw new AxiomSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
    }

}
