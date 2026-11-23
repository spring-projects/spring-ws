/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.FaultAwareWebServiceMessage;

/**
 * Thrown by {@code SimpleFaultMessageResolver} when the response message has a fault.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class WebServiceFaultException extends WebServiceClientException {

	private final @Nullable FaultAwareWebServiceMessage faultMessage;

	/** Create a new instance of the {@code WebServiceFaultException} class. */
	public WebServiceFaultException(String msg) {
		super(msg);
		this.faultMessage = null;
	}

	/**
	 * Create a new instance of the {@code WebServiceFaultException} class.
	 * @param faultMessage the fault message
	 */
	public WebServiceFaultException(FaultAwareWebServiceMessage faultMessage) {
		super(faultMessage.getFaultReason());
		this.faultMessage = faultMessage;
	}

	/** Returns the fault message. */
	public @Nullable FaultAwareWebServiceMessage getWebServiceMessage() {
		return this.faultMessage;
	}

}
