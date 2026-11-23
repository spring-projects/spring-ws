/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceException;

/**
 * Base class for all WSDL definition exceptions.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class WsdlDefinitionException extends WebServiceException {

	public WsdlDefinitionException(String reason) {
		super(reason);
	}

	public WsdlDefinitionException(@Nullable String reason, Throwable throwable) {
		super(reason, throwable);
	}

}
