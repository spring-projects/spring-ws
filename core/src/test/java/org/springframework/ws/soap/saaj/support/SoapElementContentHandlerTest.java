package org.springframework.ws.soap.saaj.support;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.xml.transform.StringSource;

public class SoapElementContentHandlerTest extends XMLTestCase {

    private SOAPMessage message;

    private Transformer transformer;

    private SOAPMessage expected;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        message = messageFactory.createMessage();
        expected = messageFactory.createMessage();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }

    public void testWriteToPayload() throws Exception {
        StringSource contents = new StringSource(
                "<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>" + "<symbol>DIS</symbol>" +
                        "</m:GetLastTradePrice>");
        transformer.transform(contents, new DOMResult(expected.getSOAPBody()));
        SoapElementContentHandler handler =
                new SoapElementContentHandler(message.getSOAPPart().getEnvelope(), message.getSOAPBody());
        transformer.transform(new DOMSource(expected.getSOAPBody().getFirstChild()), new SAXResult(handler));
        assertXMLEqual(expected.getSOAPPart(), message.getSOAPPart());
    }
}