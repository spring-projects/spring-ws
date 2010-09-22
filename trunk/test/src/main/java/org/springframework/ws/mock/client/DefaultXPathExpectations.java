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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

import org.w3c.dom.Node;

import static org.springframework.ws.mock.support.Assert.assertEquals;
import static org.springframework.ws.mock.support.Assert.fail;

/**
 * Default implementation of {@link XPathExpectations}.
 *
 * @author Lukas Krecan
 * @author Arjen Poutsma
 * @since 2.0
 */
class DefaultXPathExpectations extends TransformerObjectSupport implements XPathExpectations {

    private final XPathExpression expression;

    private final String expressionString;

    DefaultXPathExpectations(String expression, Map<String, String> namespaces) {
        Assert.hasLength(expression, "'expression' must not be empty");
        this.expression = XPathExpressionFactory.createXPathExpression(expression, namespaces);
        this.expressionString = expression;
    }

    public RequestMatcher exists() {
        return new RequestMatcher() {
            public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
                Node payload = transformToNode(request);
                Node result = expression.evaluateAsNode(payload);
                if (result == null) {
                    fail("No match for \"" + expressionString + "\" found");
                }
            }
        };
    }

    public RequestMatcher doesNotExist() {
        return new RequestMatcher() {
            public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
                Node payload = transformToNode(request);
                Node result = expression.evaluateAsNode(payload);
                if (result != null) {
                    fail("Match for \"" + expressionString + "\" found");
                }
            }
        };
    }

    public RequestMatcher evaluatesTo(final boolean expectedValue) {
        return new RequestMatcher() {
            public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
                Node payload = transformToNode(request);
                boolean result = expression.evaluateAsBoolean(payload);
                assertEquals("Evaluation of XPath expression \"" + expressionString + "\" failed.", expectedValue,
                        result);

            }
        };
    }

    public RequestMatcher evaluatesTo(int expectedValue) {
        return evaluatesTo((double) expectedValue);
    }

    public RequestMatcher evaluatesTo(final double expectedValue) {
        return new RequestMatcher() {
            public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
                Node payload = transformToNode(request);
                double result = expression.evaluateAsNumber(payload);
                assertEquals("Evaluation of XPath expression \"" + expressionString + "\" failed.", expectedValue,
                        result);

            }
        };
    }

    public RequestMatcher evaluatesTo(final String expectedValue) {
        Assert.notNull(expectedValue, "'expectedValue' must not be null");
        return new RequestMatcher() {
            public void match(URI uri, WebServiceMessage request) throws IOException, AssertionError {
                Node payload = transformToNode(request);
                String result = expression.evaluateAsString(payload);
                assertEquals("Evaluation of XPath expression \"" + expressionString + "\" failed.", expectedValue,
                        result);
            }
        };
    }

    private Node transformToNode(WebServiceMessage request) {
        DOMResult domResult = new DOMResult();
        try {
            transform(request.getPayloadSource(), domResult);
            return domResult.getNode();
        }
        catch (TransformerException ex) {
            fail("Could not transform request payload: " + ex.getMessage());
            return null;
        }
    }


}
