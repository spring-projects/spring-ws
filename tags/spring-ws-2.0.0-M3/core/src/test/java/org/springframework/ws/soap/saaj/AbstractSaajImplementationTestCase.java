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

package org.springframework.ws.soap.saaj;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public abstract class AbstractSaajImplementationTestCase {

    private SaajImplementation implementation;

    private SOAPMessage message;

    private SOAPEnvelope envelope;

    private SOAPBody body;

    private SOAPHeader header;

    @Before
    public final void setUp() throws Exception {
        implementation = createSaajImplementation();
        MessageFactory messageFactory = MessageFactory.newInstance();
        message = messageFactory.createMessage();
        envelope = message.getSOAPPart().getEnvelope();
        body = envelope.getBody();
        header = envelope.getHeader();
    }

    protected abstract SaajImplementation createSaajImplementation();

    @Test
    public void testGetName() throws Exception {
        QName name = implementation.getName(message.getSOAPPart().getEnvelope());
        Assert.assertEquals("Invalid name", SoapVersion.SOAP_11.getEnvelopeName(), name);
    }

    @Test
    public void testGetSource() throws Exception {
        Source source = implementation.getSource(message.getSOAPPart().getEnvelope().getBody());
        Assert.assertNotNull("No source returned", source);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("<Body xmlns='http://schemas.xmlsoap.org/soap/envelope/'/>", result.toString());
    }

    @Test
    public void testGetResult() throws Exception {
        Source source = new StringSource("<content xmlns='http://springframework.org/spring-ws'/>");
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, implementation.getResult(message.getSOAPPart().getEnvelope().getBody()));
    }

    @Test
    public void testGetEnvelope() throws Exception {
        SOAPEnvelope envelope = implementation.getEnvelope(message);
        Assert.assertEquals("Invalid envelope", message.getSOAPPart().getEnvelope(), envelope);
    }

    @Test
    public void testGetHeader() throws Exception {
        SOAPHeader header = implementation.getHeader(message.getSOAPPart().getEnvelope());
        Assert.assertEquals("Invalid header", message.getSOAPPart().getEnvelope().getHeader(), header);
    }

    @Test
    public void testGetBody() throws Exception {
        SOAPBody body = implementation.getBody(message.getSOAPPart().getEnvelope());
        Assert.assertEquals("Invalid body", message.getSOAPPart().getEnvelope().getBody(), body);
    }

    @Test
    public void testExampleAllHeaderElements() throws Exception {
        Iterator<SOAPHeaderElement> iterator = implementation.examineAllHeaderElements(header);
        Assert.assertFalse("Header elements present", iterator.hasNext());
        createHeaderElement();
        iterator = implementation.examineAllHeaderElements(header);
        Assert.assertTrue("No header elements present", iterator.hasNext());
    }

    @Test
    public void testExampleMustUnderstandHeaderElements() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        headerElement.setMustUnderstand(true);
        Iterator<SOAPHeaderElement> iterator = implementation.examineAllHeaderElements(header);
        Assert.assertTrue("No header elements present", iterator.hasNext());
    }

    @Test
    public void testAddHeaderElement() throws Exception {
        SOAPHeaderElement headerElement = implementation
                .addHeaderElement(header, new QName("http://springframework.org/spring-ws", "Header"));
        Assert.assertNotNull("No header element returned", headerElement);
        Assert.assertEquals("Invalid namespace", "http://springframework.org/spring-ws",
                headerElement.getElementName().getURI());
        Assert.assertEquals("Invalid local name", "Header", headerElement.getElementName().getLocalName());
    }

    @Test
    public void testGetActorOrRole() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        String actor = "http://springframework.org/spring-ws/Actor";
        headerElement.setActor(actor);
        Assert.assertEquals("Invalid actor", actor, implementation.getActorOrRole(headerElement));
    }

    private SOAPHeaderElement createHeaderElement() throws SOAPException {
        Name name = envelope.createName("Header", "", "http://springframework.org/spring-ws");
        return header.addHeaderElement(name);
    }

    @Test
    public void testSetActorOrRole() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        String actor = "http://springframework.org/spring-ws/Actor";
        implementation.setActorOrRole(headerElement, actor);
        Assert.assertEquals("Invalid actor", headerElement.getActor(), actor);
    }

    @Test
    public void testGetMustUnderstand() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        headerElement.setMustUnderstand(true);
        Assert.assertTrue("Invalid mustUnderstand", implementation.getMustUnderstand(headerElement));
    }

    @Test
    public void testSetMustUnderstand() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        implementation.setMustUnderstand(headerElement, true);
        Assert.assertTrue("Invalid mustUnderstand", headerElement.getMustUnderstand());
    }

    @Test
    public void testHasFault() throws Exception {
        Assert.assertFalse("Body has fault", implementation.hasFault(body));
        body.addFault();
        Assert.assertTrue("Body has no fault", implementation.hasFault(body));
    }

    @Test
    public void testGetFault() throws Exception {
        Assert.assertNull("Body has fault", implementation.getFault(body));
        body.addFault();
        Assert.assertNotNull("Body has no fault", implementation.getFault(body));
    }

    @Test
    public void testAddFault() throws Exception {
        implementation
                .addFault(body, new QName("http://springframework.org/spring-ws", "Fault"), "Fault", Locale.ENGLISH);
        Assert.assertTrue("No Fault added", body.hasFault());
    }

    @Test
    public void testGetFaultCode() throws Exception {
        SOAPFault fault = createFault();
        Assert.assertEquals("Invalid fault code", new QName("http://springframework.org/spring-ws", "Fault"),
                implementation.getFaultCode(fault));
    }

    private SOAPFault createFault() throws SOAPException {
        return body.addFault(new QName("http://springframework.org/spring-ws", "Fault"), "Fault", Locale.ENGLISH);
    }

    @Test
    public void testGetFaultActor() throws Exception {
        SOAPFault fault = createFault();
        String actor = "http://springframework.org/spring-ws/Actor";
        fault.setFaultActor(actor);
        Assert.assertEquals("Invalid actor", actor, implementation.getFaultActor(fault));
    }

    @Test
    public void testSetFaultActor() throws Exception {
        SOAPFault fault = createFault();
        String actor = "http://springframework.org/spring-ws/Actor";
        implementation.setFaultActor(fault, actor);
        Assert.assertEquals("Invalid actor", actor, fault.getFaultActor());
    }

    @Test
    public void testGetFaultString() throws Exception {
        SOAPFault fault = createFault();
        String faultString = "FaultString";
        fault.setFaultString(faultString);
        Assert.assertEquals("Invalid fault string", faultString, implementation.getFaultString(fault));
    }

    @Test
    public void testGetFaultStringLocale() throws Exception {
        SOAPFault fault = createFault();
        Assert.assertEquals("Invalid fault string", Locale.ENGLISH, implementation.getFaultStringLocale(fault));
    }

    @Test
    public void testGetFaultDetail() throws Exception {
        SOAPFault fault = createFault();
        Assert.assertNull("Fault Detail returned", implementation.getFaultDetail(fault));
        fault.addDetail();
        Assert.assertNotNull("No Fault Detail returned", implementation.getFaultDetail(fault));
    }

    @Test
    public void testAddFaultDetail() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = implementation.addFaultDetail(fault);
        Assert.assertEquals("Invalid fault detail", fault.getDetail(), detail);
    }

    @Test
    public void testAddDetailEntry() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = fault.addDetail();
        DetailEntry detailEntry =
                implementation.addDetailEntry(detail, new QName("http://springframework.org/spring-ws", "DetailEntry"));
        Assert.assertNotNull("No detail entry", detailEntry);
        Iterator<?> iterator = detail.getDetailEntries();
        Assert.assertTrue("No detail entry", iterator.hasNext());
        Assert.assertEquals("Invalid detail entry", detailEntry, iterator.next());
    }

    @Test
    public void testAddTextNode() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = fault.addDetail();
        Name name = envelope.createName("DetailEntry", "", "http://springframework.org/spring-ws");
        DetailEntry detailEntry = detail.addDetailEntry(name);
        implementation.addTextNode(detailEntry, "text");
        Assert.assertEquals("Invalid text", "text", detailEntry.getValue());
    }

    @Test
    public void testGetDetailEntries() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = fault.addDetail();
        Iterator<DetailEntry> iterator = implementation.getDetailEntries(detail);
        Assert.assertFalse("Detail entries found", iterator.hasNext());
        Name name = envelope.createName("DetailEntry", "", "http://springframework.org/spring-ws");
        DetailEntry detailEntry = detail.addDetailEntry(name);
        iterator = implementation.getDetailEntries(detail);
        Assert.assertTrue("No detail entries found", iterator.hasNext());
        Assert.assertEquals("Invalid detail entry found", detailEntry, iterator.next());
        Assert.assertFalse("Detail entries found", iterator.hasNext());
    }

    @Test
    public void testWriteTo() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        implementation.writeTo(message, os);
        Assert.assertTrue("Nothing written", os.toByteArray().length > 0);
    }

    @Test
    public void testGetAttachments() throws Exception {
        Iterator<AttachmentPart> iterator = implementation.getAttachments(message);
        Assert.assertFalse("Message has attachments", iterator.hasNext());
        AttachmentPart attachmentPart = message.createAttachmentPart();
        message.addAttachmentPart(attachmentPart);
        iterator = implementation.getAttachments(message);
        Assert.assertTrue("Message has no attachments", iterator.hasNext());
        Assert.assertEquals("Invalid attachment part", attachmentPart, iterator.next());
    }

    @Test
    public void testAddAttachmentPart() throws Exception {
        DataHandler dataHandler = new DataHandler("data", "text/plain");
        AttachmentPart attachmentPart = implementation.addAttachmentPart(message, dataHandler);
        Assert.assertNotNull("No attachment part", attachmentPart);
    }

    @Test
    public void testRemoveContents() throws Exception {
        body.addBodyElement(new QName("foo", "bar"));

        Assert.assertTrue("Body has child nodes", body.hasChildNodes());
        implementation.removeContents(message.getSOAPBody());
        Assert.assertFalse("Body has child nodes", body.hasChildNodes());
    }

}