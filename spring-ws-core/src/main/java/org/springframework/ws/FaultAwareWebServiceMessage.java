/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

/**
 * Sub-interface of {@link WebServiceMessage} that can contain special Fault messages.
 * Fault messages (such as {@link org.springframework.ws.soap.SoapFault} SOAP Faults)
 * often require different processing rules.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.soap.SoapMessage
 */
public interface FaultAwareWebServiceMessage extends WebServiceMessage {

	/**
	 * Whether this message has a fault.
	 * @return {@code true} if the message has a fault.
	 * @see #getFaultReason()
	 */
	boolean hasFault();

	/**
	 * Returns the fault code, if any.
	 */
	@Nullable QName getFaultCode();

	/**
	 * Returns the fault reason message.
	 * @return the fault reason message, if any; returns {@code null} when no fault is
	 * present.
	 * @see #hasFault()
	 */
	@Nullable String getFaultReason();

}
