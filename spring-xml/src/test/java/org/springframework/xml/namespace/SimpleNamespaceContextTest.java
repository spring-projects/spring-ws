/*
 * Copyright 2005-2014 the original author or authors.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleNamespaceContextTest {

	private SimpleNamespaceContext context;

	@Before
	public void setUp() throws Exception {
		context = new SimpleNamespaceContext();
		context.bindNamespaceUri("prefix", "namespaceURI");
	}

	@Test
	public void testGetNamespaceURI() {
		Assert.assertEquals("Invalid namespaceURI for default namespace", "", context
				.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
		String defaultNamespaceUri = "defaultNamespace";
		context.bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespaceUri);
		Assert.assertEquals("Invalid namespaceURI for default namespace", defaultNamespaceUri, context
				.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
		Assert.assertEquals("Invalid namespaceURI for bound prefix", "namespaceURI", context.getNamespaceURI("prefix"));
		Assert.assertEquals("Invalid namespaceURI for unbound prefix", "", context.getNamespaceURI("unbound"));
		Assert.assertEquals("Invalid namespaceURI for namespace prefix", XMLConstants.XML_NS_URI, context
				.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
		Assert.assertEquals("Invalid namespaceURI for attribute prefix", XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context
				.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));

	}

	@Test
	public void testGetPrefix() {
		context.bindDefaultNamespaceUri("defaultNamespaceURI");
		Assert.assertEquals("Invalid prefix for default namespace", XMLConstants.DEFAULT_NS_PREFIX, context.getPrefix("defaultNamespaceURI"));
		Assert.assertEquals("Invalid prefix for bound namespace", "prefix", context.getPrefix("namespaceURI"));
		Assert.assertNull("Invalid prefix for unbound namespace", context.getPrefix("unbound"));
		Assert.assertEquals("Invalid prefix for namespace", XMLConstants.XML_NS_PREFIX, context
				.getPrefix(XMLConstants.XML_NS_URI));
		Assert.assertEquals("Invalid prefix for attribute namespace", XMLConstants.XMLNS_ATTRIBUTE, context
				.getPrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
	}

	@Test
	public void testGetPrefixes() {
		context.bindDefaultNamespaceUri("defaultNamespaceURI");
		assertPrefixes("defaultNamespaceURI", XMLConstants.DEFAULT_NS_PREFIX);
		assertPrefixes("namespaceURI", "prefix");
		Assert.assertFalse("Invalid prefix for unbound namespace", context.getPrefixes("unbound").hasNext());
		assertPrefixes(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
		assertPrefixes(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void unmodifiableGetPrefixes() {
		String namespaceUri = "namespaceUri";
		context.bindNamespaceUri("prefix1", namespaceUri);
		context.bindNamespaceUri("prefix2", namespaceUri);

		Iterator<String> prefixes = context.getPrefixes(namespaceUri);
		prefixes.next();
		prefixes.remove();
	}

	@Test
	public void testMultiplePrefixes() {
		context.bindNamespaceUri("prefix1", "namespace");
		context.bindNamespaceUri("prefix2", "namespace");
		Iterator<String> iterator = context.getPrefixes("namespace");
		Assert.assertNotNull("getPrefixes returns null", iterator);
		Assert.assertTrue("iterator is empty", iterator.hasNext());
		String result = iterator.next();
		Assert.assertTrue("Invalid prefix", result.equals("prefix1") || result.equals("prefix2"));
		Assert.assertTrue("iterator is empty", iterator.hasNext());
		result = iterator.next();
		Assert.assertTrue("Invalid prefix", result.equals("prefix1") || result.equals("prefix2"));
		Assert.assertFalse("iterator contains more than two values", iterator.hasNext());
	}

	private void assertPrefixes(String namespaceUri, String prefix) {
		Iterator<String> iterator = context.getPrefixes(namespaceUri);
		Assert.assertNotNull("getPrefixes returns null", iterator);
		Assert.assertTrue("iterator is empty", iterator.hasNext());
		String result = iterator.next();
		Assert.assertEquals("Invalid prefix", prefix, result);
		Assert.assertFalse("iterator contains multiple values", iterator.hasNext());
	}

	@Test
	public void testGetBoundPrefixes() throws Exception {
		Iterator<String> iterator = context.getBoundPrefixes();
		Assert.assertNotNull("getPrefixes returns null", iterator);
		Assert.assertTrue("iterator is empty", iterator.hasNext());
		String result = iterator.next();
		Assert.assertEquals("Invalid prefix", "prefix", result);
		Assert.assertFalse("iterator contains multiple values", iterator.hasNext());
	}

	@Test
	public void testSetBindings() throws Exception {
		context.setBindings(Collections.singletonMap("prefix", "namespace"));
		Assert.assertEquals("Invalid namespace uri", "namespace", context.getNamespaceURI("prefix"));
	}

	@Test
	public void testRemoveBinding() {
		context.clear();
		String prefix1 = "prefix1";
		String prefix2 = "prefix2";
		String namespaceUri = "namespaceUri";
		context.bindNamespaceUri(prefix1,  namespaceUri);
		context.bindNamespaceUri(prefix2,  namespaceUri);
		Iterator<String> iter = context.getPrefixes(namespaceUri);
		Assert.assertTrue("iterator is empty", iter.hasNext());
		Assert.assertEquals(prefix1, iter.next());
		Assert.assertTrue("iterator is empty", iter.hasNext());
		Assert.assertEquals(prefix2, iter.next());
		Assert.assertFalse("iterator not empty", iter.hasNext());

		context.removeBinding(prefix1);

		iter = context.getPrefixes(namespaceUri);
		Assert.assertTrue("iterator is empty", iter.hasNext());
		Assert.assertEquals(prefix2, iter.next());
		Assert.assertFalse("iterator not empty", iter.hasNext());

		context.removeBinding(prefix2);

		iter = context.getPrefixes(namespaceUri);
		Assert.assertFalse("iterator not empty", iter.hasNext());
	}

	@Test
	public void testHasBinding() {
		context.clear();
		String prefix = "prefix";
		Assert.assertFalse("Context has binding", context.hasBinding(prefix));
		String namespaceUri = "namespaceUri";
		context.bindNamespaceUri(prefix, namespaceUri);
		Assert.assertTrue("Context has no binding", context.hasBinding(prefix));
	}

	@Test
	public void testDefaultNamespaceMultiplePrefixes() {
		String defaultNamespace = "http://springframework.org/spring-ws";
		context.bindDefaultNamespaceUri(defaultNamespace);
		context.bindNamespaceUri("prefix", defaultNamespace);
		Assert.assertEquals("Invalid prefix", XMLConstants.DEFAULT_NS_PREFIX, context.getPrefix(defaultNamespace));
		Iterator<String> iterator = context.getPrefixes(defaultNamespace);
		Assert.assertNotNull("getPrefixes returns null", iterator);
		Assert.assertTrue("iterator is empty", iterator.hasNext());
		String result = iterator.next();
		Assert.assertTrue("Invalid prefix", result.equals(XMLConstants.DEFAULT_NS_PREFIX) || result.equals("prefix"));
		Assert.assertTrue("iterator is empty", iterator.hasNext());
		result = iterator.next();
		Assert.assertTrue("Invalid prefix", result.equals(XMLConstants.DEFAULT_NS_PREFIX) || result.equals("prefix"));
		Assert.assertFalse("iterator contains more than two values", iterator.hasNext());
	}

}
