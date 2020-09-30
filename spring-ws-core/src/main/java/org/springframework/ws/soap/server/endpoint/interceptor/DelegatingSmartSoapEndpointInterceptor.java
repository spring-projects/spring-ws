/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.SmartSoapEndpointInterceptor;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

/**
 * Implementation of the {@link SmartSoapEndpointInterceptor} interface that delegates to a delegate
 * {@link SoapEndpointInterceptor}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class DelegatingSmartSoapEndpointInterceptor extends DelegatingSmartEndpointInterceptor
		implements SmartSoapEndpointInterceptor {

	/**
	 * Creates a new instance of the {@code DelegatingSmartSoapEndpointInterceptor} with the given delegate.
	 *
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
		} else {
			return false;
		}
	}
}
