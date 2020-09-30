/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * This annotation serves as a specialization of {@link Component @Component}, allowing for implementation classes to be
 * autodetected through classpath scanning. Instances of this class are typically picked up by an
 * {@link AbstractAnnotationMethodEndpointMapping} implementation, such as
 * {@link SoapActionAnnotationMethodEndpointMapping}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Endpoint {

	/**
	 * The value may indicate a suggestion for a logical component name, to be turned into a Spring bean in case of an
	 * autodetected component.
	 *
	 * @return the suggested component name, if any
	 */
	String value() default "";

}
