/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Document;

public abstract class AbstractWsAddressingTestCase extends XMLTestCase {

    protected MessageFactory messageFactory;

    protected final void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        XMLUnit.setIgnoreWhitespace(true);
        onSetUp();
    }

    protected void onSetUp() throws Exception {
    }

    protected SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", " application/soap+xml");
        InputStream is = getClass().getResourceAsStream(fileName);
        assertNotNull("Could not load " + fileName, is);
        try {
            return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
        }
        finally {
            is.close();
        }
    }

    protected void assertXMLEqual(String message, SaajSoapMessage expected, SaajSoapMessage result) {
        Document expectedDocument = expected.getSaajMessage().getSOAPPart();
        Document resultDocument = result.getSaajMessage().getSOAPPart();
        assertXMLEqual(message, expectedDocument, resultDocument);
    }
}
