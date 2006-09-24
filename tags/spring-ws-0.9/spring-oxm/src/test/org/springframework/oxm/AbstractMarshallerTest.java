/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.oxm;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public abstract class AbstractMarshallerTest extends XMLTestCase {

    protected Marshaller marshaller;

    protected Object flights;

    protected static final String EXPECTED_STRING = "<tns:flights xmlns:tns=\"http://samples.springframework.org/flight\">"
            + "<tns:flight><tns:number>42</tns:number></tns:flight></tns:flights>";

    protected final void setUp() throws Exception {
        this.marshaller = createMarshaller();
        this.flights = createFlights();
        XMLUnit.setIgnoreWhitespace(true);
    }

    protected abstract Marshaller createMarshaller() throws Exception;

    protected abstract Object createFlights();


    public void testMarshalDOMResult() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document document = builder.newDocument();
        DOMResult domResult = new DOMResult(document);
        marshaller.marshal(flights, domResult);        
        Document expected = builder.newDocument();
        Element flightsElement = expected.createElementNS("http://samples.springframework.org/flight", "tns:flights");
        expected.appendChild(flightsElement);
        Element flightElement = expected.createElementNS("http://samples.springframework.org/flight", "tns:flight");
        flightsElement.appendChild(flightElement);
        Element numberElement = expected.createElementNS("http://samples.springframework.org/flight", "tns:number");
        flightElement.appendChild(numberElement);
        Text text = expected.createTextNode("42");
        numberElement.appendChild(text);
        assertXMLEqual("Marshaller writes invalid DOMResult", expected, document);
    }
    

    public void testMarshalStreamResultWriter() throws Exception {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        marshaller.marshal(flights, result);
        assertXMLEqual("Marshaller writes invalid StreamResult", EXPECTED_STRING, writer.toString());
    }

    public void testMarshalStreamResultOutputStream() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(os);
        marshaller.marshal(flights, result);
        assertXMLEqual("Marshaller writes invalid StreamResult", EXPECTED_STRING, new String(os.toByteArray(), "UTF-8"));
    }

}
