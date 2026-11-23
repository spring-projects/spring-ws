/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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

	private NamespaceUtils() {
	}

	/**
	 * Creates a {@code NamespaceContext} for the specified method, based on
	 * {@link Namespaces @Namespaces} and {@link Namespace @Namespace} annotations.
	 * <p>
	 * This method will search for {@link Namespaces @Namespaces} and
	 * {@link Namespace @Namespace} annotation in the given method, its class, and its
	 * package, in reverse order. That is: package-level annotations are overridden by
	 * class-level annotations, which again are overridden by method-level annotations.
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
