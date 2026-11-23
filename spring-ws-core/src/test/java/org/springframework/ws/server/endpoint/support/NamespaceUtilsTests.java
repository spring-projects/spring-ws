/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.support;

import java.lang.reflect.Method;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.junit.jupiter.api.Test;

import org.springframework.ws.server.endpoint.annotation.Namespace;
import org.springframework.ws.server.endpoint.annotation.Namespaces;

import static org.assertj.core.api.Assertions.assertThat;

@Namespaces({ @Namespace(prefix = "prefix1", uri = "class1"), @Namespace(uri = "class2") })
class NamespaceUtilsTests {

	@Test
	void getNamespaceContextMethod() throws NoSuchMethodException {

		Method method = getClass().getMethod("method");
		NamespaceContext namespaceContext = NamespaceUtils.getNamespaceContext(method);

		assertThat(namespaceContext.getNamespaceURI("prefix1")).isEqualTo("method1");
		assertThat(namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)).isEqualTo("method2");

	}

	@Test
	void getNamespaceContextClass() throws NoSuchMethodException {

		Method method = getClass().getDeclaredMethod("getNamespaceContextClass");
		NamespaceContext namespaceContext = NamespaceUtils.getNamespaceContext(method);

		assertThat(namespaceContext.getNamespaceURI("prefix1")).isEqualTo("class1");
		assertThat(namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)).isEqualTo("class2");

	}

	@Namespaces({ @Namespace(prefix = "prefix1", uri = "method1"), @Namespace(uri = "method2") })
	public void method() {

	}

}
