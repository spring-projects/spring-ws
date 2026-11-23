/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

/**
 * The base interface for all elements that are contained in a SOAP message.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see SoapMessage
 */
public interface SoapElement {

	/**
	 * Returns the qualified name of this element.
	 * @return the qualified name of this element
	 */
	QName getName();

	/**
	 * Returns the {@code Source} of this element. This includes the element itself, i.e.
	 * {@code SoapEnvelope.getSource()} will include the {@code Envelope} tag.
	 * @return the {@code Source} of this element
	 */
	Source getSource();

	/**
	 * Adds an attribute with the specified qualified name and value to this element.
	 * @param name the qualified name of the attribute
	 * @param value the value of the attribute
	 */
	void addAttribute(QName name, String value);

	/**
	 * Removes the attribute with the specified name.
	 * @param name the qualified name of the attribute to remove
	 */
	void removeAttribute(QName name);

	/**
	 * Returns the value of the attribute with the specified qualified name.
	 * @param name the qualified name
	 * @return the value, or {@code null} if there is no such attribute
	 */
	@Nullable String getAttributeValue(QName name);

	/**
	 * Returns an {@code Iterator} over all of the attributes in element as {@link QName
	 * qualified names}.
	 * @return an iterator over all the attribute names
	 */
	Iterator<QName> getAllAttributes();

	/**
	 * Adds a namespace declaration with the specified prefix and URI to this element.
	 * @param prefix the namespace prefix. Can be empty or null to declare the default
	 * namespace
	 * @param namespaceUri the namespace uri
	 * @throws SoapElementException in case of errors
	 */
	void addNamespaceDeclaration(String prefix, String namespaceUri);

}
