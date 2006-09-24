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

import org.springframework.binding.method.ClassMethodKey;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.action.AbstractBeanInvokingAction;
import org.springframework.webflow.action.ResultEventFactory;
import org.springframework.webflow.action.ResultObjectBasedEventFactory;
import org.springframework.webflow.action.SuccessEventFactory;

/**
 * Selects the {@link ResultEventFactory} to use for each
 * {@link AbstractBeanInvokingAction bean invoking action} that is constructed.
 * 
 * @author Keith Donald
 */
public class ResultEventFactorySelector {

	/**
	 * The event factory instance for mapping a return value to a success event.
	 */
	private SuccessEventFactory successEventFactory = new SuccessEventFactory();

	/**
	 * The event factory instance for mapping a result object to an event, using
	 * the type of the result object as the mapping criteria.
	 */
	private ResultObjectBasedEventFactory resultObjectBasedEventFactory = new ResultObjectBasedEventFactory();

	/**
	 * Select the appropriate result event factory for attempts to invoke the
	 * method on the specified bean class.
	 * @param signature the method signature
	 * @param beanClass the bean class
	 * @return the result event factory.
	 */
	public ResultEventFactory forMethod(MethodSignature signature, Class beanClass) {
		ClassMethodKey key = new ClassMethodKey(beanClass, signature.getMethodName(), signature.getParameters()
				.getTypesArray());
		if (resultObjectBasedEventFactory.isMappedValueType(key.getMethod().getReturnType())) {
			return resultObjectBasedEventFactory;
		}
		else {
			return successEventFactory;
		}
	}
}