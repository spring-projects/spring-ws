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

package org.springframework.xml.namespace;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;

import org.apache.commons.collections4.IteratorUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SimpleNamespaceContextTest {

	private SimpleNamespaceContext context;

	@BeforeEach
	void setUp() {
		context = new SimpleNamespaceContext();
		context.bindNamespaceUri("prefix", "namespaceURI");
	}

	@Test
	void testGetNamespaceURI() {

		assertThat(context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)).isEmpty();

		String defaultNamespaceUri = "defaultNamespace";
		context.bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespaceUri);

		assertThat(context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)).isEqualTo(defaultNamespaceUri);
		assertThat(context.getNamespaceURI("prefix")).isEqualTo("namespaceURI");
		assertThat(context.getNamespaceURI("unbound")).isEmpty();
		assertThat(context.getNamespaceURI(XMLConstants.XML_NS_PREFIX)).isEqualTo(XMLConstants.XML_NS_URI);
		assertThat(context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE)).isEqualTo(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
	}

	@Test
	void testGetPrefix() {

		context.bindDefaultNamespaceUri("defaultNamespaceURI");

		assertThat(context.getPrefix("defaultNamespaceURI")).isEqualTo(XMLConstants.DEFAULT_NS_PREFIX);
		assertThat(context.getPrefix("namespaceURI")).isEqualTo("prefix");
		assertThat(context.getPrefix("unbound")).isNull();
		assertThat(context.getPrefix(XMLConstants.XML_NS_URI)).isEqualTo(XMLConstants.XML_NS_PREFIX);
		assertThat(context.getPrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)).isEqualTo(XMLConstants.XMLNS_ATTRIBUTE);
	}

	@Test
	void testGetPrefixes() {

		context.bindDefaultNamespaceUri("defaultNamespaceURI");

		assertPrefixes("defaultNamespaceURI", XMLConstants.DEFAULT_NS_PREFIX);
		assertPrefixes("namespaceURI", "prefix");
		assertThat(context.getPrefixes("unbound").hasNext()).isFalse();
		assertPrefixes(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
		assertPrefixes(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
	}

	@Test
	void unmodifiableGetPrefixes() {
		String namespaceUri = "namespaceUri";
		context.bindNamespaceUri("prefix1", namespaceUri);
		context.bindNamespaceUri("prefix2", namespaceUri);

		Iterator<String> prefixes = context.getPrefixes(namespaceUri);
		prefixes.next();
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(prefixes::remove);
	}

	@Test
	void testMultiplePrefixes() {

		context.bindNamespaceUri("prefix1", "namespace");
		context.bindNamespaceUri("prefix2", "namespace");

		Iterator<String> iterator = context.getPrefixes("namespace");

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		String result = iterator.next();

		assertThat(result)
				.has(new Condition<>(value -> value.equals("prefix1") || value.equals("prefix2"), "verify prefix"));
		assertThat(iterator.hasNext()).isTrue();

		result = iterator.next();

		assertThat(result)
				.has(new Condition<>(value -> value.equals("prefix1") || value.equals("prefix2"), "verify prefix"));
		assertThat(iterator.hasNext()).isFalse();
	}

	private void assertPrefixes(String namespaceUri, String prefix) {

		Iterator<String> iterator = context.getPrefixes(namespaceUri);

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		String result = iterator.next();

		assertThat(result).isEqualTo(prefix);
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	void testGetBoundPrefixes() {

		Iterator<String> iterator = context.getBoundPrefixes();

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		String result = iterator.next();

		assertThat(result).isEqualTo("prefix");
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	void testSetBindings() {

		context.setBindings(Collections.singletonMap("prefix", "namespace"));

		assertThat(context.getNamespaceURI("prefix")).isEqualTo("namespace");
	}

	@Test
	void testRemoveBinding() {

		context.clear();

		String prefix1 = "prefix1";
		String prefix2 = "prefix2";
		String namespaceUri = "namespaceUri";

		context.bindNamespaceUri(prefix1, namespaceUri);
		context.bindNamespaceUri(prefix2, namespaceUri);

		assertThat(IteratorUtils.toList(context.getPrefixes(namespaceUri))).containsExactly(prefix1, prefix2);

		context.removeBinding(prefix1);

		assertThat(IteratorUtils.toList(context.getPrefixes(namespaceUri))).containsExactly(prefix2);

		context.removeBinding(prefix2);

		assertThat(context.getPrefixes(namespaceUri).hasNext()).isFalse();
	}

	@Test
	void testHasBinding() {

		context.clear();

		String prefix = "prefix";

		assertThat(context.hasBinding(prefix)).isFalse();

		String namespaceUri = "namespaceUri";
		context.bindNamespaceUri(prefix, namespaceUri);

		assertThat(context.hasBinding(prefix)).isTrue();
	}

	@Test
	void testDefaultNamespaceMultiplePrefixes() {

		String defaultNamespace = "http://springframework.org/spring-ws";

		context.bindDefaultNamespaceUri(defaultNamespace);
		context.bindNamespaceUri("prefix", defaultNamespace);

		assertThat(context.getPrefix(defaultNamespace)).isEqualTo(XMLConstants.DEFAULT_NS_PREFIX);

		Iterator<String> iterator = context.getPrefixes(defaultNamespace);

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		String result = iterator.next();

		assertThat(result).has(new Condition<>(
				value -> value.equals(XMLConstants.DEFAULT_NS_PREFIX) || value.equals("prefix"), "Verify prefix"));
		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		result = iterator.next();

		assertThat(result).has(new Condition<>(
				value -> value.equals(XMLConstants.DEFAULT_NS_PREFIX) || value.equals("prefix"), "Verify prefix"));
		assertThat(iterator.hasNext()).isFalse();
	}

}
