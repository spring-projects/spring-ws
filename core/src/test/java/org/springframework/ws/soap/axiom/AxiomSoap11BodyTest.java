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

    public void testAddMustUnderstandFault() throws Exception {
        // Overriden because of http://issues.apache.org/jira/browse/WSCOMMONS-38
    }

    public void testAddClientFault() throws Exception {
        // Overriden because of http://issues.apache.org/jira/browse/WSCOMMONS-38
    }

    public void testAddServerFault() throws Exception {
        // Overriden because of http://issues.apache.org/jira/browse/WSCOMMONS-38
    }

    public void testAddFault() throws Exception {
        // Overriden because of http://issues.apache.org/jira/browse/WSCOMMONS-38
    }

    public void testAddFaultWithDetail() throws Exception {
        // Overriden because of http://issues.apache.org/jira/browse/WSCOMMONS-38
    }

    public void testAddFaultWithDetailResult() throws Exception {
        // Overriden because of http://issues.apache.org/jira/browse/WSCOMMONS-38
    }
}
