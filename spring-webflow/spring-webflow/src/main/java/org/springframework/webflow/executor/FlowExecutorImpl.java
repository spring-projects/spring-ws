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
package org.springframework.webflow.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.util.Assert;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepositoryFactory;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.RedirectType;

/**
 * The default implementation of the central facade for <i>driving</i> the
 * execution of flows within an application.
 * <p>
 * This object is responsible creating and starting new flow executions as
 * requested by clients, as well as signaling events for processing by existing,
 * paused executions (that are waiting to be resumed in response to a user
 * event).
 * <p>
 * This object is a facade or entry point into the Spring Web Flow execution
 * system and makes the overall system easier to use. The name <i>executor</i>
 * was chosen as <i>executors drive executions</i>.
 * <p>
 * <b>Commonly used configurable properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>description</b></td>
 * <td><b>default</b></td>
 * </tr>
 * <tr>
 * <td>repositoryFactory</td>
 * <td>The strategy for accessing a flow execution repositories that are used
 * to create, save, and store managed flow executions driven by this executor.</td>
 * <td>A {@link DefaultFlowExecutionRepositoryFactory simple}, stateful
 * server-side session-based repository factory</td>
 * </tr>
 * <tr>
 * <td>redirectOnPause</td>
 * <td>A enumeration indicating if this executor should force a redirect to an
 * {@link ApplicationView} after pausing an active flow execution. Several
 * different types of redirect are supported.</td>
 * <td>NONE, indicating no special redirect action should be taken</td>
 * </tr>
 * <tr>
 * <td>inputMapper</td>
 * <td>The service responsible for mapping attributes of
 * {@link ExternalContext external contexts} that request to launch new
 * {@link FlowExecution flow executions}. After mapping, the target map is then
 * passed to the FlowExecution, exposing context attributes as input to the flow
 * during startup.</td>
 * <td>A RequestParameterInputMapper, which exposes all request params in to
 * the flow execution for input mapping. </td>
 * </tr>
 * </table>
 * </p>
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.ViewSelection
 * @see org.springframework.webflow.support.ApplicationView
 * @see org.springframework.webflow.support.RedirectType
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutorImpl implements FlowExecutor {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow execution repository factory, for obtaining repository instances
	 * to create, save, and restore flow executions.
	 * <p>
	 * The default value is the {@link DefaultFlowExecutionRepositoryFactory}
	 * repository factory that creates repositories within the user session map.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * A value that indicates if this executor should always redirect selected
	 * application views after pausing an active flow execution.
	 * <p>
	 * This allows the user to participate in the current state of the flow
	 * execution using a bookmarkable URL.
	 */
	private RedirectType redirectOnPause;

	/**
	 * The service responsible for mapping attributes of an
	 * {@link ExternalContext} to a new {@link FlowExecution} during the
	 * {@link #launch(String, ExternalContext) launch flow} operation.
	 * <p>
	 * This allows developers to control what attributes are made available in
	 * the <code>inputMap</code> to new top-level flow executions. The
	 * starting execution may then choose to map that available input into its
	 * own local scope.
	 * <p>
	 * The default implementation simply exposes all request parameters as flow
	 * execution input attributes. May be null.
	 */
	private AttributeMapper inputMapper = new RequestParameterInputMapper();

	/**
	 * Create a new flow executor that configures use of the default repository
	 * strategy ({@link DefaultFlowExecutionRepositoryFactory}) to drive the
	 * the execution of flows loaded by the provided flow locator.
	 * @param flowLocator the flow locator
	 */
	public FlowExecutorImpl(FlowLocator flowLocator) {
		this(new DefaultFlowExecutionRepositoryFactory(flowLocator));
	}

	/**
	 * Create a new flow executor that uses the repository factory to access a
	 * repository to create, save, and restore managed flow executions driven by
	 * this executor.
	 * @param repositoryFactory the repository factory
	 */
	public FlowExecutorImpl(FlowExecutionRepositoryFactory repositoryFactory) {
		Assert.notNull(repositoryFactory,
				"The repository factory for creating, saving, and restoring flow executions is required");
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Returns a value indicating if this executor should redirect after pausing
	 * an active flow execution.
	 */
	public RedirectType getRedirectOnPause() {
		return redirectOnPause;
	}

	/**
	 * Sets the value that indicates if this executor should redirect after
	 * pausing an active flow execution.
	 * <p>
	 * Setting a redirect type allows the user to participate in the current
	 * view-state of a conversation at a refreshable URL.
	 */
	public void setRedirectOnPause(RedirectType redirectType) {
		this.redirectOnPause = redirectType;
	}

	public ResponseInstruction launch(String flowId, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecution flowExecution = repository.createFlowExecution(flowId);
		ViewSelection selectedView = flowExecution.start(createInput(flowExecution, context), context);
		if (flowExecution.isActive()) {
			FlowExecutionKey flowExecutionKey = repository.generateKey(flowExecution);
			repository.putFlowExecution(flowExecutionKey, flowExecution);
			return new ResponseInstruction(flowExecutionKey.toString(), flowExecution, pausedView(selectedView));
		}
		else {
			return new ResponseInstruction(flowExecution, selectedView);
		}
	}

	public ResponseInstruction signalEvent(String eventId, String flowExecutionKey, ExternalContext context)
			throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionKey repositoryKey = repository.parseFlowExecutionKey(flowExecutionKey);
		FlowExecutionLock lock = repository.getLock(repositoryKey);
		lock.lock();
		try {
			FlowExecution flowExecution = repository.getFlowExecution(repositoryKey);
			ViewSelection selectedView = flowExecution.signalEvent(new EventId(eventId), context);
			if (flowExecution.isActive()) {
				repositoryKey = repository.getNextKey(flowExecution, repositoryKey);
				repository.putFlowExecution(repositoryKey, flowExecution);
				return new ResponseInstruction(repositoryKey.toString(), flowExecution, pausedView(selectedView));
			}
			else {
				repository.removeFlowExecution(repositoryKey);
				return new ResponseInstruction(flowExecution, selectedView);
			}
		}
		finally {
			lock.unlock();
		}
	}

	public ResponseInstruction refresh(String flowExecutionKey, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionKey repositoryKey = repository.parseFlowExecutionKey(flowExecutionKey);
		FlowExecutionLock lock = repository.getLock(repositoryKey);
		lock.lock();
		try {
			FlowExecution flowExecution = repository.getFlowExecution(repositoryKey);
			ViewSelection selectedView = flowExecution.refresh(context);
			return new ResponseInstruction(repositoryKey.toString(), flowExecution, selectedView);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Factory method that creates the input attribute map for a newly created
	 * {@link FlowExecution}.
	 * @param context the external context
	 * @return the input map
	 */
	protected AttributeMap createInput(FlowExecution flowExecution, ExternalContext context) {
		if (inputMapper != null) {
			AttributeMap inputMap = new AttributeMap();
			inputMapper.map(context, inputMap, null);
			return inputMap;
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the repository retrieved by the configured
	 * {@link FlowExecutionRepositoryFactory}.
	 */
	protected FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}

	/**
	 * Factory method that post processes a view selection made as the result of
	 * pausing an active flow execution. This implementation applies the
	 * "redirectType" behavior if necessary.
	 * @param selectedView the view selected by the view state
	 * @return the view to return to callers
	 */
	protected ViewSelection pausedView(ViewSelection selectedView) {
		if (selectedView instanceof ApplicationView && redirectOnPause != null) {
			return redirectOnPause.select();
		}
		else {
			return selectedView;
		}
	}
}