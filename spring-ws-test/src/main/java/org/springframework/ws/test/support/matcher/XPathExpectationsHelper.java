/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.matcher;

import java.io.IOException;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Node;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.support.AssertionErrors;
import org.springframework.xml.transform.TransformerHelper;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

/**
 * Helper class for dealing with XPath expectations.
 *
 * @author Lukas Krecan
 * @author Arjen Poutsma
 * @since 2.0
 */
public class XPathExpectationsHelper {

	private final XPathExpression expression;

	private final String expressionString;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	/**
	 * Creates a new instance of the {@code XPathExpectationsSupport} with the given XPath
	 * expression.
	 * @param expression the XPath expression
	 */
	public XPathExpectationsHelper(String expression) {
		this(expression, null);
	}

	/**
	 * Creates a new instance of the {@code XPathExpectationsSupport} with the given XPath
	 * expression and namespaces.
	 * @param expression the XPath expression
	 * @param namespaces the namespaces, can be empty or {@code null}
	 */
	public XPathExpectationsHelper(String expression, Map<String, String> namespaces) {
		Assert.hasLength(expression, "'expression' must not be empty");
		this.expression = XPathExpressionFactory.createXPathExpression(expression, namespaces);
		this.expressionString = expression;
	}

	public WebServiceMessageMatcher exists() {
		return new WebServiceMessageMatcher() {
			public void match(WebServiceMessage message) throws IOException, AssertionError {
				Node payload = transformToNode(message);
				Node result = XPathExpectationsHelper.this.expression.evaluateAsNode(payload);
				if (result == null) {
					AssertionErrors.fail("No match for \"" + XPathExpectationsHelper.this.expressionString + "\" found",
							"Payload", message.getPayloadSource());
				}
			}
		};
	}

	public WebServiceMessageMatcher doesNotExist() {
		return new WebServiceMessageMatcher() {
			public void match(WebServiceMessage message) throws IOException, AssertionError {
				Node payload = transformToNode(message);
				Node result = XPathExpectationsHelper.this.expression.evaluateAsNode(payload);
				if (result != null) {
					AssertionErrors.fail("Match for \"" + XPathExpectationsHelper.this.expressionString + "\" found",
							"Payload", message.getPayloadSource());
				}
			}
		};
	}

	public WebServiceMessageMatcher evaluatesTo(final boolean expectedValue) {
		return new WebServiceMessageMatcher() {
			public void match(WebServiceMessage message) throws IOException, AssertionError {
				Node payload = transformToNode(message);
				boolean result = XPathExpectationsHelper.this.expression.evaluateAsBoolean(payload);
				AssertionErrors.assertEquals("Evaluation of XPath expression \""
						+ XPathExpectationsHelper.this.expressionString + "\" failed.", expectedValue, result,
						"Payload", message.getPayloadSource());

			}
		};
	}

	public WebServiceMessageMatcher evaluatesTo(int expectedValue) {
		return evaluatesTo((double) expectedValue);
	}

	public WebServiceMessageMatcher evaluatesTo(final double expectedValue) {
		return new WebServiceMessageMatcher() {
			public void match(WebServiceMessage message) throws IOException, AssertionError {
				Node payload = transformToNode(message);
				double result = XPathExpectationsHelper.this.expression.evaluateAsNumber(payload);
				AssertionErrors.assertEquals("Evaluation of XPath expression \""
						+ XPathExpectationsHelper.this.expressionString + "\" failed.", expectedValue, result,
						"Payload", message.getPayloadSource());

			}
		};
	}

	public WebServiceMessageMatcher evaluatesTo(final String expectedValue) {
		Assert.notNull(expectedValue, "'expectedValue' must not be null");
		return new WebServiceMessageMatcher() {
			public void match(WebServiceMessage message) throws IOException, AssertionError {
				Node payload = transformToNode(message);
				String result = XPathExpectationsHelper.this.expression.evaluateAsString(payload);
				AssertionErrors.assertEquals("Evaluation of XPath expression \""
						+ XPathExpectationsHelper.this.expressionString + "\" failed.", expectedValue, result,
						"Payload", message.getPayloadSource());
			}
		};
	}

	private Node transformToNode(WebServiceMessage request) {
		DOMResult domResult = new DOMResult();
		try {
			this.transformerHelper.transform(request.getPayloadSource(), domResult);
			return domResult.getNode();
		}
		catch (TransformerException ex) {
			AssertionErrors.fail("Could not transform request payload: " + ex.getMessage());
			return null;
		}
	}

}
