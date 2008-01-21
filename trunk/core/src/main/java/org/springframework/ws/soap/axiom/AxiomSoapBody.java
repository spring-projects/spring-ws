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

import java.util.Iterator;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.xml.transform.StaxSource;

/**
 * Axiom-specific version of <code>org.springframework.ws.soap.Soap11Body</code>.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class AxiomSoapBody extends AxiomSoapElement implements SoapBody {

    private boolean payloadCaching;

    protected AxiomSoapBody(SOAPBody axiomBody, SOAPFactory axiomFactory, boolean payloadCaching) {
        super(axiomBody, axiomFactory);
        this.payloadCaching = payloadCaching;
    }

    public Source getPayloadSource() {
        try {
            OMElement payloadElement = getPayloadElement();
            XMLStreamReader streamReader;
            if (payloadElement == null) {
                return null;
            }
            else if (payloadCaching) {
                streamReader = payloadElement.getXMLStreamReader();
            }
            else {
                streamReader = payloadElement.getXMLStreamReaderWithoutCaching();
            }
            return new StaxSource(streamReader);
        }
        catch (OMException ex) {
            throw new AxiomSoapBodyException(ex);
        }
    }

    public Result getPayloadResult() {
        AxiomUtils.removeContents(getAxiomBody());
        return new SAXResult(new AxiomContentHandler(getAxiomBody(), getAxiomFactory()));
    }

    public boolean hasFault() {
        return getAxiomBody().hasFault();
    }

    public SoapFault getFault() {
        SOAPFault axiomFault = getAxiomBody().getFault();
        return axiomFault != null ? new AxiomSoap11Fault(axiomFault, getAxiomFactory()) : null;
    }

    private OMElement getPayloadElement() throws OMException {
        return getAxiomBody().getFirstElement();
    }

    protected void detachAllBodyChildren() {
        try {
            for (Iterator iterator = getAxiomBody().getChildElements(); iterator.hasNext();) {
                OMElement child = (OMElement) iterator.next();
                child.detach();
            }
        }
        catch (OMException ex) {
            throw new AxiomSoapBodyException(ex);
        }

    }

    protected final SOAPBody getAxiomBody() {
        return (SOAPBody) getAxiomElement();
    }
}
