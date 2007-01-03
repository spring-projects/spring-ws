/*
 * Copyright 2007 the original author or authors.
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
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
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

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSaajImplementationTestCase extends XMLTestCase {

    private SaajImplementation implementation;

    private SOAPMessage message;

    private SOAPEnvelope envelope;

    private SOAPBody body;

    private SOAPHeader header;

    protected final void setUp() throws Exception {
        implementation = createSaajImplementation();
        MessageFactory messageFactory = MessageFactory.newInstance();
        message = messageFactory.createMessage();
        envelope = message.getSOAPPart().getEnvelope();
        body = envelope.getBody();
        header = envelope.getHeader();
    }

    protected abstract SaajImplementation createSaajImplementation();

    public void testGetName() throws Exception {
        QName name = implementation.getName(message.getSOAPPart().getEnvelope());
        assertEquals("Invalid name", SoapVersion.SOAP_11.getEnvelopeName(), name);
    }

    public void testGetSource() throws Exception {
        Source source = implementation.getSource(message.getSOAPPart().getEnvelope().getBody());
        assertNotNull("No source returned", source);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("<Body xmlns='http://schemas.xmlsoap.org/soap/envelope/'/>", result.toString());
    }

    public void testGetResult() throws Exception {
        Source source = new StringSource("<content xmlns='http://springframework.org/spring-ws'/>");
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, implementation.getResult(message.getSOAPPart().getEnvelope().getBody()));
    }

    public void testGetEnvelope() throws Exception {
        SOAPEnvelope envelope = implementation.getEnvelope(message);
        assertEquals("Invalid envelope", message.getSOAPPart().getEnvelope(), envelope);
    }

    public void testGetHeader() throws Exception {
        SOAPHeader header = implementation.getHeader(message.getSOAPPart().getEnvelope());
        assertEquals("Invalid header", message.getSOAPPart().getEnvelope().getHeader(), header);
    }

    public void testGetBody() throws Exception {
        SOAPBody body = implementation.getBody(message.getSOAPPart().getEnvelope());
        assertEquals("Invalid body", message.getSOAPPart().getEnvelope().getBody(), body);
    }

    public void testExampleAllHeaderElements() throws Exception {
        Iterator iterator = implementation.examineAllHeaderElements(header);
        assertFalse("Header elements present", iterator.hasNext());
        createHeaderElement();
        iterator = implementation.examineAllHeaderElements(header);
        assertTrue("No header elements present", iterator.hasNext());
    }

    public void testExampleMustUnderstandHeaderElements() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        headerElement.setMustUnderstand(true);
        Iterator iterator = implementation.examineAllHeaderElements(header);
        assertTrue("No header elements present", iterator.hasNext());
    }

    public void testAddHeaderElement() throws Exception {
        SOAPHeaderElement headerElement = implementation
                .addHeaderElement(header, new QName("http://springframework.org/spring-ws", "Header"));
        assertNotNull("No header element returned", headerElement);
        assertEquals("Invalid namespace", "http://springframework.org/spring-ws",
                headerElement.getElementName().getURI());
        assertEquals("Invalid local name", "Header", headerElement.getElementName().getLocalName());
    }

    public void testGetActorOrRole() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        String actor = "http://springframework.org/spring-ws/Actor";
        headerElement.setActor(actor);
        assertEquals("Invalid actor", actor, implementation.getActorOrRole(headerElement));
    }

    private SOAPHeaderElement createHeaderElement() throws SOAPException {
        Name name = envelope.createName("Header", "", "http://springframework.org/spring-ws");
        return header.addHeaderElement(name);
    }

    public void testSetActorOrRole() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        String actor = "http://springframework.org/spring-ws/Actor";
        implementation.setActorOrRole(headerElement, actor);
        assertEquals("Invalid actor", headerElement.getActor(), actor);
    }

    public void testGetMustUnderstand() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        headerElement.setMustUnderstand(true);
        assertTrue("Invalid mustUnderstand", implementation.getMustUnderstand(headerElement));
    }

    public void testSetMustUnderstand() throws Exception {
        SOAPHeaderElement headerElement = createHeaderElement();
        implementation.setMustUnderstand(headerElement, true);
        assertTrue("Invalid mustUnderstand", headerElement.getMustUnderstand());
    }

    public void testHasFault() throws Exception {
        assertFalse("Body has fault", implementation.hasFault(body));
        body.addFault();
        assertTrue("Body has no fault", implementation.hasFault(body));
    }

    public void testGetFault() throws Exception {
        assertNull("Body has fault", implementation.getFault(body));
        body.addFault();
        assertNotNull("Body has no fault", implementation.getFault(body));
    }

    public void testAddFault() throws Exception {
        implementation
                .addFault(body, new QName("http://springframework.org/spring-ws", "Fault"), "Fault", Locale.ENGLISH);
        assertTrue("No Fault added", body.hasFault());
    }

    public void testGetFaultCode() throws Exception {
        SOAPFault fault = createFault();
        assertEquals("Invalid fault code", new QName("http://springframework.org/spring-ws", "Fault"),
                implementation.getFaultCode(fault));
    }

    private SOAPFault createFault() throws SOAPException {
        return body.addFault(new QName("http://springframework.org/spring-ws", "Fault"), "Fault", Locale.ENGLISH);
    }

    public void testGetFaultActor() throws Exception {
        SOAPFault fault = createFault();
        String actor = "http://springframework.org/spring-ws/Actor";
        fault.setFaultActor(actor);
        assertEquals("Invalid actor", actor, implementation.getFaultActor(fault));
    }

    public void testSetFaultActor() throws Exception {
        SOAPFault fault = createFault();
        String actor = "http://springframework.org/spring-ws/Actor";
        implementation.setFaultActor(fault, actor);
        assertEquals("Invalid actor", actor, fault.getFaultActor());
    }

    public void testGetFaultString() throws Exception {
        SOAPFault fault = createFault();
        String faultString = "FaultString";
        fault.setFaultString(faultString);
        assertEquals("Invalid fault string", faultString, implementation.getFaultString(fault));
    }

    public void testGetFaultStringLocale() throws Exception {
        SOAPFault fault = createFault();
        assertEquals("Invalid fault string", Locale.ENGLISH, implementation.getFaultStringLocale(fault));
    }

    public void testGetFaultDetail() throws Exception {
        SOAPFault fault = createFault();
        assertNull("Fault Detail returned", implementation.getFaultDetail(fault));
        fault.addDetail();
        assertNotNull("No Fault Detail returned", implementation.getFaultDetail(fault));
    }

    public void testAddFaultDetail() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = implementation.addFaultDetail(fault);
        assertEquals("Invalid fault detail", fault.getDetail(), detail);
    }

    public void testAddDetailEntry() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = fault.addDetail();
        DetailEntry detailEntry =
                implementation.addDetailEntry(detail, new QName("http://springframework.org/spring-ws", "DetailEntry"));
        assertNotNull("No detail entry", detailEntry);
        Iterator iterator = detail.getDetailEntries();
        assertTrue("No detail entried", iterator.hasNext());
        assertEquals("Invalid detail entry", detailEntry, iterator.next());
    }

    public void testAddTextNode() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = fault.addDetail();
        Name name = envelope.createName("DetailEntry", "", "http://springframework.org/spring-ws");
        DetailEntry detailEntry = detail.addDetailEntry(name);
        implementation.addTextNode(detailEntry, "text");
        assertEquals("Invalid text", "text", detailEntry.getValue());
    }

    public void testGetDetailEntries() throws Exception {
        SOAPFault fault = createFault();
        Detail detail = fault.addDetail();
        Iterator iterator = implementation.getDetailEntries(detail);
        assertFalse("Detail entries found", iterator.hasNext());
        Name name = envelope.createName("DetailEntry", "", "http://springframework.org/spring-ws");
        DetailEntry detailEntry = detail.addDetailEntry(name);
        iterator = implementation.getDetailEntries(detail);
        assertTrue("No detail entries found", iterator.hasNext());
        assertEquals("Invalid detail entry found", detailEntry, iterator.next());
        assertFalse("Detail entries found", iterator.hasNext());
    }

    public void testWriteTo() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        implementation.writeTo(message, os);
        assertTrue("Nothing written", os.toByteArray().length > 0);
    }

    public void testGetAttachments() throws Exception {
        Iterator iterator = implementation.getAttachments(message);
        assertFalse("Message has attachments", iterator.hasNext());
        AttachmentPart attachmentPart = message.createAttachmentPart();
        message.addAttachmentPart(attachmentPart);
        iterator = implementation.getAttachments(message);
        assertTrue("Message has no attachments", iterator.hasNext());
        assertEquals("Invalid attachment part", attachmentPart, iterator.next());
    }

    public void testAddAttachmentPart() throws Exception {
        DataSource dataSource = new ByteArrayDataSource("data", "text");
        AttachmentPart attachmentPart = implementation.addAttachmentPart(message, dataSource);
        assertNotNull("No attachment part", attachmentPart);
    }

}