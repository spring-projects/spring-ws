/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint method as the handler for an incoming request. The annotation values
 * signify the request payload root element that is handled by the method.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PayloadRoots.class)
public @interface PayloadRoot {

	/**
	 * Signifies the local part of the payload root element handled by the annotated
	 * method.
	 * @see #namespace()
	 */
	String localPart();

	/**
	 * Signifies the namespace of the payload root element handled by the annotated
	 * method.
	 * @see #localPart()
	 */
	String namespace() default "";

}
