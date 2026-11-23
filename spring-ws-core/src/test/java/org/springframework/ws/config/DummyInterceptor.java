/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

public class DummyInterceptor implements EndpointInterceptor {

	@Autowired
	private DummyInterceptorDependency autowiredDependency;

	private DummyInterceptorDependency propertyDependency;

	public void setPropertyDependency(DummyInterceptorDependency propertyDependency) {
		this.propertyDependency = propertyDependency;
	}

	public DummyInterceptorDependency getPropertyDependency() {
		return this.propertyDependency;
	}

	public DummyInterceptorDependency getAutowiredDependency() {
		return this.autowiredDependency;
	}

	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
	}

}
