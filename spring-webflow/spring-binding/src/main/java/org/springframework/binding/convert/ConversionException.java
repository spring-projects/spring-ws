/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.convert;

import org.springframework.core.NestedRuntimeException;

/**
 * Base class for exceptions thrown by the type conversion system.
 * @author Keith Donald
 */
public class ConversionException extends NestedRuntimeException {

	/**
	 * The value we tried to convert.
	 */
	private Object value;

	/**
	 * The target type we tried to convert to.
	 */
	private Class targetClass;

	/**
	 * Creates a new conversion exception.
	 * @param value
	 * @param targetClass
	 */
	public ConversionException(Object value, Class targetClass) {
		super("Unable to convert value '" + value + "' of type '" + (value != null ? value.getClass().getName() : null)
				+ "' to class '" + targetClass.getName() + "'");
		this.value = value;
		this.targetClass = targetClass;
	}

	/**
	 * Creates a new conversion exception.
	 * @param value
	 * @param targetClass
	 * @param cause
	 */
	public ConversionException(Object value, Class targetClass, Throwable cause) {
		super("Unable to convert value '" + value + "' of type '" + (value != null ? value.getClass().getName() : null)
				+ "' to class '" + targetClass.getName() + "'", cause);
		this.value = value;
		this.targetClass = targetClass;
	}

	/**
	 * Creates a new conversion exception.
	 * @param value
	 * @param targetClass
	 * @param cause
	 * @param message
	 */
	public ConversionException(Object value, Class targetClass, Throwable cause, String message) {
		super(message, cause);
		this.value = value;
		this.targetClass = targetClass;
	}

	/**
	 * Returns the source value
	 * @return the source value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return Returns the targetClass.
	 */
	public Class getTargetClass() {
		return targetClass;
	}
}