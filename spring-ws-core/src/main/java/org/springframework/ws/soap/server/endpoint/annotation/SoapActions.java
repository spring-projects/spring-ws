/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint method as containing multiple {@link SoapAction SoapActions}.
 *
 * @author Arjen Poutsma
 * @since 2.2.1
 * @see org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SoapActions {

	SoapAction[] value();

}
