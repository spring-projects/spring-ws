/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11;

import javax.wsdl.WSDLException;

import org.springframework.ws.wsdl.WsdlDefinitionException;

/**
 * Subclass of {@code WsdlDefinitionException} that wraps {@code WSDLException}s.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class Wsdl4jDefinitionException extends WsdlDefinitionException {

	public Wsdl4jDefinitionException(WSDLException ex) {
		super(ex.getMessage(), ex);
	}

}
