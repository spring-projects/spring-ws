/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap;

import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSoapBodyTestCase extends XMLTestCase {

    protected SoapBody soapBody;

    protected Transformer transformer;

    protected final void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        soapBody = createSoapBody();
    }

    protected abstract SoapBody createSoapBody() throws Exception;

    public void testPayload() throws Exception {
        String payload = "<payload/>";
        StringSource contents = new StringSource(payload);
        transformer.transform(contents, soapBody.getPayloadResult());
        StringResult result = new StringResult();
        transformer.transform(soapBody.getPayloadSource(), result);
        assertXMLEqual("Invalid payload", payload, result.toString());
    }

    public void testNoFault() throws Exception {
        assertFalse("body has fault", soapBody.hasFault());
    }

    public void testFault() {
        QName faultCode = new QName("namespace", "localPart", "prefix");
        String faultString = "faultString";
        SoapFault fault = soapBody.addFault(faultCode, faultString);
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", soapBody.hasFault());
        assertNotNull("SoapBody has no fault", soapBody.getFault());
        assertEquals("Invalid fault code", faultCode, fault.getFaultCode());
        assertEquals("Invalid fault string", faultString, fault.getFaultString());
        fault.setFaultRole("role");
        assertEquals("Invalid fault role", "role", fault.getFaultRole());
        fault.setFaultString("new faultString", Locale.ENGLISH);
        assertEquals("Invalid fault string", "new faultString", fault.getFaultString());
        assertEquals("Invalid fault string locale", Locale.ENGLISH, fault.getFaultStringLocale());
        assertNotNull("Fault source is null", fault.getSource());
    }

    public void testAddFaultWithExistingPayload() throws Exception {
        QName faultCode = new QName("namespace", "localPart", "prefix");
        StringSource contents = new StringSource("<payload/>");
        transformer.transform(contents, soapBody.getPayloadResult());
        soapBody.addFault(faultCode, "faultString");
        assertTrue("Body has no fault", soapBody.hasFault());
    }

    public void testAddFaultWithDetail() throws Exception {
        QName faultCode = new QName("namespace", "localPart", "prefix");
        SoapFault fault = soapBody.addFault(faultCode, "Fault");
        SoapFaultDetail detail = fault.addFaultDetail();
        assertNotNull("Null returned", detail);
        QName detailQName = new QName("namespace", "localPart", "prefix");
        SoapFaultDetailElement detailElement = detail.addFaultDetailElement(detailQName);
        assertNotNull("Null returned", detailElement);
        assertEquals("Invalid QName on detail element", detailQName, detailElement.getName());
        Iterator iterator = detail.getDetailEntries();
        assertNotNull("Null returned", detail);
        assertTrue("Iterator contains no elements", iterator.hasNext());
        detailElement = (SoapFaultDetailElement) iterator.next();
        assertNotNull("Null returned", detailElement);
        assertEquals("Invalid QName on detail element", detailQName, detailElement.getName());
    }


}
