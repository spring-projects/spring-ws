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
package org.springframework.webflow.executor.struts;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.struts.ActionSupport;
import org.springframework.web.struts.DelegatingActionProxy;
import org.springframework.web.struts.SpringBindingActionForm;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.action.FormObjectAccessor;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;
import org.springframework.webflow.executor.support.FlowRequestHandler;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Point of integration between Struts and Spring Web Flow: a Struts Action that
 * acts a front controller entry point into the web flow system. A single
 * FlowAction may launch any new FlowExecution. In addition, a single Flow
 * Action may signal events in any existing/restored FlowExecutions.
 * <p>
 * Requests are managed by and delegated to a {@link FlowExecutor}, which this
 * class delegates to using a {@link FlowRequestHandler} (allowing reuse of
 * common front flow controller logic in other environments). Consult the
 * JavaDoc of those classes for more information on how requests are processed.
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
 * On each request received by this action, a {@link StrutsExternalContext}
 * object is created as input to the web flow system. This external source event
 * provides access to the action form, action mapping, and other struts-specific
 * constructs.
 * <p>
 * This class also is aware of the {@link SpringBindingActionForm} adapter,
 * which adapts Spring's data binding infrastructure (based on POJO binding, a
 * standard Errors interface, and property editor type conversion) to the Struts
 * action form model. This option gives backend web-tier developers full support
 * for POJO-based binding with minimal hassel, while still providing consistency
 * to view developers who already have a lot of experience with Struts for
 * markup and request dispatching.
 * <p>
 * Below is an example <code>struts-config.xml</code> configuration for a
 * FlowAction:
 * 
 * <pre>
 *     &lt;action path=&quot;/userRegistration&quot;
 *         type=&quot;org.springframework.webflow.executor.struts.FlowAction&quot;
 *         name=&quot;springBindingActionForm&quot; scope=&quot;request&quot;&gt;
 *     &lt;/action&gt;
 * </pre>
 * 
 * This example associates the logical request URL
 * <code>/userRegistration.do</code> as a Flow controller. It is expected that
 * flows to launch be provided in a dynamic fashion by the views (allowing this
 * single <code>FlowAction</code> to manage any number of flow executions). A
 * Spring binding action form instance is set in request scope, acting as an
 * adapter enabling POJO-based binding and validation with Spring.
 * <p>
 * Other notes regarding Struts/Spring Web Flow integration:
 * <p>
 * <ul>
 * <li>Logical view names returned when <code>ViewStates</code> and
 * <code>EndStates</code> are entered are mapped to physical view templates
 * using standard Struts action forwards (typically global forwards).
 * <li>Use of the <code>SpringBindingActionForm</code> requires no special
 * setup in <code>struts-config.xml</code>: simply declare a form bean in
 * request scope of the class
 * <code>org.springframework.web.struts.SpringBindingActionForm</code> and use
 * it with your FlowAction.
 * <li>This class depends on a {@link FlowExecutor} instance to be configured.
 * <li> If relying on Spring's {@link DelegatingActionProxy} (which is
 * recommended), a FlowExecutor reference can simply be injected using standard
 * Spring DependencyInjection techniques. If you are not using the proxy-based
 * approach, this class will attempt a root context lookup on initialization,
 * first querying for a bean of instance {@link FlowExecutor} named
 * {@link #FLOW_EXECUTOR_BEAN_NAME}, then, if not found, querying for a bean of
 * instance {@link FlowLocator} named {@link #FLOW_LOCATOR_BEAN_NAME}. If the
 * FlowLocator dependency is resolved, this class will automatically configure a
 * default flow executor implementation suitable for a Struts environment (see
 * {@link #setFlowLocator(FlowLocator)}). In addition, you may choose to simply
 * inject a FlowLocator directly if the FlowExecutor defaults meet your
 * requirements.
 * </ul>
 * <p>
 * The benefits here are substantial: developers now have a powerful web flow
 * capability integrated with Struts, with a consistent-approach to POJO-based
 * binding and validation that addresses the proliferation of
 * <code>ActionForm</code> classes found in traditional Struts-based apps.
 * 
 * @see org.springframework.webflow.executor.FlowExecutor
 * @see org.springframework.webflow.executor.support.FlowRequestHandler
 * @see org.springframework.web.struts.SpringBindingActionForm
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAction extends ActionSupport {

	/**
	 * The flow executor will be retreived from the application context using
	 * this bean name if no executor is explicitly set.
	 */
	protected static final String FLOW_EXECUTOR_BEAN_NAME = "flowExecutor";

	/**
	 * The flow locator will be retreived from the application context using
	 * this bean name if no executor and locator is explicitly set.
	 */
	protected static final String FLOW_LOCATOR_BEAN_NAME = "flowLocator";

	/**
	 * The service responsible for launching and signaling struts-originating
	 * events in flow executions.
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Delegate for extract flow executor parameters.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor;

	/**
	 * Set the flow locator to use for the lookup of flow definitions to
	 * execute.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		flowExecutor = new FlowExecutorImpl(flowLocator);
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

	protected void onInit() {
		if (getFlowExecutor() == null) {
			WebApplicationContext context = getWebApplicationContext();
			if (context.containsBean(FLOW_EXECUTOR_BEAN_NAME)) {
				setFlowExecutor((FlowExecutor)context.getBean(FLOW_EXECUTOR_BEAN_NAME, FlowExecutor.class));
			}
			else {
				try {
					setFlowLocator((FlowLocator)context.getBean(FLOW_LOCATOR_BEAN_NAME, FlowLocator.class));
				}
				catch (NoSuchBeanDefinitionException e) {
					String message = "No '" + FLOW_LOCATOR_BEAN_NAME + "' or '" + FLOW_EXECUTOR_BEAN_NAME
							+ "' bean definition could be found; to use Spring Web Flow with Struts you must "
							+ "configure this FlowAction with either a FlowLocator "
							+ "(exposing a registry of flow definitions) or a custom FlowExecutor "
							+ "(allowing more configuration options)";
					throw new FlowArtifactException(FLOW_LOCATOR_BEAN_NAME, FlowLocator.class, message, e);
				}
			}
		}
		if (getArgumentExtractor() == null) {
			argumentExtractor = new FlowExecutorArgumentExtractor();
		}
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ExternalContext context = new StrutsExternalContext(mapping, form, getServletContext(), request, response);
		ResponseInstruction responseInstruction = createRequestHandler().handleFlowRequest(context);
		return toActionForward(responseInstruction, mapping, form, request, response, context);
	}

	/**
	 * Factory method that creates a new helper for processing a request into
	 * this flow controller.
	 * @return the controller helper
	 */
	protected FlowRequestHandler createRequestHandler() {
		return new FlowRequestHandler(getFlowExecutor(), getArgumentExtractor());
	}

	/**
	 * Return a Struts ActionForward given a ViewSelection. Adds all attributes
	 * from the ViewSelection as request attributes.
	 */
	protected ActionForward toActionForward(ResponseInstruction response, ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse httpResponse, ExternalContext context) throws Exception {
		if (response.isApplicationView()) {
			// forward to a view as part of an active conversation
			ApplicationView forward = (ApplicationView)response.getViewSelection();
			Map model = new HashMap(forward.getModel());
			argumentExtractor.put(response.getFlowExecutionKey(), model);
			argumentExtractor.put(response.getFlowExecutionContext(), model);
			WebUtils.exposeRequestAttributes(request, model);
			if (form instanceof SpringBindingActionForm) {
				SpringBindingActionForm bindingForm = (SpringBindingActionForm)form;
				bindingForm.expose(getCurrentErrors(forward.getModel()), request);
			}
			return findActionForward(forward, mapping);

		}
		else if (response.isFlowExecutionRedirect()) {
			// redirect to active flow execution URL
			String flowExecutionUrl = argumentExtractor.createFlowExecutionUrl(response.getFlowExecutionKey(), response
					.getFlowExecutionContext(), context);
			return createRedirectForward(flowExecutionUrl, httpResponse);
		}
		else if (response.isExternalRedirect()) {
			// redirect to external URL
			String externalUrl = argumentExtractor.createExternalUrl((ExternalRedirect)response.getViewSelection(),
					response.getFlowExecutionKey(), context);
			return createRedirectForward(externalUrl, httpResponse);
		}
		else if (response.isFlowRedirect()) {
			// restart the flow by redirecting to flow launch URL
			String flowUrl = argumentExtractor.createFlowUrl((FlowRedirect)response.getViewSelection(), context);
			return createRedirectForward(flowUrl, httpResponse);
		}
		else if (response.isNull()) {
			// no response to issue
			return null;
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle response instruction " + response);
		}
	}

	/**
	 * Handles a redirect.  This implementation simply calls sendRedirect on the response object.
	 * @param url the url to redirect to
	 * @param response the http response
	 * @return the redirect forward, this implementation returns null
	 * @throws Exception an excpetion occured processing the redirect
	 */
	protected ActionForward createRedirectForward(String url, HttpServletResponse response) throws Exception {
		response.sendRedirect(url);
		return null;
	}

	private Errors getCurrentErrors(Map model) {
		return (Errors)model.get(FormObjectAccessor.getCurrentFormErrorsName());
	}

	private ActionForward findActionForward(ApplicationView forward, ActionMapping mapping) {
		ActionForward actionForward = mapping.findForward(forward.getViewName());
		if (actionForward != null) {
			// the 1.2.1 copy constructor would ideally be better to
			// use, but it is not Struts 1.1 compatible
			actionForward = new ActionForward(actionForward.getName(), actionForward.getPath(), false);
		}
		else {
			actionForward = new ActionForward(forward.getViewName(), false);
		}
		return actionForward;
	}
}