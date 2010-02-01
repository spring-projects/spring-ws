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

package org.springframework.xml.xpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.ResourceSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class AbstractXPathTemplateTestCase extends TestCase {

    XPathOperations template;

    private Source namespaces;

    private Source nonamespaces;

    @Override
    protected final void setUp() throws Exception {
        template = createTemplate();
        namespaces = new ResourceSource(new ClassPathResource("namespaces.xml", AbstractXPathTemplateTestCase.class));
        nonamespaces =
                new ResourceSource(new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTestCase.class));
    }

    protected abstract XPathOperations createTemplate() throws Exception;

    public void testEvaluateAsBoolean() {
        boolean result = template.evaluateAsBoolean("/root/child/boolean", nonamespaces);
        assertTrue("Invalid result", result);
    }

    public void testEvaluateAsBooleanNamespaces() {
        boolean result = template.evaluateAsBoolean("/prefix1:root/prefix2:child/prefix2:boolean", namespaces);
        assertTrue("Invalid result", result);
    }

    public void testEvaluateAsDouble() {
        double result = template.evaluateAsDouble("/root/child/number", nonamespaces);
        assertEquals("Invalid result", 42D, result, 0D);
    }

    public void testEvaluateAsDoubleNamespaces() {
        double result = template.evaluateAsDouble("/prefix1:root/prefix2:child/prefix2:number", namespaces);
        assertEquals("Invalid result", 42D, result, 0D);
    }

    public void testEvaluateAsNode() {
        Node result = template.evaluateAsNode("/root/child", nonamespaces);
        assertNotNull("Invalid result", result);
        assertEquals("Invalid localname", "child", result.getLocalName());
    }

    public void testEvaluateAsNodeNamespaces() {
        Node result = template.evaluateAsNode("/prefix1:root/prefix2:child", namespaces);
        assertNotNull("Invalid result", result);
        assertEquals("Invalid localname", "child", result.getLocalName());
    }

    public void testEvaluateAsNodes() {
        List<Node> results = template.evaluateAsNodeList("/root/child/*", nonamespaces);
        assertNotNull("Invalid result", results);
        assertEquals("Invalid amount of results", 3, results.size());
    }

    public void testEvaluateAsNodesNamespaces() {
        List<Node> results = template.evaluateAsNodeList("/prefix1:root/prefix2:child/*", namespaces);
        assertNotNull("Invalid result", results);
        assertEquals("Invalid amount of results", 3, results.size());
    }

    public void testEvaluateAsStringNamespaces() throws IOException, SAXException {
        String result = template.evaluateAsString("/prefix1:root/prefix2:child/prefix2:text", namespaces);
        assertEquals("Invalid result", "text", result);
    }

    public void testEvaluateAsString() throws IOException, SAXException {
        String result = template.evaluateAsString("/root/child/text", nonamespaces);
        assertEquals("Invalid result", "text", result);
    }

    public void testEvaluateDomSource() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(SaxUtils.createInputSource(
                new ClassPathResource("nonamespaces.xml", AbstractXPathTemplateTestCase.class)));

        String result = template.evaluateAsString("/root/child/text", new DOMSource(document));
        assertEquals("Invalid result", "text", result);
    }

    public void testEvaluateStreamSource() throws IOException, SAXException, ParserConfigurationException {
        InputStream in = AbstractXPathTemplateTestCase.class.getResourceAsStream("nonamespaces.xml");
        String result = template.evaluateAsString("/root/child/text", new StreamSource(in));
        assertEquals("Invalid result", "text", result);
    }

    public void testInvalidExpression() {
        try {
            template.evaluateAsBoolean("\\", namespaces);
            fail("No XPathException thrown");
        }
        catch (XPathException ex) {
            // Expected behaviour
        }
    }

    public void testEvaluateAsObject() throws Exception {
        String result = (String) template.evaluateAsObject("/root/child", nonamespaces, new NodeMapper<String>() {
            public String mapNode(Node node, int nodeNum) throws DOMException {
                return node.getLocalName();
            }
        });
        assertNotNull("Invalid result", result);
        assertEquals("Invalid localname", "child", result);
    }

    public void testEvaluate() throws Exception {
        List<String> results = template.evaluate("/root/child/*", nonamespaces, new NodeMapper<String>() {
            public String mapNode(Node node, int nodeNum) throws DOMException {
                return node.getLocalName();
            }
        });
        assertNotNull("Invalid result", results);
        assertEquals("Invalid amount of results", 3, results.size());
        assertEquals("Invalid first result", "text", results.get(0));
        assertEquals("Invalid first result", "number", results.get(1));
        assertEquals("Invalid first result", "boolean", results.get(2));
    }
}
