/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.xml.XMLConstants;

/**
 * Sets up a namespace to be used in an {@link Endpoint @Endpoint} method, class, or
 * package.
 * <p>
 * Typically used in combination with {@link XPathParam @XPathParam}, or
 * {@link PayloadRoot @PayloadRoot}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 * @see XPathParam
 * @see PayloadRoot
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD })
public @interface Namespace {

	/**
	 * Signifies the prefix of the namespace.
	 * @see #uri()
	 */
	String prefix() default XMLConstants.DEFAULT_NS_PREFIX;

	/**
	 * Signifies the URI of the namespace.
	 * @see #prefix()
	 */
	String uri();

}
