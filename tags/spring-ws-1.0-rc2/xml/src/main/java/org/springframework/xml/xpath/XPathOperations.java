/*
 * Copyright 2007 the original author or authors.
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
import javax.xml.transform.Source;

import org.w3c.dom.Node;

/**
 * Interface that specifies a basic set of XPath operations, implemented by various XPathTemplates. Contains numerous
 * evaluation methods,
 * <p/>
 * The templates that implement this interface do not use precompiled XPath expressions. Consider using the {@link
 * XPathExpressionFactory} or the {@link XPathExpressionFactoryBean} for optimal performance, but less flexibility.
 *
 * @author Arjen Poutsma
 * @see Jaxp13XPathTemplate
 * @see JaxenXPathTemplate
 */
public interface XPathOperations {

    /**
     * Evaluates the given expression as a <code>boolean</code>. Returns the boolean evaluation of the expression, or
     * <code>false</code> if it is invalid.
     *
     * @param expression the XPath expression
     * @param context    the context starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#booleans">XPath specification</a>
     */
    boolean evaluateAsBoolean(String expression, Source context) throws XPathException;

    /**
     * Evaluates the given expression as a {@link Node}. Returns the evaluation of the expression, or <code>null</code>
     * if it is invalid.
     *
     * @param expression the XPath expression
     * @param context    the context starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    Node evaluateAsNode(String expression, Source context) throws XPathException;

    /**
     * Evaluates the given expression as a list of {@link Node} objects. Returns the evaluation of the expression, or an
     * empty list if no results are found.
     *
     * @param expression the XPath expression
     * @param context    the context starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    List evaluateAsNodeList(String expression, Source context) throws XPathException;

    /**
     * Evaluates the given expression as a <code>double</code>. Returns the evaluation of the expression, or {@link
     * Double#NaN} if it is invalid.
     *
     * @param expression the XPath expression
     * @param context    the context starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#numbers">XPath specification</a>
     */
    double evaluateAsDouble(String expression, Source context) throws XPathException;

    /**
     * Evaluates the given expression as a {@link String}. Returns the evaluation of the expression, or
     * <code>null</code> if it is invalid.
     *
     * @param expression the XPath expression
     * @param context    the context starting point
     * @return the result of the evaluation
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#strings">XPath specification</a>
     */
    String evaluateAsString(String expression, Source context) throws XPathException;

    /**
     * Evaluates the given expression, mapping a single {@link Node} result to a Java object via a {@link NodeMapper}.
     *
     * @param expression the XPath expression
     * @param context    the context starting point
     * @param nodeMapper object that will map one object per node
     * @return the single mapped object
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    Object evaluateAsObject(String expression, Source context, NodeMapper nodeMapper) throws XPathException;

    /**
     * Evaluates the given expression, mapping each result {@link Node} objects to a Java object via a {@link
     * NodeMapper}.
     *
     * @param expression the XPath expression
     * @param context    the context starting point
     * @param nodeMapper object that will map one object per node
     * @return the result list, containing mapped objects
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    List evaluate(String expression, Source context, NodeMapper nodeMapper) throws XPathException;

    /**
     * Evaluates the given expression, handling the result {@link Node} objects on a per-node basis with a {@link
     * NodeCallbackHandler}.
     *
     * @param expression      the XPath expression
     * @param context         the context starting point
     * @param callbackHandler object that will extract results, one row at a time
     * @throws XPathException in case of XPath errors
     * @see <a href="http://www.w3.org/TR/xpath#node-sets">XPath specification</a>
     */
    void evaluate(String expression, Source context, NodeCallbackHandler callbackHandler) throws XPathException;
}
