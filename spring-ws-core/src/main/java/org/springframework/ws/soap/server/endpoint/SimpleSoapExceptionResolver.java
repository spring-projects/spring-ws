/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;

/**
 * Simple, SOAP-specific {@link org.springframework.ws.server.EndpointExceptionResolver EndpointExceptionResolver}
 * implementation that stores the exception's message as the fault string.
 * <p>
 * The fault code is always set to a Server (in SOAP 1.1) or Receiver (SOAP 1.2).
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SimpleSoapExceptionResolver extends AbstractEndpointExceptionResolver {

	private Locale locale = Locale.ENGLISH;

	/**
	 * Returns the locale for the faultstring or reason of the SOAP Fault.
	 * <p>
	 * Defaults to {@link Locale#ENGLISH}.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the locale for the faultstring or reason of the SOAP Fault.
	 * <p>
	 * Defaults to {@link Locale#ENGLISH}.
	 */
	public void setLocale(Locale locale) {
		Assert.notNull(locale, "locale must not be null");
		this.locale = locale;
	}

	@Override
	protected final boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse(),
				"SimpleSoapExceptionResolver requires a SoapMessage");
		SoapMessage response = (SoapMessage) messageContext.getResponse();
		String faultString = StringUtils.hasLength(ex.getMessage()) ? ex.getMessage() : ex.toString();
		SoapBody body = response.getSoapBody();
		SoapFault fault = body.addServerOrReceiverFault(faultString, getLocale());
		customizeFault(messageContext, endpoint, ex, fault);
		return true;
	}

	/**
	 * Empty template method to allow subclasses an opportunity to customize the given {@link SoapFault}. Called from
	 * {@link #resolveExceptionInternal(MessageContext,Object,Exception)}.
	 *
	 * @param messageContext current message context
	 * @param endpoint the executed endpoint, or {@code null} if none chosen at the time of the exception
	 * @param ex the exception that got thrown during endpoint execution
	 * @param fault the SOAP fault to be customized.
	 */
	protected void customizeFault(MessageContext messageContext, Object endpoint, Exception ex, SoapFault fault) {}
}
