/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.support;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import javax.xml.namespace.NamespaceContext;

import org.springframework.util.Assert;
import org.springframework.ws.server.endpoint.annotation.Namespace;
import org.springframework.ws.server.endpoint.annotation.Namespaces;
import org.springframework.xml.namespace.SimpleNamespaceContext;

/**
 * Helper class for handling {@link Namespace @Namespace} annotations.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class NamespaceUtils {

	private NamespaceUtils() {}

	/**
	 * Creates a {@code NamespaceContext} for the specified method, based on {@link Namespaces @Namespaces} and
	 * {@link Namespace @Namespace} annotations.
	 * <p>
	 * This method will search for {@link Namespaces @Namespaces} and {@link Namespace @Namespace} annotation in the given
	 * method, its class, and its package, in reverse order. That is: package-level annotations are overridden by
	 * class-level annotations, which again are overridden by method-level annotations.
	 *
	 * @param method the method to create the namespace context for
	 * @return the namespace context
	 */
	public static NamespaceContext getNamespaceContext(Method method) {
		Assert.notNull(method, "'method' must not be null");
		SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
		Class<?> endpointClass = method.getDeclaringClass();
		Package endpointPackage = endpointClass.getPackage();
		if (endpointPackage != null) {
			addNamespaceAnnotations(endpointPackage, namespaceContext);
		}
		addNamespaceAnnotations(endpointClass, namespaceContext);
		addNamespaceAnnotations(method, namespaceContext);
		return namespaceContext;
	}

	private static void addNamespaceAnnotations(AnnotatedElement annotatedElement,
			SimpleNamespaceContext namespaceContext) {
		if (annotatedElement.isAnnotationPresent(Namespaces.class)) {
			Namespaces namespacesAnn = annotatedElement.getAnnotation(Namespaces.class);
			for (Namespace namespaceAnn : namespacesAnn.value()) {
				namespaceContext.bindNamespaceUri(namespaceAnn.prefix(), namespaceAnn.uri());
			}
		}
		if (annotatedElement.isAnnotationPresent(Namespace.class)) {
			Namespace namespaceAnn = annotatedElement.getAnnotation(Namespace.class);
			namespaceContext.bindNamespaceUri(namespaceAnn.prefix(), namespaceAnn.uri());
		}
	}

}
