/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;

import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapElement;

/**
 * SAAJ-specific implementation of the {@code SoapElement} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPElement}.
 *
 * @param <T> the type of the {@link SOAPElement}
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
		return new DOMSource(this.element);
	}

	@Override
	public QName getName() {
		return this.element.getElementQName();
	}

	@Override
	public void addAttribute(QName name, String value) {
		try {
			this.element.addAttribute(name, value);
		}
		catch (SOAPException ex) {
			throw new SaajSoapElementException(ex);
		}
	}

	@Override
	public void removeAttribute(QName name) {
		this.element.removeAttribute(name);
	}

	@Override
	public String getAttributeValue(QName name) {
		return this.element.getAttributeValue(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<QName> getAllAttributes() {
		return this.element.getAllAttributesAsQNames();
	}

	@Override
	public void addNamespaceDeclaration(String prefix, String namespaceUri) {
		try {
			this.element.addNamespaceDeclaration(prefix, namespaceUri);
		}
		catch (SOAPException ex) {
			throw new SaajSoapElementException(ex);
		}
	}

	protected final T getSaajElement() {
		return this.element;
	}

}
