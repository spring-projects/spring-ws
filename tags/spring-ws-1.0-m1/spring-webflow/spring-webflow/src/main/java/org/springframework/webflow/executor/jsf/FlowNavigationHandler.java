/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.executor.jsf;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.DecoratingNavigationHandler;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;

/**
 * An implementation of a JSF <code>NavigationHandler</code> that provides
 * integration with Spring Web Flow. It delegates handling to the standard
 * NavigationHandler implementation when a navigation request does not pertain
 * to a flow execution.
 * <p>
 * Specifically, the following navigation handler algorithm is implemented:
 * <ul>
 * <li>If a flow execution is <strong>not</strong> currently in progress:
 * <ul>
 * <li>If the specified logical outcome <strong>is</strong> of the form
 * <em>flowId:xxx</em>, look up the corresponding
 * {@link org.springframework.webflow.Flow} definition with that id and launch a
 * new flow execution in the starting state. Expose information to indicate that
 * this flow is in progress and render the starting {@link ViewSelection}.</li>
 * <li>If the specified logical outcome is <strong>not</strong> of the form
 * <em>flowId:xxx</em>, simply delegate to the standard
 * <code>NavigationHandler</code> implementation and return.</li>
 * </ul>
 * </li>
 * <li>If a flow execution <strong>is</strong> currently in progress:
 * <ul>
 * <li>Load the reference to the current in-progress flow execution using the
 * submitted <em>_flowExecutionKey</em> parameter.</li>
 * <li>Resume the flow execution by signaling what action outcome (aka event)
 * the user took in the current state.
 * <li>Once state event processing to complete, render the
 * <code>ViewSelection</code> returned.</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Craig McClanahan
 * @author Colin Sampaleanu
 * @author Keith Donald
 */
public class FlowNavigationHandler extends DecoratingNavigationHandler {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow execution repository factoring, for obtaining repository
	 * instances to save paused executions that require user input and load
	 * resuming executions that will process user events.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * A helper for extracting parameters needed by this flow navigation
	 * handler.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor = new FlowNavigationHandlerArgumentExtractor();

	/**
	 * Create a new {@link FlowNavigationHandler} using the default constructor.
	 */
	public FlowNavigationHandler() {
		super();
	}

	/**
	 * Create a new {@link FlowNavigationHandler}, wrapping the specified
	 * standard navigation handler implementation.
	 * @param originalNavigationHandler Standard <code>NavigationHandler</code>
	 * we are wrapping
	 */
	public FlowNavigationHandler(NavigationHandler originalNavigationHandler) {
		super(originalNavigationHandler);
	}

	/**
	 * Returns the repository factory used by this navigation handler.
	 */
	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Sets the repository factory used by this navigation handler.
	 */
	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Returns the argument extractor used by this navigation handler.
	 */
	public FlowExecutorArgumentExtractor getArgumentExtractor() {
		return argumentExtractor;
	}

	/**
	 * Sets the argument extractor to use.
	 */
	public void setArgumentExtractor(FlowExecutorArgumentExtractor argumentExtractor) {
		this.argumentExtractor = argumentExtractor;
	}

	public void handleNavigation(FacesContext facesContext, String fromAction, String outcome,
			NavigationHandler originalNavigationHandler) {
		JsfExternalContext context = new JsfExternalContext(facesContext, fromAction, outcome);
		if (FlowExecutionHolderUtils.isFlowExecutionRestored(facesContext)) {
			// the flow execution has been restored, now see if we need to
			// signal an event against it
			if (argumentExtractor.isEventIdPresent(context)) {
				// a flow execution has been restored, signal an event in it
				String eventId = argumentExtractor.extractEventId(context);
				FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
				ViewSelection selectedView = holder.getFlowExecution().signalEvent(new EventId(eventId), context);
				holder.setViewSelection(selectedView);
				holder.changed();
			}
		}
		else {
			// no flow execution exists, see if we need to launch one if the
			// flow id is present
			if (argumentExtractor.isFlowIdPresent(context)) {
				// a flow execution launch has been requested, start it
				String flowId = argumentExtractor.extractFlowId(context);
				FlowExecution flowExecution = getRepository(context).createFlowExecution(flowId);
				FlowExecutionHolder holder = new FlowExecutionHolder(flowExecution);
				FlowExecutionHolderUtils.setFlowExecutionHolder(holder, facesContext);
				ViewSelection selectedView = flowExecution.start(createInput(flowExecution, context), context);
				holder.setViewSelection(selectedView);
				holder.changed();
			}
			else {
				// no flow id submitted, proceed with std navigation
				originalNavigationHandler.handleNavigation(facesContext, fromAction, outcome);
			}
		}
	}

	/**
	 * Returns the repository instance to be used by this phase listener.
	 */
	protected FlowExecutionRepository getRepository(JsfExternalContext context) {
		if (repositoryFactory == null) {
			repositoryFactory = FlowFacesUtils.getRepositoryFactory(context.getFacesContext());
		}
		return repositoryFactory.getRepository(context);
	}

	/**
	 * Factory method that creates the input attribute map for a newly created
	 * {@link FlowExecution}. TODO - add support for input mappings here
	 * @param flowExecution the new flow execution (yet to be started)
	 * @param context the external context
	 * @return the input map
	 */
	protected AttributeMap createInput(FlowExecution flowExecution, ExternalContext context) {
		return null;
	}
}