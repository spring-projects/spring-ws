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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.springframework.util.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class AbstractXPathExpressionFactoryTestCase extends TestCase {

    private Document noNamespacesDocument;

    private Document namespacesDocument;

    private Map<String, String> namespaces = new HashMap<String, String>();

    @Override
    protected void setUp() throws Exception {
        namespaces.put("prefix1", "namespace1");
        namespaces.put("prefix2", "namespace2");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputStream inputStream = getClass().getResourceAsStream("nonamespaces.xml");
        try {
            noNamespacesDocument = documentBuilder.parse(inputStream);
        }
        finally {
            inputStream.close();
        }
        inputStream = getClass().getResourceAsStream("namespaces.xml");
        try {
            namespacesDocument = documentBuilder.parse(inputStream);
        }
        finally {
            inputStream.close();
        }
    }

    public void testEvaluateAsBooleanInvalidNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
        boolean result = expression.evaluateAsBoolean(namespacesDocument);
        assertFalse("Invalid result [" + result + "]", result);
    }

    public void testEvaluateAsBooleanInvalidNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/otherchild");
        boolean result = expression.evaluateAsBoolean(noNamespacesDocument);
        assertFalse("Invalid result [" + result + "]", result);
    }

    public void testEvaluateAsBooleanNamespaces() throws IOException, SAXException {
        XPathExpression expression =
                createXPathExpression("/prefix1:root/prefix2:child/prefix2:boolean/text()", namespaces);
        boolean result = expression.evaluateAsBoolean(namespacesDocument);
        assertTrue("Invalid result", result);
    }

    public void testEvaluateAsBooleanNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/child/boolean/text()");
        boolean result = expression.evaluateAsBoolean(noNamespacesDocument);
        assertTrue("Invalid result", result);
    }

    public void testEvaluateAsDoubleInvalidNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
        double result = expression.evaluateAsNumber(noNamespacesDocument);
        assertTrue("Invalid result [" + result + "]", Double.isNaN(result));
    }

    public void testEvaluateAsDoubleInvalidNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/otherchild");
        double result = expression.evaluateAsNumber(noNamespacesDocument);
        assertTrue("Invalid result [" + result + "]", Double.isNaN(result));
    }

    public void testEvaluateAsDoubleNamespaces() throws IOException, SAXException {
        XPathExpression expression =
                createXPathExpression("/prefix1:root/prefix2:child/prefix2:number/text()", namespaces);
        double result = expression.evaluateAsNumber(namespacesDocument);
        assertEquals("Invalid result", 42D, result, 0D);
    }

    public void testEvaluateAsDoubleNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/child/number/text()");
        double result = expression.evaluateAsNumber(noNamespacesDocument);
        assertEquals("Invalid result", 42D, result, 0D);
    }

    public void testEvaluateAsNodeInvalidNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
        Node result = expression.evaluateAsNode(namespacesDocument);
        assertNull("Invalid result [" + result + "]", result);
    }

    public void testEvaluateAsNodeInvalidNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/otherchild");
        Node result = expression.evaluateAsNode(noNamespacesDocument);
        assertNull("Invalid result [" + result + "]", result);
    }

    public void testEvaluateAsNodeNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:child", namespaces);
        Node result = expression.evaluateAsNode(namespacesDocument);
        assertNotNull("Invalid result", result);
        assertEquals("Invalid localname", "child", result.getLocalName());
    }

    public void testEvaluateAsNodeNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/child");
        Node result = expression.evaluateAsNode(noNamespacesDocument);
        assertNotNull("Invalid result", result);
        assertEquals("Invalid localname", "child", result.getLocalName());
    }

    public void testEvaluateAsNodeListNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:child/*", namespaces);
        List<Node> results = expression.evaluateAsNodeList(namespacesDocument);
        assertNotNull("Invalid result", results);
        assertEquals("Invalid amount of results", 3, results.size());
    }

    public void testEvaluateAsNodeListNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/child/*");
        List<Node> results = expression.evaluateAsNodeList(noNamespacesDocument);
        assertNotNull("Invalid result", results);
        assertEquals("Invalid amount of results", 3, results.size());
    }

    public void testEvaluateAsStringInvalidNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/prefix1:root/prefix2:otherchild", namespaces);
        String result = expression.evaluateAsString(namespacesDocument);
        assertFalse("Invalid result [" + result + "]", StringUtils.hasText(result));
    }

    public void testEvaluateAsStringInvalidNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/otherchild");
        String result = expression.evaluateAsString(noNamespacesDocument);
        assertFalse("Invalid result [" + result + "]", StringUtils.hasText(result));
    }

    public void testEvaluateAsStringNamespaces() throws IOException, SAXException {
        XPathExpression expression =
                createXPathExpression("/prefix1:root/prefix2:child/prefix2:text/text()", namespaces);
        String result = expression.evaluateAsString(namespacesDocument);
        assertEquals("Invalid result", "text", result);
    }

    public void testEvaluateAsStringNoNamespaces() throws IOException, SAXException {
        XPathExpression expression = createXPathExpression("/root/child/text/text()");
        String result = expression.evaluateAsString(noNamespacesDocument);
        assertEquals("Invalid result", "text", result);
    }

    public void testEvaluateAsObject() throws Exception {
        XPathExpression expression = createXPathExpression("/root/child");
        String result = expression.evaluateAsObject(noNamespacesDocument, new NodeMapper<String>() {
            public String mapNode(Node node, int nodeNum) throws DOMException {
                return node.getLocalName();
            }
        });
        assertNotNull("Invalid result", result);
        assertEquals("Invalid localname", "child", result);
    }

    public void testEvaluate() throws Exception {
        XPathExpression expression = createXPathExpression("/root/child/*");
        List<String> results = expression.evaluate(noNamespacesDocument, new NodeMapper<String>() {
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

    public void testInvalidExpression() {
        try {
            createXPathExpression("\\");
            fail("No XPathParseException thrown");
        }
        catch (XPathParseException ex) {
            // Expected behaviour
        }
    }

    protected abstract XPathExpression createXPathExpression(String expression);

    protected abstract XPathExpression createXPathExpression(String expression, Map<String, String> namespaces);
}
