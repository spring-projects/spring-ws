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

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPPart;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.saaj.SaajSoapBodyException;

/**
 * @author Arjen Poutsma
 */
public class Saaj11SoapEnvelope extends Saaj11SoapElement implements SoapEnvelope {

    private Saaj11Soap11Body body;

//    private Saaj11SoapHeader header;

    private final SOAPPart saajPart;

    public Saaj11SoapEnvelope(SOAPEnvelope saajEnvelope, SOAPPart saajPart) throws SOAPException {
        super(saajEnvelope);
        Assert.notNull(saajPart, "saajPart must not be null");
        this.saajPart = saajPart;
    }

    public final SoapBody getBody() {
        if (body == null) {
            try {
                SOAPBody saajBody = getSaajEnvelope().getBody();
                body = new Saaj11Soap11Body(saajBody);
            }
            catch (SOAPException ex) {
                throw new SaajSoapBodyException(ex);
            }
        }
        return body;
    }

    public final SoapHeader getHeader() {
        return null;
/*
        if (header == null) {
            try {
                SOAPHeader saajHeader = getSaajEnvelope().getHeader();
                if (saajHeader != null) {
                    header = new Saaj11SoapHeader(saajHeader);
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
*/
    }

    protected SOAPEnvelope getSaajEnvelope() {
        return (SOAPEnvelope) getSaajElement();
    }
}
