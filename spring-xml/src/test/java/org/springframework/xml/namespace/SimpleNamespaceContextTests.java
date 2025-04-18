/*
 * Copyright 2005-2025 the original author or authors.
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

class SimpleNamespaceContextTests {

	private SimpleNamespaceContext context;

	@BeforeEach
	void setUp() {
		this.context = new SimpleNamespaceContext();
		this.context.bindNamespaceUri("prefix", "namespaceURI");
	}

	@Test
	void testGetNamespaceURI() {

		assertThat(this.context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)).isEmpty();

		String defaultNamespaceUri = "defaultNamespace";
		this.context.bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespaceUri);

		assertThat(this.context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX)).isEqualTo(defaultNamespaceUri);
		assertThat(this.context.getNamespaceURI("prefix")).isEqualTo("namespaceURI");
		assertThat(this.context.getNamespaceURI("unbound")).isEmpty();
		assertThat(this.context.getNamespaceURI(XMLConstants.XML_NS_PREFIX)).isEqualTo(XMLConstants.XML_NS_URI);
		assertThat(this.context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE))
			.isEqualTo(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
	}

	@Test
	void testGetPrefix() {

		this.context.bindDefaultNamespaceUri("defaultNamespaceURI");

		assertThat(this.context.getPrefix("defaultNamespaceURI")).isEqualTo(XMLConstants.DEFAULT_NS_PREFIX);
		assertThat(this.context.getPrefix("namespaceURI")).isEqualTo("prefix");
		assertThat(this.context.getPrefix("unbound")).isNull();
		assertThat(this.context.getPrefix(XMLConstants.XML_NS_URI)).isEqualTo(XMLConstants.XML_NS_PREFIX);
		assertThat(this.context.getPrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)).isEqualTo(XMLConstants.XMLNS_ATTRIBUTE);
	}

	@Test
	void testGetPrefixes() {

		this.context.bindDefaultNamespaceUri("defaultNamespaceURI");

		assertPrefixes("defaultNamespaceURI", XMLConstants.DEFAULT_NS_PREFIX);
		assertPrefixes("namespaceURI", "prefix");
		assertThat(this.context.getPrefixes("unbound").hasNext()).isFalse();
		assertPrefixes(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
		assertPrefixes(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
	}

	@Test
	void unmodifiableGetPrefixes() {
		String namespaceUri = "namespaceUri";
		this.context.bindNamespaceUri("prefix1", namespaceUri);
		this.context.bindNamespaceUri("prefix2", namespaceUri);

		Iterator<String> prefixes = this.context.getPrefixes(namespaceUri);
		prefixes.next();
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(prefixes::remove);
	}

	@Test
	void testMultiplePrefixes() {

		this.context.bindNamespaceUri("prefix1", "namespace");
		this.context.bindNamespaceUri("prefix2", "namespace");

		Iterator<String> iterator = this.context.getPrefixes("namespace");

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

		Iterator<String> iterator = this.context.getPrefixes(namespaceUri);

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		String result = iterator.next();

		assertThat(result).isEqualTo(prefix);
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	void testGetBoundPrefixes() {

		Iterator<String> iterator = this.context.getBoundPrefixes();

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		String result = iterator.next();

		assertThat(result).isEqualTo("prefix");
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	void testSetBindings() {

		this.context.setBindings(Collections.singletonMap("prefix", "namespace"));

		assertThat(this.context.getNamespaceURI("prefix")).isEqualTo("namespace");
	}

	@Test
	void testRemoveBinding() {

		this.context.clear();

		String prefix1 = "prefix1";
		String prefix2 = "prefix2";
		String namespaceUri = "namespaceUri";

		this.context.bindNamespaceUri(prefix1, namespaceUri);
		this.context.bindNamespaceUri(prefix2, namespaceUri);

		assertThat(IteratorUtils.toList(this.context.getPrefixes(namespaceUri))).containsExactly(prefix1, prefix2);

		this.context.removeBinding(prefix1);

		assertThat(IteratorUtils.toList(this.context.getPrefixes(namespaceUri))).containsExactly(prefix2);

		this.context.removeBinding(prefix2);

		assertThat(this.context.getPrefixes(namespaceUri).hasNext()).isFalse();
	}

	@Test
	void testHasBinding() {

		this.context.clear();

		String prefix = "prefix";

		assertThat(this.context.hasBinding(prefix)).isFalse();

		String namespaceUri = "namespaceUri";
		this.context.bindNamespaceUri(prefix, namespaceUri);

		assertThat(this.context.hasBinding(prefix)).isTrue();
	}

	@Test
	void testDefaultNamespaceMultiplePrefixes() {

		String defaultNamespace = "http://springframework.org/spring-ws";

		this.context.bindDefaultNamespaceUri(defaultNamespace);
		this.context.bindNamespaceUri("prefix", defaultNamespace);

		assertThat(this.context.getPrefix(defaultNamespace)).isEqualTo(XMLConstants.DEFAULT_NS_PREFIX);

		Iterator<String> iterator = this.context.getPrefixes(defaultNamespace);

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
