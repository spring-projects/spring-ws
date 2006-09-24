/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.springframework.binding.util.MapAccessor;
import org.springframework.core.style.StylerUtils;

/**
 * A base class for map decorators who manage the storage of String-keyed
 * attributes in a backing {@link Map} implementation. This base provides
 * convenient operations for accessing attributes in a typed-manner.
 * 
 * @author Keith Donald
 */
public abstract class AbstractAttributeMap implements AttributeCollection, Serializable {

	/**
	 * The backing map storing the attributes.
	 */
	private Map attributes;

	/**
	 * A helper for accessing attributes. Marked transient and restored on
	 * deserialization.
	 */
	private transient MapAccessor attributeAccessor;

	public Object get(String attributeName) {
		return attributes.get(attributeName);
	}

	public int size() {
		return attributes.size();
	}

	public Map getMap() {
		return attributeAccessor.getMap();
	}

	public abstract AttributeCollection union(AttributeCollection attributes);

	public abstract UnmodifiableAttributeMap unmodifiable();

	/**
	 * Is this parameter map empty, with a size of 0?
	 * @return true if empty, false if not
	 */
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	/**
	 * Does the attribute with the provided name exist in this scope?
	 * @param attributeName the attribute name
	 * @return true if so, false otherwise
	 */
	public boolean contains(String attributeName) {
		return attributes.containsKey(attributeName);
	}

	/**
	 * Does the attribute with the provided name exist in this scope, and is its
	 * value of the specified class?
	 * @param attributeName the attribute name
	 * @param requiredType the required class of the attribute value
	 * @return true if so, false otherwise
	 * @throws IllegalArgumentException when the value is not of the required
	 * type
	 */
	public boolean contains(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.containsKey(attributeName, requiredType);
	}

	/**
	 * Get an attribute value, returning the default value if no value is found.
	 * @param attributeName the name of the attribute
	 * @param defaultValue the default value
	 * @return the attribute value
	 */
	public Object get(String attributeName, Object defaultValue) {
		return attributeAccessor.get(attributeName, defaultValue);
	}

	/**
	 * Get an attribute value, asserting the value is of the required type.
	 * @param attributeName the name of the attribute
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value, or null if not found
	 * @throws IllegalArgumentException when the value is not of the required
	 * type
	 */
	public Object get(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.get(attributeName, requiredType);
	}

	/**
	 * Get an attribute value, asserting the value is of the required type, returning
	 * the default value if not found.
	 * @param attributeName the name of the attribute
	 * @param requiredType the value required type
	 * @param defaultValue the default value
	 * @return the attribute value, or the default if not found
	 * @throws IllegalArgumentException when the value is not of the required
	 * type
	 */
	public Object get(String attributeName, Class requiredType, Object defaultValue) throws IllegalStateException {
		return attributeAccessor.get(attributeName, requiredType, defaultValue);
	}

	/**
	 * Get the value of a required attribute.
	 * @param attributeName the name of the attribute
	 * @return the attribute value
	 * @throws IllegalArgumentException when the attribute is not found
	 */
	public Object getRequired(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequired(attributeName);
	}

	/**
	 * Get the value of a required attribute and make sure it is of the required
	 * type.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value
	 * @throws IllegalArgumentException when the attribute is not found or not
	 * of the required type
	 */
	public Object getRequired(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequired(attributeName, requiredType);
	}

	/**
	 * Returns a string attribute value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @return the string attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * string
	 */
	public String getString(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getString(attributeName);
	}

	/**
	 * Returns a string attribute value in the map, returning the default value
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the string attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * string
	 */
	public String getString(String attributeName, String defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getString(attributeName, defaultValue);
	}

	/**
	 * Returns a string attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the string attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not a string
	 */
	public String getRequiredString(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredString(attributeName);
	}

	/**
	 * Returns a collection attribute value in the map.
	 * @param attributeName the attribute name
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * collection
	 */
	public Collection getCollection(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getCollection(attributeName);
	}

	/**
	 * Returns a collection attribute value in the map and make sure it is of
	 * the required type.
	 * @param attributeName the attribute name
	 * @param requiredType the required type of the attribute value
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * collection of the required type
	 */
	public Collection getCollection(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getCollection(attributeName, requiredType);
	}

	/**
	 * Returns a collection attribute value in the map, throwing an exception if
	 * the attribute is not present or not a collection.
	 * @param attributeName the attribute name
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is not present or is
	 * present but not a collection
	 */
	public Collection getRequiredCollection(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredCollection(attributeName);
	}

	/**
	 * Returns a collection attribute value in the map, throwing an exception if
	 * the attribute is not present or not a collection of the required type.
	 * @param attributeName the attribute name
	 * @param requiredType the required collection type
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is not present or is
	 * present but not a collection of the required type
	 */
	public Collection getRequiredCollection(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequiredCollection(attributeName, requiredType);
	}

	/**
	 * Returns an array attribute value in the map and makes sure it is of
	 * the required type.
	 * @param attributeName the attribute name
	 * @param requiredType the required type of the attribute value
	 * @return the array attribute value
	 * @throws IllegalArgumentException if the attribute is present but not an
	 * array of the required type
	 */
	public Object[] getArray(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getArray(attributeName, requiredType);
	}

	/**
	 * Returns an array attribute value in the map, throwing an exception if
	 * the attribute is not present or not a collection of the required type.
	 * @param attributeName the attribute name
	 * @param requiredType the required array type
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is not present or is
	 * present but not a array of the required type
	 */
	public Object[] getRequiredArray(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequiredArray(attributeName, requiredType);
	}

	/**
	 * Returns a number attribute value in the map that is of the specified
	 * type, returning <code>null</code> if no value was found.
	 * @param attributeName the attribute name
	 * @param requiredType the required number type
	 * @return the number attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * number of the required type
	 */
	public Number getNumber(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getNumber(attributeName, requiredType);
	}

	/**
	 * Returns a number attribute value in the map of the specified type,
	 * returning the default value if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the number attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * number of the required type
	 */
	public Number getNumber(String attributeName, Class requiredType, Number defaultValue)
			throws IllegalArgumentException {
		return attributeAccessor.getNumber(attributeName, requiredType, defaultValue);
	}

	/**
	 * Returns a number attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the number attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not a number of the required type
	 */
	public Number getRequiredNumber(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequiredNumber(attributeName, requiredType);
	}

	/**
	 * Returns an integer attribute value in the map, returning
	 * <code>null</code> if no value was found.
	 * @param attributeName the attribute name
	 * @return the integer attribute value
	 * @throws IllegalArgumentException if the attribute is present but not an
	 * integer
	 */
	public Integer getInteger(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getInteger(attributeName);
	}

	/**
	 * Returns an integer attribute value in the map, returning the default
	 * value if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the integer attribute value
	 * @throws IllegalArgumentException if the attribute is present but not an
	 * integer
	 */
	public Integer getInteger(String attributeName, Integer defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getInteger(attributeName, defaultValue);
	}

	/**
	 * Returns an integer attribute value in the map, throwing an exception if
	 * the attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the integer attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not an integer
	 */
	public Integer getRequiredInteger(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredInteger(attributeName);
	}

	/**
	 * Returns a long attribute value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * long
	 */
	public Long getLong(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getLong(attributeName);
	}

	/**
	 * Returns a long attribute value in the map, returning the default value if
	 * no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * long
	 */
	public Long getLong(String attributeName, Long defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getLong(attributeName, defaultValue);
	}

	/**
	 * Returns a long attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not a long
	 */
	public Long getRequiredLong(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredLong(attributeName);
	}

	/**
	 * Returns a boolean attribute value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * boolean
	 */
	public Boolean getBoolean(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getBoolean(attributeName);
	}

	/**
	 * Returns a boolean attribute value in the map, returning the default value
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the boolean attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * boolean
	 */
	public Boolean getBoolean(String attributeName, Boolean defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getBoolean(attributeName, defaultValue);
	}

	/**
	 * Returns a boolean attribute value in the map, throwing an exception if
	 * the attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the boolean attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but is not a boolean
	 */
	public Boolean getRequiredBoolean(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredBoolean(attributeName);
	}

	/**
	 * Initializes this attribute map.
	 * @param attributes the attributes
	 */
	protected void initAttributes(Map attributes) {
		this.attributes = attributes;
		attributeAccessor = new MapAccessor(this.attributes);
	}

	/**
	 * Returns the wrapped, modifiable map implementation.
	 */
	protected Map getMapInternal() {
		return attributes;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		attributeAccessor = new MapAccessor(attributes);
	}

	public String toString() {
		return StylerUtils.style(attributes);
	}
}