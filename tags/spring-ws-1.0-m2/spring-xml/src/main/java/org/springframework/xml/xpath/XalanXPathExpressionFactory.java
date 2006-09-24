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

import javax.xml.transform.TransformerException;

import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Xalan-specific factory creating <code>XPathExpression</code>s.
 *
 * @author Arjen Poutsma
 * @see #createXPathExpression(String)
 */
abstract class XalanXPathExpressionFactory {

    /**
     * Creates a Xalan <code>XPathExpression</code> from the given string expression.
     *
     * @param expression the XPath expression
     * @return the compiled <code>XPathExpression</code>
     * @throws XPathParseException when the given expression cannot be parsed
     */
    static XPathExpression createXPathExpression(String expression) {
        try {
            XPath xpath = new XPath(expression, null, null, XPath.SELECT);
            return new XalanXPathExpression(xpath);
        }
        catch (TransformerException ex) {
            throw new org.springframework.xml.xpath.XPathParseException(
                    "Could not compile [" + expression + "] to a XPathExpression: " + ex.getMessage(), ex);
        }
    }

    /**
     * Creates a Xalan <code>XPathExpression</code> from the given string expression and namespaces.
     *
     * @param expression the XPath expression
     * @return the compiled <code>XPathExpression</code>
     * @throws XPathParseException when the given expression cannot be parsed
     */
    public static XPathExpression createXPathExpression(String expression, Map namespaces) {
        try {
            PrefixResolver prefixResolver = new SimplePrefixResolver(namespaces);
            XPath xpath = new XPath(expression, null, prefixResolver, XPath.SELECT);
            return new XalanXPathExpression(xpath);
        }
        catch (TransformerException ex) {
            throw new org.springframework.xml.xpath.XPathParseException(
                    "Could not compile [" + expression + "] to a XPathExpression: " + ex.getMessage(), ex);
        }
    }

    /**
     * Xalan implementation of the <code>XPathExpression</code> interface.
     */
    private static class XalanXPathExpression implements XPathExpression {

        private XPath xpath;

        private XPathContext context = new XPathContext();

        private XalanXPathExpression(XPath xpath) {
            this.xpath = xpath;
        }

        public Node evaluateAsNode(Node node) {
            try {
                XObject result = xpath.execute(context, node, null);
                NodeIterator iterator = result.nodeset();
                return iterator.nextNode();
            }
            catch (TransformerException ex) {
                throw new XPathException("Could not evaluate XPath expression:" + ex.getMessage(), ex);
            }
        }

        public boolean evaluateAsBoolean(Node node) {
            try {
                XObject result = xpath.execute(context, node, null);
                return result.bool();
            }
            catch (TransformerException ex) {
                throw new XPathException("Could not evaluate XPath expression:" + ex.getMessage(), ex);
            }
        }

        public double evaluateAsNumber(Node node) {
            try {
                XObject result = xpath.execute(context, node, null);
                return result.num();
            }
            catch (TransformerException ex) {
                throw new XPathException("Could not evaluate XPath expression:" + ex.getMessage(), ex);
            }
        }

        public String evaluateAsString(Node node) {
            try {
                XObject result = xpath.execute(context, node, null);
                return result.str();
            }
            catch (TransformerException ex) {
                throw new XPathException("Could not evaluate XPath expression:" + ex.getMessage(), ex);
            }
        }

        public Node[] evaluateAsNodes(Node node) {
            try {
                XObject result = xpath.execute(context, node, null);
                return toNodeArray(result.nodelist());
            }
            catch (TransformerException ex) {
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
    }

    private static class SimplePrefixResolver implements PrefixResolver {

        private Map namespaces;

        private SimplePrefixResolver(Map namespaces) {
            this.namespaces = namespaces;
        }

        public String getNamespaceForPrefix(String prefix) {
            return (String) namespaces.get(prefix);
        }

        public String getNamespaceForPrefix(String prefix, Node node) {
            return (String) namespaces.get(prefix);
        }

        public String getBaseIdentifier() {
            return null;
        }

        public boolean handlesNullPrefixes() {
            return false;
        }
    }
}
