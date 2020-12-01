/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.xml.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of {@link XPathOperations} that uses Jaxen.
 * <p>
 * Namespaces can be set using the {@code namespaces} property.
 *
 * @author Arjen Poutsma
 * @see <a href="http://www.jaxen.org/">Jaxen</a>
 * @since 1.0.0
 */
public class JaxenXPathTemplate extends AbstractXPathTemplate {

	@Override
	public boolean evaluateAsBoolean(String expression, Source context) throws XPathException {
		try {
			XPath xpath = createXPath(expression);
			Element element = getRootElement(context);
			return xpath.booleanValueOf(element);
		} catch (JaxenException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		} catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
	}

	@Override
	public Node evaluateAsNode(String expression, Source context) throws XPathException {
		try {
			XPath xpath = createXPath(expression);
			Element element = getRootElement(context);
			return (Node) xpath.selectSingleNode(element);
		} catch (JaxenException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		} catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Node> evaluateAsNodeList(String expression, Source context) throws XPathException {
		try {
			XPath xpath = createXPath(expression);
			Element element = getRootElement(context);
			return xpath.selectNodes(element);
		} catch (JaxenException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		} catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
	}

	@Override
	public double evaluateAsDouble(String expression, Source context) throws XPathException {
		try {
			XPath xpath = createXPath(expression);
			Element element = getRootElement(context);
			return xpath.numberValueOf(element).doubleValue();
		} catch (JaxenException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		} catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
	}

	@Override
	public String evaluateAsString(String expression, Source context) throws XPathException {
		try {
			XPath xpath = createXPath(expression);
			Element element = getRootElement(context);
			return xpath.stringValueOf(element);
		} catch (JaxenException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		} catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
	}

	@Override
	public <T> T evaluateAsObject(String expression, Source context, NodeMapper<T> nodeMapper) throws XPathException {
		try {
			XPath xpath = createXPath(expression);
			Element element = getRootElement(context);
			Node node = (Node) xpath.selectSingleNode(element);
			if (node != null) {
				try {
					return nodeMapper.mapNode(node, 0);
				} catch (DOMException ex) {
					throw new XPathException("Mapping resulted in DOMException", ex);
				}
			} else {
				return null;
			}

		} catch (JaxenException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		} catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
	}

	@Override
	public <T> List<T> evaluate(String expression, Source context, NodeMapper<T> nodeMapper) throws XPathException {
		try {
			XPath xpath = createXPath(expression);
			Element element = getRootElement(context);
			List<?> nodes = xpath.selectNodes(element);
			List<T> results = new ArrayList<T>(nodes.size());
			for (int i = 0; i < nodes.size(); i++) {
				Node node = (Node) nodes.get(i);
				try {
					results.add(nodeMapper.mapNode(node, i));
				} catch (DOMException ex) {
					throw new XPathException("Mapping resulted in DOMException", ex);
				}
			}
			return results;
		} catch (JaxenException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		} catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
	}

	private XPath createXPath(String expression) throws JaxenException {
		XPath xpath = new DOMXPath(expression);
		if (getNamespaces() != null && !getNamespaces().isEmpty()) {
			xpath.setNamespaceContext(new SimpleNamespaceContext(getNamespaces()));
		}
		return xpath;
	}
}
