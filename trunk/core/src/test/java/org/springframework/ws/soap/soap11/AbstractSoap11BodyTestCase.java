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

package org.springframework.ws.soap.soap11;

import java.util.Locale;
import javax.xml.namespace.QName;

import org.springframework.ws.soap.AbstractSoapBodyTestCase;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSoap11BodyTestCase extends AbstractSoapBodyTestCase {

    public void testGetName() throws Exception {
        assertEquals("Invalid qualified name", SoapVersion.SOAP_11.getBodyName(), soapBody.getName());
    }

    public void testGetSource() throws Exception {
        StringResult result = new StringResult();
        transformer.transform(soapBody.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<Body xmlns='http://schemas.xmlsoap.org/soap/envelope/' />",
                result.toString());
    }

    public void testAddMustUnderstandFault() throws Exception {
        SoapFault fault = soapBody.addMustUnderstandFault("SOAP Must Understand Error", null);
        assertEquals("Invalid fault code",
                new QName("http://schemas.xmlsoap.org/soap/envelope/", "MustUnderstand"),
                fault.getFaultCode());
        assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<faultcode>SOAP-ENV:MustUnderstand</faultcode>" +
                "<faultstring>SOAP Must Understand Error</faultstring></SOAP-ENV:Fault>");
    }

    public void testAddClientFault() throws Exception {
        SoapFault fault = soapBody.addClientOrSenderFault("faultString", null);
        assertEquals("Invalid fault code",
                new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"),
                fault.getFaultCode());
        assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<faultcode>SOAP-ENV:Client</faultcode>" + "<faultstring>faultString</faultstring>" +
                "</SOAP-ENV:Fault>");
    }

    public void testAddServerFault() throws Exception {
        SoapFault fault = soapBody.addServerOrReceiverFault("faultString", null);
        assertEquals("Invalid fault code",
                new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"),
                fault.getFaultCode());
        assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<faultcode>SOAP-ENV:Server</faultcode>" + "<faultstring>faultString</faultstring>" +
                "</SOAP-ENV:Fault>");
    }

    public void testAddFault() throws Exception {
        QName faultCode = new QName("http://www.springframework.org", "fault", "spring");
        String faultString = "faultString";
        Soap11Fault fault = ((Soap11Body) soapBody).addFault(faultCode, "faultString", Locale.ENGLISH);
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", soapBody.hasFault());
        assertNotNull("SoapBody has no fault", soapBody.getFault());
        assertEquals("Invalid fault code", faultCode, fault.getFaultCode());
        assertEquals("Invalid fault string", faultString, fault.getFaultString());
        assertEquals("Invalid fault string locale", Locale.ENGLISH, fault.getFaultStringLocale());
        String actor = "http://www.springframework.org/actor";
        fault.setFaultActorOrRole(actor);
        assertEquals("Invalid fault actor", actor, fault.getFaultActorOrRole());
        assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' " +
                "xmlns:spring='http://www.springframework.org'>" + "<faultcode>spring:fault</faultcode>" +
                "<faultstring xml:lang='en'>" + faultString + "</faultstring>" + "<faultactor>" + actor +
                "</faultactor>" + "</SOAP-ENV:Fault>");
    }

    public void testAddFaultWithDetail() throws Exception {
        SoapFault fault = ((Soap11Body) soapBody)
                .addFault(new QName("namespace", "localPart", "prefix"), "Fault", null);
        SoapFaultDetail detail = fault.addFaultDetail();
        SoapFaultDetailElement detailElement =
                detail.addFaultDetailElement(new QName("namespace", "localPart", "prefix"));
        StringSource detailContents = new StringSource("<detailContents xmlns='namespace'/>");
        transformer.transform(detailContents, detailElement.getResult());
        assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<faultcode xmlns:prefix='namespace'>prefix:localPart</faultcode>" +
                "<faultstring>Fault</faultstring>" +
                "<detail><prefix:localPart xmlns:prefix='namespace'><detailContents xmlns='namespace'/>" +
                "</prefix:localPart></detail></SOAP-ENV:Fault>");
    }

}
