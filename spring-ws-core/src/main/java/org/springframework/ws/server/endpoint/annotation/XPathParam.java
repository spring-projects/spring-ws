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

/**
 * Indicates that a method parameter should be bound to an XPath expression. The
 * annotation value signifies the XPath expression to use. The parameter can be of the
 * following types:
 * <ul>
 * <li>{@code boolean}, or {@link Boolean}</li>
 * <li>{@code double}, or {@link Double}</li>
 * <li>{@link String}</li>
 * <li>{@link org.w3c.dom.Node}</li>
 * <li>{@link org.w3c.dom.NodeList}</li>
 * </ul>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.server.endpoint.adapter.method.XPathParamMethodArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XPathParam {

	String value();

}
