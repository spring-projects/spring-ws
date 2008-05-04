package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.soap11.AbstractSoap11EnvelopeTestCase;

public class AxiomSoap11EnvelopeTest extends AbstractSoap11EnvelopeTestCase {

    protected SoapEnvelope createSoapEnvelope() throws Exception {
        SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
        AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);
        return axiomSoapMessage.getEnvelope();
    }
}
