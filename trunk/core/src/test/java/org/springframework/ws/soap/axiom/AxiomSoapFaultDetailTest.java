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

package org.springframework.ws.soap.axiom;

import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

public class AxiomSoapFaultDetailTest extends TestCase {

    private static final String FAILING_FAULT =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n  " + "<soapenv:Body>\n  " +
                    "<soapenv:Fault>\n  " + "<faultcode>Client</faultcode>\n  " +
                    "<faultstring>Client Error</faultstring>\n  " + "<detail>\n " +
                    "<ns1:dispositionReport xmlns:ns1=\"urn:uddi-org:api_v3\">\n  " +
                    "<ns1:result errno=\"10210\"/>\n  " + "</ns1:dispositionReport>" + "</detail>" +
                    "</soapenv:Fault>" + "</soapenv:Body>" + "</soapenv:Envelope>";

    private static final String SUCCEEDING_FAULT =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n  " + "<soapenv:Body>\n  " +
                    "<soapenv:Fault>\n  " + "<faultcode>Client</faultcode>\n  " +
                    "<faultstring>Client Error</faultstring>\n  " + "<detail>" +
                    "<ns1:dispositionReport xmlns:ns1=\"urn:uddi-org:api_v3\">\n  " +
                    "<ns1:result errno=\"10210\"/>\n  " + "</ns1:dispositionReport>" + "</detail>" +
                    "</soapenv:Fault>" + "</soapenv:Body>" + "</soapenv:Envelope>";

    private AxiomSoapMessage failingMessage;

    private AxiomSoapMessage succeedingMessage;

    @Override
    protected void setUp() throws Exception {
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(FAILING_FAULT));
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser);
        SOAPMessage soapMessage = builder.getSoapMessage();

        failingMessage = new AxiomSoapMessage(soapMessage, null, false, true);

        parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(SUCCEEDING_FAULT));
        builder = new StAXSOAPModelBuilder(parser);
        soapMessage = builder.getSoapMessage();

        succeedingMessage = new AxiomSoapMessage(soapMessage, null, false, true);

    }

    public void testGetDetailEntriesWorksWithWhitespaceNodes() throws Exception {
        SoapFault fault = failingMessage.getSoapBody().getFault();
        assertNotNull("Fault is null", fault);
        assertNotNull("Fault detail is null", fault.getFaultDetail());
        SoapFaultDetail detail = fault.getFaultDetail();
        assertTrue("No next detail entry present", detail.getDetailEntries().hasNext());
        detail.getDetailEntries().next();

    }

    public void testGetDetailEntriesWorksWithoutWhitespaceNodes() throws Exception {
        SoapFault fault = succeedingMessage.getSoapBody().getFault();
        assertNotNull("Fault is null", fault);
        assertNotNull("Fault detail is null", fault.getFaultDetail());
        SoapFaultDetail detail = fault.getFaultDetail();
        assertTrue("No next detail entry present", detail.getDetailEntries().hasNext());
        detail.getDetailEntries().next();
    }

}