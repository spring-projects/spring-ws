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

package org.springframework.ws.soap.axiom.support;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;
import java.util.Locale;

public class AxiomUtilsTest extends TestCase {

    private OMElement element;

    protected void setUp() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = factory.createOMNamespace("http://www.springframework.org", "prefix");
        element = factory.createOMElement("element", namespace);
    }

    public void testToNamespaceDeclared() throws Exception {
        QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
    }

    public void testToNamespaceUndeclared() throws Exception {
        QName qName = new QName("http://www.example.com", "localPart");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
        assertFalse("Invalid prefix", "prefix".equals(namespace.getPrefix()));
    }

    public void testToNamespacePrefixDeclared() throws Exception {
        QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart", "prefix");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
        assertEquals("Invalid prefix", "prefix", namespace.getPrefix());
    }

    public void testToNamespacePrefixUndeclared() throws Exception {
        QName qName = new QName("http://www.example.com", "localPart", "otherPrefix");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
        assertEquals("Invalid prefix", qName.getPrefix(), namespace.getPrefix());
    }

    public void testToLanguage() throws Exception {
        assertEquals("Invalid conversion", "fr-CA", AxiomUtils.toLanguage(Locale.CANADA_FRENCH));
        assertEquals("Invalid conversion", "en", AxiomUtils.toLanguage(Locale.ENGLISH));
    }

    public void testToLocale() throws Exception {
        assertEquals("Invalid conversion", Locale.CANADA_FRENCH, AxiomUtils.toLocale("fr-CA"));
        assertEquals("Invalid conversion", Locale.ENGLISH, AxiomUtils.toLocale("en"));
    }
}