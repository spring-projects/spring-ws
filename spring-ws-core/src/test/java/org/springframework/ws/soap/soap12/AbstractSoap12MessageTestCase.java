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

package org.springframework.ws.soap.soap12;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.AbstractSoapMessageTestCase;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.MockTransportOutputStream;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.xml.transform.StringSource;

import junit.framework.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

public abstract class AbstractSoap12MessageTestCase extends AbstractSoapMessageTestCase {

    @Override
    public void testGetVersion() throws Exception {
        Assert.assertEquals("Invalid SOAP version", SoapVersion.SOAP_12, soapMessage.getVersion());
    }

    @Override
    protected final Resource[] getSoapSchemas() {
        return new Resource[]{new ClassPathResource("xml.xsd", AbstractSoap12MessageTestCase.class),
                new ClassPathResource("soap12.xsd", AbstractSoap12MessageTestCase.class)};
    }

    @Override
    public void testWriteToTransportOutputStream() throws Exception {
        SoapBody body = soapMessage.getSoapBody();
        String payload = "<payload xmlns='http://www.springframework.org' />";
        transformer.transform(new StringSource(payload), body.getPayloadResult());

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MockTransportOutputStream tos = new MockTransportOutputStream(bos);
        String soapAction = "http://springframework.org/spring-ws/Action";
        soapMessage.setSoapAction(soapAction);
        soapMessage.writeTo(tos);
        String result = bos.toString("UTF-8");
        assertXMLEqual(
                "<Envelope xmlns='http://www.w3.org/2003/05/soap-envelope'><Body><payload xmlns='http://www.springframework.org' /></Body></Envelope>",
                result);
        String contentType = tos.getHeaders().get(TransportConstants.HEADER_CONTENT_TYPE);
        assertTrue("Invalid Content-Type set",
		        contentType.contains(SoapVersion.SOAP_12.getContentType()));
        assertNull(TransportConstants.HEADER_SOAP_ACTION + " header must not be found",
                tos.getHeaders().get(TransportConstants.HEADER_SOAP_ACTION));
        String resultAccept = tos.getHeaders().get("Accept");
        assertNotNull("Invalid accept header", resultAccept);
    }

    @Override
    public void testWriteToTransportResponseAttachment() throws Exception {
        InputStreamSource inputStreamSource = new ByteArrayResource("contents".getBytes("UTF-8"));
        soapMessage.addAttachment("contentId", inputStreamSource, "text/plain");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MockTransportOutputStream tos = new MockTransportOutputStream(bos);
        soapMessage.writeTo(tos);
        String contentType = tos.getHeaders().get("Content-Type");
        assertTrue("Content-Type for attachment message does not contains multipart/related",
		        contentType.contains("multipart/related"));
        assertTrue("Content-Type for attachment message does not contains type=\"application/soap+xml\"",
		        contentType.contains("type=\"application/soap+xml\""));
    }

    @Override
    public void testToDocument() throws Exception {
        transformer.transform(new StringSource("<payload xmlns='http://www.springframework.org' />"),
                soapMessage.getSoapBody().getPayloadResult());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document expected = documentBuilder.newDocument();
        Element envelope = expected.createElementNS("http://www.w3.org/2003/05/soap-envelope", "Envelope");
        expected.appendChild(envelope);
        Element body = expected.createElementNS("http://www.w3.org/2003/05/soap-envelope", "Body");
        envelope.appendChild(body);
        Element payload = expected.createElementNS("http://www.springframework.org", "payload");
        body.appendChild(payload);

        Document result = soapMessage.getDocument();

        assertXMLEqual(expected, result);
    }

    @Override
    public void testSetLiveDocument() throws Exception {
        transformer.transform(new StringSource("<payload xmlns='http://www.springframework.org' />"),
                soapMessage.getSoapBody().getPayloadResult());

        Document document = soapMessage.getDocument();

        soapMessage.setDocument(document);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        soapMessage.writeTo(bos);

        String result = bos.toString("UTF-8");
        assertXMLEqual(
                "<Envelope xmlns='http://www.w3.org/2003/05/soap-envelope'><Body><payload xmlns='http://www.springframework.org' /></Body></Envelope>",                
                result);
    }

    @Override
    public void testSetOtherDocument() throws Exception {
        transformer.transform(new StringSource("<payload xmlns='http://www.springframework.org' />"),
                soapMessage.getSoapBody().getPayloadResult());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        soapMessage.writeTo(bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        DOMResult domResult = new DOMResult();
        transformer.transform(new StreamSource(bis), domResult);

        Document document = (Document) domResult.getNode();

        soapMessage.setDocument(document);

        bos = new ByteArrayOutputStream();
        soapMessage.writeTo(bos);

        String result = bos.toString("UTF-8");
        assertXMLEqual(
                "<Envelope xmlns='http://www.w3.org/2003/05/soap-envelope'><Body><payload xmlns='http://www.springframework.org' /></Body></Envelope>",
                result);
    }




}
