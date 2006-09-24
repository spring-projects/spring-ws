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

package org.springframework.xml.namespace;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class QNameUtilsTest extends TestCase {

    public void testValidQNames() {
        assertTrue("Namespace QName not validated", QNameUtils.validateQName("{namespace}local"));
        assertTrue("No Namespace QName not validated", QNameUtils.validateQName("local"));
    }

    public void testInvalidQNames() {
        assertFalse("Null QName validated", QNameUtils.validateQName(null));
        assertFalse("Empty QName validated", QNameUtils.validateQName(""));
        assertFalse("Invalid QName validated", QNameUtils.validateQName("{namespace}"));
        assertFalse("Invalid QName validated", QNameUtils.validateQName("{namespace"));
    }

    public void testGetQNameForNodeNoNamespace() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element element = document.createElement("localname");
        QName qName = QNameUtils.getQNameForNode(element);
        assertNotNull("getQNameForNode returns null", qName);
        assertEquals("QName has invalid localname", "localname", qName.getLocalPart());
        assertFalse("Qname has invalid namespace", StringUtils.hasLength(qName.getNamespaceURI()));
        assertFalse("Qname has invalid prefix", StringUtils.hasLength(qName.getPrefix()));

    }

    public void testGetQNameForNodeNoPrefix() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element element = document.createElementNS("namespace", "localname");
        QName qName = QNameUtils.getQNameForNode(element);
        assertNotNull("getQNameForNode returns null", qName);
        assertEquals("QName has invalid localname", "localname", qName.getLocalPart());
        assertEquals("Qname has invalid namespace", "namespace", qName.getNamespaceURI());
        assertFalse("Qname has invalid prefix", StringUtils.hasLength(qName.getPrefix()));
    }

    public void testGetQNameForNode() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element element = document.createElementNS("namespace", "prefix:localname");
        QName qName = QNameUtils.getQNameForNode(element);
        assertNotNull("getQNameForNode returns null", qName);
        assertEquals("QName has invalid localname", "localname", qName.getLocalPart());
        assertEquals("Qname has invalid namespace", "namespace", qName.getNamespaceURI());
        assertEquals("Qname has invalid prefix", "prefix", qName.getPrefix());
    }

    public void testToQualifiedNamePrefix() throws Exception {
        QName qName = new QName("namespace", "localName", "prefix");
        String result = QNameUtils.toQualifiedName(qName);
        assertEquals("Invalid result", "prefix:localName", result);
    }

    public void testToQualifiedNameNoPrefix() throws Exception {
        QName qName = new QName("localName");
        String result = QNameUtils.toQualifiedName(qName);
        assertEquals("Invalid result", "localName", result);
    }

    public void testToQNamePrefix() throws Exception {
        QName result = QNameUtils.toQName("namespace", "prefix:localName");
        assertEquals("namespace", result.getNamespaceURI());
        assertEquals("prefix", result.getPrefix());
        assertEquals("localName", result.getLocalPart());
    }

    public void testToQNameNoPrefix() throws Exception {
        QName result = QNameUtils.toQName("namespace", "localName");
        assertEquals("namespace", result.getNamespaceURI());
        assertEquals("", result.getPrefix());
        assertEquals("localName", result.getLocalPart());
    }
}
