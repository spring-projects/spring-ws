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

import org.springframework.webflow.RequestContext;

/**
 * A service for managing the saving and restoring of state associated with a
 * invokable bean.
 * <p>
 * Some people might call what this strategy enables is memento-like
 * <i>bijection</i>, where state is <i>injected</i> into a bean before
 * invocation and then <i>outjected</i> after invocation.
 * 
 * @author Keith Donald
 */
public interface BeanStatePersister {

	/**
	 * Save (outject) the bean's state to the context.
	 * @param bean the bean
	 * @param context the flow execution request context
	 * @throws Exception an exception occured
	 */
	public void saveState(Object bean, RequestContext context);

	/**
	 * Restore (inject) the bean's state from the context.
	 * @param bean the bean
	 * @param context the flow execution request context
	 * @return the bean
	 * @throws Exception an exception occured
	 */
	public Object restoreState(Object bean, RequestContext context);

}