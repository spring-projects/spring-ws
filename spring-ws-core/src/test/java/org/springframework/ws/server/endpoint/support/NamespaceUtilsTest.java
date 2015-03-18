/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.support;

import java.lang.reflect.Method;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.springframework.ws.server.endpoint.annotation.Namespace;
import org.springframework.ws.server.endpoint.annotation.Namespaces;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Namespaces({@Namespace(prefix = "prefix1", uri = "class1"), @Namespace(uri = "class2")})
public class NamespaceUtilsTest {

	@Test
	public void getNamespaceContextMethod() throws NoSuchMethodException {
		Method method = getClass().getMethod("method");
		NamespaceContext namespaceContext = NamespaceUtils.getNamespaceContext(method);
		assertEquals("method1", namespaceContext.getNamespaceURI("prefix1"));
		assertEquals("method2", namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));

	}
	
	@Test
	public void getNamespaceContextClass() throws NoSuchMethodException {
		Method method = getClass().getMethod("getNamespaceContextClass");
		NamespaceContext namespaceContext = NamespaceUtils.getNamespaceContext(method);
		assertEquals("class1", namespaceContext.getNamespaceURI("prefix1"));
		assertEquals("class2", namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));

	}

	@Namespaces({@Namespace(prefix = "prefix1", uri = "method1"), @Namespace(uri = "method2")})
	public void method() {

	}

}
