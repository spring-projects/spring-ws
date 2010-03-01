/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.saaj.support;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessageException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class SaajUtilsTest {

    private MessageFactory messageFactory;

    @Before
    public void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }

    @Test
    public void testToName() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        QName qName = new QName("localPart");
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name name = SaajUtils.toName(qName, envelope);
        Assert.assertNotNull("Invalid name", name);
        Assert.assertEquals("Invalid local part", qName.getLocalPart(), name.getLocalName());
        Assert.assertFalse("Invalid prefix", StringUtils.hasLength(name.getPrefix()));
        Assert.assertFalse("Invalid namespace", StringUtils.hasLength(name.getURI()));
    }

    @Test
    public void testToNameNamespace() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        QName qName = new QName("namespace", "localPart");
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration("prefix", "namespace");
        Name name = SaajUtils.toName(qName, envelope);
        Assert.assertNotNull("Invalid name", name);
        Assert.assertEquals("Invalid namespace", qName.getNamespaceURI(), name.getURI());
        Assert.assertEquals("Invalid local part", qName.getLocalPart(), name.getLocalName());
        Assert.assertEquals("Invalid prefix", "prefix", name.getPrefix());
    }

    @Test
    public void testToNameNamespacePrefix() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        QName qName = new QName("namespace", "localPart", "prefix");
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name name = SaajUtils.toName(qName, envelope);
        Assert.assertNotNull("Invalid name", name);
        Assert.assertEquals("Invalid namespace", qName.getNamespaceURI(), name.getURI());
        Assert.assertEquals("Invalid local part", qName.getLocalPart(), name.getLocalName());
        Assert.assertEquals("Invalid prefix", qName.getPrefix(), name.getPrefix());
    }

    @Test
    public void testToQName() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        Name name = message.getSOAPPart().getEnvelope().createName("localPart", null, null);
        QName qName = SaajUtils.toQName(name);
        Assert.assertNotNull("Invalid qName", qName);
        Assert.assertEquals("Invalid namespace", name.getURI(), qName.getNamespaceURI());
        Assert.assertFalse("Invalid prefix", StringUtils.hasLength(qName.getPrefix()));
        Assert.assertFalse("Invalid namespace", StringUtils.hasLength(qName.getNamespaceURI()));
    }

    @Test
    public void testToQNameNamespace() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        Name name = message.getSOAPPart().getEnvelope().createName("localPart", null, "namespace");
        QName qName = SaajUtils.toQName(name);
        Assert.assertNotNull("Invalid qName", qName);
        Assert.assertEquals("Invalid namespace", name.getURI(), qName.getNamespaceURI());
        Assert.assertEquals("Invalid local part", name.getLocalName(), qName.getLocalPart());
        Assert.assertFalse("Invalid prefix", StringUtils.hasLength(qName.getPrefix()));
    }

    @Test
    public void testToQNamePrefixNamespace() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        Name name = message.getSOAPPart().getEnvelope().createName("localPart", "prefix", "namespace");
        QName qName = SaajUtils.toQName(name);
        Assert.assertNotNull("Invalid qName", qName);
        Assert.assertEquals("Invalid namespace", name.getURI(), qName.getNamespaceURI());
        Assert.assertEquals("Invalid local part", name.getLocalName(), qName.getLocalPart());
        Assert.assertEquals("Invalid prefix", name.getPrefix(), qName.getPrefix());
    }

    @Test
    public void testLoadMessage() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(getClass().getResourceAsStream("soapMessage.xml"));
        SOAPMessage soapMessage =
                SaajUtils.loadMessage(new ClassPathResource("soapMessage.xml", getClass()), messageFactory);
        assertXMLEqual(soapMessage.getSOAPPart(), document);
    }

    @Test
    public void testGetSaajVersion() throws Exception {
        Assert.assertEquals("Invalid SAAJ version", SaajUtils.SAAJ_13, SaajUtils.getSaajVersion());
    }
    
    @Test
    public void testGetSaajVersionInvalidEnvelope() throws Exception {
        Resource resource = new ClassPathResource("invalidNamespaceReferenceSoapMessage.xml", getClass());
    	InputStream in = null;
    	try {
            in = resource.getInputStream();
            MimeHeaders headers = new MimeHeaders();
            SOAPMessage soapMessage = messageFactory.createMessage(headers, in);
			SaajUtils.getSaajVersion(soapMessage);
    		Assert.fail(
                    "Should have thrown SaajSoapMessageException as message envelope is invalid and cannot be accessed.");
    	}
    	catch (SaajSoapMessageException e) {
    		// expected
    	}
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

}