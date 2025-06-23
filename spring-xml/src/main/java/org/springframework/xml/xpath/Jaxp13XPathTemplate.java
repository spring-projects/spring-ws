/*
 * Copyright 2005-present the original author or authors.
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

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.springframework.util.xml.StaxUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.springframework.xml.transform.TransformerHelper;
import org.springframework.xml.transform.TraxUtils;

/**
 * Implementation of {@link XPathOperations} that uses JAXP 1.3. JAXP 1.3 is part of Java
 * SE since 1.5.
 * <p>
 * Namespaces can be set using the {@code namespaces} property.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setNamespaces(java.util.Map)
 */
public class Jaxp13XPathTemplate extends AbstractXPathTemplate {

	private XPathFactory xpathFactory;

	public Jaxp13XPathTemplate() {
		this(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
	}

	public Jaxp13XPathTemplate(String xpathFactoryUri) {
		try {
			this.xpathFactory = XPathFactory.newInstance(xpathFactoryUri);
		}
		catch (XPathFactoryConfigurationException ex) {
			throw new XPathException("Could not create XPathFactory", ex);
		}
	}

	@Override
	public boolean evaluateAsBoolean(String expression, Source context) throws XPathException {
		Boolean result = (Boolean) evaluate(expression, context, XPathConstants.BOOLEAN);
		return result != null && result;
	}

	@Override
	public @Nullable Node evaluateAsNode(String expression, Source context) throws XPathException {
		return (Node) evaluate(expression, context, XPathConstants.NODE);
	}

	@Override
	public List<Node> evaluateAsNodeList(String expression, Source context) throws XPathException {
		NodeList result = (NodeList) evaluate(expression, context, XPathConstants.NODESET);
		List<Node> nodes = new ArrayList<>();
		if (result != null) {
			for (int i = 0; i < result.getLength(); i++) {
				nodes.add(result.item(i));
			}
		}
		return nodes;
	}

	@Override
	public double evaluateAsDouble(String expression, Source context) throws XPathException {
		Double result = (Double) evaluate(expression, context, XPathConstants.NUMBER);
		return (result != null) ? result : Double.NaN;
	}

	@Override
	public @Nullable String evaluateAsString(String expression, Source context) throws XPathException {
		return (String) evaluate(expression, context, XPathConstants.STRING);
	}

	@Override
	public <T> @Nullable T evaluateAsObject(String expression, Source context, NodeMapper<T> nodeMapper)
			throws XPathException {
		Node node = evaluateAsNode(expression, context);
		try {
			return (node != null) ? nodeMapper.mapNode(node, 0) : null;
		}
		catch (DOMException ex) {
			throw new XPathException("Mapping resulted in DOMException", ex);
		}
	}

	@Override
	public <T> List<T> evaluate(String expression, Source context, NodeMapper<T> nodeMapper) throws XPathException {
		NodeList nodes = (NodeList) evaluate(expression, context, XPathConstants.NODESET);
		List<T> results = new ArrayList<>();
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				try {
					results.add(nodeMapper.mapNode(nodes.item(i), i));
				}
				catch (DOMException ex) {
					throw new XPathException("Mapping resulted in DOMException", ex);
				}
			}
		}
		return results;
	}

	private @Nullable Object evaluate(String expression, Source context, QName returnType) throws XPathException {
		XPath xpath = createXPath();
		Map<String, String> namespaces = getNamespaces();
		if (!namespaces.isEmpty()) {
			SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
			namespaceContext.setBindings(namespaces);
			xpath.setNamespaceContext(namespaceContext);
		}
		try {
			EvaluationCallback callback = new EvaluationCallback(xpath, expression, returnType);
			TraxUtils.doWithSource(context, callback);
			return callback.result;
		}
		catch (javax.xml.xpath.XPathException ex) {
			throw new XPathException("Could not evaluate XPath expression [" + expression + "]", ex);
		}
		catch (TransformerException ex) {
			throw new XPathException("Could not transform context to DOM Node", ex);
		}
		catch (Exception ex) {
			throw new XPathException(ex.getMessage(), ex);
		}
	}

	private synchronized XPath createXPath() {
		return this.xpathFactory.newXPath();
	}

	private static final class EvaluationCallback implements TraxUtils.SourceCallback {

		private final XPath xpath;

		private final String expression;

		private final QName returnType;

		private final TransformerHelper transformerHelper = new TransformerHelper();

		private @Nullable Object result;

		private EvaluationCallback(XPath xpath, String expression, QName returnType) {
			this.xpath = xpath;
			this.expression = expression;
			this.returnType = returnType;
		}

		@Override
		public void domSource(Node node) throws XPathExpressionException {
			this.result = this.xpath.evaluate(this.expression, node, this.returnType);
		}

		@Override
		public void saxSource(XMLReader reader, InputSource inputSource) throws XPathExpressionException {
			inputSource(inputSource);
		}

		@Override
		public void staxSource(XMLEventReader eventReader)
				throws XPathExpressionException, XMLStreamException, TransformerException {
			Element element = getRootElement(StaxUtils.createCustomStaxSource(eventReader));
			domSource(element);
		}

		@Override
		public void staxSource(XMLStreamReader streamReader) throws TransformerException, XPathExpressionException {
			Element element = getRootElement(StaxUtils.createCustomStaxSource(streamReader));
			domSource(element);
		}

		@Override
		public void streamSource(InputStream inputStream) throws XPathExpressionException {
			inputSource(new InputSource(inputStream));
		}

		@Override
		public void streamSource(Reader reader) throws XPathExpressionException {
			inputSource(new InputSource(reader));
		}

		@Override
		public void source(String systemId) throws XPathExpressionException {
			inputSource(new InputSource(systemId));
		}

		private void inputSource(InputSource inputSource) throws XPathExpressionException {
			this.result = this.xpath.evaluate(this.expression, inputSource, this.returnType);
		}

		private Element getRootElement(Source source) throws TransformerException {
			DOMResult domResult = new DOMResult();
			this.transformerHelper.transform(source, domResult);
			Document document = (Document) domResult.getNode();
			return document.getDocumentElement();
		}

	}

}
