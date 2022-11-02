/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xml.xpath;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Factory for compiled {@code XPathExpression}s, being aware of JAXP 1.3+ XPath functionality, and Jaxen. Mainly for
 * internal use of the framework.
 * <p>
 * The goal of this class is to avoid runtime dependencies a specific XPath engine, simply using the best XPath
 * implementation that is available. Prefers JAXP 1.3+ XPath implementations to Jaxen.
 *
 * @author Arjen Poutsma
 * @see XPathExpression
 * @since 1.0.0
 */
public abstract class XPathExpressionFactory {

	private static final Log logger = LogFactory.getLog(XPathExpressionFactory.class);

	/**
	 * Create a compiled XPath expression using the given string.
	 *
	 * @param expression the XPath expression
	 * @return the compiled XPath expression
	 * @throws IllegalStateException if neither JAXP 1.3+, or Jaxen are available
	 * @throws XPathParseException if the given expression cannot be parsed
	 */
	public static XPathExpression createXPathExpression(String expression)
			throws IllegalStateException, XPathParseException {
		return createXPathExpression(expression, Collections.<String, String> emptyMap());
	}

	/**
	 * Create a compiled XPath expression using the given string and namespaces. The namespace map should consist of
	 * string prefixes mapped to string namespaces.
	 *
	 * @param expression the XPath expression
	 * @param namespaces a map that binds string prefixes to string namespaces
	 * @return the compiled XPath expression
	 * @throws IllegalStateException if neither JAXP 1.3+, or Jaxen are available
	 * @throws XPathParseException if the given expression cannot be parsed
	 */
	public static XPathExpression createXPathExpression(String expression, Map<String, String> namespaces)
			throws IllegalStateException, XPathParseException {
		Assert.hasLength(expression, "expression is empty");
		if (namespaces == null) {
			namespaces = Collections.emptyMap();
		}
		try {
			logger.trace("Creating [javax.xml.xpath.XPathExpression]");
			return Jaxp13XPathExpressionFactory.createXPathExpression(expression, namespaces);
		} catch (XPathException e) {
			throw e;
		}
	}

}
