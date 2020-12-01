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

package org.springframework.ws.soap.axiom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.soap.SoapElement;

/**
 * Axiom-specific version of {@link SoapElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoapElement implements SoapElement {

	private final OMElement axiomElement;

	private final SOAPFactory axiomFactory;

	protected AxiomSoapElement(OMElement axiomElement, SOAPFactory axiomFactory) {
		Assert.notNull(axiomElement, "axiomElement must not be null");
		Assert.notNull(axiomFactory, "axiomFactory must not be null");
		this.axiomElement = axiomElement;
		this.axiomFactory = axiomFactory;
	}

	@Override
	public final QName getName() {
		try {
			return axiomElement.getQName();
		} catch (OMException ex) {
			throw new AxiomSoapElementException(ex);
		}
	}

	@Override
	public final Source getSource() {
		try {
			return StaxUtils.createCustomStaxSource(axiomElement.getXMLStreamReader());
		} catch (OMException ex) {
			throw new AxiomSoapElementException(ex);
		}
	}

	@Override
	public final void addAttribute(QName name, String value) {
		try {
			String namespaceUri = name.getNamespaceURI();
			String prefix = name.getPrefix();
			if (StringUtils.hasLength(namespaceUri) && !StringUtils.hasLength(prefix)) {
				prefix = null;
			}
			OMNamespace namespace = getAxiomFactory().createOMNamespace(namespaceUri, prefix);
			OMAttribute attribute = getAxiomFactory().createOMAttribute(name.getLocalPart(), namespace, value);
			getAxiomElement().addAttribute(attribute);
		} catch (OMException ex) {
			throw new AxiomSoapElementException(ex);
		}
	}

	@Override
	public void removeAttribute(QName name) {
		try {
			OMAttribute attribute = getAxiomElement().getAttribute(name);
			if (attribute != null) {
				getAxiomElement().removeAttribute(attribute);
			}
		} catch (OMException ex) {
			throw new AxiomSoapElementException(ex);
		}
	}

	@Override
	public final String getAttributeValue(QName name) {
		try {
			return getAxiomElement().getAttributeValue(name);
		} catch (OMException ex) {
			throw new AxiomSoapElementException(ex);
		}
	}

	@Override
	public final Iterator<QName> getAllAttributes() {
		try {
			List<QName> results = new ArrayList<QName>();
			for (Iterator<?> iterator = getAxiomElement().getAllAttributes(); iterator.hasNext();) {
				OMAttribute attribute = (OMAttribute) iterator.next();
				results.add(attribute.getQName());
			}
			return results.iterator();

		} catch (OMException ex) {
			throw new AxiomSoapElementException(ex);
		}
	}

	@Override
	public void addNamespaceDeclaration(String prefix, String namespaceUri) {
		try {
			if (StringUtils.hasLength(prefix)) {
				getAxiomElement().declareNamespace(namespaceUri, prefix);
			} else {
				getAxiomElement().declareDefaultNamespace(namespaceUri);
			}
		} catch (OMException ex) {
			throw new AxiomSoapElementException(ex);
		}
	}

	protected final OMElement getAxiomElement() {
		return axiomElement;
	}

	protected final SOAPFactory getAxiomFactory() {
		return axiomFactory;
	}
}
