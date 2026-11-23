/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;

/**
 * Represents the {@code detail} element in a SOAP fault. A detail contains
 * {@code SoapFaultDetailElement}s, which represent the individual details.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see SoapFaultDetailElement
 */
public interface SoapFaultDetail extends SoapElement {

	/**
	 * Adds a new {@code SoapFaultDetailElement} with the specified qualified name to this
	 * detail.
	 * @param name the qualified name of the new detail element
	 * @return the created {@code SoapFaultDetailElement}
	 */
	SoapFaultDetailElement addFaultDetailElement(QName name);

	/**
	 * Returns a {@code Result} that represents the concents of the detail.
	 * <p>
	 * The result can be used for marshalling.
	 * @return the {@code Result} of this element
	 */
	Result getResult();

	/**
	 * Gets an iterator over all of the {@code SoapFaultDetailElement}s in this detail.
	 * @return an iterator over all the {@code SoapFaultDetailElement}s
	 * @see SoapFaultDetailElement
	 */
	Iterator<SoapFaultDetailElement> getDetailEntries();

}
