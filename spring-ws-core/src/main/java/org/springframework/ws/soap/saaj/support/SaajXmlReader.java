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

package org.springframework.ws.soap.saaj.support;

import java.util.Iterator;

import jakarta.xml.soap.Name;
import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributesImpl;

import org.springframework.util.StringUtils;
import org.springframework.xml.sax.AbstractXmlReader;

/**
 * SAX {@code XMLReader} that reads from a SAAJ {@code Node}. Consumes {@code XMLEvents}
 * from an {@code XMLEventReader}, and calls the corresponding methods on the SAX callback
 * interfaces.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see jakarta.xml.soap.Node
 * @see jakarta.xml.soap.SOAPElement
 */
public class SaajXmlReader extends AbstractXmlReader {

	private static final String NAMESPACES_FEATURE_NAME = "http://xml.org/sax/features/namespaces";

	private static final String NAMESPACE_PREFIXES_FEATURE_NAME = "http://xml.org/sax/features/namespace-prefixes";

	private final Node startNode;

	private boolean namespacesFeature = true;

	private boolean namespacePrefixesFeature = false;

	/**
	 * Constructs a new instance of the {@code SaajXmlReader} that reads from the given
	 * {@code Node}.
	 * @param startNode the SAAJ {@code Node} to read from
	 */
	public SaajXmlReader(Node startNode) {
		this.startNode = startNode;
	}

	@Override
	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		if (NAMESPACES_FEATURE_NAME.equals(name)) {
			return this.namespacesFeature;
		}
		else if (NAMESPACE_PREFIXES_FEATURE_NAME.equals(name)) {
			return this.namespacePrefixesFeature;
		}
		else {
			return super.getFeature(name);
		}
	}

	@Override
	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		if (NAMESPACES_FEATURE_NAME.equals(name)) {
			this.namespacesFeature = value;
		}
		else if (NAMESPACE_PREFIXES_FEATURE_NAME.equals(name)) {
			this.namespacePrefixesFeature = value;
		}
		else {
			super.setFeature(name, value);
		}
	}

	/**
	 * Parses the StAX XML reader passed at construction-time.
	 * <p>
	 * <strong>Note</strong> that the given {@code InputSource} is not read, but ignored.
	 * @param ignored is ignored
	 * @throws org.xml.sax.SAXException a SAX exception, possibly wrapping a
	 * {@code XMLStreamException}
	 */
	@Override
	public final void parse(InputSource ignored) throws SAXException {
		parse();
	}

	/**
	 * Parses the StAX XML reader passed at construction-time.
	 * <p>
	 * <strong>Note</strong> that the given system identifier is not read, but ignored.
	 * @param ignored is ignored
	 * @throws SAXException a SAX exception, possibly wrapping a
	 * {@code XMLStreamException}
	 */
	@Override
	public final void parse(String ignored) throws SAXException {
		parse();
	}

	private void parse() throws SAXException {
		if (getContentHandler() != null) {
			getContentHandler().startDocument();
		}
		handleNode(this.startNode);
		if (getContentHandler() != null) {
			getContentHandler().endDocument();
		}
	}

	private void handleNode(Node node) throws SAXException {
		if (node instanceof SOAPElement) {
			handleElement((SOAPElement) node);
		}
		else if (node instanceof Text text) {
			handleText(text);
		}
	}

	private void handleElement(SOAPElement element) throws SAXException {
		Name elementName = element.getElementName();
		if (getContentHandler() != null) {
			if (this.namespacesFeature) {
				for (Iterator<?> iterator = element.getNamespacePrefixes(); iterator.hasNext();) {
					String prefix = (String) iterator.next();
					String namespaceUri = element.getNamespaceURI(prefix);
					getContentHandler().startPrefixMapping(prefix, namespaceUri);
				}
				getContentHandler().startElement(elementName.getURI(), elementName.getLocalName(),
						elementName.getQualifiedName(), getAttributes(element));
			}
			else {
				getContentHandler().startElement("", "", elementName.getQualifiedName(), getAttributes(element));
			}
		}
		for (Iterator<?> iterator = element.getChildElements(); iterator.hasNext();) {
			Node child = (Node) iterator.next();
			handleNode(child);
		}
		if (getContentHandler() != null) {
			if (this.namespacesFeature) {
				getContentHandler().endElement(elementName.getURI(), elementName.getLocalName(),
						elementName.getQualifiedName());
				for (Iterator<?> iterator = element.getNamespacePrefixes(); iterator.hasNext();) {
					String prefix = (String) iterator.next();
					getContentHandler().endPrefixMapping(prefix);
				}
			}
			else {
				getContentHandler().endElement("", "", elementName.getQualifiedName());
			}
		}
	}

	private void handleText(Text text) throws SAXException {
		if (getContentHandler() != null) {
			char[] ch = (text.getValue() != null) ? text.getValue().toCharArray() : new char[0];
			getContentHandler().characters(ch, 0, ch.length);
		}
	}

	private Attributes getAttributes(SOAPElement element) {
		AttributesImpl attributes = new AttributesImpl();

		for (Iterator<?> iterator = element.getAllAttributes(); iterator.hasNext();) {
			Name attributeName = (Name) iterator.next();
			String namespace = attributeName.getURI();
			if (namespace == null || !this.namespacesFeature) {
				namespace = "";
			}
			String attributeValue = element.getAttributeValue(attributeName);
			attributes.addAttribute(namespace, attributeName.getLocalName(), attributeName.getQualifiedName(), "CDATA",
					attributeValue);
		}
		if (this.namespacePrefixesFeature) {
			for (Iterator<?> iterator = element.getNamespacePrefixes(); iterator.hasNext();) {
				String prefix = (String) iterator.next();
				String namespaceUri = element.getNamespaceURI(prefix);
				String qName;
				if (StringUtils.hasLength(prefix)) {
					qName = "xmlns:" + prefix;
				}
				else {
					qName = "xmlns";
				}
				attributes.addAttribute("", "", qName, "CDATA", namespaceUri);
			}
		}
		return attributes;
	}

}
