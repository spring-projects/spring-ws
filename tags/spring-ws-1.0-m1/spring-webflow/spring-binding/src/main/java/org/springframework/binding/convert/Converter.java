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

import java.util.Map;

/**
 * A type converter converts objects of one type to that of another. They may
 * also support conversion from multiple source types to multiple different
 * target types.
 * <p>
 * Implementations of this interface are thread-safe.
 * @author Keith Donald
 */
public interface Converter {

	/**
	 * The source classes this converter can convert from.
	 * @return The supported source classes.
	 */
	public Class[] getSourceClasses();

	/**
	 * The target classes this converter can convert to.
	 * @return The supported target classes.
	 */
	public Class[] getTargetClasses();

	/**
	 * Convert the provided source object argument to an instance of the
	 * specified target class.
	 * 
	 * @param source the source object to convert, its class must be one of the
	 * supported <code>sourceClasses</code>
	 * @param targetClass the target class to convert the source to, must be one
	 * of the supported <code>targetClasses</code>
	 * @param conversionContext an optional conversion context that may be used
	 * to influence the conversion process.
	 * @return The converted object, an instance of the default target type
	 * @throws ConversionException An exception occured during the conversion
	 */
	public Object convert(Object source, Class targetClass, Map context) throws ConversionException;

}