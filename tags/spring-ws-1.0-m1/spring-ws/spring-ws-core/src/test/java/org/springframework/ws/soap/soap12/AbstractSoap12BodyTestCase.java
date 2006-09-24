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

package org.springframework.ws.soap.soap12;

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
public abstract class AbstractSoap12BodyTestCase extends AbstractSoapBodyTestCase {

    public void testGetName() throws Exception {
        assertEquals("Invalid qualified name", new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "Body"),
                soapBody.getName());
    }

    public void testGetSource() throws Exception {
        StringResult result = new StringResult();
        transformer.transform(soapBody.getSource(), result);
        assertXMLEqual("Invalid contents of body", "<Body xmlns='http://www.w3.org/2003/05/soap-envelope' />",
                result.toString());
    }

    public void testAddMustUnderstandFaultSoap12() throws Exception {
        SoapFault fault = soapBody.addFault(SoapVersion.SOAP_12.getMustUnderstandFaultName(),
                SoapMessageUtils.DEFAULT_MUST_UNDERSTAND_FAULT_STRING);
        assertEquals("Invalid fault code", SoapVersion.SOAP_12.getMustUnderstandFaultName(), fault.getFaultCode());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" +
                        "<soapenv:Code><soapenv:Value>soapenv:MustUnderstand</soapenv:Value></soapenv:Code>" +
                        "<soapenv:Reason><soapenv:Text>SOAP Must Understand Error</soapenv:Text></soapenv:Reason>" +
                        "</soapenv:Fault>", result.toString());
    }

    public void testAddSenderFaultSoap12() throws Exception {
        SoapFault fault = soapBody.addFault(SoapVersion.SOAP_12.getSenderFaultName(), "faultString");
        assertEquals("Invalid fault code", SoapVersion.SOAP_12.getSenderFaultName(), fault.getFaultCode());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" +
                        "<soapenv:Code><soapenv:Value>soapenv:Sender</soapenv:Value></soapenv:Code>" +
                        "<soapenv:Reason><soapenv:Text>faultString</soapenv:Text></soapenv:Reason>" +
                        "</soapenv:Fault>", result.toString());
    }

    public void testAddReceiverFaultSoap12() throws Exception {
        SoapFault fault = soapBody.addFault(SoapVersion.SOAP_12.getReceiverFaultName(), "faultString");
        assertEquals("Invalid fault code", SoapVersion.SOAP_12.getReceiverFaultName(), fault.getFaultCode());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" +
                        "<soapenv:Code><soapenv:Value>soapenv:Receiver</soapenv:Value></soapenv:Code>" +
                        "<soapenv:Reason><soapenv:Text>faultString</soapenv:Text></soapenv:Reason>" +
                        "</soapenv:Fault>", result.toString());
    }

    public void testAddFaultSoap12() throws Exception {
        QName faultCode = new QName("http://www.springframework.org", "fault", "spring");
        SoapFault fault = soapBody.addFault(faultCode, "faultString");
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid contents of body",
                "<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" +
                        "<soapenv:Code><soapenv:Value xmlns:spring='http://www.springframework.org'>" +
                        "spring:fault</soapenv:Value></soapenv:Code>" +
                        "<soapenv:Reason><soapenv:Text>faultString</soapenv:Text></soapenv:Reason>" +
                        "</soapenv:Fault>", result.toString());
    }

    public void testAddFaultWithDetail() throws Exception {
        QName faultCode = new QName("http://www.springframework.org", "fault", "spring");
        SoapFault fault = soapBody.addFault(faultCode, "faultString");
        SoapFaultDetail detail = fault.addFaultDetail();
        SoapFaultDetailElement detailElement =
                detail.addFaultDetailElement(new QName("namespace", "localPart", "prefix"));
        StringSource detailContents = new StringSource("<detailContents xmlns='namespace'/>");
        transformer.transform(detailContents, detailElement.getResult());
        StringResult result = new StringResult();
        transformer.transform(fault.getSource(), result);
        assertXMLEqual("Invalid source for body",
                "<soapenv:Fault xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'>" +
                        "<soapenv:Code><soapenv:Value xmlns:spring='http://www.springframework.org'>" +
                        "spring:fault</soapenv:Value></soapenv:Code>" +
                        "<soapenv:Reason><soapenv:Text>faultString</soapenv:Text></soapenv:Reason>" +
                        "<soapenv:Detail><prefix:localPart xmlns:prefix='namespace'><detailContents xmlns='namespace'/>" +
                        "</prefix:localPart></soapenv:Detail></soapenv:Fault>", result.toString());
    }

}
