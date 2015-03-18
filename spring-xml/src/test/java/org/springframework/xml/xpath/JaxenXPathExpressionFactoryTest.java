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

import java.io.IOException;
import java.util.Map;

import org.xml.sax.SAXException;

public class JaxenXPathExpressionFactoryTest extends AbstractXPathExpressionFactoryTestCase {

	@Override
	protected XPathExpression createXPathExpression(String expression) {
		return JaxenXPathExpressionFactory.createXPathExpression(expression);
	}

	@Override
	protected XPathExpression createXPathExpression(String expression, Map<String, String> namespaces) {
		return JaxenXPathExpressionFactory.createXPathExpression(expression, namespaces);
	}

	@Override
	public void testEvaluateAsDoubleNoNamespaces() throws IOException, SAXException {
		// Currently not working on Jaxen 1.1 beta 8, hence the override here
	}

	@Override
	public void testEvaluateAsDoubleNamespaces() throws IOException, SAXException {
		// Currently not working on Jaxen 1.1 beta 8, hence the override here
	}

}
