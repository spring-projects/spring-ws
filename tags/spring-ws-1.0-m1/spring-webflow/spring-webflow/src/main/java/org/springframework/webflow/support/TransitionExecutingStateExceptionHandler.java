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
package org.springframework.webflow.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.JdkVersion;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelection;

/**
 * A flow state exception handler that maps the occurence of a specific type of
 * exception to a transition to a new {@link org.springframework.webflow.State}.
 * 
 * @author Keith Donald
 */
public class TransitionExecutingStateExceptionHandler implements StateExceptionHandler {

	private static final Log logger = LogFactory.getLog(TransitionExecutingStateExceptionHandler.class);

	/**
	 * The name of the attribute to expose an handled state exception under in
	 * request scope.
	 */
	public static final String STATE_EXCEPTION_ATTRIBUTE = "stateException";

	/**
	 * The name of the attribute to expose an handled state exception under in
	 * request scope.
	 */
	public static final String ROOT_CAUSE_EXCEPTION_ATTRIBUTE = "rootCauseException";

	/**
	 * The exceptionType->targetStateId map.
	 */
	private Map exceptionTargetStateResolverMappings = new HashMap();

	/**
	 * Adds an exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateId the id of the state to transition to if the
	 * specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, String targetStateId) {
		return add(exceptionClass, new DefaultTargetStateResolver(targetStateId));
	}

	/**
	 * Adds a exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateResolver the resolver to calculate the state to
	 * transition to if the specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, TargetStateResolver targetStateResolver) {
		Assert.notNull(exceptionClass, "The exception class is required");
		Assert.notNull(targetStateResolver, "The target state resolver is required");
		exceptionTargetStateResolverMappings.put(exceptionClass, targetStateResolver);
		return this;
	}

	public boolean handles(StateException e) {
		return getTargetStateResolver(e) != null;
	}

	public ViewSelection handle(StateException e, FlowExecutionControlContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Handling state exception " + e);
		}
		State sourceState = context.getCurrentState();
		if (!(sourceState instanceof TransitionableState)) {
			throw new IllegalStateException("The source state '" + sourceState.getId()
					+ "' to transition from must be transitionable!");
		}
		context.getRequestScope().put(STATE_EXCEPTION_ATTRIBUTE, e);
		Throwable rootCause = findRootCause(e);
		if (logger.isDebugEnabled()) {
			logger.debug("Exposing state exception root cause " + rootCause + " under attribute '"
					+ ROOT_CAUSE_EXCEPTION_ATTRIBUTE + "'");
		}
		context.getRequestScope().put(ROOT_CAUSE_EXCEPTION_ATTRIBUTE, rootCause);
		return new Transition(getTargetStateResolver(e)).execute((TransitionableState)sourceState, context);
	}

	// helpers

	/**
	 * Find the mapped target state ID for given exception. Returns
	 * <code>null</code> if no mapping can be found for given exception. Will
	 * try all exceptions in the exception cause chain.
	 */
	protected TargetStateResolver getTargetStateResolver(StateException e) {
		if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_13) {
			return getTargetStateResolver13(e);
		}
		else {
			return getTargetStateResolver14(e);
		}
	}

	/**
	 * Internal getTargetState implementation for use with JDK 1.3.
	 */
	private TargetStateResolver getTargetStateResolver13(NestedRuntimeException e) {
		TargetStateResolver resolver;
		if (isRootCause13(e)) {
			return findTargetStateResolver(e.getClass());
		}
		else {
			resolver = (TargetStateResolver)exceptionTargetStateResolverMappings.get(e.getClass());
			if (resolver != null) {
				return resolver;
			}
			else {
				if (e.getCause() instanceof NestedRuntimeException) {
					return getTargetStateResolver13((NestedRuntimeException)e.getCause());
				}
				else {
					return null;
				}
			}
		}
	}

	/**
	 * Internal getTargetState implementation for use with JDK 1.4 or later.
	 */
	private TargetStateResolver getTargetStateResolver14(Throwable t) {
		TargetStateResolver resolver;
		if (isRootCause14(t)) {
			return findTargetStateResolver(t.getClass());
		}
		else {
			resolver = (TargetStateResolver)exceptionTargetStateResolverMappings.get(t.getClass());
			if (resolver != null) {
				return resolver;
			}
			else {
				return getTargetStateResolver14(t.getCause());
			}
		}
	}

	private boolean isRootCause13(NestedRuntimeException t) {
		return t.getCause() == null;
	}

	private boolean isRootCause14(Throwable t) {
		return t.getCause() == null;
	}

	private TargetStateResolver findTargetStateResolver(Class argumentType) {
		while (argumentType != null && argumentType.getClass() != Object.class) {
			if (exceptionTargetStateResolverMappings.containsKey(argumentType)) {
				return (TargetStateResolver)exceptionTargetStateResolverMappings.get(argumentType);
			}
			else {
				argumentType = argumentType.getSuperclass();
			}
		}
		return null;
	}

	protected Throwable findRootCause(Throwable e) {
		return new RootCauseResolver().findRootCause(e);
	}

	private static class RootCauseResolver {
		public Throwable findRootCause(Throwable e) {
			if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_13) {
				return findRootCause13(e);
			}
			else {
				return findRootCause14(e);
			}
		}

		private Throwable findRootCause13(Throwable e) {
			if (e instanceof NestedRuntimeException) {
				NestedRuntimeException nre = (NestedRuntimeException)e;
				Throwable cause = e.getCause();
				if (cause == null) {
					return nre;
				}
				else {
					return findRootCause13(cause);
				}
			}
			else {
				return e;
			}
		}

		private Throwable findRootCause14(Throwable e) {
			Throwable cause = e.getCause();
			if (cause == null) {
				return e;
			}
			else {
				return findRootCause14(cause);
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("exceptionTargetStateResolverMappings",
				exceptionTargetStateResolverMappings).toString();
	}
}