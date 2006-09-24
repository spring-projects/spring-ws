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

package org.springframework.xml.transform;

import java.io.StringWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;

public class StaxResultTest extends XMLTestCase {

    private static final String XML = "<root xmlns='namespace'><child/></root>";

    private Transformer transformer;

    private XMLOutputFactory inputFactory;

    protected void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        inputFactory = XMLOutputFactory.newInstance();
    }

    public void testStreamWriterSource() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter streamWriter = inputFactory.createXMLStreamWriter(stringWriter);
        Source source = new StringSource(XML);
        Result result = new StaxResult(streamWriter);
        transformer.transform(source, result);
        assertXMLEqual("Invalid result", XML, stringWriter.toString());
    }

    public void testEventWriterSource() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLEventWriter eventWriter = inputFactory.createXMLEventWriter(stringWriter);
        Source source = new StringSource(XML);
        Result result = new StaxResult(eventWriter);
        transformer.transform(source, result);
        assertXMLEqual("Invalid result", XML, stringWriter.toString());
    }

}