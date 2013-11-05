/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.soap11;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.AbstractSoapMessageFactoryTestCase;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;

import org.junit.Test;

import static org.junit.Assert.*;

public abstract class AbstractSoap11MessageFactoryTestCase extends AbstractSoapMessageFactoryTestCase {

    @Test
    public void testCreateEmptySoap11Message() throws Exception {
        WebServiceMessage message = messageFactory.createWebServiceMessage();
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, soapMessage.getVersion());
    }

    @Override
    public void testCreateSoapMessageNoAttachment() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11.xml");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "text/xml");
        String soapAction = "\"http://springframework.org/spring-ws/Action\"";
        headers.put("SOAPAction", soapAction);
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, soapMessage.getVersion());
        assertEquals("Invalid soap action", soapAction, soapMessage.getSoapAction());
        assertFalse("Message a XOP pacakge", soapMessage.isXopPackage());
    }

    @Override
    public void testCreateSoapMessageIllFormedXml() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-ill-formed.xml");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "text/xml");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        messageFactory.createWebServiceMessage(tis);
    }

    @Override
    public void testCreateSoapMessageSwA() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-attachment.bin");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type",
                "multipart/related;" + "type=\"text/xml\";" + "boundary=\"----=_Part_0_11416420.1149699787554\"");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, soapMessage.getVersion());
        assertFalse("Message a XOP package", soapMessage.isXopPackage());
        Iterator<Attachment> iter = soapMessage.getAttachments();
        assertTrue("No attachments read", iter.hasNext());
        Attachment attachment = soapMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
        assertEquals("Invalid content id", "interface21", attachment.getContentId());
    }

    @Override
    public void testCreateSoapMessageMtom() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-mtom.bin");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "multipart/related;" + "start-info=\"text/xml\";" +
                "type=\"application/xop+xml\";" + "start=\"<0.urn:uuid:492264AB42E57108E01176731445508@apache.org>\";" +
                "boundary=\"MIMEBoundaryurn_uuid_492264AB42E57108E01176731445507\"");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, soapMessage.getVersion());
        assertTrue("Message not a XOP package", soapMessage.isXopPackage());
        Iterator<Attachment> iter = soapMessage.getAttachments();
        assertTrue("No attachments read", iter.hasNext());

        Attachment attachment = soapMessage.getAttachment("<1.urn:uuid:492264AB42E57108E01176731445504@apache.org>");
        assertNotNull("No attachment read", attachment);
    }

    @Test
    public void testCreateSoapMessageMtomWeirdStartInfo() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-mtom.bin");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "multipart/related;" + "startinfo=\"text/xml\";" +
                "type=\"application/xop+xml\";" + "start=\"<0.urn:uuid:492264AB42E57108E01176731445508@apache.org>\";" +
                "boundary=\"MIMEBoundaryurn_uuid_492264AB42E57108E01176731445507\"");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        WebServiceMessage message = messageFactory.createWebServiceMessage(tis);
        assertTrue("Not a SoapMessage", message instanceof SoapMessage);
        SoapMessage soapMessage = (SoapMessage) message;
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, soapMessage.getVersion());
        assertTrue("Message not a XOP package", soapMessage.isXopPackage());
        Iterator<Attachment> iter = soapMessage.getAttachments();
        assertTrue("No attachments read", iter.hasNext());

        Attachment attachment = soapMessage.getAttachment("<1.urn:uuid:492264AB42E57108E01176731445504@apache.org>");
        assertNotNull("No attachment read", attachment);
    }

    @Test
    public void testCreateSoapMessageUtf8ByteOrderMark() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-utf8-bom.xml");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "text/xml; charset=UTF-8");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        SoapMessage message = (SoapMessage) messageFactory.createWebServiceMessage(tis);
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, message.getVersion());
    }

    @Test
    public void testCreateSoapMessageUtf16BigEndianByteOrderMark() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-utf16-be-bom.xml");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "text/xml; charset=UTF-16");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        SoapMessage message = (SoapMessage) messageFactory.createWebServiceMessage(tis);
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, message.getVersion());
    }

    @Test
    public void testCreateSoapMessageUtf16LittleEndianByteOrderMark() throws Exception {
        InputStream is = AbstractSoap11MessageFactoryTestCase.class.getResourceAsStream("soap11-utf16-le-bom.xml");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "text/xml; charset=UTF-16");
        TransportInputStream tis = new MockTransportInputStream(is, headers);

        SoapMessage message = (SoapMessage) messageFactory.createWebServiceMessage(tis);
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, message.getVersion());
    }


}
