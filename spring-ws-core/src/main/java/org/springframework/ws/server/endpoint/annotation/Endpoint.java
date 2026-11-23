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

import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.mapping.AbstractAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping;

/**
 * Indicates that an annotated class is an "Endpoint" (e.g. a web service endpoint).
 * <p>
 * This annotation serves as a specialization of {@link Component @Component}, allowing
 * for implementation classes to be autodetected through classpath scanning. Instances of
 * this class are typically picked up by an
 * {@link AbstractAnnotationMethodEndpointMapping} implementation, such as
 * {@link SoapActionAnnotationMethodEndpointMapping}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Endpoint {

	/**
	 * The value may indicate a suggestion for a logical component name, to be turned into
	 * a Spring bean in case of an autodetected component.
	 * @return the suggested component name, if any
	 */
	String value() default "";

}
