/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

/**
 * Represents an abstraction for WSDL definitions.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface WsdlDefinition {

	/**
	 * Return the name of the definition, if known.
	 * @since 5.1.0
	 */
	default @Nullable String getName() {
		return null;
	}

	/**
	 * Returns the {@code Source} of the definition.
	 * @return the {@code Source} of this WSDL definition
	 */
	Source getSource();

}
