package org.springframework.ws.soap.context;

import java.util.Properties;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mock.MockTransportContext;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;

public abstract class AbstractSoap12MessageContextFactoryTestCase extends AbstractSoapMessageContextFactoryTestCase {

    public void testCreateContextNoAttachment() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "application/soap+xml");
        headers.setProperty("SOAPAction", "\"Some-URI\"");
        MockTransportContext transportContext = createTransportContext(headers, "soap12.xml");

        MessageContext messageContext = contextFactory.createContext(transportContext);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
    }

    public void testCreateContextAttachments() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type",
                "multipart/related; type=\"application/soap+xml\"; boundary=\"----=_Part_0_11416420.1149699787554\"");
        MockTransportContext transportContext = createTransportContext(headers, "soap12-attachment.bin");

        MessageContext messageContext = contextFactory.createContext(transportContext);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertEquals("Invalid soap version", SoapVersion.SOAP_12, requestMessage.getVersion());
        Attachment attachment = requestMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }

}
