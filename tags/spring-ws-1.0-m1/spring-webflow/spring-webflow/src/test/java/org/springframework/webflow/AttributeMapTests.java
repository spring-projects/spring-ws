package org.springframework.webflow;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class AttributeMapTests extends TestCase {
	private AttributeMap attributeMap = new AttributeMap();

	public void setUp() {
		attributeMap.put("string", "A string");
		attributeMap.put("integer", new Integer(12345));
		attributeMap.put("boolean", Boolean.TRUE);
		attributeMap.put("long", new Long(12345));
		attributeMap.put("double", new Double(12345));
		attributeMap.put("float", new Float(12345));
		attributeMap.put("bigDecimal", new BigDecimal("12345.67"));
		attributeMap.put("bean", new TestBean());
		attributeMap.put("stringArray", new String[] { "1", "2", "3" });
		attributeMap.put("collection", new LinkedList());
	}

	public void testGet() {
		TestBean bean = (TestBean)attributeMap.get("bean");
		assertNotNull(bean);
	}

	public void testGetNull() {
		TestBean bean = (TestBean)attributeMap.get("bogus");
		assertNull(bean);
	}

	public void testGetRequiredType() {
		TestBean bean = (TestBean)attributeMap.get("bean", TestBean.class);
		assertNotNull(bean);
	}

	public void testGetWrongType() {
		try {
			attributeMap.get("bean", String.class);
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetWithDefaultOption() {
		TestBean d = new TestBean();
		TestBean bean = (TestBean)attributeMap.get("bean", d);
		assertNotNull(bean);
		assertNotSame(bean, d);
	}

	public void testGetWithDefault() {
		TestBean d = new TestBean();
		TestBean bean = (TestBean)attributeMap.get("bogus", d);
		assertSame(bean, d);
	}

	public void testGetRequired() {
		TestBean bean = (TestBean)attributeMap.getRequired("bean");
		assertNotNull(bean);
	}

	public void testGetRequiredNotPresent() {
		try {
			attributeMap.getRequired("bogus");
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetRequiredOfType() {
		TestBean bean = (TestBean)attributeMap.getRequired("bean", TestBean.class);
		assertNotNull(bean);
	}

	public void testGetRequiredWrongType() {
		try {
			attributeMap.getRequired("bean", String.class);
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetNumber() {
		BigDecimal bd = (BigDecimal)attributeMap.getNumber("bigDecimal", BigDecimal.class);
		assertEquals(new BigDecimal("12345.67"), bd);
	}

	public void testGetNumberWrongType() {
		try {
			attributeMap.getNumber("bigDecimal", Integer.class);
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetNumberWithDefaultOption() {
		BigDecimal d = new BigDecimal("1");
		BigDecimal bd = (BigDecimal)attributeMap.getNumber("bigDecimal", BigDecimal.class, d);
		assertEquals(new BigDecimal("12345.67"), bd);
		assertNotSame(d, bd);
	}

	public void testGetNumberWithDefault() {
		BigDecimal d = new BigDecimal("1");
		BigDecimal bd = (BigDecimal)attributeMap.getNumber("bogus", BigDecimal.class, d);
		assertEquals(d, bd);
		assertSame(d, bd);
	}

	public void testGetNumberRequired() {
		BigDecimal bd = (BigDecimal)attributeMap.getRequiredNumber("bigDecimal", BigDecimal.class);
		assertEquals(new BigDecimal("12345.67"), bd);
	}

	public void testGetNumberRequiredNotPresent() {
		try {
			attributeMap.getRequiredNumber("bogus", BigDecimal.class);
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetInteger() {
		Integer i = (Integer)attributeMap.getInteger("integer");
		assertEquals(new Integer(12345), i);
	}

	public void testGetIntegerNull() {
		Integer i = (Integer)attributeMap.getInteger("bogus");
		assertNull(i);
	}

	public void testGetIntegerRequired() {
		Integer i = (Integer)attributeMap.getRequiredInteger("integer");
		assertEquals(new Integer(12345), i);
	}

	public void testGetIntegerRequiredNotPresent() {
		try {
			attributeMap.getRequiredInteger("bogus");
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetLong() {
		Long i = (Long)attributeMap.getLong("long");
		assertEquals(new Long(12345), i);
	}

	public void testGetLongNull() {
		Long i = (Long)attributeMap.getLong("bogus");
		assertNull(i);
	}

	public void testGetLongRequired() {
		Long i = (Long)attributeMap.getRequiredLong("long");
		assertEquals(new Long(12345), i);
	}

	public void testGetLongRequiredNotPresent() {
		try {
			attributeMap.getRequiredLong("bogus");
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetString() {
		String i = (String)attributeMap.getString("string");
		assertEquals("A string", i);
	}

	public void testGetStringNull() {
		String i = (String)attributeMap.getString("bogus");
		assertNull(i);
	}

	public void testGetStringRequired() {
		String i = (String)attributeMap.getRequiredString("string");
		assertEquals("A string", i);
	}

	public void testGetStringRequiredNotPresent() {
		try {
			attributeMap.getRequiredString("bogus");
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetBoolean() {
		Boolean i = (Boolean)attributeMap.getBoolean("boolean");
		assertEquals(Boolean.TRUE, i);
	}

	public void testGetBooleanNull() {
		Boolean i = (Boolean)attributeMap.getBoolean("bogus");
		assertNull(i);
	}

	public void testGetBooleanRequired() {
		Boolean i = (Boolean)attributeMap.getRequiredBoolean("boolean");
		assertEquals(Boolean.TRUE, i);
	}

	public void testGetBooleanRequiredNotPresent() {
		try {
			attributeMap.getRequiredBoolean("bogus");
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetArray() {
		String[] i = (String[])attributeMap.getArray("stringArray", String[].class);
		assertEquals(3, i.length);
	}

	public void testGetArrayNull() {
		String[] i = (String[])attributeMap.getArray("A bogus array", String[].class);
		assertNull(i);
	}

	public void testGetArrayRequired() {
		String[] i = (String[])attributeMap.getRequiredArray("stringArray", String[].class);
		assertEquals(3, i.length);
	}

	public void testGetArrayRequiredNotPresent() {
		try {
			attributeMap.getRequiredArray("A bogus array", String[].class);
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetCollection() {
		LinkedList i = (LinkedList)attributeMap.getCollection("collection", List.class);
		assertEquals(0, i.size());
	}

	public void testGetCollectionNull() {
		LinkedList i = (LinkedList)attributeMap.getCollection("bogus", List.class);
		assertNull(i);
	}

	public void testGetCollectionRequired() {
		LinkedList i = (LinkedList)attributeMap.getRequiredCollection("collection", List.class);
		assertEquals(0, i.size());
	}

	public void testGetCollectionRequiredNotPresent() {
		try {
			attributeMap.getRequiredCollection("A bogus collection");
			fail("Should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testGetMap() {
		Map map = attributeMap.getMap();
		assertEquals(10, map.size());
		try {
			map.put("can't", "modify");
			fail("Cant modify but u did");
		}
		catch (UnsupportedOperationException e) {

		}
	}
	
	public void testUnion() {
		AttributeMap one = new AttributeMap();
		one.put("foo", "bar");
		one.put("bar", "baz");

		AttributeMap two = new AttributeMap();
		two.put("cat", "coz");
		two.put("bar", "boo");

		AttributeCollection three = one.union(two);
		assertEquals(3, three.size());
		assertEquals("bar", three.get("foo"));
		assertEquals("coz", three.get("cat"));
		assertEquals("boo", three.get("bar"));
	}
	
	public void testEquality() {
		AttributeMap map = new AttributeMap();
		map.put("foo", "bar");
		
		AttributeMap map2 = new AttributeMap();
		map2.put("foo", "bar");
		
		assertEquals(map, map2);
	}
	
	public void testEqualityUnmodifiable() {
		UnmodifiableAttributeMap map1 = CollectionUtils.singleEntryMap("foo", "bar");
		UnmodifiableAttributeMap map2 = CollectionUtils.singleEntryMap("foo", "bar");	
		assertEquals(map1, map2);
	}
}