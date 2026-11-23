/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractLoggingInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

/**
 * SOAP-specific {@code EndpointInterceptor} that logs the complete request and response
 * envelope of {@code SoapMessage} messages. By default, request, response and fault
 * messages are logged, but this behaviour can be changed using the {@code logRequest},
 * {@code logResponse}, {@code logFault} properties.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setLogRequest(boolean)
 * @see #setLogResponse(boolean)
 * @see #setLogFault(boolean)
 */
public class SoapEnvelopeLoggingInterceptor extends AbstractLoggingInterceptor implements SoapEndpointInterceptor {

	private boolean logFault = true;

	/** Indicates whether a SOAP Fault should be logged. Default is {@code true}. */
	public void setLogFault(boolean logFault) {
		this.logFault = logFault;
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		if (this.logFault && isLogEnabled()) {
			logMessageSource("Fault: ", getSource(messageContext.getResponse()));
		}
		return true;
	}

	@Override
	public boolean understands(SoapHeaderElement header) {
		return false;
	}

	@Override
	protected @Nullable Source getSource(WebServiceMessage message) {
		if (message instanceof SoapMessage soapMessage) {
			return soapMessage.getEnvelope().getSource();
		}
		else {
			return null;
		}
	}

}
