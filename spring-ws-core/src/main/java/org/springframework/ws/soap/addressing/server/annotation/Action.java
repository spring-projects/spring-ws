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
 * Marks an endpoint method as the handler for an incoming request. The annotation value
 * signifies the value for the request WS-Addressing {@code Action} header that is handled
 * by the method.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {

	/**
	 * Signifies the value for the request WS-Addressing {@code Action} header that is
	 * handled by the method.
	 */
	String value();

	/**
	 * Signifies the value for the response WS-Addressing {@code Action} header that is
	 * provided by the method.
	 */
	String output() default "";

	/**
	 * Signifies the value for the fault response WS-Addressing {@code Action} header that
	 * is provided by the method.
	 */
	String fault() default "";

}
