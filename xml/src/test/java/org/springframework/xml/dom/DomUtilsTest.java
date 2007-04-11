/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.dom;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class DomUtilsTest extends TestCase {

    private static final String NAMESPACE = "http://springframework.org/spring-ws";

    private static final String LOCAL_NAME = "Root";

    private static final String XML = "<" + LOCAL_NAME + " xmlns='" + NAMESPACE + "'/>";

    private TransformerFactory transformerFactory;

    protected void setUp() throws Exception {
        transformerFactory = TransformerFactory.newInstance();
    }

    public void testGetRootElementDomSource() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElementNS(NAMESPACE, LOCAL_NAME);
        document.appendChild(rootElement);

        testSource(new DOMSource(document));
    }

    public void testGetRootElementSaxSource() throws Exception {
        InputSource inputSource = new InputSource(new StringReader(XML));
        testSource(new SAXSource(inputSource));
    }

    public void testGetRootElementStreamSource() throws Exception {
        testSource(new StreamSource(new StringReader(XML)));
    }

    private void testSource(Source source) throws TransformerException {
        Element result = DomUtils.getRootElement(source, transformerFactory);
        assertNotNull("No result", result);
        assertEquals("Invalid namespace", NAMESPACE, result.getNamespaceURI());
        assertEquals("Invalid local name", LOCAL_NAME, result.getLocalName());
    }
}