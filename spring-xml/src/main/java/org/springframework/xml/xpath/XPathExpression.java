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

import java.util.List;

import org.w3c.dom.Node;

/**
 * Defines the contract for a precompiled XPath expression. Concrete instances can be obtained through the {@link
 * XPathExpressionFactory}.
 *
 * <p>Implementations of this interface are precompiled, and thus faster, but less flexible, than the XPath expressions
 * used by {@link XPathOperations} implementations.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface XPathExpression {

	/**
	 * Evaluates the given expression as a {@code boolean}. Returns the boolean evaluation of the expression, or
	 * {@code false} if it is invalid.
	 *
	 * <p>The return value is determined per the {@code boolean()} function defined in the XPath specification.
	 * This means that an expression that selects zero nodes will return {@code false}, while an expression that
	 * selects one or more nodes will return {@code true}.
	 * An expression that returns a string returns {@code false} for empty strings and {@code true} for all other
	 * strings.
	 * An expression that returns a number returns {@code false} for zero and {@code true} for non-zero numbers.
	 *
	 * @param node the starting point
	 * @return the result of the evaluation
	 * @throws XPathException in case of XPath errors
	 * @see <a href="https://www.w3.org/TR/xpath/#function-boolean">XPath specification - boolean() function</a>
	 */
	boolean evaluateAsBoolean(Node node) throws XPathException;

	/**
	 * Evaluates the given expression as a {@link Node}. Returns the evaluation of the expression, or {@code null}
	 * if it is invalid.
	 *
	 * @param node the starting point
	 * @return the result of the evaluation
	 * @throws XPathException in case of XPath errors
	 * @see <a href="https://www.w3.org/TR/xpath#node-sets">XPath specification</a>
	 */
	Node evaluateAsNode(Node node) throws XPathException;

	/**
	 * Evaluates the given expression, and returns all {@link Node} objects that conform to it. Returns an empty list if
	 * no result could be found.
	 *
	 * @param node the starting point
	 * @return a list of {@code Node}s that are selected by the expression
	 * @throws XPathException in case of XPath errors
	 * @see <a href="https://www.w3.org/TR/xpath#node-sets">XPath specification</a>
	 */
	List<Node> evaluateAsNodeList(Node node) throws XPathException;

	/**
	 * Evaluates the given expression as a number ({@code double}). Returns the numeric evaluation of the
	 * expression, or {@link Double#NaN} if it is invalid.
	 *
	 * <p>The return value is determined per the {@code number()} function as defined in the XPath specification.
	 * This means that if the expression selects multiple nodes, it will return the number value of the first node.
	 *
	 * @param node the starting point
	 * @return the result of the evaluation
	 * @throws XPathException in case of XPath errors
	 * @see <a href="https://www.w3.org/TR/xpath/#function-number">XPath specification - number() function</a>
	 */
	double evaluateAsNumber(Node node) throws XPathException;

	/**
	 * Evaluates the given expression as a String. Returns {@code null} if no result could be found.
	 *
	 * <p>The return value is determined per the {@code string()} function as defined in the XPath specification.
	 * This means that if the expression selects multiple nodes, it will return the string value of the first node.
	 *
	 * @param node the starting point
	 * @return the result of the evaluation
	 * @throws XPathException in case of XPath errors
	 * @see <a href="https://www.w3.org/TR/xpath/#function-string">XPath specification - string() function</a>
	 */
	String evaluateAsString(Node node) throws XPathException;

	/**
	 * Evaluates the given expression, mapping a single {@link Node} result to a Java object via a {@link NodeMapper}.
	 *
	 * @param node		 the  starting point
	 * @param nodeMapper object that will map one object per node
	 * @return the single mapped object
	 * @throws XPathException in case of XPath errors
	 * @see <a href="https://www.w3.org/TR/xpath#node-sets">XPath specification</a>
	 */
	<T> T evaluateAsObject(Node node, NodeMapper<T> nodeMapper) throws XPathException;

	/**
	 * Evaluates the given expression, mapping each result {@link Node} objects to a Java object via a {@link
	 * NodeMapper}.
	 *
	 * @param node		 the  starting point
	 * @param nodeMapper object that will map one object per node
	 * @return the result list, containing mapped objects
	 * @throws XPathException in case of XPath errors
	 * @see <a href="https://www.w3.org/TR/xpath#node-sets">XPath specification</a>
	 */
	<T> List<T> evaluate(Node node, NodeMapper<T> nodeMapper) throws XPathException;
}
