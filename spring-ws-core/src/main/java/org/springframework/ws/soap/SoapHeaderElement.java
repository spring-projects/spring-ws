/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import javax.xml.transform.Result;

/**
 * Represents the contents of an individual SOAP header in the a SOAP message. All
 * {@code SoapHeaderElement}s are contained in a {@code SoapHeader}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see SoapHeader
 */
public interface SoapHeaderElement extends SoapElement {

	/**
	 * Returns the actor or role for this header element. In a SOAP 1.1 compliant message,
	 * this will read the {@code actor} attribute; in SOAP 1.2, the {@code role}
	 * attribute.
	 * @return the role of the header
	 */
	String getActorOrRole() throws SoapHeaderException;

	/**
	 * Sets the actor or role for this header element. In a SOAP 1.1 compliant message,
	 * this will result in an {@code actor} attribute being set; in SOAP 1.2, a
	 * {@code actorOrRole} attribute.
	 * @param actorOrRole the actorOrRole value
	 */
	void setActorOrRole(String actorOrRole) throws SoapHeaderException;

	/**
	 * Indicates whether the {@code mustUnderstand} attribute for this header element is
	 * set.
	 * @return {@code true} if the {@code mustUnderstand} attribute is set; {@code false}
	 * otherwise
	 */
	boolean getMustUnderstand() throws SoapHeaderException;

	/**
	 * Sets the {@code mustUnderstand} attribute for this header element. If the attribute
	 * is on, the role who receives the header must process it.
	 * @param mustUnderstand {@code true} to set the {@code mustUnderstand} attribute on;
	 * {@code false} to turn it off
	 */
	void setMustUnderstand(boolean mustUnderstand) throws SoapHeaderException;

	/**
	 * Returns a {@code Result} that allows for writing to the <strong>contents</strong>
	 * of the header element.
	 */
	Result getResult() throws SoapHeaderException;

	/**
	 * Returns the text content of this header element, if any.
	 * @return the text content of this header element
	 */
	String getText();

	/**
	 * Sets the text content of this header element.
	 * @param content the new text content of this header element
	 */
	void setText(String content);

}
