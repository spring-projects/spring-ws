/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import javax.xml.transform.Result;

import org.jspecify.annotations.Nullable;

/**
 * Represents the content for an individual SOAP detail entry in a SOAP Message. All
 * {@code SoapFaultDetailElement}s are contained in a {@code SoapDetail}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see SoapFaultDetail
 */
public interface SoapFaultDetailElement extends SoapElement {

	/**
	 * Returns a {@code Result} that allows for writing to the <strong>contents</strong>
	 * of the detail element.
	 */
	Result getResult();

	/** Adds a new text node to this element. */
	void addText(@Nullable String text);

}
