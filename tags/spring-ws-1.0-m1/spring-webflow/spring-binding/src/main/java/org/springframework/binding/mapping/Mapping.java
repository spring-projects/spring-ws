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
package org.springframework.binding.mapping;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A single mapping definition, encapulating the information neccessary to map
 * the result of evaluating an expression on a source object to a property on a
 * target object, optionally applying a type conversion during the mapping
 * process.
 * @author Keith Donald
 */
public class Mapping implements AttributeMapper, Serializable {

	protected static final Log logger = LogFactory.getLog(Mapping.class);

	/**
	 * The source expression to evaluate against a source object to map from.
	 */
	private final Expression sourceExpression;

	/**
	 * The target property expression to set on a target object to map to.
	 */
	private final PropertyExpression targetPropertyExpression;

	/**
	 * A type converter to apply during the mapping process.
	 */
	private final ConversionExecutor typeConverter;

	/**
	 * Creates a new mapping.
	 * @param sourceExpression
	 * @param targetPropertyExpression
	 * @param typeConverter
	 */
	public Mapping(Expression sourceExpression, PropertyExpression targetPropertyExpression,
			ConversionExecutor typeConverter) {
		Assert.notNull(sourceExpression, "The source expression is required");
		Assert.notNull(targetPropertyExpression, "The target property expression is required");
		this.sourceExpression = sourceExpression;
		this.targetPropertyExpression = targetPropertyExpression;
		this.typeConverter = typeConverter;
	}

	/**
	 * Map the <code>sourceAttribute</code> in to the
	 * <code>targetAttribute</code> target map, performing type conversion if
	 * necessary.
	 * @param source The source data structure
	 * @param target The target data structure
	 */
	public void map(Object source, Object target, Map mappingContext) {
		// get source value
		Object sourceValue = sourceExpression.evaluateAgainst(source, mappingContext);
		Object targetValue = sourceValue;
		if (typeConverter != null) {
			targetValue = typeConverter.execute(sourceValue);
		}
		// set target value
		if (logger.isDebugEnabled()) {
			logger.debug("Mapping " + sourceExpression + " value [" + sourceValue + "] to target property '"
					+ targetPropertyExpression + "'; setting property value to [" + targetValue + "]");
		}
		targetPropertyExpression.setValue(target, targetValue, mappingContext);
	}

	public boolean equals(Object o) {
		if (!(o instanceof Mapping)) {
			return false;
		}
		Mapping other = (Mapping)o;
		return sourceExpression.equals(other.sourceExpression)
				&& targetPropertyExpression.equals(other.targetPropertyExpression);
	}

	public int hashCode() {
		return sourceExpression.hashCode() + targetPropertyExpression.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append(sourceExpression + " -> " + targetPropertyExpression).append(
				"typeConverter", typeConverter).toString();
	}
}