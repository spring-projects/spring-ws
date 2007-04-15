package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.soap11.AbstractSoap11BodyTestCase;

public class AxiomSoap11BodyTest extends AbstractSoap11BodyTestCase {

    protected SoapBody createSoapBody() throws Exception {
        SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
        AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);
        return axiomSoapMessage.getSoapBody();
    }

}
