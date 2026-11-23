/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.SmartSoapEndpointInterceptor;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

/**
 * Implementation of the {@link SmartSoapEndpointInterceptor} interface that delegates to
 * a delegate {@link SoapEndpointInterceptor}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class DelegatingSmartSoapEndpointInterceptor extends DelegatingSmartEndpointInterceptor
		implements SmartSoapEndpointInterceptor {

	/**
	 * Creates a new instance of the {@code DelegatingSmartSoapEndpointInterceptor} with
	 * the given delegate.
	 * @param delegate the endpoint interceptor to delegate to.
	 */
	public DelegatingSmartSoapEndpointInterceptor(EndpointInterceptor delegate) {
		super(delegate);
	}

	@Override
	public boolean understands(SoapHeaderElement header) {
		EndpointInterceptor delegate = getDelegate();
		if (delegate instanceof SoapEndpointInterceptor) {
			return ((SoapEndpointInterceptor) delegate).understands(header);
		}
		else {
			return false;
		}
	}

}
