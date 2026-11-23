/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

/**
 * Strategy for adding {@link javax.wsdl.Binding}s to a {@link javax.wsdl.Definition}.
 * <p>
 * Used by {@link org.springframework.ws.wsdl.wsdl11.ProviderBasedWsdl4jDefinition}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface BindingsProvider {

	void addBindings(Definition definition) throws WSDLException;

}
