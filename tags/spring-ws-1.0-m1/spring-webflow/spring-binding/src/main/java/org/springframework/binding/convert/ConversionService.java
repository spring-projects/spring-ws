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

/**
 * A service interface for retrieving type conversion executors. The returned
 * command object is thread-safe and may be safely cached for use by client
 * code.
 * 
 * @author Keith Donald
 */
public interface ConversionService {

	/**
	 * Return all conversion executors capable of converting source objects of
	 * the the specified <code>sourceClass</code>.
	 * @param sourceClass the source class to convert from
	 * @return the matching conversion executors
	 */
	public ConversionExecutor[] getConversionExecutorsFrom(Class sourceClass) throws ConversionException;

	/**
	 * Return a conversion executor command object capable of converting source
	 * objects of the specified <code>sourceClass</code> to instances of the
	 * <code>targetClass</code>.
	 * <p>
	 * The returned ConversionExecutor is thread-safe and may safely be cached
	 * for use in client code.
	 * 
	 * @param sourceClass The source class to convert from
	 * @param targetClass The target class to convert to
	 * @return The executor that can execute instance conversion
	 * @throws ConversionException An exception occured retrieving a converter
	 * for the source-to-target pair.
	 */
	public ConversionExecutor getConversionExecutor(Class sourceClass, Class targetClass) throws ConversionException;

	/**
	 * Return a conversion executor command object capable of converting source
	 * objects of the specified <code>sourceClass</code> to target objects of
	 * the type associated with the specified alias.
	 * 
	 * @param sourceClass the sourceClass
	 * @param targetAlias the target alias, may also be the fully qualified
	 * target class name
	 * @return the conversion executor
	 */
	public ConversionExecutor getConversionExecutorByTargetAlias(Class sourceClass, String targetAlias)
			throws ConversionException;

	/**
	 * Return the class with the specified alias.
	 * @param alias the class alias
	 * @return the class
	 */
	public Class getClassByAlias(String alias) throws ConversionException;

}