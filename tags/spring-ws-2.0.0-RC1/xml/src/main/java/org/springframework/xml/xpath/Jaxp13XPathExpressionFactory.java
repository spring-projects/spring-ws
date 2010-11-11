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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.xml.namespace.SimpleNamespaceContext;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * JAXP 1.3-specific factory creating {@link XPathExpression} objects.
 *
 * @author Arjen Poutsma
 * @see #createXPathExpression(String)
 * @since 1.0.0
 */
abstract class Jaxp13XPathExpressionFactory {

    private static XPathFactory xpathFactory = XPathFactory.newInstance();

    /**
     * Creates a JAXP 1.3 <code>XPathExpression</code> from the given string expression.
     *
     * @param expression the XPath expression
     * @return the compiled <code>XPathExpression</code>
     * @throws XPathParseException when the given expression cannot be parsed
     */
    static XPathExpression createXPathExpression(String expression) {
        try {
            XPath xpath = createXPath();
            javax.xml.xpath.XPathExpression xpathExpression = xpath.compile(expression);
            return new Jaxp13XPathExpression(xpathExpression);
        }
        catch (XPathExpressionException ex) {
            throw new org.springframework.xml.xpath.XPathParseException(
                    "Could not compile [" + expression + "] to a XPathExpression: " + ex.getMessage(), ex);
        }
    }

    /**
     * Creates a JAXP 1.3 <code>XPathExpression</code> from the given string expression and namespaces.
     *
     * @param expression the XPath expression
     * @param namespaces the namespaces
     * @return the compiled <code>XPathExpression</code>
     * @throws XPathParseException when the given expression cannot be parsed
     */
    public static XPathExpression createXPathExpression(String expression, Map<String, String> namespaces) {
        try {
            XPath xpath = createXPath();
            SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
            namespaceContext.setBindings(namespaces);
            xpath.setNamespaceContext(namespaceContext);
            javax.xml.xpath.XPathExpression xpathExpression = xpath.compile(expression);
            return new Jaxp13XPathExpression(xpathExpression);
        }
        catch (XPathExpressionException ex) {
            throw new org.springframework.xml.xpath.XPathParseException(
                    "Could not compile [" + expression + "] to a XPathExpression: " + ex.getMessage(), ex);
        }
    }

    private static synchronized XPath createXPath() {
        return xpathFactory.newXPath();
    }
    

    /** JAXP 1.3 implementation of the <code>XPathExpression</code> interface. */
    private static class Jaxp13XPathExpression implements XPathExpression {

        private final javax.xml.xpath.XPathExpression xpathExpression;

        private Jaxp13XPathExpression(javax.xml.xpath.XPathExpression xpathExpression) {
            this.xpathExpression = xpathExpression;
        }

        public String evaluateAsString(Node node) {
            return (String) evaluate(node, XPathConstants.STRING);
        }

        public List<Node> evaluateAsNodeList(Node node) {
            NodeList nodeList = (NodeList) evaluate(node, XPathConstants.NODESET);
            return toNodeList(nodeList);
        }

        private Object evaluate(Node node, QName returnType) {
            try {
                // XPathExpression is not thread-safe
                synchronized (xpathExpression) {
                    return xpathExpression.evaluate(node, returnType);
                }
            }
            catch (XPathExpressionException ex) {
                throw new XPathException("Could not evaluate XPath expression:" + ex.getMessage(), ex);
            }
        }

        private List<Node> toNodeList(NodeList nodeList) {
            List<Node> result = new ArrayList<Node>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                result.add(nodeList.item(i));
            }
            return result;
        }

        public double evaluateAsNumber(Node node) {
            return (Double) evaluate(node, XPathConstants.NUMBER);
        }

        public boolean evaluateAsBoolean(Node node) {
            return (Boolean) evaluate(node, XPathConstants.BOOLEAN);
        }

        public Node evaluateAsNode(Node node) {
            return (Node) evaluate(node, XPathConstants.NODE);
        }

        public <T> T evaluateAsObject(Node node, NodeMapper<T> nodeMapper) throws XPathException {
            Node result = (Node) evaluate(node, XPathConstants.NODE);
            if (result != null) {
                try {
                    return nodeMapper.mapNode(result, 0);
                }
                catch (DOMException ex) {
                    throw new XPathException("Mapping resulted in DOMException", ex);
                }
            }
            else {
                return null;
            }
        }

        public <T> List<T> evaluate(Node node, NodeMapper<T> nodeMapper) throws XPathException {
            NodeList nodes = (NodeList) evaluate(node, XPathConstants.NODESET);
            List<T> results = new ArrayList<T>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                try {
                    results.add(nodeMapper.mapNode(nodes.item(i), i));
                }
                catch (DOMException ex) {
                    throw new XPathException("Mapping resulted in DOMException", ex);
                }
            }
            return results;
        }
    }

}
