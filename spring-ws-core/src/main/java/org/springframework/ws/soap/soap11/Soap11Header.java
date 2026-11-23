/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.soap11;

import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;

/**
 * Subinterface of {@code SoapHeader} that exposes SOAP 1.1 functionality.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface Soap11Header extends SoapHeader {

	/**
	 * Returns an {@code Iterator} over all the {@link SoapHeaderElement header elements}
	 * that should be processed for the given actors. Headers target to the "next" actor
	 * or role will always be included.
	 * @param actors an array of actors to search for
	 * @return an iterator over all the header elements that contain the specified actors
	 * @throws SoapHeaderException if the headers cannot be returned
	 * @see SoapHeaderElement
	 */
	Iterator<SoapHeaderElement> examineHeaderElementsToProcess(String @Nullable [] actors) throws SoapHeaderException;

}
