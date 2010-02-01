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

import java.io.StringWriter;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.custommonkey.xmlunit.XMLTestCase;

import org.springframework.xml.transform.StaxResult;

public class NonCachingPayloadTest extends XMLTestCase {

    private Payload payload;

    private SOAPBody body;

    @Override
    public final void setUp() {
        SOAPFactory soapFactory = new SOAP11Factory();
        body = soapFactory.createSOAPBody();
        payload = new NonCachingPayload(body, soapFactory);
    }

    public void testDelegatingStreamWriter() throws Exception {
        StaxResult result = (StaxResult) payload.getResult();
        XMLStreamWriter streamWriter = result.getXMLStreamWriter();

        String namespace = "http://springframework.org/spring-ws";
        streamWriter.setDefaultNamespace(namespace);
        streamWriter.writeStartElement(namespace, "root");
        streamWriter.writeDefaultNamespace(namespace);
        streamWriter.writeStartElement(namespace, "child");
        streamWriter.writeCharacters("text");
        streamWriter.writeEndElement();
        streamWriter.writeEndElement();
        streamWriter.flush();

        StringWriter writer = new StringWriter();
        body.serialize(writer);

        String expected = "<soapenv:Body xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<root xmlns='http://springframework.org/spring-ws'>" + "<child>text</child>" + "</root></soapenv:Body>"
                ;
        assertXMLEqual(expected, writer.toString());
    }

    public void testDelegatingStreamWriterWriteEndDocument() throws Exception {
        StaxResult result = (StaxResult) payload.getResult();
        XMLStreamWriter streamWriter = result.getXMLStreamWriter();

        String namespace = "http://springframework.org/spring-ws";
        streamWriter.setDefaultNamespace(namespace);
        streamWriter.writeStartElement(namespace, "root");
        streamWriter.writeDefaultNamespace(namespace);
        streamWriter.writeStartElement(namespace, "child");
        streamWriter.writeCharacters("text");
        streamWriter.writeEndDocument();
        streamWriter.flush();

        StringWriter writer = new StringWriter();
        body.serialize(writer);

        String expected = "<soapenv:Body xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<root xmlns='http://springframework.org/spring-ws'>" + "<child>text</child>" + "</root></soapenv:Body>"
                ;
        assertXMLEqual(expected, writer.toString());
    }

    public void testDelegatingStreamWriterWriteEmptyElement() throws Exception {
        StaxResult result = (StaxResult) payload.getResult();
        XMLStreamWriter streamWriter = result.getXMLStreamWriter();

        String namespace = "http://springframework.org/spring-ws";
        streamWriter.setDefaultNamespace(namespace);
        streamWriter.writeStartElement(namespace, "root");
        streamWriter.writeDefaultNamespace(namespace);
        streamWriter.writeEmptyElement(namespace, "child");
        streamWriter.writeEndElement();
        streamWriter.flush();

        StringWriter writer = new StringWriter();
        body.serialize(writer);

        String expected = "<soapenv:Body xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<root xmlns='http://springframework.org/spring-ws'>" + "<child />" + "</root></soapenv:Body>";
        assertXMLEqual(expected, writer.toString());
    }
}
