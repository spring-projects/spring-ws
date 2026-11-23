/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.soap11;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapFault;

/**
 * Subinterface of {@code SoapFault} that exposes SOAP 1.1 functionality. Necessary
 * because SOAP 1.1 differs from SOAP 1.2 with respect to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface Soap11Fault extends SoapFault {

	/** Returns the locale of the fault string. */
	@Nullable Locale getFaultStringLocale();

}
