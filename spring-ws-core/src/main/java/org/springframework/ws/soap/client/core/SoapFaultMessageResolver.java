/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.client.core;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * Simple fault resolver that simply throws a {@link SoapFaultClientException} when a
 * fault occurs.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see SoapFaultClientException
 */
public class SoapFaultMessageResolver implements FaultMessageResolver {

	@Override
	public void resolveFault(WebServiceMessage message) throws IOException {
		SoapMessage soapMessage = (SoapMessage) message;
		throw new SoapFaultClientException(soapMessage);
	}

}
