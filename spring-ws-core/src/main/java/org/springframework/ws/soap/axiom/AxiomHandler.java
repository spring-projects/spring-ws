/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.axiom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import org.springframework.util.Assert;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Specific SAX {@link ContentHandler} and {@link LexicalHandler} that adds the resulting
 * AXIOM OMElement to a specified parent element when <code>endDocument</code> is called.
 * Used for returing <code>SAXResult</code>s from Axiom elements.
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomHandler implements ContentHandler, LexicalHandler {

	private final OMFactory factory;

	private final List<OMContainer> elements = new ArrayList<OMContainer>();

	private final OMContainer container;

	private int charactersType = XMLStreamConstants.CHARACTERS;

	private List<Map<String, String>> namespaceMappings =
			new ArrayList<Map<String, String>>();

	AxiomHandler(OMContainer container, OMFactory factory) {
		Assert.notNull(container, "'container' must not be null");
		Assert.notNull(factory, "'factory' must not be null");
		this.factory = factory;
		this.container = container;
	}

	private OMContainer getParent() {
		if (!elements.isEmpty()) {
			return elements.get(elements.size() - 1);
		}
		else {
			return container;
		}
	}

	public void startDocument() throws SAXException {
		removeAllNamespaceMappings();
		newNamespaceMapping();
	}

	public void endDocument() throws SAXException {
		removeAllNamespaceMappings();
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		currentNamespaceMapping().put(prefix, uri);
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		OMContainer parent = getParent();
		OMNamespace ns = factory.createOMNamespace(uri,
				QNameUtils.toQName(uri, qName).getPrefix());
		OMElement element = factory.createOMElement(localName, ns, parent);

		// declare namespaces
		Map<String, String> namespaceMappings = currentNamespaceMapping();
		for (Map.Entry<String, String> entry : namespaceMappings.entrySet()) {
			String prefix = entry.getKey();
			String namespaceUri = entry.getValue();

			if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
				element.declareDefaultNamespace(namespaceUri);
			} else {
				element.declareNamespace(namespaceUri, prefix);
			}
		}
		// declare attributes
		for (int i = 0; i < attributes.getLength(); i++) {
			QName attrName =
					QNameUtils.toQName(attributes.getURI(i), attributes.getQName(i));
			if (!isNamespaceDeclaration(attrName)) {
				OMNamespace namespace =
						factory.createOMNamespace(attrName.getNamespaceURI(),
								attrName.getPrefix());
				OMAttribute attribute =
						factory.createOMAttribute(attrName.getLocalPart(), namespace,
								attributes.getValue(i));
				element.addAttribute(attribute);
			}
		}
		elements.add(element);
		newNamespaceMapping();
	}

	private boolean isNamespaceDeclaration(QName qName) {
		String prefix = qName.getPrefix();
		String localPart = qName.getLocalPart();
		return (XMLConstants.XMLNS_ATTRIBUTE.equals(localPart) && prefix.length() == 0) ||
				(XMLConstants.XMLNS_ATTRIBUTE.equals(prefix) && localPart.length() != 0);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		elements.remove(elements.size() - 1);
		removeNamespaceMapping();
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		String data = new String(ch, start, length);
		OMContainer parent = getParent();
		factory.createOMText(parent, data, charactersType);
	}

	public void ignorableWhitespace(char ch[], int start, int length)
			throws SAXException {
		charactersType = XMLStreamConstants.SPACE;
		characters(ch, start, length);
		charactersType = XMLStreamConstants.CHARACTERS;
	}

	public void processingInstruction(String target, String data) throws SAXException {
		OMContainer parent = getParent();
		factory.createOMProcessingInstruction(parent, target, data);
	}

	public void comment(char ch[], int start, int length) throws SAXException {
		String content = new String(ch, start, length);
		OMContainer parent = getParent();
		factory.createOMComment(parent, content);
	}

	public void startCDATA() throws SAXException {
		charactersType = XMLStreamConstants.CDATA;
	}

	public void endCDATA() throws SAXException {
		charactersType = XMLStreamConstants.CHARACTERS;
	}

	public void startEntity(String name) throws SAXException {
		if (!isPredefinedEntityReference(name)) {
			charactersType = XMLStreamConstants.ENTITY_REFERENCE;
		}
	}

	public void endEntity(String name) throws SAXException {
		charactersType = XMLStreamConstants.CHARACTERS;
	}

	private boolean isPredefinedEntityReference(String name) {
		return "lt".equals(name) || "gt".equals(name) || "amp".equals(name) ||
				"quot".equals(name) ||
				"apos".equals(name);
	}

    /*
    * Unsupported
    */

	public void setDocumentLocator(Locator locator) {
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
	}

	public void endDTD() throws SAXException {
	}

	private Map<String, String> currentNamespaceMapping() {
		return namespaceMappings.get(namespaceMappings.size() - 1);
	}

	private void newNamespaceMapping() {
		namespaceMappings.add(new HashMap<String, String>());
	}

	private void removeNamespaceMapping() {
		namespaceMappings.remove(namespaceMappings.size() - 1);
	}

	private void removeAllNamespaceMappings() {
		namespaceMappings.clear();
	}

}
