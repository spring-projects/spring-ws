/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapFault;

/**
 * Sub-interface of {@link WebServiceConnection} that is aware of any Fault messages
 * received. Fault messages (such as {@link SoapFault} SOAP Faults) often require
 * different processing rules. Typically, fault detection is done by inspecting connection
 * error codes, etc.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface FaultAwareWebServiceConnection extends WebServiceConnection {

	/**
	 * Indicates whether this connection received a fault.
	 * <p>
	 * Typically implemented by looking at an HTTP status code.
	 * @return {@code true} if this connection received a fault; {@code false} otherwise.
	 * @throws IOException in case of I/O errors
	 */
	boolean hasFault() throws IOException;

	/**
	 * Sets a specific fault code.
	 * <p>
	 * Typically implemented by setting an HTTP status code.
	 * @param faultCode the fault code to be set on the connection, or {@code null} for no
	 * fault.
	 * @throws IOException in case of I/O errors
	 */
	void setFaultCode(@Nullable QName faultCode) throws IOException;

}
