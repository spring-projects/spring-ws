/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping.jaxb;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XmlRootElementEndpointMappingTest {

	private XmlRootElementEndpointMapping mapping;

	@BeforeEach
	public void createMapping() throws NoSuchMethodException {
		mapping = new XmlRootElementEndpointMapping();
	}

	@Test
	public void rootElement() throws NoSuchMethodException {

		Method rootElement = getClass().getMethod("rootElement", MyRootElement.class);
		QName name = mapping.getLookupKeyForMethod(rootElement);

		assertThat(name).isEqualTo(new QName("myNamespace", "myRoot"));
	}

	public void rootElement(MyRootElement rootElement) {}

	@XmlRootElement(name = "myRoot", namespace = "myNamespace")
	public static class MyRootElement {

	}

}
