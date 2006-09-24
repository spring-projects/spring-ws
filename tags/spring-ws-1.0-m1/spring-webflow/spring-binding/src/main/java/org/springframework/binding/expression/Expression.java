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
package org.springframework.binding.expression;

import java.util.Map;

/**
 * Evaluates a single parsed expression of the provided input object in the
 * specified context. This provides a common abstraction for expression
 * evaluation independent of any language like OGNL or Spring's BeanWrapper.
 * @author Keith Donald
 */
public interface Expression {

	/**
	 * Evaluate the expression encapsulated by this evaluator against the
	 * provided target object, returning the result of the evaluation.
	 * @param target The expression target object
	 * @param evaluationContext The expression evaluation context
	 * @return the evaluation result
	 * @throws EvaluationException an exception occured during evaluation.
	 */
	public Object evaluateAgainst(Object target, Map evaluationContext) throws EvaluationException;
}