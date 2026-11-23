/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.soap12;

import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapFault;

/**
 * Subinterface of {@code SoapFault} that exposes SOAP 1.2 functionality. Necessary
 * because SOAP 1.1 differs from SOAP 1.2 with respect to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface Soap12Fault extends SoapFault {

	/**
	 * Returns an iteration over the fault subcodes. The subcodes are returned in order:
	 * from top to bottom.
	 * @return an Iterator that contains {@code QNames} representing the fault subcodes
	 */
	Iterator<QName> getFaultSubcodes();

	/**
	 * Adds a fault subcode this fault.
	 * @param subcode the qualified name of the subcode
	 */
	void addFaultSubcode(QName subcode);

	/** Returns the fault node. Optional. */
	@Nullable String getFaultNode();

	/** Sets the fault node. */
	void setFaultNode(String uri);

	/** Sets the specified fault reason text. */
	void setFaultReasonText(Locale locale, String text);

	/** Returns the reason associated with the given language. */
	@Nullable String getFaultReasonText(Locale locale);

}
