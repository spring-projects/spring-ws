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

package org.springframework.xml.transform;

import java.io.StringReader;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;

public class StaxSourceTest extends XMLTestCase {

    private static final String XML = "<root xmlns='namespace'><child/></root>";

    private Transformer transformer;

    private XMLInputFactory inputFactory;

    @Override
    protected void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        inputFactory = XMLInputFactory.newInstance();
    }

    public void testStreamReaderSource() throws Exception {
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(XML));
        StaxSource source = new StaxSource(streamReader);
        assertEquals("Invalid streamReader returned", streamReader, source.getXMLStreamReader());
        assertNull("EventReader returned", source.getXMLEventReader());
        Result result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid result", XML, result.toString());
    }

    public void testEventReaderSource() throws Exception {
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(XML));
        StaxSource source = new StaxSource(eventReader);
        assertEquals("Invalid eventReader returned", eventReader, source.getXMLEventReader());
        assertNull("StreamReader returned", source.getXMLStreamReader());
        Result result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid result", XML, result.toString());
    }
}