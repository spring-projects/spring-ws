/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.support.SoapUtils;

/**
 * Implementation of the {@link org.springframework.ws.server.SmartEndpointInterceptor}
 * interface that only intercepts requests that have a specified soap action.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class SoapActionSmartEndpointInterceptor extends DelegatingSmartSoapEndpointInterceptor {

	private final String soapAction;

	public SoapActionSmartEndpointInterceptor(EndpointInterceptor delegate, String soapAction) {
		super(delegate);
		Assert.hasLength(soapAction, "soapAction can not be empty");
		this.soapAction = soapAction;
	}

	@Override
	protected boolean shouldIntercept(WebServiceMessage request, Object endpoint) {
		if (request instanceof SoapMessage soapMessage) {
			return this.soapAction.equals(SoapUtils.unescapeAction(soapMessage.getSoapAction()));
		}
		else {
			return false;
		}
	}

}
