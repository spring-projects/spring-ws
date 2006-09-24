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
package org.springframework.webflow.executor.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;
import org.springframework.web.portlet.mvc.Controller;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepositoryFactory;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Point of integration between Spring Portlet MVC and Spring Web Flow: a
 * {@link Controller} that routes incoming portlet requests to one or more
 * managed flow executions.
 * <p>
 * Requests into the web flow system are handled by a {@link FlowExecutor},
 * which this class delegates to. Consult the JavaDoc of that class for more
 * information on how requests are processed.
 * <p>
 * Note: a single PortletFlowController may execute all flows within your
 * portlet. See the phonebook-portlet sample application for examples of the
 * various strategies for launching and resuming flow executions in a Portlet
 * environment.
 * </ul>
 * <p>
 * Usage example:
 * 
 * <pre>
 * &lt;!--
 *     Exposes flows for execution.
 * --&gt;
 * &lt;bean id=&quot;flowController&quot; class=&quot;org.springframework.webflow.executor.mvc.PortletFlowController&quot;&gt;
 *     &lt;property name=&quot;flowLocator&quot; ref=&quot;flowRegistry&quot;/&gt;
 *     &lt;property name=&quot;defaultFlowId&quot; value=&quot;example-flow&quot;/&gt;
 * &lt;/bean&gt;
 *                                                                                          
 * &lt;!-- Creates the registry of flow definitions for this application --&gt;
 *     &lt;bean name=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *     &lt;property name=&quot;flowLocations&quot; value=&quot;/WEB-INF/flows/*-flow.xml&quot;/&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * It is also possible to customize the {@link FlowExecutorArgumentExtractor}
 * strategy to allow for different types of controller parameterization, for
 * example perhaps in conjunction with a REST-style request mapper.
 * 
 * @see FlowExecutor
 * @see FlowExecutorArgumentExtractor
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class PortletFlowController extends AbstractController implements InitializingBean {

	/**
	 * Delegate for executing flow executions (launching new executions, and
	 * resuming existing executions).
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Delegate for extracting flow executor arguments.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor = new FlowExecutorArgumentExtractor();

	public PortletFlowController() {
		initDefaults();
	}

	/**
	 * Initialize the defaults of this constructor.
	 */
	protected void initDefaults() {
		// set the cache seconds property to 0 so no pages are cached by default
		// for flows.
		setCacheSeconds(0);
	}

	/**
	 * Sets the flow locator responsible for loading flow definitions when
	 * requested for execution by clients.
	 * <p>
	 * This is a convenience setter that configures a {@link FlowExecutorImpl}
	 * with a default {@link DefaultFlowExecutionRepositoryFactory} for managing
	 * the storage of executing flows.
	 * @param flowLocator the locator responsible for loading flow definitions
	 * when this controller is invoked.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		flowExecutor = new FlowExecutorImpl(new DefaultFlowExecutionRepositoryFactory(flowLocator));
	}

	/**
	 * Returns the flow executor used by this controller.
	 * @return the flow executor
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}

	/**
	 * Configures the flow executor implementation to use.
	 * @param flowExecutor the flow executor
	 */
	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	/**
	 * Returns the flow executor argument extractor used by this controller.
	 * @return the argument extractor
	 */
	public FlowExecutorArgumentExtractor getArgumentExtractor() {
		return argumentExtractor;
	}

	/**
	 * Sets the flow executor argument extractor to use.
	 * @param argumentExtractor the argument extractor
	 */
	public void setArgumentExtractor(FlowExecutorArgumentExtractor argumentExtractor) {
		this.argumentExtractor = argumentExtractor;
	}

	/**
	 * Sets the identifier of the default flow to launch if no flowId argument
	 * can be extracted by the configured {@link FlowExecutorArgumentExtractor}
	 * during render request processing.
	 */
	public void setDefaultFlowId(String defaultFlowId) {
		argumentExtractor.setDefaultFlowId(defaultFlowId);
	}

	public void afterPropertiesSet() {
		Assert.notNull(flowExecutor, "The flow executor property is required");
		Assert.notNull(argumentExtractor, "The argument extractor property is required");
	}

	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
		if (argumentExtractor.isFlowExecutionKeyPresent(context)) {
			String flowExecutionKey = argumentExtractor.extractFlowExecutionKey(context);
			ResponseInstruction responseInstruction = getActionResponseInstruction(request, flowExecutionKey);
			if (responseInstruction == null) {
				responseInstruction = flowExecutor.refresh(flowExecutionKey, context);
			}
			return toModelAndView(responseInstruction);
		}
		else {
			// launch a new flow execution
			String flowId = argumentExtractor.extractFlowId(context);
			ResponseInstruction responseInstruction = flowExecutor.launch(flowId, context);
			return toModelAndView(responseInstruction);
		}
	}

	protected void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		PortletExternalContext context = new PortletExternalContext(getPortletContext(), request, response);
		String flowExecutionKey = argumentExtractor.extractFlowExecutionKey(context);
		String eventId = argumentExtractor.extractEventId(context);
		ResponseInstruction responseInstruction = flowExecutor.signalEvent(eventId, flowExecutionKey, context);
		if (responseInstruction.isApplicationView() || responseInstruction.isFlowExecutionRedirect()) {
			response.setRenderParameter(argumentExtractor.getFlowExecutionKeyParameterName(), responseInstruction
					.getFlowExecutionKey());
			exposeToRenderPhase(responseInstruction, request);
		}
		else if (responseInstruction.isFlowRedirect()) {
			// request that a new flow be launched within this portlet
			String flowId = ((FlowRedirect)responseInstruction.getViewSelection()).getFlowId();
			response.setRenderParameter(argumentExtractor.getFlowIdParameterName(), flowId);
		}
		else if (responseInstruction.isExternalRedirect()) {
			ExternalRedirect redirect = (ExternalRedirect)responseInstruction.getViewSelection();
			String url = argumentExtractor.createExternalUrl(redirect, flowExecutionKey, context);
			response.sendRedirect(url);
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + responseInstruction);
		}
	}

	// helpers

	private ResponseInstruction getActionResponseInstruction(PortletRequest request, String flowExecutionKey) {
		PortletSession session = request.getPortletSession(false);
		ResponseInstruction response = null;
		if (session != null) {
			String attributeName = getAttributeName(flowExecutionKey);
			response = (ResponseInstruction)session.getAttribute(attributeName);
			if (response != null) {
				// remove it
				session.removeAttribute(attributeName);
			}
		}
		return response;
	}

	protected ModelAndView toModelAndView(ResponseInstruction response) {
		if (response.isApplicationView()) {
			// forward to a view as part of an active conversation
			ApplicationView forward = (ApplicationView)response.getViewSelection();
			Map model = new HashMap(forward.getModel());
			argumentExtractor.put(response.getFlowExecutionKey(), model);
			argumentExtractor.put(response.getFlowExecutionContext(), model);
			return new ModelAndView(forward.getViewName(), model);
		}
		else if (response.isNull()) {
			return null;
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + response);
		}
	}

	private void exposeToRenderPhase(ResponseInstruction responseInstruction, ActionRequest request) {
		PortletSession session = request.getPortletSession(false);
		if (session != null) {
			session.setAttribute(getAttributeName(responseInstruction.getFlowExecutionKey()), responseInstruction);
		}
	}

	private String getAttributeName(String flowExecutionKey) {
		return "actionRequest.responseInstruction." + flowExecutionKey;
	}
}