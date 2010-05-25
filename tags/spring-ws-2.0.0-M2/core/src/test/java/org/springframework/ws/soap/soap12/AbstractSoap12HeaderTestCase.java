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

import java.util.Iterator;
import javax.xml.namespace.QName;

import org.springframework.ws.soap.AbstractSoapHeaderTestCase;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;

import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

public abstract class AbstractSoap12HeaderTestCase extends AbstractSoapHeaderTestCase {

    @Test
    public void testGetType() {
        assertTrue("Invalid type returned", soapHeader instanceof Soap12Header);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("Invalid qualified name", new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "Header"),
                soapHeader.getName());
    }

    @Test
    public void testGetSource() throws Exception {
        StringResult result = new StringResult();
        transformer.transform(soapHeader.getSource(), result);
        assertXMLEqual("Invalid contents of header", "<Header xmlns='http://www.w3.org/2003/05/soap-envelope' />",
                result.toString());
    }

    @Test
    public void testAddNotUnderstood() throws Exception {
        Soap12Header soap12Header = (Soap12Header) soapHeader;
        QName headerName = new QName("http://www.springframework.org", "NotUnderstood", "spring-ws");
        soap12Header.addNotUnderstoodHeaderElement(headerName);
        StringResult result = new StringResult();
        transformer.transform(soapHeader.getSource(), result);
        assertXMLEqual("Invalid contents of header", "<Header xmlns='http://www.w3.org/2003/05/soap-envelope' >" +
                "<NotUnderstood qname='spring-ws:NotUnderstood' xmlns:spring-ws='http://www.springframework.org' />" +
                "</Header>", result.toString());
    }

    @Test
    public void testAddUpgrade() throws Exception {
        String[] supportedUris =
                new String[]{"http://schemas.xmlsoap.org/soap/envelope/", "http://www.w3.org/2003/05/soap-envelope"};
        Soap12Header soap12Header = (Soap12Header) soapHeader;
        SoapHeaderElement header = soap12Header.addUpgradeHeaderElement(supportedUris);
        StringResult result = new StringResult();
        transformer.transform(soapHeader.getSource(), result);
        assertEquals("Invalid name", header.getName(), new QName("http://www.w3.org/2003/05/soap-envelope", "Upgrade"));
        // XMLUnit can't test this:
/*
        assertXMLEqual("Invalid contents of header", "<Header xmlns='http://www.w3.org/2003/05/soap-envelope' >" +
                "<Upgrade>" +
                "<SupportedEnvelope xmlns:ns0='http://schemas.xmlsoap.org/soap/envelope/' qname='ns0:Envelope'/>" +
                "<SupportedEnvelope xmlns:ns1='http://www.w3.org/2003/05/soap-envelope' qname='ns1:Envelope'/>" +
                "</Upgrade>" +
                "</Header>", result.toString());
*/
    }

    @Test
    public void testExamineHeaderElementsToProcessActors() throws Exception {
        QName qName = new QName(NAMESPACE, "localName1", PREFIX);
        SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
        headerElement.setActorOrRole("role1");
        qName = new QName(NAMESPACE, "localName2", PREFIX);
        headerElement = soapHeader.addHeaderElement(qName);
        headerElement.setActorOrRole("role2");
        qName = new QName(NAMESPACE, "localName3", PREFIX);
        headerElement = soapHeader.addHeaderElement(qName);
        headerElement.setActorOrRole(SoapVersion.SOAP_12.getNextActorOrRoleUri());
        Iterator<SoapHeaderElement> iterator = ((Soap12Header) soapHeader).examineHeaderElementsToProcess(new String[]{"role1"}, false);
        assertNotNull("header element iterator is null", iterator);
        assertTrue("header element iterator has no elements", iterator.hasNext());
        checkHeaderElement((SoapHeaderElement) iterator.next());
        assertTrue("header element iterator has no elements", iterator.hasNext());
        checkHeaderElement((SoapHeaderElement) iterator.next());
        assertFalse("header element iterator has too many elements", iterator.hasNext());
    }

    @Test
    public void testExamineHeaderElementsToProcessNoActors() throws Exception {
        QName qName = new QName(NAMESPACE, "localName1", PREFIX);
        SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
        headerElement.setActorOrRole("");
        qName = new QName(NAMESPACE, "localName2", PREFIX);
        headerElement = soapHeader.addHeaderElement(qName);
        headerElement.setActorOrRole("role1");
        qName = new QName(NAMESPACE, "localName3", PREFIX);
        headerElement = soapHeader.addHeaderElement(qName);
        headerElement.setActorOrRole(SoapVersion.SOAP_12.getNextActorOrRoleUri());
        Iterator<SoapHeaderElement> iterator = ((Soap12Header) soapHeader).examineHeaderElementsToProcess(new String[0], false);
        assertNotNull("header element iterator is null", iterator);
        assertTrue("header element iterator has no elements", iterator.hasNext());
        checkHeaderElement((SoapHeaderElement) iterator.next());
        assertTrue("header element iterator has no elements", iterator.hasNext());
        checkHeaderElement((SoapHeaderElement) iterator.next());
        assertFalse("header element iterator has too many elements", iterator.hasNext());
    }

    @Test
    public void testExamineHeaderElementsToProcessUltimateDestination() throws Exception {
        QName qName = new QName(NAMESPACE, "localName", PREFIX);
        SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
        headerElement.setActorOrRole(SoapVersion.SOAP_12.getUltimateReceiverRoleUri());
        Iterator<SoapHeaderElement> iterator = ((Soap12Header) soapHeader).examineHeaderElementsToProcess(new String[]{"role"}, true);
        assertNotNull("header element iterator is null", iterator);
        headerElement = (SoapHeaderElement) iterator.next();
        assertEquals("Invalid name on header element", new QName(NAMESPACE, "localName", PREFIX),
                headerElement.getName());
        assertFalse("header element iterator has too many elements", iterator.hasNext());
    }

    private void checkHeaderElement(SoapHeaderElement headerElement) {
        QName name = headerElement.getName();
        assertTrue("Invalid name on header element", new QName(NAMESPACE, "localName1", PREFIX).equals(name) ||
                new QName(NAMESPACE, "localName3", PREFIX).equals(name));
    }

}
