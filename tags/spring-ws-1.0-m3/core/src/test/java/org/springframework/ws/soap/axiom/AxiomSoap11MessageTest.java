package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageTestCase;

public class AxiomSoap11MessageTest extends AbstractSoap11MessageTestCase {

    protected SoapMessage createSoapMessage() throws Exception {
        SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
        return new AxiomSoapMessage(axiomFactory);
    }

    public void testAttachments() throws Exception {
        // Attachment support not supported
    }

    public void testWriteToTransportResponseAttachment() throws Exception {
        // Attachment support not supported
    }
}
