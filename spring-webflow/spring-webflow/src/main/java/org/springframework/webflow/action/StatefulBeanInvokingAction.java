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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.method.MethodSignature;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Simple action that delegates to a bean that is managed in a stateful scope.
 * The target of this action can be any object (and generally should be
 * serializable). The target may hold modifiable state as instance members in a
 * thread safe manner.
 * <p>
 * The resolved bean name will be treated as the identifier of a prototype bean
 * definition in the configured bean factory to retrieve, as well as the name of
 * the attribute to expose the bean in under the configured
 * {@link #getBeanScope() scope}. Subsequent requests to invoke this action
 * will pull the cached bean instance from the scope.
 * <p>
 * Example:
 * 
 * <pre>
 *     &lt;action-state id=&quot;executeMethod&quot;&gt;
 *         &lt;action bean=&quot;myAction&quot; method=&quot;myActionMethod&quot;&gt;
 *             &lt;attribute name=&quot;stateful&quot; value=&quot;true&quot;/&gt;
 *         &lt;/action&gt;
 *     &lt;/action-state&gt;
 * </pre>
 * 
 * Note: this action is not ideal for cases when the target bean manages
 * transient references. This is because the bean instance is placed <i>directly</i>
 * into a scope eligible for serialization between requests. Consider a
 * memento-based or metadata-driven based strategy for selectively storing
 * serializable fields separately from the bean itself when you mix transient
 * and serializable references within the same stateful object.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class StatefulBeanInvokingAction extends BeanFactoryBeanInvokingAction {

	/**
	 * The scope to place the stateful bean instance in.
	 */
	private ScopeType beanScope = ScopeType.FLOW;

	/**
	 * Creates a new stateful bean invoking action.
	 */
	public StatefulBeanInvokingAction(MethodSignature methodSignature, String beanName, BeanFactory beanFactory) {
		super(methodSignature, beanName, beanFactory);
		setBeanStatePersister(new ScopeBeanStatePersister());
	}

	/**
	 * Returns the scope to put the stateful bean instance in.
	 */
	public ScopeType getBeanScope() {
		return beanScope;
	}

	/**
	 * Sets the scope to put the stateful bean instance in.
	 */
	public void setBeanScope(ScopeType beanScope) {
		if (beanScope == null) {
			beanScope = ScopeType.FLOW;
		}
		this.beanScope = beanScope;
	}

	/**
	 * Retrieves the bean to invoke from the configured {@link #getBeanScope()}.
	 */
	protected Object getBean(RequestContext context) {
		return beanScope.getScope(context).get(getBeanName());
	}

	/**
	 * Bean state perister that loads new stateful beans from the configured
	 * bean factory and saves modified beans out to the configured bean scope.
	 * @author Keith Donald
	 */
	private class ScopeBeanStatePersister implements BeanStatePersister {
		public Object restoreState(Object bean, RequestContext context) {
			if (bean == null) {
				return getPrototypeBean(getBeanName());
			}
			else {
				return bean;
			}
		}

		public void saveState(Object bean, RequestContext context) {
			getBeanScope().getScope(context).put(getBeanName(), bean);
		}

		private Object getPrototypeBean(String beanName) {
			Assert.isTrue(!getBeanFactory().isSingleton(beanName), "The definition of the stateful with name '"
					+ beanName + "' must be a prototype");
			return getBeanFactory().getBean(beanName);
		}
	}
}