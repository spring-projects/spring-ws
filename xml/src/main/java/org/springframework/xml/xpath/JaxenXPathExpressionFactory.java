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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Jaxen-specific factory for creating <code>XPathExpression</code>s.
 *
 * @author Arjen Poutsma
 * @see #createXPathExpression(String)
 * @since 1.0
 */
abstract class JaxenXPathExpressionFactory {

    /**
     * Creates a Jaxen <code>XPathExpression</code> from the given string expression.
     *
     * @param expression the XPath expression
     * @return the compiled <code>XPathExpression</code>
     * @throws XPathParseException when the given expression cannot be parsed
     */
    static XPathExpression createXPathExpression(String expression) {
        try {
            XPath xpath = new DOMXPath(expression);
            return new JaxenXpathExpression(xpath);
        }
        catch (JaxenException ex) {
            throw new org.springframework.xml.xpath.XPathParseException(
                    "Could not compile [" + expression + "] to a XPathExpression: " + ex.getMessage(), ex);
        }
    }

    /**
     * Creates a Jaxen <code>XPathExpression</code> from the given string expression and prefixes.
     *
     * @param expression the XPath expression
     * @param namespaces the namespaces
     * @return the compiled <code>XPathExpression</code>
     * @throws XPathParseException when the given expression cannot be parsed
     */
    public static XPathExpression createXPathExpression(String expression, Map namespaces) {
        try {
            XPath xpath = new DOMXPath(expression);
            xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));
            return new JaxenXpathExpression(xpath);
        }
        catch (JaxenException ex) {
            throw new org.springframework.xml.xpath.XPathParseException(
                    "Could not compile [" + expression + "] to a XPathExpression: " + ex.getMessage(), ex);
        }
    }

    /** Jaxen implementation of the <code>XPathExpression</code> interface. */
    private static class JaxenXpathExpression implements XPathExpression {

        private XPath xpath;

        private JaxenXpathExpression(XPath xpath) {
            this.xpath = xpath;
        }

        public Node evaluateAsNode(Node node) {
            try {
                return (Node) xpath.selectSingleNode(node);
            }
            catch (JaxenException ex) {
                throw new XPathException("Could not evaluate XPath expression [" + xpath + "] :" + ex.getMessage(), ex);
            }
        }

        public boolean evaluateAsBoolean(Node node) {
            try {
                return xpath.booleanValueOf(node);
            }
            catch (JaxenException ex) {
                throw new XPathException("Could not evaluate XPath expression [" + xpath + "] :" + ex.getMessage(), ex);
            }
        }

        public double evaluateAsNumber(Node node) {
            try {
                return xpath.numberValueOf(node).doubleValue();
            }
            catch (JaxenException ex) {
                throw new XPathException("Could not evaluate XPath expression [" + xpath + "] :" + ex.getMessage(), ex);
            }
        }

        public String evaluateAsString(Node node) {
            try {
                return xpath.stringValueOf(node);
            }
            catch (JaxenException ex) {
                throw new XPathException("Could not evaluate XPath expression [" + xpath + "] :" + ex.getMessage(), ex);
            }
        }

        public List evaluateAsNodeList(Node node) {
            try {
                return xpath.selectNodes(node);
            }
            catch (JaxenException ex) {
                throw new XPathException("Could not evaluate XPath expression [" + xpath + "] :" + ex.getMessage(), ex);
            }
        }

        public Object evaluateAsObject(Node context, NodeMapper nodeMapper) throws XPathException {
            try {
                Node result = (Node) xpath.selectSingleNode(context);
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
            catch (JaxenException ex) {
                throw new XPathException("Could not evaluate XPath expression [" + xpath + "] :" + ex.getMessage(), ex);
            }
        }

        public List evaluate(Node context, NodeMapper nodeMapper) throws XPathException {
            try {
                List nodes = xpath.selectNodes(context);
                List results = new ArrayList(nodes.size());
                for (int i = 0; i < nodes.size(); i++) {
                    Node node = (Node) nodes.get(i);
                    try {
                        results.add(nodeMapper.mapNode(node, i));
                    }
                    catch (DOMException ex) {
                        throw new XPathException("Mapping resulted in DOMException", ex);
                    }
                }
                return results;
            }
            catch (JaxenException ex) {
                throw new XPathException("Could not evaluate XPath expression [" + xpath + "] :" + ex.getMessage(), ex);
            }
        }
    }
}
