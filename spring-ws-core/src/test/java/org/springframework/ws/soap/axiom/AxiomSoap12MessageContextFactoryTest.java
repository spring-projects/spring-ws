package org.springframework.ws.soap.axiom;

import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.soap.context.AbstractSoap12MessageContextFactoryTestCase;

public class AxiomSoap12MessageContextFactoryTest extends AbstractSoap12MessageContextFactoryTestCase {

    protected MessageContextFactory createSoapMessageContextFactory() {
        return new AxiomSoapMessageContextFactory();
    }

    public void testCreateContextAttachments() throws Exception {
        // Axiom does not support SwA with SOAP 1.2
    }
}
