/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint with a WS-Addressing {@code Address}. If this annotation is applied,
 * the {@linkplain #value() value} is compared to the
 * {@link org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getTo()
 * destination} property of the incoming message.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Address {

	/**
	 * Signifies the value for the request WS-Addressing destination that is handled by
	 * the class.
	 */
	String value();

}
