package org.springframework.ws.soap.saaj;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.soap12.AbstractSoap12BodyTestCase;

public class SaajSoap12BodyTest extends AbstractSoap12BodyTestCase {

    protected SoapBody createSoapBody() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage saajMessage = messageFactory.createMessage();
        saajMessage.getSOAPPart().getEnvelope().setPrefix("soapenv");
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(saajMessage);
        return saajSoapMessage.getSoapBody();
    }

}
