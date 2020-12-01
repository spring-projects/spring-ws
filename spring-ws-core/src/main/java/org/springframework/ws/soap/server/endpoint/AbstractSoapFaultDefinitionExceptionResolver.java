/*
 * Copyright 2005-2014 the original author or authors.
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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;

/**
 * Abstract base class for SOAP-based {@link EndpointExceptionResolver} implementations that depend on
 * {@link SoapFaultDefinition}. Provides a default endpoint property, and a template method that provides the definition
 * for a given exception.
 *
 * @author Arjen Poutsma
 * @see #setDefaultFault(SoapFaultDefinition)
 * @see #getFaultDefinition(Object,Exception)
 * @since 1.0.0
 */
public abstract class AbstractSoapFaultDefinitionExceptionResolver extends AbstractEndpointExceptionResolver {

	private SoapFaultDefinition defaultFault;

	/** Set the default fault. This fault will be returned if no specific mapping was found. */
	public void setDefaultFault(SoapFaultDefinition defaultFault) {
		this.defaultFault = defaultFault;
	}

	/**
	 * Template method that returns the {@link SoapFaultDefinition} for the given exception.
	 *
	 * @param endpoint the executed endpoint, or {@code null} if none chosen at the time of the exception
	 * @param ex the exception to be handled
	 * @return the definition mapped to the exception, or {@code null} if none is found.
	 */
	protected abstract SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex);

	@Override
	protected final boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse(),
				"AbstractSoapFaultDefinitionExceptionResolver requires a SoapMessage");

		SoapFaultDefinition definition = getFaultDefinition(endpoint, ex);
		if (definition == null) {
			definition = defaultFault;
		}
		if (definition == null) {
			return false;
		}

		String faultStringOrReason = definition.getFaultStringOrReason();
		if (!StringUtils.hasLength(faultStringOrReason)) {
			faultStringOrReason = StringUtils.hasLength(ex.getMessage()) ? ex.getMessage() : ex.toString();
		}
		SoapBody soapBody = ((SoapMessage) messageContext.getResponse()).getSoapBody();
		SoapFault fault;

		if (SoapFaultDefinition.SERVER.equals(definition.getFaultCode())
				|| SoapFaultDefinition.RECEIVER.equals(definition.getFaultCode())) {
			fault = soapBody.addServerOrReceiverFault(faultStringOrReason, definition.getLocale());
		} else if (SoapFaultDefinition.CLIENT.equals(definition.getFaultCode())
				|| SoapFaultDefinition.SENDER.equals(definition.getFaultCode())) {
			fault = soapBody.addClientOrSenderFault(faultStringOrReason, definition.getLocale());
		} else {
			if (soapBody instanceof Soap11Body) {
				Soap11Body soap11Body = (Soap11Body) soapBody;
				fault = soap11Body.addFault(definition.getFaultCode(), faultStringOrReason, definition.getLocale());
			} else if (soapBody instanceof Soap12Body) {
				Soap12Body soap12Body = (Soap12Body) soapBody;
				Soap12Fault soap12Fault = soap12Body.addServerOrReceiverFault(faultStringOrReason, definition.getLocale());
				soap12Fault.addFaultSubcode(definition.getFaultCode());
				fault = soap12Fault;
			} else {
				throw new IllegalStateException("This class only supports SOAP 1.1 and SOAP 1.2.");
			}
		}
		if (fault != null) {
			customizeFault(endpoint, ex, fault);
		}
		return true;
	}

	/**
	 * Customize the {@link SoapFault} created by this resolver. Called for each created fault
	 * <p>
	 * The default implementation is empty. Can be overridden in subclasses to customize the properties of the fault, such
	 * as adding details, etc.
	 *
	 * @param endpoint the executed endpoint, or {@code null} if none chosen at the time of the exception
	 * @param ex the exception to be handled
	 * @param fault the created fault
	 */
	protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {}

}
