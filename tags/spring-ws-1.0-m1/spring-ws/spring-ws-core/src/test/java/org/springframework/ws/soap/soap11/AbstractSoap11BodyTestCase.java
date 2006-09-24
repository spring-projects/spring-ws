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

import javax.xml.namespace.QName;

import org.springframework.ws.soap.AbstractSoapBodyTestCase;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.support.SoapMessageUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

/**
 * @author Arjen Poutsma
 */
public abstract class AbstractSoap11BodyTestCase extends AbstractSoapBodyTestCase {

    public void testGetName() throws Exception {
        assertEquals("Invalid qualified name", new QName(SoapVersion.SOAP_11.getEnvelopeNamespaceUri(), "Body"),
                soapBody.getName());
    }

    public void testGetSource() throws Exception {
        StringResult result = new StringResult();
        transformer.transform(soapBody.getSource(), result);
        assertXMLEqual("Invalid contents of body", "<Body xmlns='http://schemas.xmlsoap.org/soap/envelope/' />",
                result.toString());
    }

    public void testAddMustUnderstandFaultSoap11() throws Exception {
        SoapFault fault = soapBody.addFault(SoapVersion.SOAP_11.getMustUnderstandFaultName(),
                SoapMessageUtils.DEFAULT_MUST_UNDERSTAND_FAULT_STRING);
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "MustUnderstand"),
                fault.getFaultCode());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                        "<faultcode>SOAP-ENV:MustUnderstand</faultcode>" +
                        "<faultstring>SOAP Must Understand Error</faultstring>" + "</SOAP-ENV:Fault>",
                result.toString());
    }

    public void testAddSenderFaultSoap11() throws Exception {
        SoapFault fault = soapBody.addFault(SoapVersion.SOAP_11.getSenderFaultName(), "faultString");
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"),
                fault.getFaultCode());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                        "<faultcode>SOAP-ENV:Client</faultcode>" + "<faultstring>faultString</faultstring>" +
                        "</SOAP-ENV:Fault>", result.toString());
    }

    public void testAddReceiverFaultSoap11() throws Exception {
        SoapFault fault = soapBody.addFault(SoapVersion.SOAP_11.getReceiverFaultName(), "faultString");
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"),
                fault.getFaultCode());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                        "<faultcode>SOAP-ENV:Server</faultcode>" + "<faultstring>faultString</faultstring>" +
                        "</SOAP-ENV:Fault>", result.toString());
    }

    public void testAddFaultSoap11() throws Exception {
        QName faultCode = new QName("http://www.springframework.org", "fault", "spring");
        SoapFault fault = soapBody.addFault(faultCode, "faultString");
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' " +
                        "xmlns:spring='http://www.springframework.org'>" + "<faultcode>spring:fault</faultcode>" +
                        "<faultstring>faultString</faultstring>" + "</SOAP-ENV:Fault>", result.toString());
    }

    public void testAddFaultWithDetail() throws Exception {
        SoapFault fault = soapBody.addFault(new QName("namespace", "localPart", "prefix"), "Fault");
        SoapFaultDetail detail = fault.addFaultDetail();
        SoapFaultDetailElement detailElement =
                detail.addFaultDetailElement(new QName("namespace", "localPart", "prefix"));
        StringSource detailContents = new StringSource("<detailContents xmlns='namespace'/>");
        transformer.transform(detailContents, detailElement.getResult());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid source for body",
                "<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                        "<faultcode xmlns:prefix='namespace'>prefix:localPart</faultcode>" +
                        "<faultstring>Fault</faultstring>" +
                        "<detail><prefix:localPart xmlns:prefix='namespace'><detailContents xmlns='namespace'/>" +
                        "</prefix:localPart></detail></SOAP-ENV:Fault>", result.toString());
    }

}
