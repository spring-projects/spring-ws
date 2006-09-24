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
package org.springframework.webflow.action;

import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.MultiAction.MethodResolver;

/**
 * Default method key resolver used by the MultiAction class. It uses the
 * following algorithm to calculate a method name:
 * <ol>
 * <li>If the currently executing action has a "method" property defined,
 * use the value as method name.</li>
 * <li>Else, use the name of the current state of the flow execution as a
 * method name.</li>
 * </ol>
 * 
 * @author Erwin Vervaet
 */
class DefaultMultiActionMethodResolver implements MethodResolver {
	public String resolveMethod(RequestContext context) {
		String method = context.getAttributes().getString(AnnotatedAction.METHOD_ATTRIBUTE);
		if (method == null) {
			if (context.getCurrentState() != null) {
				// default to the stateId
				method = context.getCurrentState().getId();
			}
			else {
				throw new IllegalStateException("Unable to resolve action method; no '"
						+ AnnotatedAction.METHOD_ATTRIBUTE + "' context attribute set");
			}
		}
		return method;
	}
}