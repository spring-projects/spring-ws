/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

/**
 * Defines the contract for payloads in Axiom.
 *
 * @author Arjen Poutsma
 * @since 1.5.2
 */
abstract class Payload {

	/**
	 * Returns the source of the payload.
	 * @return the source of the payload
	 */
	public abstract @Nullable Source getSource();

	/**
	 * Returns the result of the payload.
	 * @return the result of the payload
	 */
	public abstract Result getResult();

}
