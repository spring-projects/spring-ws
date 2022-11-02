/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.soap.saaj;

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapElement;

/**
 * SAAJ-specific implementation of the {@code SoapElement} interface. Wraps a {@link jakarta.xml.soap.SOAPElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoapElement<T extends SOAPElement> implements SoapElement {

	private final T element;

	SaajSoapElement(T element) {
		Assert.notNull(element, "element must not be null");
		this.element = element;
	}

	@Override
	public Source getSource() {
		return new DOMSource(element);
	}

	@Override
	public QName getName() {
		return element.getElementQName();
	}

	@Override
	public void addAttribute(QName name, String value) {
		try {
			element.addAttribute(name, value);
		} catch (SOAPException ex) {
			throw new SaajSoapElementException(ex);
		}
	}

	@Override
	public void removeAttribute(QName name) {
		element.removeAttribute(name);
	}

	@Override
	public String getAttributeValue(QName name) {
		return element.getAttributeValue(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<QName> getAllAttributes() {
		return element.getAllAttributesAsQNames();
	}

	@Override
	public void addNamespaceDeclaration(String prefix, String namespaceUri) {
		try {
			element.addNamespaceDeclaration(prefix, namespaceUri);
		} catch (SOAPException ex) {
			throw new SaajSoapElementException(ex);
		}
	}

	protected final T getSaajElement() {
		return element;
	}

}
