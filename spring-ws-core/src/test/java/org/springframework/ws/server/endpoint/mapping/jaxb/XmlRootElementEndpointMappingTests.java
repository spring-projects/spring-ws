/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.mapping.jaxb;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XmlRootElementEndpointMappingTests {

	private XmlRootElementEndpointMapping mapping;

	@BeforeEach
	void createMapping() {
		this.mapping = new XmlRootElementEndpointMapping();
	}

	@Test
	void rootElement() throws NoSuchMethodException {

		Method rootElement = getClass().getMethod("rootElement", MyRootElement.class);
		QName name = this.mapping.getLookupKeyForMethod(rootElement);

		assertThat(name).isEqualTo(new QName("myNamespace", "myRoot"));
	}

	public void rootElement(MyRootElement rootElement) {
	}

	@XmlRootElement(name = "myRoot", namespace = "myNamespace")
	public static class MyRootElement {

	}

}
