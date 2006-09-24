/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.builder;

import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.util.MapAccessor;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.support.DefaultTargetStateResolver;

/**
 * Converter that takes an encoded string representation and produces a
 * corresponding <code>Transition.TargetStateResolver</code> object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"stateId" - will result in a TargetStateResolver that always resolves to
 * the same state, an instance of ({@link org.springframework.webflow.support.DefaultTargetStateResolver})
 * </li>
 * <li>"bean:&lt;id&gt;" - will result in usage of a custom TargetStateResolver
 * bean implementation.</li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionTargetStateResolver extends AbstractConverter {

	/**
	 * Prefix used when the user wants to use a custom TransitionCriteria
	 * implementation managed by a factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Locator to use for loading custom TransitionCriteria beans.
	 */
	private FlowServiceLocator flowServiceLocator;

	/**
	 * Create a new converter that converts strings to transition target state
	 * resovler objects. The given conversion service will be used to do all
	 * necessary internal conversion (e.g. parsing expression strings).
	 */
	public TextToTransitionTargetStateResolver(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { TargetStateResolver.class };
	}

	protected Object doConvert(Object source, Class targetClass, MapAccessor context) throws Exception {
		String targetStateId = (String)source;
		if (flowServiceLocator.getExpressionParser().isDelimitedExpression(targetStateId)) {
			Expression expression = flowServiceLocator.getExpressionParser().parseExpression(targetStateId);
			return new DefaultTargetStateResolver(expression);
		}
		else if (targetStateId.startsWith(BEAN_PREFIX)) {
			return flowServiceLocator.getTargetStateResolver(targetStateId.substring(BEAN_PREFIX.length()));
		}
		else {
			return new DefaultTargetStateResolver(targetStateId);
		}
	}
}