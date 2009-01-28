package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageTestCase;

public class AxiomSoap12NonCachingMessageTest extends AbstractSoap12MessageTestCase {

    protected SoapMessage createSoapMessage() throws Exception {
        AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
        messageFactory.setPayloadCaching(false);
        messageFactory.setSoapVersion(SoapVersion.SOAP_12);

        return (SoapMessage) messageFactory.createWebServiceMessage();
    }

    public void testWriteToTransportOutputStream() throws Exception {
        super.testWriteToTransportOutputStream();

        SoapBody body = soapMessage.getSoapBody();
        OMSourcedElementImpl axiomPayloadEle =
                (OMSourcedElementImpl) ((AxiomSoapBody) body).getAxiomElement().getFirstElement();
        assertFalse("Non-cached body should not be expanded now", axiomPayloadEle.isExpanded());
        axiomPayloadEle.getFirstElement();
        assertTrue("Non-cached body should now be expanded", axiomPayloadEle.isExpanded());
        assertEquals("Invalid payload", "payload", axiomPayloadEle.getLocalName());
    }
}
