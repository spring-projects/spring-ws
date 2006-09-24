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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.util.MapAccessor;
import org.springframework.binding.util.MapAdaptable;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * A base class for map decorators who manage the storage of immutable
 * String-keyed, String-valued parameters in a backing {@link Map}
 * implementation. This base provides convenient operations for accessing
 * parameters in a typed-manner. It also includes support file accessing
 * {@link MultipartFile} parameters.
 * 
 * @author Keith Donald
 */
public class ParameterMap implements MapAdaptable, Serializable {

	/**
	 * The backing map storing the parameters.
	 */
	private Map parameters;

	/**
	 * A helper for accessing parameters. Marked transient and restored on
	 * deserialization.
	 */
	private transient MapAccessor parameterAccessor;

	/**
	 * A helper for converting string parameter values. Marked transient and
	 * restored on deserialization.
	 */
	private transient ConversionService conversionService;

	/**
	 * Creates a new parameter map from the provided map.
	 * <p>
	 * It is expected that the contents of the backing map adhere to the
	 * parameter map contract; that is, map entries have string keys, string
	 * values, and remain unmodifiable.
	 * @param parameters the contents of this parameter map
	 */
	public ParameterMap(Map parameters) {
		this(parameters, new DefaultConversionService());
	}

	/**
	 * Creates a new parameter map from the provided map.
	 * <p>
	 * It is expected that the contents of the backing map adhere to the
	 * parameter map contract; that is, map entries have string keys, string
	 * values, and remain unmodifiable.
	 * @param parameters the contents of this parameter map
	 * @param conversionService a helper for performing type conversion of map
	 * entry values
	 */
	public ParameterMap(Map parameters, ConversionService conversionService) {
		initParameters(parameters);
		this.conversionService = conversionService;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ParameterMap)) {
			return false;
		}
		ParameterMap other = (ParameterMap)o;
		return parameters.equals(other.parameters);
	}
	
	public int hashCode() {
		return parameters.hashCode();
	}
	
	public Map getMap() {
		return parameterAccessor.getMap();
	}

	/**
	 * Is this parameter map empty, with a size of 0?
	 * @return true if empty, false if not
	 */
	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	/**
	 * Returns the number of parameters in this map.
	 * @return the parameter count
	 */
	public int size() {
		return parameters.size();
	}

	/**
	 * Does the parameter with the provided name exist in this map?
	 * @param parameterName the parameter name
	 * @return true if so, false otherwise
	 */
	public boolean contains(String parameterName) {
		return parameters.containsKey(parameterName);
	}

	/**
	 * Get a parameter value, returning <code>null</code> if no value is
	 * found.
	 * @param parameterName the parameter name
	 * @return the parameter value
	 */
	public String get(String parameterName) {
		return get(parameterName, (String)null);
	}

	/**
	 * Get a parameter value, returning the defaultValue if no value is found.
	 * @param parameterName the parameter name
	 * @param defaultValue the default
	 * @return the parameter value
	 */
	public String get(String parameterName, String defaultValue) {
		if (!parameters.containsKey(parameterName)) {
			return defaultValue;
		}
		Object value = parameters.get(parameterName);
		if (value.getClass().isArray()) {
			parameterAccessor.assertKeyValueInstanceOf(parameterName, value, String[].class);
			String[] array = (String[])value;
			if (array.length == 0) {
				return null;
			}
			else {
				Object first = ((String[])value)[0];
				parameterAccessor.assertKeyValueInstanceOf(parameterName, first, String.class);
				return (String)first;
			}

		}
		else {
			parameterAccessor.assertKeyValueInstanceOf(parameterName, value, String.class);
			return (String)value;
		}
	}

	/**
	 * Get a multi-valued parameter value, returning <code>null</code> if no
	 * value is found. If the parameter is single valued an array with a single
	 * element is returned.
	 * @param parameterName the parameter name
	 * @return the parameter value array
	 */
	public String[] getArray(String parameterName) {
		if (!parameters.containsKey(parameterName)) {
			return null;
		}
		Object value = parameters.get(parameterName);
		if (value.getClass().isArray()) {
			parameterAccessor.assertKeyValueInstanceOf(parameterName, value, String[].class);
			return (String[])value;
		}
		else {
			parameterAccessor.assertKeyValueInstanceOf(parameterName, value, String.class);
			return new String[] { (String)value };
		}
	}

	/**
	 * Get a multi-valued parameter value, converting each value to the target
	 * type or returning <code>null</code> if no value is found.
	 * @param parameterName the parameter name
	 * @param targetElementType the target type of the array's elements
	 * @return the converterd parameter value array
	 * @throws ConversionException when the value could not be converted
	 */
	public Object[] getArray(String parameterName, Class targetElementType) throws ConversionException {
		String[] parameters = getArray(parameterName);
		return parameters != null ? convert(parameters, targetElementType) : null;
	}

	/**
	 * Get a parameter value, converting it from <code>String</code> to the
	 * target type.
	 * @param parameterName the name of the parameter
	 * @param targetType the target type of the parameter value
	 * @return the converted parameter value, or null if not found
	 * @throws ConversionException when the value could not be converted
	 */
	public Object get(String parameterName, Class targetType) throws ConversionException {
		return get(parameterName, targetType, null);
	}

	/**
	 * Get a parameter value, converting it from <code>String</code> to the
	 * target type or returning the defaultValue if not found.
	 * @param parameterName name of the parameter to get
	 * @param targetType the target type of the parameter value
	 * @param defaultValue the default value
	 * @return the converted parameter value, or the default if not found
	 * @throws ConversionException when a value could not be converted
	 */
	public Object get(String parameterName, Class targetType, Object defaultValue) throws ConversionException {
		if (defaultValue != null) {
			assertAssignableTo(targetType, defaultValue.getClass());
		}
		String parameter = get(parameterName);
		return parameter != null ? convert(parameter, targetType) : defaultValue;
	}

	/**
	 * Get a multi-part file parameter value, returning <code>null</code> if
	 * no value is found.
	 * @param parameterName the parameter name
	 * @return the multipart file
	 */
	public MultipartFile getMultipartFile(String parameterName) {
		return (MultipartFile)parameterAccessor.get(parameterName, MultipartFile.class);
	}

	/**
	 * Get the value of a required parameter.
	 * @param parameterName the name of the parameter
	 * @return the parameter value
	 * @throws IllegalArgumentException when the parameter is not found
	 */
	public String getRequired(String parameterName) throws IllegalArgumentException {
		parameterAccessor.assertContainsKey(parameterName);
		return get(parameterName);
	}

	/**
	 * Get a required multi-valued parameter value.
	 * @param parameterName the name of the parameter
	 * @return the parameter value
	 * @throws IllegalArgumentException when the parameter is not found
	 */
	public String[] getRequiredArray(String parameterName) throws IllegalArgumentException {
		parameterAccessor.assertContainsKey(parameterName);
		return (String[])getArray(parameterName);
	}

	/**
	 * Get the value of a required multipart file parameter.
	 * @param parameterName the name of the parameter
	 * @return the parameter value
	 * @throws IllegalArgumentException when the parameter is not found
	 */
	public MultipartFile getRequiredMultipartFile(String parameterName) throws IllegalArgumentException {
		return (MultipartFile)parameterAccessor.getRequired(parameterName, MultipartFile.class);
	}

	/**
	 * Get a required multi-valued parameter value, converting each value to the
	 * target type.
	 * @param parameterName the name of the parameter
	 * @return the parameter value
	 * @throws IllegalArgumentException when the parameter is not found
	 * @throws ConversionException when a value could not be converted
	 */
	public Object[] getRequiredArray(String parameterName, Class targetElementType) throws IllegalArgumentException,
			ConversionException {
		String[] parameters = getRequiredArray(parameterName);
		return convert(parameters, targetElementType);
	}

	/**
	 * Get the value of a required parameter and convert it to the target type.
	 * @param parameterName the name of the parameter
	 * @param targetType the target type of the parameter value
	 * @return the converted parameter value
	 * @throws IllegalArgumentException when the parameter is not found
	 * @throws ConversionException when the value could not be converted
	 */
	public Object getRequired(String parameterName, Class targetType) throws IllegalArgumentException,
			ConversionException {
		return convert(getRequired(parameterName), targetType);
	}

	/**
	 * Returns a number parameter value in the map that is of the specified
	 * type, returning <code>null</code> if no value was found.
	 * @param parameterName the parameter name
	 * @param targetType the target number type
	 * @return the number parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Number getNumber(String parameterName, Class targetType) throws ConversionException {
		assertAssignableTo(Number.class, targetType);
		return (Number)get(parameterName, targetType);
	}

	/**
	 * Returns a number parameter value in the map of the specified type,
	 * returning the defaultValue if no value was found.
	 * @param parameterName the parameter name
	 * @param defaultValue the default
	 * @return the number parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Number getNumber(String parameterName, Class targetType, Number defaultValue) throws ConversionException {
		assertAssignableTo(Number.class, targetType);
		return (Number)get(parameterName, targetType, defaultValue);
	}

	/**
	 * Returns a number parameter value in the map, throwing an exception if the
	 * parameter is not present or could not be converted.
	 * @param parameterName the parameter name
	 * @return the number parameter value
	 * @throws IllegalArgumentException if the parameter is not present
	 * @throws ConversionException when the value could not be converted
	 */
	public Number getRequiredNumber(String parameterName, Class targetType) throws IllegalArgumentException,
			ConversionException {
		assertAssignableTo(Number.class, targetType);
		return (Number)getRequired(parameterName, targetType);
	}

	/**
	 * Returns an integer parameter value in the map, returning
	 * <code>null</code> if no value was found.
	 * @param parameterName the parameter name
	 * @return the integer parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Integer getInteger(String parameterName) throws ConversionException {
		return (Integer)get(parameterName, Integer.class);
	}

	/**
	 * Returns an integer parameter value in the map, returning the defaultValue
	 * if no value was found.
	 * @param parameterName the parameter name
	 * @param defaultValue the default
	 * @return the integer parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Integer getInteger(String parameterName, Integer defaultValue) throws ConversionException {
		return (Integer)get(parameterName, Integer.class, defaultValue);
	}

	/**
	 * Returns an integer parameter value in the map, throwing an exception if
	 * the parameter is not present or could not be converted.
	 * @param parameterName the parameter name
	 * @return the integer parameter value
	 * @throws IllegalArgumentException if the parameter is not present
	 * @throws ConversionException when the value could not be converted
	 */
	public Integer getRequiredInteger(String parameterName) throws IllegalArgumentException, ConversionException {
		return (Integer)getRequired(parameterName, Integer.class);
	}

	/**
	 * Returns a long parameter value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param parameterName the parameter name
	 * @return the long parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Long getLong(String parameterName) throws ConversionException {
		return (Long)get(parameterName, Long.class);
	}

	/**
	 * Returns a long parameter value in the map, returning the defaultValue if
	 * no value was found.
	 * @param parameterName the parameter name
	 * @param defaultValue the default
	 * @return the long parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Long getLong(String parameterName, Long defaultValue) throws ConversionException {
		return (Long)get(parameterName, Long.class, defaultValue);
	}

	/**
	 * Returns a long parameter value in the map, throwing an exception if the
	 * parameter is not present or could not be converted.
	 * @param parameterName the parameter name
	 * @return the long parameter value
	 * @throws IllegalArgumentException if the parameter is not present
	 * @throws ConversionException when the value could not be converted
	 */
	public Long getRequiredLong(String parameterName) throws IllegalArgumentException, ConversionException {
		return (Long)getRequired(parameterName, Long.class);
	}

	/**
	 * Returns a boolean parameter value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param parameterName the parameter name
	 * @return the long parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Boolean getBoolean(String parameterName) throws ConversionException {
		return (Boolean)get(parameterName, Boolean.class);
	}

	/**
	 * Returns a boolean parameter value in the map, returning the defaultValue
	 * if no value was found.
	 * @param parameterName the parameter name
	 * @param defaultValue the default
	 * @return the boolean parameter value
	 * @throws ConversionException when the value could not be converted
	 */
	public Boolean getBoolean(String parameterName, Boolean defaultValue) throws ConversionException {
		return (Boolean)get(parameterName, Boolean.class, defaultValue);
	}

	/**
	 * Returns a boolean parameter value in the map, throwing an exception if
	 * the parameter is not present or could not be converted.
	 * @param parameterName the parameter name
	 * @return the boolean parameter value
	 * @throws IllegalArgumentException if the parameter is not present
	 * @throws ConversionException when the value could not be converted
	 */
	public Boolean getRequiredBoolean(String parameterName) throws IllegalArgumentException, ConversionException {
		return (Boolean)getRequired(parameterName, Boolean.class);
	}

	/**
	 * Adapts this parameter map to an {@link UnmodifiableAttributeMap}.
	 * @return the underlying map as a unmodifiable attribute map.
	 */
	public UnmodifiableAttributeMap asAttributeMap() {
		return new UnmodifiableAttributeMap(getMapInternal());
	}
	
	/**
	 * Initializes this parameter map.
	 * @param parameters the parameters
	 */
	protected void initParameters(Map parameters) {
		this.parameters = parameters;
		parameterAccessor = new MapAccessor(this.parameters);
	}

	/**
	 * Returns the wrapped, modifiable map implementation.
	 */
	protected Map getMapInternal() {
		return parameters;
	}

	private Object convert(String parameter, Class targetType) throws ConversionException {
		return conversionService.getConversionExecutor(String.class, targetType).execute(parameter);
	}

	private Object[] convert(String[] parameters, Class targetElementType) throws ConversionException {
		List list = new ArrayList(parameters.length);
		ConversionExecutor converter = conversionService.getConversionExecutor(String.class, targetElementType);
		for (int i = 0; i < parameters.length; i++) {
			list.add(converter.execute(parameters[i]));
		}
		return list.toArray((Object[])Array.newInstance(targetElementType, parameters.length));
	}

	private void assertAssignableTo(Class clazz, Class requiredType) {
		Assert.isTrue(clazz.isAssignableFrom(requiredType), "The provided required type must be assignable to ["
				+ clazz + "]");
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		parameterAccessor = new MapAccessor(parameters);
		conversionService = new DefaultConversionService();
	}

	public String toString() {
		return StylerUtils.style(parameters);
	}
}