package org.springframework.ws.soap.saaj;

import javax.xml.soap.SOAPConstants;

import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.context.AbstractSoap12MessageContextFactoryTestCase;

public class SaajSoap12MessageContextFactoryTest extends AbstractSoap12MessageContextFactoryTestCase {

    protected MessageContextFactory createSoapMessageContextFactory() throws Exception {
        SaajSoapMessageContextFactory contextFactory = new SaajSoapMessageContextFactory();
        contextFactory.setSoapProtocol(SOAPConstants.SOAP_1_2_PROTOCOL);
        return contextFactory;
    }

}
