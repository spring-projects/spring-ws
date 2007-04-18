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

import java.util.List;

import org.w3c.dom.Node;

/**
 * Defines the contract for a precompiled XPath expression. Concrete instances can be obtained through the {@link
 * XPathExpressionFactory}.
 * <p/>
 * Implementations of this interface are precompiled, and thus faster, but less flexible, than the XPath expressions
 * used by {@link XPathOperations} implementations.
 *
 * @author Arjen Poutsma
 */
public interface XPathExpression {

    /**
     * Evaluates the given expression as a <code>boolean</code>. Returns the boolean evaluation of the expression, or
     * <code>false</code> if it is invalid.
     *
     * @param node the starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#booleans">XPath specification</a>
     */
    boolean evaluateAsBoolean(Node node) throws XPathException;

    /**
     * Evaluates the given expression as a {@link Node}. Returns the evaluation of the expression, or <code>null</code>
     * if it is invalid.
     *
     * @param node the starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    Node evaluateAsNode(Node node) throws XPathException;

    /**
     * Evaluates the given expression, and returns all {@link Node} objects that conform to it. Returns an empty list if
     * no result could be found.
     *
     * @param node the starting point
     * @return a list of <code>Node</code>s that are selected by the expression
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    List evaluateAsNodeList(Node node) throws XPathException;

    /**
     * Evaluates the given expression as a number (<code>double</code>). Returns the numeric evaluation of the
     * expression, or {@link Double#NaN} if it is invalid.
     *
     * @param node the starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#numbers">XPath specification</a>
     */
    double evaluateAsNumber(Node node) throws XPathException;

    /**
     * Evaluates the given expression as a String. Returns <code>null</code> if no result could be found.
     *
     * @param node the starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#strings">XPath specification</a>
     */
    String evaluateAsString(Node node) throws XPathException;

    /**
     * Evaluates the given expression, mapping a single {@link Node} result to a Java object via a {@link NodeMapper}.
     *
     * @param node       the  starting point
     * @param nodeMapper object that will map one object per node
     * @return the single mapped object
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    Object evaluateAsObject(Node node, NodeMapper nodeMapper) throws XPathException;

    /**
     * Evaluates the given expression, mapping each result {@link Node} objects to a Java object via a {@link
     * NodeMapper}.
     *
     * @param node       the  starting point
     * @param nodeMapper object that will map one object per node
     * @return the result list, containing mapped objects
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    List evaluate(Node node, NodeMapper nodeMapper) throws XPathException;
}
