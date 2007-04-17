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

package org.springframework.xml.xpath;

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * JAXP 1.3-specific factory creating <code>XPathExpression</code>s.
 *
 * @author Arjen Poutsma
 * @see #createXPathExpression(String)
 */
abstract class Jaxp13XPathExpressionFactory {

    private static XPathFactory xpathFactory;

    static {
        xpathFactory = XPathFactory.newInstance();
    }

    /**
     * Creates a JAXP 1.3 <code>XPathExpression</code> from the given string expression.
     *
     * @param expression the XPath expression
     * @return the compiled <code>XPathExpression</code>
     * @throws XPathParseException when the given expression cannot be parsed
     */
    static XPathExpression createXPathExpression(String expression) {
        try {
            XPath xpath = xpathFactory.newXPath();
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
    public static XPathExpression createXPathExpression(String expression, Map namespaces) {
        try {
            XPath xpath = xpathFactory.newXPath();
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

    /** JAXP 1.3 implementation of the <code>XPathExpression</code> interface. */
    private static class Jaxp13XPathExpression implements XPathExpression {

        javax.xml.xpath.XPathExpression xpathExpression;

        private Jaxp13XPathExpression(javax.xml.xpath.XPathExpression xpathExpression) {
            this.xpathExpression = xpathExpression;
        }

        public String evaluateAsString(Node node) {
            return (String) evaluate(node, XPathConstants.STRING);
        }

        public Node[] evaluateAsNodes(Node node) {
            NodeList nodeList = (NodeList) evaluate(node, XPathConstants.NODESET);
            return toNodeArray(nodeList);
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

        private Node[] toNodeArray(NodeList nodeList) {
            Node[] result = new Node[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); i++) {
                result[i] = nodeList.item(i);
            }
            return result;
        }

        public double evaluateAsNumber(Node node) {
            Double result = (Double) evaluate(node, XPathConstants.NUMBER);
            return result.doubleValue();
        }

        public boolean evaluateAsBoolean(Node node) {
            Boolean result = (Boolean) evaluate(node, XPathConstants.BOOLEAN);
            return result.booleanValue();
        }

        public Node evaluateAsNode(Node node) {
            return (Node) evaluate(node, XPathConstants.NODE);
        }
    }

}
