package org.springframework.ws.soap.saaj.saaj13;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageTestCase;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class Saaj13Soap12MessageTest extends AbstractSoap12MessageTestCase {

    private SOAPMessage saajMessage;

    protected SoapMessage createSoapMessage() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        saajMessage = messageFactory.createMessage();
        return new Saaj13SoapMessage(saajMessage);
    }

    public void testGetPayloadSource() throws Exception {
        saajMessage.getSOAPBody().addChildElement("child");
        Source source = soapMessage.getPayloadSource();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    public void testGetPayloadSourceText() throws Exception {
        saajMessage.getSOAPBody().addTextNode(" ");
        saajMessage.getSOAPBody().addChildElement("child");
        Source source = soapMessage.getPayloadSource();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    public void testGetPayloadResult() throws Exception {
        StringSource source = new StringSource("<child/>");
        Result result = soapMessage.getPayloadResult();
        transformer.transform(source, result);
        assertTrue("No child nodes created", saajMessage.getSOAPBody().hasChildNodes());
        assertEquals("Invalid child node created", "child", saajMessage.getSOAPBody().getFirstChild().getLocalName());
    }

}
