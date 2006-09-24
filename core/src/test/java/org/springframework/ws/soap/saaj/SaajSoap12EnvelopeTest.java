package org.springframework.ws.soap.saaj;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.soap12.AbstractSoap12EnvelopeTestCase;

public class SaajSoap12EnvelopeTest extends AbstractSoap12EnvelopeTestCase {

    protected SoapEnvelope createSoapEnvelope() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage saajMessage = messageFactory.createMessage();
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(saajMessage);
        return saajSoapMessage.getEnvelope();
    }
}
