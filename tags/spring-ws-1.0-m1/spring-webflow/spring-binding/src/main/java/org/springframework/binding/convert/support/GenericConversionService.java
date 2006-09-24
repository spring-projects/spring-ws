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
package org.springframework.binding.convert.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.ConversionServiceAware;
import org.springframework.binding.convert.Converter;
import org.springframework.binding.util.MapAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Base implementation of a conversion service.
 * 
 * @author Keith Donald
 */
public class GenericConversionService implements ConversionService {

	/**
	 * An indexed map of converters. Each entry key is a source class that can
	 * be converted from, and each entry value is a map of target classes that
	 * can be convertered to, ultimately mapping to a specific converter that
	 * can perform the source->target conversion.
	 */
	private Map sourceClassConverters = new HashMap();

	/**
	 * A map of string aliases to convertible classes. Allows lookup of
	 * converters by alias.
	 */
	private Map aliasMap = new HashMap();

	/**
	 * An optional parent conversion service.
	 */
	private ConversionService parent;

	public void setParent(ConversionService parent) {
		this.parent = parent;
	}

	public void addConverter(Converter converter) {
		Class[] sourceClasses = converter.getSourceClasses();
		Class[] targetClasses = converter.getTargetClasses();
		for (int i = 0; i < sourceClasses.length; i++) {
			Class sourceClass = sourceClasses[i];
			Map sourceMap = (Map)sourceClassConverters.get(sourceClass);
			if (sourceMap == null) {
				sourceMap = new HashMap();
				sourceClassConverters.put(sourceClass, sourceMap);
			}
			for (int j = 0; j < targetClasses.length; j++) {
				Class targetClass = targetClasses[j];
				sourceMap.put(targetClass, converter);
			}
		}
		if (converter instanceof ConversionServiceAware) {
			((ConversionServiceAware)converter).setConversionService(this);
		}
	}

	public void addConverter(Converter converter, String alias) {
		aliasMap.put(alias, converter);
		addConverter(converter);
	}

	public void addConverters(Converter[] converters) {
		for (int i = 0; i < converters.length; i++) {
			addConverter(converters[i]);
		}
	}

	public void addAlias(String alias, Class targetType) {
		aliasMap.put(alias, targetType);
	}

	public void addDefaultAlias(Class targetType) {
		addAlias(StringUtils.uncapitalize(ClassUtils.getShortName(targetType)), targetType);
	}

	public ConversionExecutor[] getConversionExecutorsFrom(Class sourceClass) {
		Map sourceTargetConverters = (Map)findConvertersForSource(sourceClass);
		if (sourceTargetConverters.isEmpty()) {
			if (parent != null) {
				return parent.getConversionExecutorsFrom(sourceClass);
			}
			else {
				return new ConversionExecutor[0];
			}
		}
		Set executors = new HashSet();
		if (parent != null) {
			executors.addAll(Arrays.asList(parent.getConversionExecutorsFrom(sourceClass)));
		}
		Iterator it = sourceTargetConverters.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			executors.add(new ConversionExecutor(sourceClass, (Class)entry.getKey(), (Converter)entry.getValue()));
		}
		return (ConversionExecutor[])executors.toArray(new ConversionExecutor[executors.size()]);
	}

	public ConversionExecutor getConversionExecutor(Class sourceClass, Class targetClass) {
		if (this.sourceClassConverters == null || this.sourceClassConverters.isEmpty()) {
			throw new IllegalStateException("No converters have been added to this service's registry");
		}
		if (sourceClass.equals(targetClass)) {
			return new ConversionExecutor(sourceClass, targetClass, new NoOpConverter(sourceClass, targetClass));
		}
		Map sourceTargetConverters = (Map)findConvertersForSource(sourceClass);
		Converter converter = (Converter)findTargetConverter(sourceTargetConverters, targetClass);
		if (converter != null) {
			return new ConversionExecutor(sourceClass, targetClass, converter);
		}
		else {
			if (parent != null) {
				return parent.getConversionExecutor(sourceClass, targetClass);
			}
			else {
				throw new IllegalArgumentException("No converter registered to convert from sourceClass '"
						+ sourceClass + "' to target class '" + targetClass + "'");
			}
		}
	}

	public ConversionExecutor getConversionExecutorByTargetAlias(Class sourceClass, String alias)
			throws IllegalArgumentException {
		Assert.hasText(alias, "The target alias is required and must either be a type alias (e.g 'boolean') "
				+ "or a generic converter alias (e.g. 'bean') ");
		Object targetType = aliasMap.get(alias);
		if (targetType == null) {
			if (parent != null) {
				return parent.getConversionExecutorByTargetAlias(sourceClass, alias);
			}
			else {
				return null;
			}
		}
		else if (targetType instanceof Class) {
			return getConversionExecutor(sourceClass, (Class)targetType);
		}
		else {
			Assert.isInstanceOf(Converter.class, targetType, "Not a converter: ");
			Converter conv = (Converter)targetType;
			return new ConversionExecutor(sourceClass, Object.class, conv);
		}
	}

	public Class getClassByAlias(String alias) {
		Object clazz = aliasMap.get(alias);
		if (clazz != null) {
			Assert.isInstanceOf(Class.class, clazz, "Not a Class alias '" + alias + "': ");
		}
		return (Class)clazz;
	}

	protected Map findConvertersForSource(Class sourceClass) {
		LinkedList classQueue = new LinkedList();
		classQueue.addFirst(sourceClass);
		while (!classQueue.isEmpty()) {
			sourceClass = (Class)classQueue.removeLast();
			Map sourceTargetConverters = (Map)sourceClassConverters.get(sourceClass);
			if (sourceTargetConverters != null && !sourceTargetConverters.isEmpty()) {
				return sourceTargetConverters;
			}
			if (!sourceClass.isInterface() && (sourceClass.getSuperclass() != null)) {
				classQueue.addFirst(sourceClass.getSuperclass());
			}
			// queue up source class's implemented interfaces.
			Class[] interfaces = sourceClass.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				classQueue.addFirst(interfaces[i]);
			}
		}
		return Collections.EMPTY_MAP;
	}

	private Converter findTargetConverter(Map sourceTargetConverters, Class targetClass) {
		LinkedList classQueue = new LinkedList();
		classQueue.addFirst(targetClass);
		while (!classQueue.isEmpty()) {
			targetClass = (Class)classQueue.removeLast();
			Converter converter = (Converter)sourceTargetConverters.get(targetClass);
			if (converter != null) {
				return converter;
			}
			if (!targetClass.isInterface() && (targetClass.getSuperclass() != null)) {
				classQueue.addFirst(targetClass.getSuperclass());
			}
			// queue up target class's implemented interfaces.
			Class[] interfaces = targetClass.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				classQueue.addFirst(interfaces[i]);
			}
		}
		return null;
	}

	public ConversionService getParent() {
		return parent;
	}

	protected Map getSourceClassConverters() {
		return sourceClassConverters;
	}

	protected Map getAliasMap() {
		return aliasMap;
	}

	public static class NoOpConverter extends AbstractConverter {

		private Class sourceClass;

		private Class targetClass;

		public NoOpConverter(Class sourceClass, Class targetClass) {
			this.sourceClass = sourceClass;
			this.targetClass = targetClass;
		}

		protected Object doConvert(Object source, Class targetClass, MapAccessor context) throws Exception {
			return source;
		}

		public Class[] getSourceClasses() {
			return new Class[] { sourceClass };
		}

		public Class[] getTargetClasses() {
			return new Class[] { targetClass };
		}
	}
}