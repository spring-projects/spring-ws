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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepositoryFactory;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;
import org.springframework.webflow.executor.support.FlowRequestHandler;
import org.springframework.webflow.executor.support.RequestPathFlowExecutorArgumentExtractor;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Point of integration between Spring MVC and Spring Web Flow: a
 * {@link Controller} that routes incoming requests to one or more managed flow
 * executions.
 * <p>
 * Requests into the web flow system are handled by a {@link FlowExecutor},
 * which this class delegates to using a {@link FlowRequestHandler} helper.
 * Consult the JavaDoc of that class for more information on how requests are
 * processed.
 * <p>
 * Note: a single FlowController may execute all flows of your application.
 * <p>
 * <li>By default, to have this controller launch a new flow execution
 * (conversation), have the client send a
 * {@link FlowExecutorArgumentExtractor#getFlowIdParameterName()} request
 * parameter indicating the flow definition to launch.
 * <li>To have this controller participate in an existing flow execution
 * (conversation), have the client send a
 * {@link FlowExecutorArgumentExtractor#getFlowExecutionKeyParameterName()}
 * request parameter identifying the conversation to participate in.
 * <p>
 * See the flowLauncher sample application for examples of the various
 * strategies for launching and resuming flow executions.
 * <p>
 * Usage example:
 * 
 * <pre>
 *     &lt;!--
 *         Exposes flows for execution at a single request URL.
 *         The id of a flow to launch should be passed in by clients using
 *         the &quot;_flowId&quot; request parameter:
 *         e.g. /app.htm?_flowId=flow1
 *     --&gt;
 *     &lt;bean name=&quot;/app.htm&quot; class=&quot;org.springframework.webflow.executor.mvc.FlowController&quot;&gt;
 *         &lt;property name=&quot;flowLocator&quot; ref=&quot;flowRegistry&quot;/&gt;
 *     &lt;/bean&gt;
 *                                                                                      
 *     &lt;!-- Creates the registry of flow definitions for this application --&gt;
 *     &lt;bean name=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowLocations&quot; value=&quot;/WEB-INF/flows/*-flow.xml&quot;/&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * It is also possible to customize the {@link FlowExecutorArgumentExtractor}
 * strategy to allow for different types of controller parameterization, for
 * example perhaps in conjunction with a REST-style request mapper (see
 * {@link RequestPathFlowExecutorArgumentExtractor}).
 * 
 * @see FlowExecutor
 * @see FlowExecutorArgumentExtractor
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController implements InitializingBean {

	/**
	 * The facade for executing flows (launching new executions, and resuming
	 * existing executions).
	 */
	private FlowExecutor flowExecutor;

	/**
	 * The strategy for extracting flow executor parameters from a request made
	 * by a {@link ExternalContext}.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor = new FlowExecutorArgumentExtractor();

	public FlowController() {
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
		this.flowExecutor = new FlowExecutorImpl(flowLocator);
	}

	/**
	 * Returns the flow executor used by this controller.
	 * @return the flow executor
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}

	/**
	 * Sets the flow executor to use.
	 * <p>
	 * This setter allows for customization of the backing flow execution
	 * strategy, which might involve configuration of a custom
	 * {@link FlowExecutionRepositoryFactory} for managing the storage of flows
	 * and/or one or more {@link FlowExecutionListener} objects for observing
	 * the lifecycle of executing flows.
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
	 * @param parameterExtractor the argument extractor
	 */
	public void setArgumentExtractor(FlowExecutorArgumentExtractor parameterExtractor) {
		this.argumentExtractor = parameterExtractor;
	}

	/**
	 * Sets the identifier of the default flow to launch if no flowId argument
	 * can be extracted by the configured {@link FlowExecutorArgumentExtractor}
	 * during request processing.
	 */
	public void setDefaultFlowId(String defaultFlowId) {
		this.argumentExtractor.setDefaultFlowId(defaultFlowId);
	}

	public void afterPropertiesSet() {
		Assert.notNull(flowExecutor, "The flow executor property is required");
		Assert.notNull(argumentExtractor, "The argument extractor property is required");
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ServletExternalContext context = new ServletExternalContext(getServletContext(), request, response);
		ResponseInstruction responseInstruction = createRequestHandler().handleFlowRequest(context);
		return toModelAndView(responseInstruction, context);
	}

	/**
	 * Factory method that creates a new helper for processing a request into
	 * this flow controller. The controller is a basic template encapsulating
	 * reusable flow execution request handling workflow.
	 * @return the controller helper
	 */
	protected FlowRequestHandler createRequestHandler() {
		return new FlowRequestHandler(getFlowExecutor(), getArgumentExtractor());
	}

	/**
	 * Create a ModelAndView object based on the information in the selected
	 * response instruction. Subclasses can override this to return a
	 * specialized ModelAndView or to do custom processing on it.
	 * @param response instruction the response instruction to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ResponseInstruction response, ExternalContext context) {
		if (response.isApplicationView()) {
			// forward to a view as part of an active conversation
			ApplicationView view = (ApplicationView)response.getViewSelection();
			Map model = new HashMap(view.getModel());
			argumentExtractor.put(response.getFlowExecutionKey(), model);
			argumentExtractor.put(response.getFlowExecutionContext(), model);
			return new ModelAndView(view.getViewName(), model);
		}
		else if (response.isFlowExecutionRedirect()) {
			// redirect to active flow execution URL
			String flowExecutionUrl = argumentExtractor.createFlowExecutionUrl(response.getFlowExecutionKey(), response
					.getFlowExecutionContext(), context);
			return new ModelAndView(new RedirectView(flowExecutionUrl));
		}
		else if (response.isExternalRedirect()) {
			// redirect to external URL
			ExternalRedirect redirect = (ExternalRedirect)response.getViewSelection();
			String externalUrl = argumentExtractor.createExternalUrl(redirect, response.getFlowExecutionKey(), context);
			return new ModelAndView(new RedirectView(externalUrl));
		}
		else if (response.isFlowRedirect()) {
			// restart the flow by redirecting to flow launch URL
			String flowUrl = argumentExtractor.createFlowUrl((FlowRedirect)response.getViewSelection(), context);
			return new ModelAndView(new RedirectView(flowUrl));
		}
		else if (response.isNull()) {
			// no response to issue
			return null;
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + response);
		}
	}
}