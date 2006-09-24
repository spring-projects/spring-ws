package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.soap11.AbstractSoap11HeaderTestCase;

public class AxiomSoap11HeaderTest extends AbstractSoap11HeaderTestCase {

    protected SoapHeader createSoapHeader() throws Exception {
        SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
        AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);
        return axiomSoapMessage.getSoapHeader();
    }

}
