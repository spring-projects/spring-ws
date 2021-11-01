/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.saaj.support;

import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX {@code ContentHandler} that transforms callback calls to the creation of SAAJ {@code Node}s and
 * {@code SOAPElement}s.
 *
 * @author Arjen Poutsma
 * @see jakarta.xml.soap.Node
 * @see jakarta.xml.soap.SOAPElement
 * @since 1.0.0
 */
public class SaajContentHandler implements ContentHandler {

	private SOAPElement element;

	private final SOAPEnvelope envelope;

	private Map<String, String> namespaces = new LinkedHashMap<String, String>();

	/**
	 * Constructs a new instance of the {@code SaajContentHandler} that creates children of the given {@code SOAPElement}.
	 *
	 * @param element the element to write to
	 */
	public SaajContentHandler(SOAPElement element) {
		Assert.notNull(element, "element must not be null");
		if (element instanceof SOAPEnvelope) {
			envelope = (SOAPEnvelope) element;
		} else {
			envelope = SaajUtils.getEnvelope(element);
		}
		this.element = element;
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		try {
			String text = new String(ch, start, length);
			element.addTextNode(text);
		} catch (SOAPException ex) {
			throw new SAXException(ex);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		try {
			String childPrefix = getPrefix(qName);
			SOAPElement child = element.addChildElement(localName, childPrefix, uri);
			for (int i = 0; i < atts.getLength(); i++) {
				if (StringUtils.hasLength(atts.getLocalName(i))) {
					String attributePrefix = getPrefix(atts.getQName(i));
					if (!"xmlns".equals(atts.getLocalName(i)) && !"xmlns".equals(attributePrefix)) {
						Name attributeName = envelope.createName(atts.getLocalName(i), attributePrefix, atts.getURI(i));
						child.addAttribute(attributeName, atts.getValue(i));
					}
				}
			}
			for (String namespacePrefix : namespaces.keySet()) {
				String namespaceUri = namespaces.get(namespacePrefix);
				if (!findParentNamespaceDeclaration(child, namespacePrefix, namespaceUri)) {
					child.addNamespaceDeclaration(namespacePrefix, namespaceUri);
				}
			}
			element = child;
		} catch (SOAPException ex) {
			throw new SAXException(ex);
		}
	}

	private boolean findParentNamespaceDeclaration(SOAPElement element, String prefix, String namespaceUri) {
		String result = element.getNamespaceURI(prefix);
		if (namespaceUri.equals(result)) {
			return true;
		} else {
			try {
				SOAPElement parent = element.getParentElement();
				if (parent != null) {
					return findParentNamespaceDeclaration(parent, prefix, namespaceUri);
				}
			} catch (UnsupportedOperationException ex) {
				// ignore
			}
			return false;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Assert.isTrue(localName.equals(element.getElementName().getLocalName()), "Invalid element on stack");
		Assert.isTrue(uri.equals(element.getElementName().getURI()), "Invalid element on stack");
		element = element.getParentElement();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		namespaces.put(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		namespaces.remove(prefix);
	}

	@Override
	public void setDocumentLocator(Locator locator) {}

	@Override
	public void startDocument() throws SAXException {}

	@Override
	public void endDocument() throws SAXException {}

	@Override
	public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {}

	@Override
	public void skippedEntity(String name) throws SAXException {}

	private String getPrefix(String qName) {
		int idx = qName.indexOf(':');
		if (idx != -1) {
			return qName.substring(0, idx);
		} else {
			return null;
		}
	}
}
