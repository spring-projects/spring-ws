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

package org.springframework.xml.stream;

import java.io.StringReader;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.xml.transform.StaxSource;
import org.springframework.xml.transform.StringResult;

public class XmlEventStreamReaderTest extends XMLTestCase {

    private static final String XML =
            "<?pi content?><root xmlns='namespace'><prefix:child xmlns:prefix='namespace2'>content</prefix:child></root>";

    private XmlEventStreamReader streamReader;

    protected void setUp() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(XML));
        streamReader = new XmlEventStreamReader(eventReader);
    }

    public void testReadAll() throws Exception {
        while (streamReader.hasNext()) {
            streamReader.next();
        }
    }

    public void testReadCorrect() throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StaxSource source = new StaxSource(streamReader);
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual(XML, result.toString());
    }

}