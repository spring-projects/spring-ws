/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security;

import javax.xml.namespace.QName;

/**
 * Exception indicating that a WS-Security executions should result in a SOAP Fault.
 *
 * @author Arjen Poutsma
 * @since 1.0.1
 */
@SuppressWarnings("serial")
public abstract class WsSecurityFaultException extends WsSecurityException {

	private final QName faultCode;

	private final String faultString;

	private final String faultActor;

	/**
	 * Construct a new {@code WsSecurityFaultException} with the given fault code, string,
	 * and actor.
	 */
	public WsSecurityFaultException(QName faultCode, String faultString, String faultActor) {
		super(faultString);
		this.faultCode = faultCode;
		this.faultString = faultString;
		this.faultActor = faultActor;
	}

	/** Returns the fault code for the exception. */
	public QName getFaultCode() {
		return this.faultCode;
	}

	/** Returns the fault string for the exception. */
	public String getFaultString() {
		return this.faultString;
	}

	/** Returns the fault actor for the exception. */
	public String getFaultActor() {
		return this.faultActor;
	}

}
