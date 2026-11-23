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
 * Marks an endpoint method parameter to be resolved as a single or a list of
 * {@link org.springframework.ws.soap.SoapHeaderElement}.
 *
 * @author Tareq Abedrabbo
 * @since 2.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SoapHeader {

	/**
	 * The qualified name of the soap header. The format used is that of
	 * {@link javax.xml.namespace.QName#toString()}, i.e. "{" + Namespace URI + "}" +
	 * local part, where the namespace is optional.
	 */
	String value();

}
