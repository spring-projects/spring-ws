/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import java.util.Collections;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for implementations of {@link XPathOperations}. Contains a
 * namespaces property.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractXPathTemplate extends TransformerObjectSupport implements XPathOperations {

	private Map<String, String> namespaces = Collections.emptyMap();

	/** Returns namespaces used in the XPath expression. */
	public Map<String, String> getNamespaces() {
		return this.namespaces;
	}

	/** Sets namespaces used in the XPath expression. Maps prefixes to namespaces. */
	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}

	@Override
	public final void evaluate(String expression, Source context, NodeCallbackHandler callbackHandler)
			throws XPathException {
		evaluate(expression, context, new NodeCallbackHandlerNodeMapper(callbackHandler));
	}

	/**
	 * Returns the root element of the given source.
	 * @param source the source to get the root element from
	 * @return the root element
	 */
	protected Element getRootElement(Source source) throws TransformerException {
		DOMResult domResult = new DOMResult();
		transform(source, domResult);
		Document document = (Document) domResult.getNode();
		return document.getDocumentElement();
	}

	/**
	 * Static inner class that adapts a {@link NodeCallbackHandler} to the interface of
	 * {@link NodeMapper}.
	 */
	private static final class NodeCallbackHandlerNodeMapper implements NodeMapper<Object> {

		private final NodeCallbackHandler callbackHandler;

		NodeCallbackHandlerNodeMapper(NodeCallbackHandler callbackHandler) {
			this.callbackHandler = callbackHandler;
		}

		@Override
		public @Nullable Object mapNode(Node node, int nodeNum) throws DOMException {
			this.callbackHandler.processNode(node);
			return null;
		}

	}

}
