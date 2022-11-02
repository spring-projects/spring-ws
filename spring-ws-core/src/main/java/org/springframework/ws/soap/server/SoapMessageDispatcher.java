/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.Soap11Header;
import org.springframework.ws.soap.soap12.Soap12Header;

/**
 * SOAP-specific subclass of the {@link MessageDispatcher}. Adds functionality for adding actor roles to a endpoint
 * invocation chain, and endpoint interception using {@link SoapEndpointInterceptor} objects.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.SoapMessage
 * @see SoapEndpointInterceptor
 * @since 1.0.0
 */
public class SoapMessageDispatcher extends MessageDispatcher {

	/** Default message used when creating a SOAP MustUnderstand fault. */
	public static final String DEFAULT_MUST_UNDERSTAND_FAULT_STRING = "One or more mandatory SOAP header blocks not understood";

	private String mustUnderstandFaultString = DEFAULT_MUST_UNDERSTAND_FAULT_STRING;

	private Locale mustUnderstandFaultStringLocale = Locale.ENGLISH;

	/**
	 * Sets the message used for {@code MustUnderstand} fault. Default to {@link #DEFAULT_MUST_UNDERSTAND_FAULT_STRING}.
	 */
	public void setMustUnderstandFaultString(String mustUnderstandFaultString) {
		this.mustUnderstandFaultString = mustUnderstandFaultString;
	}

	/** Sets the locale of the message used for {@code MustUnderstand} fault. Default to {@link Locale#ENGLISH}. */
	public void setMustUnderstandFaultStringLocale(Locale mustUnderstandFaultStringLocale) {
		this.mustUnderstandFaultStringLocale = mustUnderstandFaultStringLocale;
	}

	/**
	 * Process the headers targeted at the actor or role fullfilled by the endpoint. Also processed the
	 * {@code MustUnderstand} headers in the incoming SOAP request message. Iterates over all SOAP headers which should be
	 * understood for this role, and determines whether these are supported. Generates a SOAP MustUnderstand fault if a
	 * header is not understood.
	 *
	 * @param mappedEndpoint the mapped EndpointInvocationChain
	 * @param messageContext the message context
	 * @return {@code true} if all necessary headers are understood; {@code false} otherwise
	 * @see SoapEndpointInvocationChain#getActorsOrRoles()
	 * @see org.springframework.ws.soap.SoapHeader#examineMustUnderstandHeaderElements(String)
	 */
	@Override
	protected boolean handleRequest(EndpointInvocationChain mappedEndpoint, MessageContext messageContext) {
		if (messageContext.getRequest() instanceof SoapMessage) {
			String[] actorsOrRoles = null;
			boolean isUltimateReceiver = true;
			if (mappedEndpoint instanceof SoapEndpointInvocationChain) {
				SoapEndpointInvocationChain soapChain = (SoapEndpointInvocationChain) mappedEndpoint;
				actorsOrRoles = soapChain.getActorsOrRoles();
				isUltimateReceiver = soapChain.isUltimateReceiver();
			}
			return handleHeaders(mappedEndpoint, messageContext, actorsOrRoles, isUltimateReceiver);
		}
		return true;
	}

	private boolean handleHeaders(EndpointInvocationChain mappedEndpoint, MessageContext messageContext,
			String[] actorsOrRoles, boolean isUltimateReceiver) {
		SoapMessage soapRequest = (SoapMessage) messageContext.getRequest();
		SoapHeader soapHeader = soapRequest.getSoapHeader();
		if (soapHeader == null) {
			return true;
		}
		Iterator<SoapHeaderElement> headerIterator;
		if (soapHeader instanceof Soap11Header) {
			headerIterator = ((Soap11Header) soapHeader).examineHeaderElementsToProcess(actorsOrRoles);
		} else {
			headerIterator = ((Soap12Header) soapHeader).examineHeaderElementsToProcess(actorsOrRoles, isUltimateReceiver);
		}
		List<QName> notUnderstoodHeaderNames = new ArrayList<QName>();
		while (headerIterator.hasNext()) {
			SoapHeaderElement headerElement = headerIterator.next();
			QName headerName = headerElement.getName();
			if (headerElement.getMustUnderstand() && logger.isDebugEnabled()) {
				logger.debug("Handling MustUnderstand header " + headerName);
			}
			if (headerElement.getMustUnderstand() && !headerUnderstood(mappedEndpoint, headerElement)) {
				notUnderstoodHeaderNames.add(headerName);
			}
		}
		if (notUnderstoodHeaderNames.isEmpty()) {
			return true;
		} else {
			SoapMessage response = (SoapMessage) messageContext.getResponse();
			createMustUnderstandFault(response, notUnderstoodHeaderNames, actorsOrRoles);
			return false;
		}
	}

	/**
	 * Handles the request for a single SOAP actor/role. Iterates over all {@code MustUnderstand} headers for a specific
	 * SOAP 1.1 actor or SOAP 1.2 role, and determines whether these are understood by any of the registered
	 * {@code SoapEndpointInterceptor}. If they are, returns {@code true}. If they are not, a SOAP fault is created, and
	 * false is returned.
	 *
	 * @see SoapEndpointInterceptor#understands(org.springframework.ws.soap.SoapHeaderElement)
	 */
	private boolean headerUnderstood(EndpointInvocationChain mappedEndpoint, SoapHeaderElement headerElement) {
		EndpointInterceptor[] interceptors = mappedEndpoint.getInterceptors();
		if (ObjectUtils.isEmpty(interceptors)) {
			return false;
		}
		for (EndpointInterceptor interceptor : interceptors) {
			if (interceptor instanceof SoapEndpointInterceptor
					&& ((SoapEndpointInterceptor) interceptor).understands(headerElement)) {
				return true;
			}
		}
		return false;
	}

	private void createMustUnderstandFault(SoapMessage soapResponse, List<QName> notUnderstoodHeaderNames,
			String[] actorsOrRoles) {
		if (logger.isWarnEnabled()) {
			logger.warn("Could not handle mustUnderstand headers: "
					+ StringUtils.collectionToCommaDelimitedString(notUnderstoodHeaderNames) + ". Returning fault");
		}
		SoapBody responseBody = soapResponse.getSoapBody();
		SoapFault fault = responseBody.addMustUnderstandFault(mustUnderstandFaultString, mustUnderstandFaultStringLocale);
		if (!ObjectUtils.isEmpty(actorsOrRoles)) {
			fault.setFaultActorOrRole(actorsOrRoles[0]);
		}
		SoapHeader header = soapResponse.getSoapHeader();
		if (header instanceof Soap12Header) {
			Soap12Header soap12Header = (Soap12Header) header;
			for (QName headerName : notUnderstoodHeaderNames) {
				soap12Header.addNotUnderstoodHeaderElement(headerName);
			}
		}
	}

}
