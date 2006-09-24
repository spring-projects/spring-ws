/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.context.servlet;

import java.util.HashMap;

import junit.framework.TestCase;

import org.springframework.mock.web.MockServletContext;

/**
 * Test case for the HttpServletContextMap class.
 * 
 * @author Ulrik Sandberg
 */
public class HttpServletContextMapTests extends TestCase {

	private HttpServletContextMap tested;

	private MockServletContext context;

	protected void setUp() throws Exception {
		super.setUp();
		context = new MockServletContext();
		// a fresh MockServletContext seems to already contain an element;
		// that's confusing, so we remove it
		context.removeAttribute("javax.servlet.context.tempdir");
		tested = new HttpServletContextMap(context);
		tested.put("SomeKey", "SomeValue");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		context = null;
		tested = null;
	}

	public void testIsEmpty() {
		tested.remove("SomeKey");
		assertEquals("size,", 0, tested.size());
		assertEquals("isEmpty,", true, tested.isEmpty());
	}

	public void testSizeAddOne() {
		assertEquals("size,", 1, tested.size());
	}

	public void testSizeAddTwo() {
		tested.put("SomeOtherKey", "SomeOtherValue");
		assertEquals("size,", 2, tested.size());
	}

	public void testContainsKey() {
		assertEquals("containsKey,", true, tested.containsKey("SomeKey"));
	}

	public void testContainsValue() {
		tested.containsValue("SomeValue");
		// TODO
	}

	public void testGet() {
		assertEquals("get,", "SomeValue", tested.get("SomeKey"));
	}

	public void testPut() {
		Object old = tested.put("SomeKey", "SomeNewValue");

		assertEquals("old value,", "SomeValue", old);
		assertEquals("new value,", "SomeNewValue", tested.get("SomeKey"));
	}

	public void testRemove() {
		Object old = tested.remove("SomeKey");

		assertEquals("old value,", "SomeValue", old);
		assertNull("should be gone", tested.get("SomeKey"));
	}

	public void testPutAll() {
		tested.putAll(new HashMap());
		// TODO
	}

	public void testClear() {
		tested.clear();
		// TODO
	}

	public void testKeySet() {
		tested.keySet();
		// TODO
	}

	public void testValues() {
		tested.values();
		// TODO
	}

	public void testEntrySet() {
		tested.entrySet();
		// TODO
	}
}